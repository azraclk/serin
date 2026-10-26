package app.azracelik.serin.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MinuteMillis = 60_000L

data class SessionState(
    val lengthMs: Long,
    val elapsedMs: Long = 0,
    /** Seans süresi doldu ve ses kendiliğinden durdu; yeni seans başlayınca ya da yeniden çalınınca kalkar. */
    val completed: Boolean = false,
) {
    val lengthMinutes: Int get() = (lengthMs / MinuteMillis).toInt()
    val remainingMs: Long get() = (lengthMs - elapsedMs).coerceAtLeast(0)
    /** Kalan süre, tam dakika olarak aşağı yuvarlanır: 9:40 kalmışsa 9, son dakikada 0. */
    val remainingMinutes: Int get() = (remainingMs / MinuteMillis).toInt()
    /** Kalan süre, bildirimde gösterilmek üzere yukarı yuvarlanır: son dakikada da "1 dk kaldı" der. */
    val remainingMinutesCeil: Int get() = ((remainingMs + MinuteMillis - 1) / MinuteMillis).toInt()
    val progress: Float get() = if (lengthMs > 0) (elapsedMs.toFloat() / lengthMs).coerceIn(0f, 1f) else 0f
}

/**
 * Seçilen seans süresi ve seansın ne kadarının geçtiği. [PlaybackService] ile ekranlar aynı süreçte
 * çalıştığı için bu durum ikisi arasında doğrudan paylaşılır.
 */
object MeditationSession {
    /** Uzun süreler uykuya dalarken ses kendiliğinden kapansın diye. */
    val LengthsMinutes = listOf(5, 10, 20, 30, 60)
    const val DefaultLengthMinutes = 10

    private val _state = MutableStateFlow(SessionState(DefaultLengthMinutes * MinuteMillis))
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var baseLengthMs = DefaultLengthMinutes * MinuteMillis

    fun setLength(minutes: Int) {
        baseLengthMs = minutes * MinuteMillis
        _state.update { it.copy(lengthMs = baseLengthMs) }
    }

    /** Seçilen süreyi değiştirmeden bu seansı uzatır; yeni seans başlayınca ya da seans bitince kalkar. */
    internal fun extend(minutes: Int) = _state.update { it.copy(lengthMs = it.lengthMs + minutes * MinuteMillis) }

    internal fun clearExtension() = _state.update { it.copy(lengthMs = baseLengthMs) }

    internal fun setElapsed(elapsedMs: Long) = _state.update { it.copy(elapsedMs = elapsedMs) }

    internal fun setCompleted(completed: Boolean) = _state.update { it.copy(completed = completed) }
}
