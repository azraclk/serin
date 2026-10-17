package app.azracelik.serin.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FadeOutTest {
    @Test
    fun shortSessionsFadeOutQuickly() {
        assertEquals(10_000L, fadeOutMillis(20 * 60_000L))
    }

    @Test
    fun longSessionsFadeOutSlowly() {
        assertEquals(30_000L, fadeOutMillis(30 * 60_000L))
        assertEquals(30_000L, fadeOutMillis(60 * 60_000L))
    }

    @Test
    fun sessionLengthsStaySortedAndIncludeTheDefault() {
        val lengths = MeditationSession.LengthsMinutes
        assertEquals(lengths.sorted(), lengths)
        assertTrue(MeditationSession.DefaultLengthMinutes in lengths)
        assertTrue(60 in lengths)
    }

    @Test
    fun volumeFadesInThenHoldsThenFadesOut() {
        val length = 10 * 60_000L
        assertEquals(0f, sessionVolume(0, length), 0.0001f)
        assertEquals(0.5f, sessionVolume(1_500, length), 0.0001f)
        assertEquals(1f, sessionVolume(3_000, length), 0.0001f)
        assertEquals(1f, sessionVolume(length / 2, length), 0.0001f)
        // Kısa seansta son 10 saniyede kısılır.
        assertEquals(0.5f, sessionVolume(length - 5_000, length), 0.0001f)
        assertEquals(0f, sessionVolume(length, length), 0.0001f)
    }

    @Test
    fun longSessionsFadeOutOverThirtySeconds() {
        val length = 30 * 60_000L
        assertEquals(0.5f, sessionVolume(length - 15_000, length), 0.0001f)
    }

    @Test
    fun volumeNeverGoesNegative() {
        assertEquals(0f, sessionVolume(700_000, 600_000), 0.0001f)
    }
}
