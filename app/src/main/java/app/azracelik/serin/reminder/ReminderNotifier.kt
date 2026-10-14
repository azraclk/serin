package app.azracelik.serin.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import app.azracelik.serin.MainActivity
import app.azracelik.serin.R

private const val ChannelId = "daily_reminder"
private const val NotificationId = 1

/** Günlük hatırlatma bildirimi; bildirim izni yoksa hiçbir şey göstermez. */
object ReminderNotifier {
    /** Android 13'ten itibaren bildirim göstermek için kullanıcıdan izin istenir. */
    fun needsPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    fun show(context: Context, streakDays: Int) {
        if (needsPermission(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ChannelId, context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val text = if (streakDays > 0) {
            context.getString(R.string.reminder_text_streak, streakDays)
        } else {
            context.getString(R.string.reminder_text)
        }
        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.logo))
            .setContentText(text)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NotificationId, notification)
    }
}
