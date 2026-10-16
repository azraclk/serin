package app.azracelik.serin

import android.content.Context
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.PlayerViewModel
import app.azracelik.serin.ui.meditation.MeditationDetailTag
import app.azracelik.serin.ui.meditation.SessionStartTag
import android.os.ParcelFileDescriptor
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ToLight = "Açık temaya geç"
private const val ToDark = "Gece temasına geç"

/** Tema düğmesi ve seans süresi seçicisi: seçim ekrana yansır ve uygulama yeniden açılınca korunur. */
@RunWith(AndroidJUnit4::class)
class SettingsTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    companion object {
        private var animatorScale = "1"

        private fun shell(command: String): String {
            val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            return ParcelFileDescriptor.AutoCloseInputStream(pfd).bufferedReader().use { it.readText().trim() }
        }

        /** Başlatılan seansın detay ekranındaki sonsuz animasyonlar Compose testinin "boşta" beklemesini engeller. */
        @BeforeClass
        @JvmStatic
        fun disableAnimations() {
            animatorScale = shell("settings get global animator_duration_scale").ifEmpty { "1" }.takeIf { it != "null" } ?: "1"
            shell("settings put global animator_duration_scale 0")
        }

        @AfterClass
        @JvmStatic
        fun restoreAnimations() {
            shell("settings put global animator_duration_scale $animatorScale")
        }
    }

    private fun toggleLabeled(label: String) = SemanticsMatcher("tıklama etiketi: $label") {
        it.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

    private fun toggleExists(label: String) =
        rule.onAllNodes(toggleLabeled(label)).fetchSemanticsNodes().isNotEmpty()

    private fun textExists(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun awaitHome() = rule.waitUntil(timeoutMillis = 10_000) { textExists("Keşfet") }

    /** Düğmenin şu an önerdiği etiket; sistemin teması ne olursa olsun test çalışsın diye. */
    private fun currentLabel(): String {
        awaitHome()
        rule.waitUntil(timeoutMillis = 5_000) { toggleExists(ToLight) || toggleExists(ToDark) }
        return if (toggleExists(ToLight)) ToLight else ToDark
    }

    private fun other(label: String) = if (label == ToLight) ToDark else ToLight

    private fun click(label: String) = rule.onNode(toggleLabeled(label)).performClick()

    private fun awaitLabel(label: String) = rule.waitUntil(timeoutMillis = 5_000) { toggleExists(label) }

    @After
    fun reset() {
        // Tema seçimi ve seans süresi sonraki testlere (ve cihaza) sızmasın.
        context.getSharedPreferences("serin_theme", Context.MODE_PRIVATE).edit().clear().commit()
        val player = ViewModelProvider(rule.activity)[PlayerViewModel::class.java]
        rule.runOnUiThread {
            player.setSessionLength(MeditationSession.DefaultLengthMinutes)
            player.stop()
        }
    }

    @Test
    fun themeToggle_switchesAndSwitchesBack() {
        val start = currentLabel()
        click(start)
        awaitLabel(other(start))
        click(other(start))
        awaitLabel(start)
    }

    @Test
    fun themeToggle_choiceSurvivesRecreate() {
        val start = currentLabel()
        click(start)
        awaitLabel(other(start))
        rule.activityRule.scenario.recreate()
        awaitLabel(other(start))
        assertTrue(!toggleExists(start))
    }

    @Test
    fun themeToggle_worksOnOtherTabs() {
        val start = currentLabel()
        rule.onNode(hasContentDescription("Meditasyon") and hasClickAction()).performClick()
        awaitLabel(start)
        click(start)
        awaitLabel(other(start))
    }

    private fun existsTag(tag: String) = rule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    /** Karta dokununca dinleme sayfası değil, süre seçim sayfası açılır. */
    private fun openSheet() {
        awaitHome()
        rule.onAllNodesWithText("farkındalık").onFirst().performClick()
        rule.waitUntil(timeoutMillis = 10_000) { existsTag(SessionStartTag) }
    }

    private fun startFromSheet() {
        rule.onNode(hasTestTag(SessionStartTag)).performClick()
        rule.waitUntil(timeoutMillis = 10_000) { existsTag(MeditationDetailTag) }
    }

    private fun chip(text: String) = rule.onNode(hasText(text) and isSelectable())

    private fun awaitClock(text: String) = rule.waitUntil(timeoutMillis = 5_000) { textExists(text) }

    private fun selectedChipExists(text: String) =
        rule.onAllNodes(hasText(text) and isSelectable() and isSelected()).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun sessionLength_sheetStartsOnTheStoredLength() {
        openSheet()
        assertTrue(selectedChipExists("10 dk"))
    }

    @Test
    fun sessionLength_sheetShowsWhenTheSessionEnds() {
        openSheet()
        assertTrue(rule.onAllNodes(hasText("Bitiş", substring = true)).fetchSemanticsNodes().isNotEmpty())
        val before = rule.onAllNodes(hasText("Bitiş", substring = true)).fetchSemanticsNodes().first()
            .config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }
        chip("60 dk").performClick()
        // Süre değişince bitiş saati de değişir.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodes(hasText("Bitiş", substring = true)).fetchSemanticsNodes().firstOrNull()
                ?.config?.getOrNull(SemanticsProperties.Text)?.joinToString { it.text } != before
        }
    }

    @Test
    fun sessionLength_selectionMovesAndStartsTheSession() {
        openSheet()
        chip("5 dk").performClick()
        assertTrue(selectedChipExists("5 dk"))
        chip("20 dk").performClick()
        assertTrue(selectedChipExists("20 dk"))
        assertTrue(!selectedChipExists("5 dk"))
        startFromSheet()
        awaitClock("20:00")
        // Dinleme sayfasında süre seçici yok.
        assertTrue(!textExists("5 dk"))
    }

    @Test
    fun sessionLength_isStoredAndShownOnHome() {
        openSheet()
        chip("20 dk").performClick()
        // Seçim, Başla'ya basılana kadar kaydedilmez.
        val before = context.getSharedPreferences("player", Context.MODE_PRIVATE).getInt("session_length_minutes", -1)
        assertTrue(before != 20)
        startFromSheet()
        awaitClock("20:00")
        val stored = context.getSharedPreferences("player", Context.MODE_PRIVATE).getInt("session_length_minutes", -1)
        assertEquals(20, stored)

        rule.onNode(hasContentDescription("Ana Sayfa") and hasClickAction()).performClick()
        awaitHome()
        // Ana sayfadaki "Başla" kartı seçili süreyi gösterir.
        assertTrue(textExists("20 dk"))
    }
}
