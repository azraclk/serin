package app.azracelik.serin.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

private const val PartSuffix = ".part"

/** Bir meditasyon sesinin telefondaki durumu; hiç kaydı olmayan ses indirilmemiştir. */
enum class AudioStatus { Downloaded, Downloading, Failed }

/**
 * Seslerin çevrimdışı dinlemek için telefona indirilmiş kopyaları. Önbellekten farkı: kullanıcı
 * kaldırana kadar silinmez ve hiç dinlenmemiş sesler de indirilebilir.
 *
 * Dosya adı sesin adresine bağlıdır; içerikte ses başka bir adrese taşınırsa eski kopya geçersiz
 * sayılır ve bir sonraki indirmede silinir. Adres aynı kalıp dosya değişirse kopya eski kalır.
 */
class AudioStore(private val dir: File, private val client: OkHttpClient) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _status = MutableStateFlow(scan())

    /** [key] ile eşleşen durumlar; listede olmayan ses indirilmemiştir. */
    val status: StateFlow<Map<String, AudioStatus>> = _status.asStateFlow()

    /** Sesi indirilmişse yerel dosyası, değilse null. */
    fun localFile(id: String, url: String): File? =
        File(dir, key(id, url)).takeIf { _status.value[key(id, url)] == AudioStatus.Downloaded && it.exists() }

    /** Uygulama kapansa da sürsün diye uygulama boyunca yaşayan kapsamda indirir. */
    fun startDownload(id: String, url: String) {
        scope.launch { download(id, url) }
    }

    suspend fun download(id: String, url: String) = withContext(Dispatchers.IO) {
        val key = key(id, url)
        var claimed = false
        _status.update {
            claimed = it[key] != AudioStatus.Downloading && it[key] != AudioStatus.Downloaded
            if (claimed) it + (key to AudioStatus.Downloading) else it
        }
        if (!claimed) return@withContext
        val part = File(dir, key + PartSuffix)
        var result = AudioStatus.Failed
        try {
            dir.mkdirs()
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                response.body.byteStream().use { input -> part.outputStream().use { input.copyTo(it) } }
                val expected = response.body.contentLength()
                if (expected >= 0 && part.length() != expected) throw IOException("Eksik indirme")
            }
            // Aynı sesin eski adresinden kalan kopyalar artık gereksiz.
            dir.listFiles { file -> file.name.startsWith("$id$KeySeparator") && file != part }?.forEach { it.delete() }
            if (!part.renameTo(File(dir, key))) throw IOException("Dosya kaydedilemedi")
            result = AudioStatus.Downloaded
        } catch (e: IOException) {
            // Yarım kalan dosya bırakılmaz; durum Failed olur, kullanıcı tekrar deneyebilir.
        } finally {
            part.delete()
            _status.update { it + (key to result) }
        }
    }

    fun remove(id: String, url: String) {
        val key = key(id, url)
        File(dir, key).delete()
        _status.update { it - key }
    }

    private fun scan(): Map<String, AudioStatus> {
        dir.listFiles()?.filter { it.name.endsWith(PartSuffix) }?.forEach { it.delete() }
        return dir.list().orEmpty().filterNot { it.endsWith(PartSuffix) }.associateWith { AudioStatus.Downloaded }
    }

    companion object {
        private const val KeySeparator = "@"

        /** Sesin durum tablosundaki ve diskteki adı. */
        fun key(id: String, url: String) = "$id$KeySeparator${url.hashCode().toUInt().toString(16)}"

        private var instance: AudioStore? = null

        @Synchronized
        fun get(context: Context): AudioStore = instance
            ?: AudioStore(File(context.applicationContext.filesDir, "audio"), OkHttpClient()).also { instance = it }
    }
}
