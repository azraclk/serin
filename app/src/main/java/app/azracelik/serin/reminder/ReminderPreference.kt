package app.azracelik.serin.reminder

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Ekrandaki hatırlatıcı ayarı; değişince alarmı da kurar ya da kaldırır. */
class ReminderPreference(private val context: Context) {
    private val store = ReminderStore(context)

    var settings: ReminderSettings by mutableStateOf(store.load())
        private set

    fun update(new: ReminderSettings) {
        store.save(new)
        settings = new
        if (new.enabled) ReminderScheduler.schedule(context, new) else ReminderScheduler.cancel(context)
    }
}

/** Üst çubuktaki hatırlatıcı düğmesinin kaynağı; splash gibi yerlerde verilmez. */
val LocalReminder = staticCompositionLocalOf<ReminderPreference?> { null }
