package app.azracelik.serin.ui.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.markdown.Markdown
import app.azracelik.serin.ui.markdown.MdBlock
import app.azracelik.serin.ui.markdown.parseMarkdown
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

sealed interface PostBodyState {
    data object Loading : PostBodyState
    data class Loaded(val blocks: List<MdBlock>) : PostBodyState
    data object Failed : PostBodyState
    /** content.json'da yazının metni henüz yok. */
    data object ComingSoon : PostBodyState
}

/** Okunabilir satır uzunluğu için yazı sütununun en geniş hâli. */
private val ReadingMaxWidth = 720.dp

@Composable
fun BlogPostScreen(
    post: BlogPost,
    body: PostBodyState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Başlık sabit kalır, yazı altında kayar (blog listesindeki gibi).
    Column(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        PostContent(post, body, onRetry)
    }
}

@Composable
private fun PostContent(post: BlogPost, body: PostBodyState, onRetry: () -> Unit) {
    val colors = SerinTheme.colors
    // Kaydırma tüm genişlikte kalır; okunacak sütun ise okuma ölçüsünde ortalanır.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentBottomPadding() + 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = ReadingMaxWidth).fillMaxWidth(),
        ) {
            // Blog listesindeki kartla aynı ölçüler; burada görsel soluk değil.
            RemoteImage(
                url = post.image,
                fallback = bundledImage(post.id),
                modifier = Modifier
                    .padding(horizontal = 12.5.dp)
                    .fillMaxWidth()
                    .height(if (LocalWindowSize.current.useRail) 260.dp else 186.dp)
                    .serinShadow()
                    .clip(CardShape),
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = post.title,
                style = SerinType.PostTitle,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .size(width = 48.dp, height = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.accent),
            )
            Spacer(Modifier.height(28.dp))
            when (body) {
                PostBodyState.Loading -> CircularProgressIndicator(color = colors.accent)
                is PostBodyState.Loaded -> Markdown(
                    body.blocks,
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                )
                PostBodyState.Failed -> Message(stringResource(R.string.post_failed)) {
                    Text(
                        text = stringResource(R.string.try_again),
                        style = SerinType.PostBody.copy(color = colors.accent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(role = Role.Button, onClick = onRetry)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                PostBodyState.ComingSoon -> Message(stringResource(R.string.post_coming_soon))
            }
        }
    }
}

@Composable
private fun Message(text: String, action: @Composable () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
        Text(
            text,
            style = SerinType.PostBody.copy(textAlign = TextAlign.Center, color = SerinTheme.colors.textSoft),
        )
        Spacer(Modifier.height(12.dp))
        action()
    }
}

@AdaptivePreviews
@Composable
private fun BlogPostScreenPreview() {
    PreviewWindow(SerinTab.Blog) {
        BlogPostScreen(
            post = PreviewContent.blogPosts[0],
            body = PostBodyState.Loaded(
                parseMarkdown(
                    """
                    Meditation has a long history across many cultures, with **early records** in ancient texts.

                    ## Where it started

                    - Contemplative practices in early traditions
                    - Later spread through *trade* and travel

                    ## Sources

                    1. [Example source](https://example.com)
                    """.trimIndent(),
                ),
            ),
            onRetry = {},
        )
    }
}
