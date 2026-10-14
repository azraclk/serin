package app.azracelik.serin.reminder

import androidx.core.content.edit
import android.content.Context
import app.azracelik.serin.data.SessionRecord
import java.time.LocalDate
import java.time.LocalDateTime

/** Günlük hatırlatıcının ayarı; saat yerel saattir. */
data class ReminderSettings(val enabled: Boolean = false, val hour: Int = 21, val minute: Int = 0)

private const val PrefsName = "serin_reminder"
private const val EnabledKey = "enabled"
private const val HourKey = "hour"
private const val MinuteKey = "minute"

/** Ayarları telefonda saklar; uygulama kapalıyken çalışan alıcılar da buradan okur. */
class ReminderStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    fun load(): ReminderSettings {
        val default = ReminderSettings()
        return ReminderSettings(
            enabled = prefs.getBoolean(EnabledKey, default.enabled),
            hour = prefs.getInt(HourKey, default.hour).coerceIn(0, 23),
            minute = prefs.getInt(MinuteKey, default.minute).coerceIn(0, 59),
        )
    }

    fun save(settings: ReminderSettings) {
        prefs.edit {
            putBoolean(EnabledKey, settings.enabled)
            putInt(HourKey, settings.hour)
            putInt(MinuteKey, settings.minute)
        }
    }
}

/** [hour]:[minute] saatinin [now]'dan sonraki ilk gelişi: bugün henüz gelmediyse bugün, geçtiyse yarın. */
fun nextReminderAt(now: LocalDateTime, hour: Int, minute: Int): LocalDateTime {
    val today = now.toLocalDate().atTime(hour, minute)
    return if (today.isAfter(now)) today else today.plusDays(1)
}

/** Bugün zaten meditasyon yapıldıysa kullanıcıyı rahatsız etmeyiz. */
fun shouldRemind(records: List<SessionRecord>, today: LocalDate): Boolean =
    records.none { it.epochDay == today.toEpochDay() }
