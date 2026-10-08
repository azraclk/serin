package app.azracelik.serin.ui.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(17.dp),
            modifier = Modifier
                .padding(horizontal = 28.dp)
                // İki kart, uzun olanın yüksekliğini alır.
                .height(IntrinsicSize.Min),
        ) {
            featuredPosts.forEach { post ->
                PostCard(post, onClick = { onPostClick(post) }, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

/** Üst kısımdaki slogan ve arkasındaki üst üste binen mor daireler. */
@Composable
private fun Hero() {
    Box(Modifier.fillMaxWidth()) {
        SloganCircle(x = (-54).dp, y = (-137).dp)
        SloganCircle(x = (-22).dp, y = (-178).dp)
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
        SloganCircle(x = (-133).dp, y = (-210).dp)
    }
}

/**
 * 500dp çaplı, gölgeli daire. Görsel gölgeyle birlikte 556dp; daire görselin içinde (28, 12) konumunda.
 * [x], [y] dairenin ekranın sol üstüne göre konumu (durum çubuğunun altından itibaren).
 */
@Composable
private fun BoxScope.SloganCircle(x: Dp, y: Dp) {
    Image(
        painter = painterResource(R.drawable.slogan_circle),
        contentDescription = null,
        modifier = Modifier
            .matchParentSize()
            .wrapContentSize(Alignment.TopStart, unbounded = true)
            .offset(x = x - 28.dp, y = y - 12.dp)
            .requiredSize(556.dp),
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
