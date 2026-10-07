package app.azracelik.serin.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * [url] adresindeki görseli indirip gösterir (Coil indirdiği görseli önbelleğe alır).
 * İndirilemezse [fallback] gösterilir; önizlemede de doğrudan [fallback] kullanılır.
 */
@Composable
fun RemoteImage(
    url: String,
    @DrawableRes fallback: Int?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = 1f,
) {
    val fallbackPainter = fallback?.let { painterResource(it) }
    if (LocalInspectionMode.current && fallbackPainter != null) {
        Image(fallbackPainter, null, modifier, contentScale = contentScale, alpha = alpha)
        return
    }
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        error = fallbackPainter,
        contentScale = contentScale,
        alpha = alpha,
        modifier = modifier,
    )
}
