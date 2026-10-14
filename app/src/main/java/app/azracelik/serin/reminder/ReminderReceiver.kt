package app.azracelik.serin.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.azracelik.serin.data.HistoryStats
import app.azracelik.serin.data.HistoryStore
import java.time.LocalDate

/** Alarm çalınca: yarının alarmını kurar, bugün henüz meditasyon yapılmadıysa bildirimi gösterir. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = ReminderStore(context).load()
        if (!settings.enabled) return
        ReminderScheduler.schedule(context, settings)
        val today = LocalDate.now()
        val records = HistoryStore.get(context).records.value
        if (shouldRemind(records, today)) {
            ReminderNotifier.show(context, HistoryStats.of(records, today).streakDays)
        }
    }
}

/** Telefon yeniden başlayınca, uygulama güncellenince ya da saat değişince alarmı yeniden kurar. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = ReminderScheduler.restore(context)
}
