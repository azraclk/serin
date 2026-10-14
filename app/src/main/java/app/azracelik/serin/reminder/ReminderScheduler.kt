package app.azracelik.serin.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Hatırlatıcı için her seferinde yalnızca bir sonraki alarmı kurar; alarm çalınca [ReminderReceiver]
 * ertesi günküsünü kurar. Kesin alarm izni (Android 12+) varsa alarm tam saatinde çalar; yoksa sistem
 * alarmı saatten sonraki yaklaşık bir saat içinde herhangi bir ana erteleyebilir.
 */
object ReminderScheduler {
    fun schedule(context: Context, settings: ReminderSettings) {
        val next = nextReminderAt(LocalDateTime.now(), settings.hour, settings.minute)
        val millis = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val manager = alarmManager(context)
        val intent = pendingIntent(context)
        try {
            if (canScheduleExact(context)) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
                return
            }
        } catch (e: SecurityException) {
            // İzin tam bu sırada geri alınmış olabilir; kesin olmayan alarma düşülür.
        }
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, intent)
    }

    /** Android 12 öncesinde kesin alarm için izin gerekmez; sonrasında kullanıcı ayarlardan verir. */
    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager(context).canScheduleExactAlarms()

    fun cancel(context: Context) = alarmManager(context).cancel(pendingIntent(context))

    /** Uygulama açılırken ya da telefon yeniden başlayınca: ayar açıksa alarmın kurulu olmasını sağlar. */
    fun restore(context: Context) {
        val settings = ReminderStore(context).load()
        if (settings.enabled) schedule(context, settings)
    }

    private fun alarmManager(context: Context) = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
