package app.azracelik.serin.ui.home

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.ui.adaptive.WidthClass
import app.azracelik.serin.ui.components.FitText
import app.azracelik.serin.ui.components.ImageCard
import app.azracelik.serin.ui.components.PostRow
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.scaledByFont
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType
import java.time.LocalTime

/** Günün saatine göre karşılama ve önerilen meditasyon. */
private enum class DayPart(@StringRes val greeting: Int, val meditationId: String) {
    Morning(R.string.greeting_morning, "focus"),
    Afternoon(R.string.greeting_afternoon, "breathing"),
    Evening(R.string.greeting_evening, "stress-relief"),
    Night(R.string.greeting_night, "sleep"),
}

private fun dayPartAt(hour: Int) = when (hour) {
    in 5..11 -> DayPart.Morning
    in 12..16 -> DayPart.Afternoon
    in 17..21 -> DayPart.Evening
    else -> DayPart.Night
}

private val ScreenPadding = 28.dp
private val SectionGap = 32.dp

/** Tek sütunlu (dar ve orta) düzende içeriğin en geniş hâli; daha genişte iki yanı boş kalır. */
private val SingleColumnMaxWidth = 640.dp

/** İki bölmeli (geniş) düzende her iki bölmenin iç kenar boşluğu ve toplam içeriğin en geniş hâli. */
private val PanePadding = 32.dp
private val TwoPaneMaxWidth = 1200.dp

@Composable
fun HomeScreen(
    meditations: List<Meditation>,
    blogPosts: List<BlogPost>,
    /** Daha önce dinlenmiş ve şu an seansı sürmeyen meditasyon; yoksa kart gösterilmez. */
    resume: Meditation?,
    sessionMinutes: Int,
    onMeditationClick: (Meditation) -> Unit,
    onStartClick: (Meditation) -> Unit,
    onPostClick: (BlogPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayPart = remember { dayPartAt(LocalTime.now().hour) }
    val pick = meditations.find { it.id == dayPart.meditationId } ?: meditations.firstOrNull()
    // Seçilen meditasyon zaten büyük kartta; aynısı ikinci kez önerilmez.
    val resumeCard = resume?.takeIf { it.id != pick?.id }
    val greeting = stringResource(dayPart.greeting)
    val windowSize = LocalWindowSize.current

    if (windowSize.width == WidthClass.Expanded) {
        TwoPaneHome(
            greeting, pick, resumeCard, meditations, blogPosts, sessionMinutes,
            onMeditationClick, onStartClick, onPostClick, modifier,
        )
        return
    }

    val sidePadding = windowSize.sidePadding(SingleColumnMaxWidth, min = ScreenPadding)
    Column(
        modifier
            .fillMaxSize()
            .background(SerinTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentBottomPadding()),
    ) {
        Hero(greeting, sidePadding)
        Spacer(Modifier.height(24.dp))
        Featured(
            pick, resumeCard, sessionMinutes, onMeditationClick, onStartClick,
            Modifier.padding(horizontal = sidePadding),
        )
        Browse(meditations, blogPosts, sidePadding, windowSize.useRail, onMeditationClick, onPostClick)
    }
}

/**
 * Geniş pencere: solda selam ve önerilen meditasyon, sağda keşfet ve okuma listeleri.
 * Bölmeler ayrı kayar; hilal ikisinin de arkasında, üst çubuğun hizasında durur.
 */
@Composable
private fun TwoPaneHome(
    greeting: String,
    pick: Meditation?,
    resume: Meditation?,
    meditations: List<Meditation>,
    blogPosts: List<BlogPost>,
    sessionMinutes: Int,
    onMeditationClick: (Meditation) -> Unit,
    onStartClick: (Meditation) -> Unit,
    onPostClick: (BlogPost) -> Unit,
    modifier: Modifier = Modifier,
) {
    val outerPadding = LocalWindowSize.current.sidePadding(TwoPaneMaxWidth, min = 0.dp)
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        HeroMoon(
            fromEnd = outerPadding + PanePadding + MoonRadius,
            centerY = statusBar + HeaderCenterY,
            modifier = Modifier.matchParentSize(),
        )
        Column(Modifier.fillMaxSize()) {
            SerinHeader()
            Row(Modifier.weight(1f).fillMaxWidth().padding(horizontal = outerPadding)) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = contentBottomPadding()),
                ) {
                    Spacer(Modifier.height(24.dp))
                    HeroText(greeting, PanePadding)
                    Spacer(Modifier.height(24.dp))
                    Featured(
                        pick, resume, sessionMinutes, onMeditationClick, onStartClick,
                        Modifier.padding(horizontal = PanePadding),
                    )
                }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = contentBottomPadding()),
                ) {
                    Browse(meditations, blogPosts, PanePadding, true, onMeditationClick, onPostClick)
                }
            }
        }
    }
}

/** Önerilen meditasyon ve varsa kaldığın yerden devam kartı. */
@Composable
private fun Featured(
    pick: Meditation?,
    resume: Meditation?,
    sessionMinutes: Int,
    onMeditationClick: (Meditation) -> Unit,
    onStartClick: (Meditation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (pick != null) {
            PickCard(
                meditation = pick,
                sessionMinutes = sessionMinutes,
                onClick = { onMeditationClick(pick) },
                onStart = { onStartClick(pick) },
            )
        }
        if (resume != null) {
            Spacer(Modifier.height(16.dp))
            ResumeCard(meditation = resume, onClick = { onStartClick(resume) })
        }
    }
}

/** Keşfet ve okuma bölümleri; her bölüm kendinden önce [SectionGap] boşluk bırakır. */
@Composable
private fun Browse(
    meditations: List<Meditation>,
    blogPosts: List<BlogPost>,
    sidePadding: Dp,
    wrapChips: Boolean,
    onMeditationClick: (Meditation) -> Unit,
    onPostClick: (BlogPost) -> Unit,
) {
    if (meditations.isNotEmpty()) {
        Spacer(Modifier.height(SectionGap))
        SectionTitle(R.string.home_explore, sidePadding)
        MeditationChips(meditations, sidePadding, wrapChips, onMeditationClick)
    }
    if (blogPosts.isNotEmpty()) {
        Spacer(Modifier.height(SectionGap))
        SectionTitle(R.string.home_read, sidePadding)
        Column(Modifier.padding(horizontal = sidePadding)) {
            blogPosts.take(3).forEach { PostRow(it, onClick = { onPostClick(it) }) }
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int, sidePadding: Dp) {
    Text(
        text = stringResource(title),
        style = SerinType.SectionTitle,
        modifier = Modifier.padding(horizontal = sidePadding).padding(bottom = 14.dp),
    )
}

/**
 * Meditasyon kartları: dar ekranda yana kayan tek satır, geniş ekranda alt satıra sarılan ızgara.
 * İlk ve son öğenin kenar boşluğu sayfayla aynı.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MeditationChips(
    meditations: List<Meditation>,
    sidePadding: Dp,
    wrap: Boolean,
    onClick: (Meditation) -> Unit,
) {
    // Gölge kenarlarda kırpılmasın diye dikey boşluk veriliyor.
    val padding = Modifier.padding(horizontal = sidePadding, vertical = 4.dp)
    if (wrap) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = padding,
        ) {
            meditations.forEachIndexed { index, it -> MeditationChip(it, index, onClick = { onClick(it) }) }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()).then(padding),
        ) {
            meditations.forEachIndexed { index, it -> MeditationChip(it, index, onClick = { onClick(it) }) }
        }
    }
}

@Composable
private fun PickCard(
    meditation: Meditation,
    sessionMinutes: Int,
    onClick: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SerinTheme.colors
    // Geniş pencerede kart da geniştir; yüksekliği de buna uyar.
    val minHeight = if (LocalWindowSize.current.useRail) 240.dp else 200.dp
    ImageCard(meditation.image, meditation.id, onClick, modifier.fillMaxWidth().heightIn(min = minHeight), phase = 0) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.home_pick_label),
                style = SerinType.Caption.copy(color = colors.textSoft),
            )
            Text(meditation.title, style = SerinType.PickTitle)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StartButton(onStart)
                Text(
                    text = stringResource(R.string.session_minutes, sessionMinutes),
                    style = SerinType.Caption.copy(color = colors.textSoft),
                )
            }
        }
    }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
    val colors = SerinTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.onAccent),
            modifier = Modifier.size(20.dp),
        )
        Text(stringResource(R.string.home_start), style = SerinType.SessionChip.copy(color = colors.onAccent))
    }
}

@Composable
private fun ResumeCard(meditation: Meditation, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    ImageCard(meditation.image, meditation.id, onClick, modifier.fillMaxWidth().heightIn(min = 72.dp), shine = false) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_resume), style = SerinType.Caption.copy(color = colors.textSoft))
                Text(meditation.title, style = SerinType.MiniTitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Image(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = stringResource(R.string.play),
                colorFilter = ColorFilter.tint(colors.onAccent),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.accent)
                    .padding(8.dp)
                    .size(24.dp),
            )
        }
    }
}

@Composable
private fun MeditationChip(meditation: Meditation, index: Int, onClick: () -> Unit) {
    ImageCard(meditation.image, meditation.id, onClick, Modifier.size(width = 112.dp, height = 140.dp), phase = index) {
        FitText(
            text = meditation.label,
            style = SerinType.Caption.copy(textAlign = TextAlign.Center, shadow = serinTextShadow()),
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 8.dp, start = 6.dp, end = 6.dp),
        )
    }
}

/** Üst kısım: gün saatine göre selam ve arkasında yavaşça süzülen hilal. */
@Composable
private fun Hero(greeting: String, sidePadding: Dp) {
    Box(Modifier.fillMaxWidth().heightIn(min = 270.dp)) {
        HeroMoon(
            fromEnd = sidePadding + MoonRadius,
            centerY = HeroMoonY,
            modifier = Modifier.matchParentSize(),
        )
        Column {
            SerinHeader()
            Spacer(Modifier.height(72.dp))
            HeroText(greeting, sidePadding)
        }
    }
}

@Composable
private fun HeroText(greeting: String, sidePadding: Dp) {
    val colors = SerinTheme.colors
    Text(
        text = greeting,
        style = SerinType.Display.copy(textAlign = TextAlign.Start),
        modifier = Modifier.padding(horizontal = sidePadding),
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.home_hero_line),
        style = SerinType.Caption.copy(fontSize = 16.sp, color = colors.textMuted),
        modifier = Modifier.padding(horizontal = sidePadding).widthIn(max = 280.dp.scaledByFont()),
    )
}

/**
 * Yavaşça süzülen hilal ve çevresindeki ışık. Merkezi sağ kenardan [fromEnd], üst kenardan [centerY]
 * uzaklıktadır; çizim kendi sınırlarının dışına taşabilir, kırpılması üstteki kaydırma alanına kalır.
 */
@Composable
private fun HeroMoon(fromEnd: Dp, centerY: Dp, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    val drift = HeroMoonDrift
    val progress by rememberInfiniteTransition(label = "moonDrift").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(drift.periodMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "moonDrift",
    )
    Canvas(modifier) {
        val center = Offset(
            size.width - fromEnd.toPx() + drift.dx.toPx() * progress,
            centerY.toPx() + drift.dy.toPx() * progress,
        )
        val glowRadius = 200.dp.toPx()
        drawCircle(
            Brush.radialGradient(listOf(colors.accent.copy(alpha = 0.3f), Color.Transparent), center, glowRadius),
            glowRadius,
            center,
        )
        // Hilal: tam daireden, kaydırılmış ikinci daire çıkarılarak oyulur.
        val full = Path().apply { addOval(Rect(center, MoonRadius.toPx())) }
        val cut = Path().apply { addOval(Rect(center + Offset((-14).dp.toPx(), (-6).dp.toPx()), 31.dp.toPx())) }
        drawPath(Path.combine(PathOperation.Difference, full, cut), colors.accent)
    }
}

private val MoonRadius = 34.dp

/** Hilalin tek başlıklı düzende üst kenardan uzaklığı. */
private val HeroMoonY = 118.dp

/** Üst çubuktaki logonun dikey ortası; durum çubuğunun altından ölçülür (11dp boşluk + 40dp'nin yarısı). */
private val HeaderCenterY = 31.dp

/** Hilalin yavaş süzülmesi; ölçü değişmez, yalnızca çizim kayar. */
private class MoonDrift(val periodMillis: Int, val dx: Dp, val dy: Dp)

private val HeroMoonDrift = MoonDrift(periodMillis = 9000, dx = (-14).dp, dy = 12.dp)

@AdaptivePreviews
@Composable
private fun HomeScreenPreview() {
    PreviewWindow(SerinTab.Home) {
        HomeScreen(
            meditations = PreviewContent.meditations,
            blogPosts = PreviewContent.blogPosts,
            resume = PreviewContent.meditations[2],
            sessionMinutes = 10,
            onMeditationClick = {},
            onStartClick = {},
            onPostClick = {},
        )
    }
}
