package app.azracelik.serin.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

private const val PrefsName = "serin_library"
private const val HistoryKey = "history"

/** Telefonda tutulan en fazla kayıt sayısı; eskiler atılır. */
private const val MaxRecords = 500

private const val StreakWindowDays = 7

private val json = Json { ignoreUnknownKeys = true }

/** Sonuna kadar dinlenmiş bir seans. [epochDay], seansın bittiği günün yerel tarihidir. */
@Serializable
data class SessionRecord(val meditationId: String, val minutes: Int, val epochDay: Long)

/** Ana sayfadaki özet: ardışık gün serisi, son yedi günde dinlenen süre ve toplam seans. */
data class HistoryStats(val streakDays: Int, val weekMinutes: Int, val totalSessions: Int) {
    companion object {
        /**
         * Seri, bugün ya da dünden geriye doğru kesintisiz giden gün sayısıdır; bugün henüz
         * dinlenmediyse dünkü seri bozulmaz, gün bitene kadar devam ettirilebilir.
         */
        fun of(records: List<SessionRecord>, today: LocalDate): HistoryStats {
            val todayDay = today.toEpochDay()
            val days = records.mapTo(HashSet()) { it.epochDay }
            var day = if (todayDay in days) todayDay else todayDay - 1
            var streak = 0
            while (day in days) {
                streak++
                day--
            }
            val weekMinutes = records
                .filter { it.epochDay in (todayDay - StreakWindowDays + 1)..todayDay }
                .sumOf { it.minutes }
            return HistoryStats(streak, weekMinutes, records.size)
        }
    }
}

/** [record] eklenmiş, [MaxRecords]'u aşmayan liste. */
fun List<SessionRecord>.withRecord(record: SessionRecord): List<SessionRecord> = (this + record).takeLast(MaxRecords)

/**
 * Tamamlanan seansların kaydı. [PlaybackService] yazar, ekranlar okur; ikisi de aynı süreçte
 * çalıştığı için tek bir örnek paylaşılır.
 */
class HistoryStore private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    private val _records = MutableStateFlow(load())
    val records: StateFlow<List<SessionRecord>> = _records.asStateFlow()

    fun add(meditationId: String, minutes: Int) {
        val record = SessionRecord(meditationId, minutes, LocalDate.now().toEpochDay())
        _records.update { it.withRecord(record) }
        prefs.edit().putString(HistoryKey, json.encodeToString(_records.value)).apply()
    }

    private fun load(): List<SessionRecord> {
        val text = prefs.getString(HistoryKey, null) ?: return emptyList()
        return try {
            json.decodeFromString(text)
        } catch (e: IllegalArgumentException) {
            // Bozuk kayıt uygulamayı açılmaz hâle getirmesin; geçmiş sıfırdan başlar.
            emptyList()
        }
    }

    companion object {
        private var instance: HistoryStore? = null

        @Synchronized
        fun get(context: Context): HistoryStore =
            instance ?: HistoryStore(context.applicationContext).also { instance = it }
    }
}
