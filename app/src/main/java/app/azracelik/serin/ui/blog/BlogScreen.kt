package app.azracelik.serin.ui.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardBorderWidth
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.scaledByFont
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun BlogScreen(
    posts: List<BlogPost>,
    onPostClick: (BlogPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(42.dp),
            contentPadding = PaddingValues(start = 12.5.dp, end = 12.5.dp, bottom = contentBottomPadding()),
        ) {
            items(posts, key = { it.id }) { post ->
                BlogCard(post, onClick = { onPostClick(post) })
            }
        }
    }
}

@Composable
private fun BlogCard(post: BlogPost, onClick: () -> Unit) {
    val colors = SerinTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 186.dp)
            .serinShadow()
            .clip(CardShape)
            .background(colors.surface)
            .border(CardBorderWidth, colors.accent, CardShape)
            .clickable(onClick = onClick),
    ) {
        RemoteImage(
            url = post.image,
            fallback = bundledImage(post.id),
            alpha = 0.25f,
            modifier = Modifier.matchParentSize(),
        )
        Text(
            text = post.title,
            style = SerinType.BlogTitle.copy(shadow = serinTextShadow()),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 259.dp.scaledByFont()),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun BlogScreenPreview() {
    SerinTheme { BlogScreen(PreviewContent.blogPosts, onPostClick = {}) }
}
