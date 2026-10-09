package app.azracelik.serin.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.HomeBannerId
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardBorderWidth
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.scaledByFont
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.theme.Merriweather
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun HomeScreen(
    bannerUrl: String,
    featuredPosts: List<BlogPost>,
    onBannerClick: () -> Unit,
    onPostClick: (BlogPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(SerinTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentBottomPadding()),
    ) {
        Hero()
        Spacer(Modifier.height(104.dp))
        RemoteImage(
            url = bannerUrl,
            fallback = bundledImage(HomeBannerId),
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .height(141.dp)
                .serinShadow()
                .clip(CardShape)
                .clickable(onClick = onBannerClick),
        )
        Spacer(Modifier.height(49.dp))
        // Büyük yazıda iki dar kart kelimeleri tireyle bölüyor; kartlar alt alta dizilir.
        val stacked = LocalDensity.current.fontScale > StackCardsFontScale
        val cardsModifier = Modifier.padding(horizontal = 28.dp)
        val cards: @Composable (Modifier) -> Unit = { cardModifier ->
            featuredPosts.forEach { post ->
                PostCard(post, onClick = { onPostClick(post) }, modifier = cardModifier)
            }
        }
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(17.dp), modifier = cardsModifier) {
                cards(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(17.dp),
                // İki kart, uzun olanın yüksekliğini alır.
                modifier = cardsModifier.height(IntrinsicSize.Min),
            ) {
                cards(Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

private const val StackCardsFontScale = 1.3f

/** Üst kısımdaki slogan ve arkasındaki üst üste binen mor daireler. */
@Composable
private fun Hero() {
    Box(Modifier.fillMaxWidth()) {
        SloganCircle(x = (-54).dp, y = (-137).dp, drift = CircleDrifts[0])
        SloganCircle(x = (-22).dp, y = (-178).dp, drift = CircleDrifts[1])
        Column {
            SerinHeader()
            Spacer(Modifier.height(32.dp))
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.home_hero_prefix))
                    withStyle(
                        SpanStyle(
                            fontFamily = Merriweather,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = SerinTheme.colors.accent,
                        ),
                    ) { append(stringResource(R.string.home_hero_accent)) }
                    append(stringResource(R.string.home_hero_suffix))
                },
                style = SerinType.Display,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(x = 3.dp)
                    .widthIn(max = 251.dp.scaledByFont())
                    .heightIn(min = 220.dp),
            )
        }
        // Üçüncü daire Figma'da metnin üzerinde duruyor.
        SloganCircle(x = (-133).dp, y = (-210).dp, drift = CircleDrifts[2])
    }
}

/** Dairenin yavaş süzülmesi: her daire farklı sürede ve yönde gidip gelir, hiçbiri senkron olmaz. */
private class CircleDrift(val periodMillis: Int, val dx: Dp, val dy: Dp)

private val CircleDrifts = listOf(
    CircleDrift(periodMillis = 7000, dx = 14.dp, dy = (-10).dp),
    CircleDrift(periodMillis = 9000, dx = (-12).dp, dy = 12.dp),
    CircleDrift(periodMillis = 11000, dx = 10.dp, dy = 14.dp),
)

/**
 * 500dp çaplı, gölgeli daire. Görsel gölgeyle birlikte 556dp; daire görselin içinde (28, 12) konumunda.
 * [x], [y] dairenin ekranın sol üstüne göre konumu (durum çubuğunun altından itibaren).
 */
@Composable
private fun BoxScope.SloganCircle(x: Dp, y: Dp, drift: CircleDrift) {
    val progress by rememberInfiniteTransition(label = "circleDrift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(drift.periodMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "circleDrift",
    )
    Image(
        painter = painterResource(R.drawable.slogan_circle),
        contentDescription = null,
        modifier = Modifier
            .matchParentSize()
            .wrapContentSize(Alignment.TopStart, unbounded = true)
            .offset(x = x - 28.dp, y = y - 12.dp)
            .requiredSize(556.dp)
            // Ölçü/konum değişmez, yalnızca çizim katmanı kayar; her karede yeniden düzen yapılmaz.
            .graphicsLayer {
                translationX = drift.dx.toPx() * progress
                translationY = drift.dy.toPx() * progress
            },
    )
}

@Composable
private fun PostCard(post: BlogPost, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 133.dp)
            .serinShadow()
            .clip(CardShape)
            .background(colors.surface)
            .background(colors.accentSoft)
            .border(CardBorderWidth, colors.accent, CardShape)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = post.title,
            style = SerinType.HomeCard,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .widthIn(max = 104.dp.scaledByFont()),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenPreview() {
    SerinTheme {
        HomeScreen(
            bannerUrl = PreviewContent.home.banner,
            featuredPosts = PreviewContent.featuredPosts,
            onBannerClick = {},
            onPostClick = {},
        )
    }
}
