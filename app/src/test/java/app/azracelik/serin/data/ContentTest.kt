package app.azracelik.serin.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
