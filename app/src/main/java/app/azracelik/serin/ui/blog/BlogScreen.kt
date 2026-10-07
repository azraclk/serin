package app.azracelik.serin.ui.blog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.ui.components.CardBorderWidth
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.bottomBarHeight
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.theme.SerinPurple
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun BlogScreen(onPostClick: (BlogPost) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Color.White)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(42.dp),
            contentPadding = PaddingValues(start = 12.5.dp, end = 12.5.dp, bottom = bottomBarHeight()),
        ) {
            items(BlogPost.entries) { post ->
                BlogCard(post, onClick = { onPostClick(post) })
            }
        }
    }
}

@Composable
private fun BlogCard(post: BlogPost, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp)
            .serinShadow()
            .clip(CardShape)
            .background(Color.White)
            .border(CardBorderWidth, SerinPurple, CardShape)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(post.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.25f,
            modifier = Modifier.matchParentSize(),
        )
        Text(
            text = stringResource(post.title),
            style = SerinType.BlogTitle.copy(shadow = serinTextShadow()),
            modifier = Modifier.width(259.dp),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun BlogScreenPreview() {
    SerinTheme { BlogScreen(onPostClick = {}) }
}
