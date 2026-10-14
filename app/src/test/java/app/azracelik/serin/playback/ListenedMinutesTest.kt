package app.azracelik.serin.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class ListenedMinutesTest {
    private val tenMinutes = 10 * 60_000L

    @Test
    fun noMinuteCountsBeforeAFullMinute() {
        assertEquals(0, listenedMinutes(0, tenMinutes))
        assertEquals(0, listenedMinutes(59_999, tenMinutes))
    }

    @Test
    fun wholeMinutesAreCounted() {
        assertEquals(1, listenedMinutes(60_000, tenMinutes))
        assertEquals(1, listenedMinutes(119_000, tenMinutes))
        assertEquals(2, listenedMinutes(120_000, tenMinutes))
    }

    @Test
    fun minutesNeverExceedTheSessionLength() {
        assertEquals(10, listenedMinutes(tenMinutes + 5_000, tenMinutes))
    }

    @Test
    fun shortenedSessionCapsAlreadyListenedTime() {
        assertEquals(5, listenedMinutes(8 * 60_000L, 5 * 60_000L))
    }
}
