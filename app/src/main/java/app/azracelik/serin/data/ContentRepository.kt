package app.azracelik.serin.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

const val ContentBaseUrl = "https://raw.githubusercontent.com/azraclk/serin-content/main/"
private const val ContentFileName = "content.json"
private const val Tag = "ContentRepository"

private val json = Json { ignoreUnknownKeys = true }

fun parseContent(text: String): SerinContent =
    json.decodeFromString<SerinContent>(text).resolveUrls(ContentBaseUrl)

/**
 * İçeriği GitHub'daki serin-content reposundan indirir. Son başarılı indirme telefonda saklanır;
 * hiç indirme yapılmamışsa uygulamayla gelen assets/content.json kullanılır.
 */
class ContentRepository(context: Context, private val client: OkHttpClient) {
    private val assets = context.assets
    private val cacheFile = File(context.filesDir, ContentFileName)

    private val _content = MutableStateFlow(loadLocal())
    val content: StateFlow<SerinContent> = _content.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val text = fetch(ContentBaseUrl + ContentFileName) ?: return@withContext
        try {
            // Bozuk bir JSON burada hata fırlatır ve önceki içerik ekranda kalır.
            _content.value = parseContent(text)
            cacheFile.writeText(text)
        } catch (e: IllegalArgumentException) {
            Log.w(Tag, "content.json okunamadı", e)
        }
    }

    /** Blog yazısı gibi bir metin dosyasını indirir; internet yoksa daha önce indirilmiş hâlini döner. */
    suspend fun loadText(url: String): String? = withContext(Dispatchers.IO) {
        fetch(url) ?: fetch(url, CacheControl.FORCE_CACHE)
    }

    private fun fetch(url: String, cacheControl: CacheControl? = null): String? = try {
        val request = Request.Builder()
            .url(url)
            .apply { if (cacheControl != null) cacheControl(cacheControl) }
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.body.string()
            } else {
                Log.w(Tag, "$url indirilemedi: HTTP ${response.code}")
                null
            }
        }
    } catch (e: IOException) {
        Log.w(Tag, "$url indirilemedi", e)
        null
    }

    private fun loadLocal(): SerinContent {
        if (cacheFile.exists()) {
            try {
                return parseContent(cacheFile.readText())
            } catch (e: IllegalArgumentException) {
                Log.w(Tag, "Saklanan içerik okunamadı", e)
            }
        }
        return parseContent(assets.open(ContentFileName).bufferedReader().use { it.readText() })
    }
}
