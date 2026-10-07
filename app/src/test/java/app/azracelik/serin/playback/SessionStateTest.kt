package app.azracelik.serin.playback

import app.azracelik.serin.ui.components.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStateTest {
    @Test
    fun computesRemainingAndProgress() {
        val state = SessionState(lengthMs = 600_000, elapsedMs = 150_000)
        assertEquals(10, state.lengthMinutes)
        assertEquals(450_000, state.remainingMs)
        assertEquals(0.25f, state.progress, 0.0001f)
    }

    @Test
    fun clampsWhenElapsedPassesLength() {
        val state = SessionState(lengthMs = 300_000, elapsedMs = 400_000)
        assertEquals(0, state.remainingMs)
        assertEquals(1f, state.progress, 0.0001f)
    }

    @Test
    fun formatsDurationRoundingUpSeconds() {
        assertEquals("10:00", formatDuration(600_000))
        assertEquals("9:05", formatDuration(544_100))
        assertEquals("0:01", formatDuration(1))
        assertEquals("0:00", formatDuration(0))
    }
}
