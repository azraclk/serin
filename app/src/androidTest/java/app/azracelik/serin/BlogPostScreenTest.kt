package app.azracelik.serin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.ui.blog.BlogPostScreen
import app.azracelik.serin.ui.blog.PostBodyState
import app.azracelik.serin.ui.markdown.parseMarkdown
import app.azracelik.serin.ui.theme.SerinTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Blog yazısı ekranının dört hâli: yükleniyor, yüklendi, indirilemedi ve henüz yazılmamış. */
@RunWith(AndroidJUnit4::class)
class BlogPostScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val post = BlogPost(id = "deneme", title = "Deneme Yazısı", image = "images/blog/none.jpg")

    private fun show(body: PostBodyState, onRetry: () -> Unit = {}) {
        rule.setContent { SerinTheme(darkTheme = false) { BlogPostScreen(post, body, onRetry) } }
    }

    private fun count(text: String) = rule.onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().size

    @Test
    fun loaded_showsTitleAndBody() {
        show(PostBodyState.Loaded(parseMarkdown("Sakin bir nefes al.")))
        rule.onNodeWithText("Deneme Yazısı").assertIsDisplayed()
        rule.onNodeWithText("Sakin bir nefes al.", substring = true).assertIsDisplayed()
    }

    @Test
    fun failed_showsMessageAndRetryCallsBack() {
        var retries = 0
        show(PostBodyState.Failed, onRetry = { retries++ })
        rule.onNodeWithText("Deneme Yazısı").assertIsDisplayed()
        rule.onNodeWithText("Yazı yüklenemedi", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Tekrar dene").performClick()
        assertEquals(1, retries)
        rule.onNodeWithText("Tekrar dene").performClick()
        assertEquals(2, retries)
    }

    @Test
    fun comingSoon_showsMessageWithoutRetry() {
        show(PostBodyState.ComingSoon)
        rule.onNodeWithText("Bu yazı yakında burada olacak.").assertIsDisplayed()
        assertEquals(0, count("Tekrar dene"))
    }

    @Test
    fun loading_showsTitleOnly() {
        show(PostBodyState.Loading)
        rule.onNodeWithText("Deneme Yazısı").assertIsDisplayed()
        assertEquals(0, count("Tekrar dene"))
        assertEquals(0, count("Yazı yüklenemedi"))
    }
}
