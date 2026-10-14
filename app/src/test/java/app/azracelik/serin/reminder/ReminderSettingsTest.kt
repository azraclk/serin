package app.azracelik.serin.reminder

import app.azracelik.serin.data.SessionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderSettingsTest {
    private val now = LocalDateTime.of(2026, 10, 14, 15, 30)

    @Test
    fun reminderLaterTodayFiresToday() {
        assertEquals(LocalDateTime.of(2026, 10, 14, 21, 0), nextReminderAt(now, 21, 0))
    }

    @Test
    fun reminderAlreadyPastFiresTomorrow() {
        assertEquals(LocalDateTime.of(2026, 10, 15, 9, 15), nextReminderAt(now, 9, 15))
    }

    @Test
    fun reminderAtTheExactMinuteFiresTomorrow() {
        assertEquals(LocalDateTime.of(2026, 10, 15, 15, 30), nextReminderAt(now, 15, 30))
    }

    @Test
    fun reminderCrossesMonthEnd() {
        val lastDay = LocalDateTime.of(2026, 10, 31, 23, 0)
        assertEquals(LocalDateTime.of(2026, 11, 1, 8, 0), nextReminderAt(lastDay, 8, 0))
    }

    @Test
    fun noReminderWhenTodayAlreadyHasASession() {
        val today = LocalDate.of(2026, 10, 14)
        assertFalse(shouldRemind(listOf(SessionRecord("sleep", 5, today.toEpochDay())), today))
    }

    @Test
    fun reminderWhenOnlyEarlierDaysHaveSessions() {
        val today = LocalDate.of(2026, 10, 14)
        assertTrue(shouldRemind(listOf(SessionRecord("sleep", 5, today.toEpochDay() - 1)), today))
        assertTrue(shouldRemind(emptyList(), today))
    }

    @Test
    fun defaultsAreOffAtNineInTheEvening() {
        assertEquals(ReminderSettings(enabled = false, hour = 21, minute = 0), ReminderSettings())
    }
}
