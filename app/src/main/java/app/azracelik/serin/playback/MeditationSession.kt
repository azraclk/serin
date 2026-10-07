package app.azracelik.serin.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val MinuteMillis = 60_000L

data class SessionState(
    val lengthMs: Long,
    val elapsedMs: Long = 0,
) {
    val lengthMinutes: Int get() = (lengthMs / MinuteMillis).toInt()
    val remainingMs: Long get() = (lengthMs - elapsedMs).coerceAtLeast(0)
    val progress: Float get() = if (lengthMs > 0) (elapsedMs.toFloat() / lengthMs).coerceIn(0f, 1f) else 0f
}

/**
 * Seçilen seans süresi ve seansın ne kadarının geçtiği. [PlaybackService] ile ekranlar aynı süreçte
 * çalıştığı için bu durum ikisi arasında doğrudan paylaşılır.
 */
object MeditationSession {
    val LengthsMinutes = listOf(5, 10, 20)
    const val DefaultLengthMinutes = 10

    private val _state = MutableStateFlow(SessionState(DefaultLengthMinutes * MinuteMillis))
    val state: StateFlow<SessionState> = _state.asStateFlow()

    fun setLength(minutes: Int) = _state.update { it.copy(lengthMs = minutes * MinuteMillis) }

    internal fun setElapsed(elapsedMs: Long) = _state.update { it.copy(elapsedMs = elapsedMs) }
}
