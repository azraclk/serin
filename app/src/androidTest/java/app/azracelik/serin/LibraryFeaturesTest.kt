package app.azracelik.serin

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.PlayerViewModel
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Favoriler, uzun seans süreleri, nefes ritmi, hatırlatıcı penceresi ve çevrimdışı indirme:
 * uygulama gerçekten sürülerek bu özelliklerin ekranda göründüğü ve çalıştığı doğrulanır.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class LibraryFeaturesTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    companion object {
        private var animatorScale = "1"

        private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

        private fun shell(command: String): String {
            val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
            return ParcelFileDescriptor.AutoCloseInputStream(pfd).bufferedReader().use { it.readText().trim() }
        }

        /** Önceki çalıştırmalardan kalan favori, ritim ve indirmeler testi etkilemesin. */
        private fun clearUserData() {
            context.deleteSharedPreferences("serin_library")
            context.deleteSharedPreferences("serin_reminder")
            File(context.filesDir, "audio").deleteRecursively()
        }

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            // Sonsuz ışık ve parıltı animasyonları Compose testinin "boşta" beklemesini engeller.
            animatorScale = shell("settings get global animator_duration_scale").ifEmpty { "1" }.takeIf { it != "null" } ?: "1"
            shell("settings put global animator_duration_scale 0")
            clearUserData()
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            shell("settings put global animator_duration_scale $animatorScale")
            clearUserData()
        }
    }

    @After
    fun resetSessionLength() {
        val player = ViewModelProvider(rule.activity)[PlayerViewModel::class.java]
        rule.runOnUiThread { player.setSessionLength(MeditationSession.DefaultLengthMinutes) }
    }

    private fun tab(label: String) = rule.onNode(hasContentDescription(label) and hasClickAction())

    private fun exists(text: String) = rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun existsDescription(description: String) =
        rule.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()

    private fun waitFor(what: String, timeoutMillis: Long = 10_000, condition: () -> Boolean) {
        try {
            rule.waitUntil(timeoutMillis = timeoutMillis) { condition() }
        } catch (e: Throwable) {
            throw AssertionError("Beklenen durum gelmedi: $what", e)
        }
    }

    private fun awaitHome() = waitFor("Ana Sayfa") { exists("Keşfet") }

    private fun openMeditationList() {
        awaitHome()
        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { exists("farkındalık") && exists("çakra") }
    }

    /** Listeden etiketi verilen meditasyonun detayını açar; detayda "20 dk" seçeneği bulunur. */
    private fun openDetail(label: String) {
        openMeditationList()
        rule.onAllNodesWithText(label).onFirst().performClick()
        waitFor("$label detayı") { exists("20 dk") }
    }

    @Test
    fun favoritingFromTheList_addsAndRemovesTheHomeSection() {
        awaitHome()
        assertFalse("Başlangıçta favori olmamalı", exists("Favorilerin"))

        openMeditationList()
        rule.onAllNodesWithContentDescription("Favori").onFirst().performClick()
        tab("Ana Sayfa").performClick()
        waitFor("Favorilerin bölümü") { exists("Favorilerin") }

        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { exists("çakra") }
        rule.onAllNodesWithContentDescription("Favori").onFirst().performClick()
        tab("Ana Sayfa").performClick()
        waitFor("Favorilerin bölümünün kalkması") { !exists("Favorilerin") }
    }

    @Test
    fun sessionLengths_includeThirtyAndSixtyMinutes() {
        openDetail("farkındalık")
        assertTrue(exists("30 dk"))
        assertTrue(exists("60 dk"))

        rule.onAllNodesWithText("60 dk").onFirst().performClick()
        waitFor("60 dakikalık saat") { exists("60:00") }
    }

    @Test
    fun breathingRhythmPicker_showsOnlyOnTheBreathingMeditation() {
        openDetail("farkındalık")
        assertFalse("Nefes dışı meditasyonda ritim seçici olmamalı", exists("4-7-8"))

        tab("Meditasyon").performClick()
        waitFor("meditasyon listesi") { exists("çakra") }
        rule.onAllNodesWithText("nefes").onFirst().performClick()
        waitFor("ritim seçici") { exists("4-7-8") }

        assertTrue("Varsayılan ritim Sakin", rule.onAllNodes(hasText("Sakin") and isSelected()).fetchSemanticsNodes().isNotEmpty())
        rule.onAllNodesWithText("4-7-8").onFirst().performClick()
        waitFor("4-7-8 seçimi") { rule.onAllNodes(hasText("4-7-8") and isSelected()).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun reminderDialog_opensFromTheBellAndCancels() {
        awaitHome()
        rule.onNode(hasContentDescription("Günlük hatırlatıcı") and hasClickAction()).performClick()
        waitFor("hatırlatıcı penceresi") { exists("Her gün hatırlat") }

        rule.onAllNodesWithText("Vazgeç").onFirst().performClick()
        waitFor("pencerenin kapanması") { !exists("Her gün hatırlat") }
    }

    @Test
    fun audioCanBeDownloadedAndRemoved() {
        openDetail("odak")
        waitFor("indirme düğmesi") { existsDescription("Çevrimdışı dinlemek için indir") }

        rule.onAllNodesWithContentDescription("Çevrimdışı dinlemek için indir").onFirst().performClick()
        waitFor("indirmenin bitmesi", timeoutMillis = 40_000) { existsDescription("İndirildi, kaldırmak için dokun") }
        assertTrue(File(context().filesDir, "audio").list().orEmpty().any { it.startsWith("focus@") })

        rule.onAllNodesWithContentDescription("İndirildi, kaldırmak için dokun").onFirst().performClick()
        waitFor("indirmenin kaldırılması") { existsDescription("Çevrimdışı dinlemek için indir") }
        assertTrue(File(context().filesDir, "audio").list().orEmpty().none { it.startsWith("focus@") })
    }

    private fun context() = InstrumentationRegistry.getInstrumentation().targetContext
}
