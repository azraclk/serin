package app.azracelik.serin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

private val ThumbShape = RoundedCornerShape(16.dp)

/** Kartsız blog satırı: küçük görsel, başlık ve altında ince ayraç. Ana sayfa ve blog listesi kullanır. */
@Composable
fun PostRow(post: BlogPost, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 14.dp),
        ) {
            RemoteImage(
                url = post.image,
                fallback = bundledImage(post.id),
                modifier = Modifier.size(72.dp).clip(ThumbShape).background(colors.surface),
            )
            Text(
                text = post.title,
                style = SerinType.PostRowTitle,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Column(Modifier.fillMaxWidth().height(1.dp).background(colors.outline.copy(alpha = 0.5f))) {}
    }
}
