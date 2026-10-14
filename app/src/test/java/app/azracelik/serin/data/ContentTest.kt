package app.azracelik.serin.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentTest {
    private val bundled = parseContent(File("src/main/assets/content.json").readText())

    @Test
    fun bundledContentHasEveryItem() {
        assertEquals(6, bundled.meditations.size)
        assertEquals(3, bundled.blogPosts.size)
        assertEquals(listOf("history-of-meditation", "power-of-frequencies"), bundled.featuredPosts.map { it.id })
    }

    @Test
    fun imagePathsResolveToContentRepo() {
        assertEquals(ContentBaseUrl + "images/home/banner.jpg", bundled.home.banner)
        bundled.meditations.forEach {
            assertTrue(it.image.startsWith(ContentBaseUrl))
            assertTrue(it.audio.orEmpty().startsWith(ContentBaseUrl))
        }
    }

    @Test
    fun everyBundledItemHasFallbackImage() {
        (bundled.meditations.map { it.id } + bundled.blogPosts.map { it.id } + HomeBannerId).forEach {
            assertNotNull("$it için uygulamayla gelen görsel yok", bundledImage(it))
        }
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val content = parseContent(
            """{"home":{"banner":"b.jpg","featuredPosts":[],"new":1},"meditations":[],"blogPosts":[]}""",
        )
        assertEquals(ContentBaseUrl + "b.jpg", content.home.banner)
    }

    @Test
    fun favoritesKeepContentOrderAndSkipUnknownIds() {
        val favorites = bundled.meditations(setOf("sleep", "mindfulness", "removed"))
        assertEquals(listOf("mindfulness", "sleep"), favorites.map { it.id })
    }

    @Test
    fun everyBundledMeditationHasADescription() {
        bundled.meditations.forEach {
            assertTrue("${it.id} için açıklama yok", !it.description.isNullOrBlank())
        }
    }

    @Test
    fun onlyTheBreathingMeditationIsMarkedAsBreathing() {
        assertEquals(listOf("breathing"), bundled.meditations.filter { it.breathing }.map { it.id })
    }

    @Test
    fun meditationWithoutNewFieldsStillParses() {
        val content = parseContent(
            """{"home":{"banner":"b.jpg","featuredPosts":[]},"meditations":[{"id":"sleep","label":"uyku","title":"T","image":"i.jpg"}],"blogPosts":[]}""",
        )
        val meditation = content.meditations.single()
        assertNull(meditation.description)
        assertFalse(meditation.breathing)
        assertFalse(meditation.hasBreathingPattern)
    }

    @Test
    fun breathingMeditationKeepsItsPickerWithOlderContent() {
        // Eski içerikte `breathing` alanı yok; id'ye göre de tanınır.
        assertTrue(Meditation("breathing", "nefes", "T", "i.jpg").hasBreathingPattern)
        assertTrue(Meditation("custom", "x", "T", "i.jpg", breathing = true).hasBreathingPattern)
    }
}
