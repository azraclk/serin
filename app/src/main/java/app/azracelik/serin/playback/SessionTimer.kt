package app.azracelik.serin.playback

import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TickMillis = 200L
private const val FadeInMillis = 3_000L
private const val ShortFadeOutMillis = 10_000L
private const val LongFadeOutMillis = 30_000L
private const val LongSessionMillis = 30 * 60_000L
private const val MinuteMillis = 60_000L

/** Uzun seanslar uykuya dalmaya yardım eder; ses daha yavaş kısılır. */
internal fun fadeOutMillis(lengthMs: Long): Long =
    if (lengthMs >= LongSessionMillis) LongFadeOutMillis else ShortFadeOutMillis

/**
 * Seansın [elapsedMs] anındaki ses seviyesi (0..1): başta [FadeInMillis]'te yavaşça açılır, sonda kısılarak biter.
 * Dinleme sayfasındaki ışık da aynı eğriyi izler; ses ile görüntü birlikte açılıp kapanır.
 */
internal fun sessionVolume(elapsedMs: Long, lengthMs: Long): Float =
    minOf(1f, elapsedMs.toFloat() / FadeInMillis, (lengthMs - elapsedMs).toFloat() / fadeOutMillis(lengthMs))
        .coerceAtLeast(0f)

/** Seansın dinlenen tam dakikası; seans süresini aşamaz. */
internal fun listenedMinutes(elapsedMs: Long, lengthMs: Long): Int =
    (minOf(elapsedMs, lengthMs) / MinuteMillis).toInt()

/**
 * Sesi [MeditationSession] süresi boyunca döngüde çalar; başta yavaşça açar, son saniyelerde kısarak
 * bitirir ve başa sarıp duraklatır. Servisin içinde çalıştığı için ekran kapalıyken de süre işler.
 */
class SessionTimer(
    private val player: Player,
    private val scope: CoroutineScope,
    /** Seansın her tam dakikası dinlendiğinde çağrılır; [firstMinute] seansın ilk dakikasıdır. */
    private val onMinuteListened: (mediaId: String?, firstMinute: Boolean) -> Unit,
) : Player.Listener {
    private var ticker: Job? = null
    private var elapsedMs = 0L
    private var reportedMinutes = 0

    fun start() {
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.addListener(this)
        reset()
    }

    fun release() {
        ticker?.cancel()
        player.removeListener(this)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        // Döngüde parçanın başa dönmesi seansın devamıdır, yeni seans değil.
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT) reset()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        ticker?.cancel()
        if (!isPlaying) return
        MeditationSession.setCompleted(false)
        ticker = scope.launch {
            var last = SystemClock.elapsedRealtime()
            while (isActive) {
                delay(TickMillis)
                val now = SystemClock.elapsedRealtime()
                tick(now - last)
                last = now
            }
        }
    }

    private fun tick(deltaMs: Long) {
        elapsedMs += deltaMs
        val lengthMs = MeditationSession.state.value.lengthMs
        val wholeMinutes = listenedMinutes(elapsedMs, lengthMs)
        while (reportedMinutes < wholeMinutes) {
            reportedMinutes++
            onMinuteListened(player.currentMediaItem?.mediaId, reportedMinutes == 1)
        }
        if (elapsedMs >= lengthMs) {
            player.pause()
            player.seekTo(0)
            reset()
            // Sayfa, seansın bittiğini sessizce söyleyebilsin diye; reset() bayrağı temizler.
            MeditationSession.setCompleted(true)
            return
        }
        player.volume = sessionVolume(elapsedMs, lengthMs)
        // Ses seviyesi sık güncellenir ama ekran saniyede bir değişir; gereksiz yeniden çizimi önlemek için
        // süre saniyeye yuvarlanarak yayınlanır.
        val wholeSecondMs = elapsedMs / 1000 * 1000
        if (wholeSecondMs != MeditationSession.state.value.elapsedMs) MeditationSession.setElapsed(wholeSecondMs)
    }

    private fun reset() {
        elapsedMs = 0
        reportedMinutes = 0
        // Yeni seans sessizden başlar; ses ilk saniyelerde açılır.
        player.volume = 0f
        MeditationSession.clearExtension()
        MeditationSession.setElapsed(0)
        MeditationSession.setCompleted(false)
    }
}
