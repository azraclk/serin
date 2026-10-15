package app.azracelik.serin.ui.meditation

import app.azracelik.serin.data.BreathPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BreathHapticsTest {
    private fun HapticWave.pulseAmplitudes() = amplitudes.filter { it > 0 }

    @Test
    fun inhaleGetsDenserAndStrongerAsTheBreathFills() {
        val wave = hapticWave(BreathPhase.Inhale, 4)!!
        val amplitudes = wave.pulseAmplitudes()
        assertEquals(amplitudes.sorted(), amplitudes)
        assertTrue(amplitudes.first() < amplitudes.last())
        val gaps = wave.timings.filterIndexed { i, _ -> i % 2 == 0 }.drop(1)
        assertEquals(gaps.sortedDescending(), gaps)
    }

    @Test
    fun exhaleGetsSparserAndSofterAsTheBreathEmpties() {
        val wave = hapticWave(BreathPhase.Exhale, 8)!!
        val amplitudes = wave.pulseAmplitudes()
        assertEquals(amplitudes.sortedDescending(), amplitudes)
        assertTrue(amplitudes.first() > amplitudes.last())
        val gaps = wave.timings.filterIndexed { i, _ -> i % 2 == 0 }.drop(1)
        assertEquals(gaps.sorted(), gaps)
    }

    @Test
    fun theWaveFillsThePhaseWithoutRunningOverIt() {
        listOf(BreathPhase.Inhale to 4, BreathPhase.Inhale to 8, BreathPhase.Exhale to 4, BreathPhase.Exhale to 8)
            .forEach { (phase, seconds) ->
                val total = hapticWave(phase, seconds)!!.timings.sum()
                assertTrue("$phase $seconds: $total", total in (seconds * 1000L - 20)..(seconds * 1000L))
            }
    }

    @Test
    fun timingsAndAmplitudesLineUpAndNeverGoNegative() {
        BreathPhase.entries.mapNotNull { hapticWave(it, 7) }.forEach { wave ->
            assertEquals(wave.timings.size, wave.amplitudes.size)
            assertTrue(wave.timings.all { it >= 0 })
            assertTrue(wave.amplitudes.all { it in 0..255 })
            // Sessiz süreler genlik 0, titreşimli süreler 0'dan büyük.
            wave.amplitudes.forEachIndexed { i, amplitude -> assertEquals(i % 2 == 1, amplitude > 0) }
        }
    }

    @Test
    fun holdingFullGivesOneSoftTick() {
        val wave = hapticWave(BreathPhase.HoldFull, 7)!!
        assertEquals(1, wave.pulseAmplitudes().size)
    }

    @Test
    fun waitingWithEmptyLungsStaysSilent() {
        assertNull(hapticWave(BreathPhase.HoldEmpty, 4))
    }
}
