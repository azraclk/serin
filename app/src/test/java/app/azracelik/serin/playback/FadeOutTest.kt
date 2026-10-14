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
}
