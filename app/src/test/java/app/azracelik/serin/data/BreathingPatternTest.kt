package app.azracelik.serin.data

import org.junit.Assert.assertEquals
import org.junit.Test

class BreathingPatternTest {
    @Test
    fun calmIsTheOriginalEightSecondBreath() {
        assertEquals(8, BreathingPattern.Calm.cycleSeconds)
        assertEquals(listOf(BreathPhase.Inhale, BreathPhase.Exhale), BreathingPattern.Calm.steps.map { it.phase })
    }

    @Test
    fun fourSevenEightHoldsAfterTheInhale() {
        val steps = BreathingPattern.FourSevenEight.steps
        assertEquals(listOf(4, 7, 8), steps.map { it.seconds })
        assertEquals(listOf(BreathPhase.Inhale, BreathPhase.HoldFull, BreathPhase.Exhale), steps.map { it.phase })
    }

    @Test
    fun boxBreathingHasFourEqualPhases() {
        val steps = BreathingPattern.Box.steps
        assertEquals(listOf(4, 4, 4, 4), steps.map { it.seconds })
        assertEquals(16, BreathingPattern.Box.cycleSeconds)
    }

    @Test
    fun everyPatternStartsWithAnInhale() {
        BreathingPattern.entries.forEach { assertEquals(BreathPhase.Inhale, it.steps.first().phase) }
    }

    @Test
    fun unknownSavedNameFallsBackToCalm() {
        assertEquals(BreathingPattern.Calm, BreathingPattern.fromName(null))
        assertEquals(BreathingPattern.Calm, BreathingPattern.fromName("Eski"))
        assertEquals(BreathingPattern.Box, BreathingPattern.fromName("Box"))
    }
}
