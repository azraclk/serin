package app.azracelik.serin

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.azracelik.serin.data.AudioStatus
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.playback.SessionState
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.meditation.MeditationDetailScreen
import app.azracelik.serin.ui.meditation.QuietEffect
import app.azracelik.serin.ui.meditation.QuietMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sessiz modun sayaç mantığı ve dinleme sayfasının sessiz / bitmiş hâli. Uygulamanın tamamı değil bileşenler
 * sürülür: çalan seansın sonsuz nefes animasyonu olmadığı için test saati istenen kadar ilerletilebilir.
 */
@RunWith(AndroidJUnit4::class)
class QuietModeTest {
    @get:Rule
    val rule = createComposeRule()

    private fun exists(text: String) = rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()

    private val quiet = QuietMode()
    private var enabled by mutableStateOf(true)

    private fun showEffect() {
        enabled = true
        rule.setContent {
            QuietEffect(quiet, enabled)
            Text(if (quiet.isQuiet) "sessiz" else "görünür")
        }
    }

    @Test
    fun fallsQuietOnlyAfterEightIdleSeconds() {
        showEffect()
        rule.mainClock.advanceTimeBy(7_800)
        assertFalse(quiet.isQuiet)
        rule.mainClock.advanceTimeBy(400)
        assertTrue(quiet.isQuiet)
        assertTrue(exists("sessiz"))
    }

    @Test
    fun touchWakesTheUiAndRestartsTheCountdown() {
        showEffect()
        rule.mainClock.advanceTimeBy(8_200)
        assertTrue(quiet.isQuiet)

        rule.runOnUiThread { quiet.touch() }
        rule.mainClock.advanceTimeBy(100)
        assertFalse(quiet.isQuiet)
        assertTrue(exists("görünür"))

        // Sayaç baştan başlar: dokunuştan 7 sn sonra hâlâ görünür, 8 sn sonra soluk.
        rule.mainClock.advanceTimeBy(7_000)
        assertFalse(quiet.isQuiet)
        rule.mainClock.advanceTimeBy(1_200)
        assertTrue(quiet.isQuiet)
    }

    @Test
    fun disablingBringsTheUiBackAtOnce() {
        showEffect()
        rule.mainClock.advanceTimeBy(8_200)
        assertTrue(quiet.isQuiet)

        // Duraklatma, sayfadan çıkma ya da animasyonların kapanması.
        rule.runOnUiThread { enabled = false }
        rule.mainClock.advanceTimeBy(100)
        assertFalse(quiet.isQuiet)

        rule.mainClock.advanceTimeBy(20_000)
        assertFalse("Kapalıyken hiç solmamalı", quiet.isQuiet)
    }

    private val session = SessionState(lengthMs = 600_000, elapsedMs = 240_000)

    private fun showDetail(isQuiet: Boolean = false, state: SessionState = session, weekMinutes: Int = 0) {
        rule.setContent {
            PreviewWindow(SerinTab.Meditation) {
                MeditationDetailScreen(
                    meditation = PreviewContent.meditations[3],
                    isPlaying = false,
                    isBuffering = false,
                    session = state,
                    isFavorite = false,
                    downloadStatus = AudioStatus.Downloaded,
                    onPlayClick = {},
                    onFavoriteToggle = {},
                    onDownloadClick = {},
                    quiet = isQuiet,
                    weekMinutes = weekMinutes,
                )
            }
        }
    }

    @Test
    fun detail_showsTheFullClockWhenNotQuiet() {
        showDetail()
        rule.waitForIdle()
        assertTrue(exists("6:00"))
        assertFalse(exists("6 dk"))
    }

    @Test
    fun detail_showsOnlyRemainingMinutesWhenQuiet() {
        showDetail(isQuiet = true)
        rule.mainClock.advanceTimeBy(2_000)
        assertTrue(exists("6 dk"))
        assertFalse(exists("6:00"))
    }

    @Test
    fun detail_finishedSessionShowsTheNoteAndSummary() {
        showDetail(state = SessionState(lengthMs = 600_000, completed = true), weekMinutes = 40)
        rule.mainClock.advanceTimeBy(2_000)
        assertTrue(exists("Tamamlandı"))
        assertTrue(exists("10 dk dinledin · bu hafta 40 dk"))
        assertFalse(exists("10:00"))
    }

    @Test
    fun detail_unfinishedSessionHasNoNote() {
        showDetail()
        rule.waitForIdle()
        assertFalse(exists("Tamamlandı"))
    }
}
