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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.ui.adaptive.WidthClass
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardBorderWidth
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.PostRow
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.scaledByFont
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

private val OneColumnMaxWidth = 680.dp
private val TwoColumnMaxWidth = 1120.dp

@Composable
fun BlogScreen(
    posts: List<BlogPost>,
    onPostClick: (BlogPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSize = LocalWindowSize.current
    // Geniş pencerede satırlar iki sütuna dizilir; çok geniş pencerede içerik ortada toplanır.
    val twoColumns = windowSize.width == WidthClass.Expanded
    val sidePadding = windowSize.sidePadding(if (twoColumns) TwoColumnMaxWidth else OneColumnMaxWidth, min = 28.dp)
    Column(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (twoColumns) 2 else 1),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(start = sidePadding, end = sidePadding, bottom = contentBottomPadding()),
        ) {
            items(posts, key = { it.id }) { post ->
                PostRow(post, onClick = { onPostClick(post) })
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun BlogScreenPreview() {
    PreviewWindow(SerinTab.Blog) { BlogScreen(PreviewContent.blogPosts, onPostClick = {}) }
}
