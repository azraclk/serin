package app.azracelik.serin.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HistoryTest {
    private val today = LocalDate.of(2026, 10, 14)

    private fun record(daysAgo: Long, minutes: Int = 10) =
        SessionRecord("sleep", minutes, today.toEpochDay() - daysAgo)

    @Test
    fun emptyHistoryHasNoStats() {
        assertEquals(HistoryStats(0, 0, 0), HistoryStats.of(emptyList(), today))
    }

    @Test
    fun streakCountsConsecutiveDaysEndingToday() {
        val stats = HistoryStats.of(listOf(record(0), record(1), record(2)), today)
        assertEquals(3, stats.streakDays)
    }

    @Test
    fun streakSurvivesUntilTheDayEnds() {
        val stats = HistoryStats.of(listOf(record(1), record(2)), today)
        assertEquals(2, stats.streakDays)
    }

    @Test
    fun streakBreaksAfterAMissedDay() {
        assertEquals(0, HistoryStats.of(listOf(record(2), record(3)), today).streakDays)
        assertEquals(1, HistoryStats.of(listOf(record(0), record(2), record(3)), today).streakDays)
    }

    @Test
    fun severalSessionsOnOneDayCountOnce() {
        val stats = HistoryStats.of(listOf(record(0), record(0), record(1)), today)
        assertEquals(2, stats.streakDays)
        assertEquals(3, stats.totalSessions)
    }

    @Test
    fun weekMinutesCoverTheLastSevenDays() {
        val stats = HistoryStats.of(listOf(record(0, 5), record(6, 10), record(7, 20)), today)
        assertEquals(15, stats.weekMinutes)
        assertEquals(3, stats.totalSessions)
    }

    @Test
    fun oldestRecordsAreDroppedBeyondTheLimit() {
        val records = (0 until 600).fold(emptyList<SessionRecord>()) { list, i ->
            list.withRecord(SessionRecord("sleep", 5, i.toLong()))
        }
        assertEquals(500, records.size)
        assertEquals(100L, records.first().epochDay)
        assertEquals(599L, records.last().epochDay)
    }

    @Test
    fun firstMinuteStartsARecord() {
        val records = emptyList<SessionRecord>().withMinute("sleep", firstMinute = true, epochDay = 5)
        assertEquals(listOf(SessionRecord("sleep", 1, 5)), records)
    }

    @Test
    fun laterMinutesExtendTheOpenRecord() {
        val records = emptyList<SessionRecord>()
            .withMinute("sleep", true, 5)
            .withMinute("sleep", false, 5)
            .withMinute("sleep", false, 5)
        assertEquals(listOf(SessionRecord("sleep", 3, 5)), records)
    }

    @Test
    fun aNewSessionGetsItsOwnRecord() {
        val records = emptyList<SessionRecord>()
            .withMinute("sleep", true, 5)
            .withMinute("sleep", false, 5)
            .withMinute("sleep", true, 6)
        assertEquals(listOf(SessionRecord("sleep", 2, 5), SessionRecord("sleep", 1, 6)), records)
    }

    @Test
    fun aMinuteOfADifferentMeditationDoesNotExtendTheLastRecord() {
        val records = listOf(SessionRecord("sleep", 4, 5)).withMinute("focus", firstMinute = false, epochDay = 5)
        assertEquals(listOf(SessionRecord("sleep", 4, 5), SessionRecord("focus", 1, 5)), records)
    }

    @Test
    fun listeningForOneMinuteCountsForTheStreak() {
        val stats = HistoryStats.of(emptyList<SessionRecord>().withMinute("sleep", true, today.toEpochDay()), today)
        assertEquals(HistoryStats(streakDays = 1, weekMinutes = 1, totalSessions = 1), stats)
    }
}
