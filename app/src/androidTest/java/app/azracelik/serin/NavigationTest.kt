package app.azracelik.serin

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.ViewModelProvider
import app.azracelik.serin.data.ContentBaseUrl
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.PlayerViewModel
import app.azracelik.serin.ui.meditation.MeditationDetailTag
import app.azracelik.serin.ui.meditation.SessionStartTag
import android.view.KeyEvent
import android.os.ParcelFileDescriptor
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Gerçek uygulamayı (splash dahil) sürerek ekranlar arası gezinmeyi doğrular. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    companion object {
        private var animatorScale = "1"

        private fun shell(command: String): String {
            val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            return ParcelFileDescriptor.AutoCloseInputStream(pfd).bufferedReader().use { it.readText().trim() }
        }

        /**
         * Çalan seansın detay ekranındaki sonsuz nefes animasyonu Compose testinin "boşta" beklemesini
         * engeller; uygulama animasyonlar kapalıyken bu animasyonu çizmez. Activity açılmadan önce kapatılır.
         */
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

    private fun tab(label: String) = rule.onNode(hasContentDescription(label) and hasClickAction())

    private fun exists(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private val onHome get() = exists("Keşfet")
    private fun existsTag(tag: String) = rule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private val onDetail get() = existsTag(MeditationDetailTag)
    private val onSheet get() = existsTag(SessionStartTag)
    private val onMeditationList get() = !onHome && !onDetail && exists("farkındalık") && exists("çakra")
    private val onBlogList get() = !onHome && !onDetail && exists("21 Günde Farkındalık Alışkanlığı Planı")
    private val onPost get() = !onHome && !onDetail && !onBlogList && exists("Meditasyonun Tarihi: Geçmişten Bugüne")

    private fun waitFor(what: String, condition: () -> Boolean) {
        try {
            rule.waitUntil(timeoutMillis = 10_000) { condition() }
        } catch (e: Throwable) {
            throw AssertionError("Beklenen ekran gelmedi: $what", e)
        }
    }

    private fun awaitHome() = waitFor("Ana Sayfa") { onHome }

    private fun pressBack() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            rule.activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
    }

    /** Yazı satırı alt menünün altında kalmasın diye önce sayfa kaydırılır. */
    private fun openHomePost() {
        rule.onRoot().performTouchInput { swipeUp() }
        rule.waitForIdle()
        rule.onAllNodesWithText("Meditasyonun Tarihi: Geçmişten Bugüne").onFirst().performClick()
    }

    /** Süre seçim sayfasında Başla'ya basar; dinleme sayfası açılır. */
    private fun startFromSheet() {
        waitFor("süre seçim sayfası") { onSheet }
        rule.onNode(hasTestTag(SessionStartTag)).performClick()
        waitFor("meditasyon detayı") { onDetail }
    }

    private fun openDetailFromHome() {
        awaitHome()
        rule.onAllNodesWithText("farkındalık").onFirst().performClick()
        startFromSheet()
    }

    private fun openDetailFromList() {
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
        rule.onAllNodesWithText("farkındalık").onFirst().performClick()
        startFromSheet()
    }

    @Test
    fun detailFromExplore_homeTabReturnsHome() {
        openDetailFromHome()
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
    }

    @Test
    fun detailFromExplore_meditationTabShowsList() {
        openDetailFromHome()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
    }

    @Test
    fun detailFromExplore_blogTabShowsBlog() {
        openDetailFromHome()
        tab("Blog").performClick()
        waitFor("blog listesi") { onBlogList }
    }

    @Test
    fun detailFromExplore_backReturnsHome() {
        openDetailFromHome()
        pressBack()
        waitFor("Ana Sayfa") { onHome }
    }

    @Test
    fun detailFromList_homeTabReturnsHome() {
        awaitHome()
        openDetailFromList()
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
    }

    @Test
    fun detailFromList_meditationTabReturnsList() {
        awaitHome()
        openDetailFromList()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
    }

    @Test
    fun detailFromList_backReturnsList() {
        awaitHome()
        openDetailFromList()
        pressBack()
        waitFor("meditasyon listesi") { onMeditationList }
    }

    @Test
    fun homeAfterEveryTab_roundTrips() {
        awaitHome()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
        tab("Blog").performClick()
        waitFor("blog listesi") { onBlogList }
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
    }

    @Test
    fun blogPost_tabsAndBack() {
        awaitHome()
        tab("Blog").performClick()
        waitFor("blog listesi") { onBlogList }
        rule.onAllNodesWithText("Meditasyonun Tarihi: Geçmişten Bugüne").onFirst().performClick()
        waitFor("yazı") { onPost }
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
        tab("Blog").performClick()
        waitFor("blog listesi") { onBlogList }
    }

    @Test
    fun blogPostFromHome_homeTabAndBack() {
        awaitHome()
        openHomePost()
        waitFor("yazı") { onPost }
        pressBack()
        waitFor("Ana Sayfa") { onHome }
        openHomePost()
        waitFor("yazı") { onPost }
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
    }

    private val player get() = ViewModelProvider(rule.activity)[PlayerViewModel::class.java]

    /**
     * Seansı arayüzden değil doğrudan başlatır ve hemen duraklatır: çalarken detay ekranındaki sonsuz nefes
     * animasyonu Compose testinin "boşta" beklemesini engeller. Ağ yoksa da seans (mediaId) açılır.
     */
    private fun startSession() {
        val base = ContentBaseUrl
        val meditation = Meditation(
            id = "mindfulness",
            label = "farkındalık",
            title = "İçsel Dinginlik",
            image = base + "images/meditations/mindfulness.jpg",
            audio = base + "audio/meditations/mindfulness.wav",
        )
        waitFor("seans başladı") {
            if (player.state.value.mediaId == null) rule.runOnUiThread { player.togglePlayback(meditation) }
            player.state.value.mediaId != null
        }
        if (player.state.value.isPlaying) rule.runOnUiThread { player.toggleCurrent() }
        waitFor("seans duraklatıldı") { !player.state.value.isPlaying }
    }

    /** Servis testler arasında yaşadığı için açık kalan seans sonraki testin ana sayfasını bozmasın. */
    @After
    fun endSession() {
        rule.runOnUiThread { player.stop() }
        rule.waitUntil(timeoutMillis = 5_000) { player.state.value.mediaId == null }
    }

    private fun miniPlayerVisible() = rule.onAllNodes(hasContentDescription("Kapat")).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun session_miniPlayerShowsOnEveryTab_andOpensDetail() {
        awaitHome()
        startSession()
        waitFor("mini player (ana sayfa)") { miniPlayerVisible() }

        rule.onAllNodesWithText("İçsel Dinginlik").onFirst().performClick()
        waitFor("meditasyon detayı") { onDetail }
        // Kendi detay ekranında mini player yok.
        assertTrue(!miniPlayerVisible())

        tab("Blog").performClick()
        waitFor("blog listesi") { onBlogList }
        waitFor("mini player (blog)") { miniPlayerVisible() }

        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
        waitFor("mini player (meditasyon)") { miniPlayerVisible() }
    }

    @Test
    fun session_finishedSessionShowsTheClosingNote() {
        awaitHome()
        startSession()
        waitFor("mini player") { miniPlayerVisible() }
        rule.onAllNodesWithText("İçsel Dinginlik").onFirst().performClick()
        waitFor("meditasyon detayı") { onDetail }
        assertTrue(!exists("Tamamlandı"))

        rule.runOnUiThread { MeditationSession.setCompleted(true) }
        waitFor("bitiş notu") { exists("Tamamlandı") }

        // Yeniden çalınınca not kalkar; saat geri gelir.
        rule.runOnUiThread { MeditationSession.setCompleted(false) }
        waitFor("bitiş notu kalktı") { !exists("Tamamlandı") }
    }

    @Test
    fun session_miniPlayerCloseEndsSession() {
        awaitHome()
        startSession()
        waitFor("mini player") { miniPlayerVisible() }
        rule.onNode(hasContentDescription("Kapat") and hasClickAction()).performClick()
        waitFor("mini player kapandı") { !miniPlayerVisible() }
        assertTrue(onHome)
    }

    @Test
    fun session_otherMeditation_sheetKeepsMiniPlayer_andStartingReplacesTheSession() {
        awaitHome()
        startSession()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
        rule.onAllNodesWithText("nefes").onFirst().performClick()
        waitFor("süre seçim sayfası") { onSheet }
        // Başka bir meditasyonun seçimi açıkken eski seans (ve mini player) yerinde durur.
        assertTrue(miniPlayerVisible())
        assertTrue(player.state.value.mediaId == "mindfulness")

        startFromSheet()
        // Başla seansı yenisiyle değiştirir; yeni seans kendi dinleme sayfasındadır, mini player görünmez.
        waitFor("yeni seans") { player.state.value.mediaId == "breathing" }
        assertTrue(!miniPlayerVisible())
        tab("Ana Sayfa").performClick()
        waitFor("Ana Sayfa") { onHome }
        waitFor("mini player") { miniPlayerVisible() }
    }

    @Test
    fun sheet_dismissingItStartsNothing() {
        awaitHome()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { onMeditationList }
        rule.onAllNodesWithText("farkındalık").onFirst().performClick()
        waitFor("süre seçim sayfası") { onSheet }

        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        waitFor("sayfanın kapanması") { !onSheet }
        assertTrue(onMeditationList)
        assertTrue(!miniPlayerVisible())
        assertTrue(player.state.value.mediaId == null)
    }
}
