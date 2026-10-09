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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R
import app.azracelik.serin.data.BlogPost
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.ui.components.FitText
import app.azracelik.serin.ui.components.ImageCard
import app.azracelik.serin.ui.components.PostRow
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.scaledByFont
import app.azracelik.serin.ui.components.serinTextShadow
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
    Column(
        modifier
            .fillMaxSize()
            .background(SerinTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentBottomPadding()),
    ) {
        Hero(greeting = stringResource(dayPart.greeting))
        Spacer(Modifier.height(24.dp))
        if (pick != null) {
            PickCard(
                meditation = pick,
                sessionMinutes = sessionMinutes,
                onClick = { onMeditationClick(pick) },
                onStart = { onStartClick(pick) },
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
        }
        if (resume != null && resume.id != pick?.id) {
            Spacer(Modifier.height(16.dp))
            ResumeCard(
                meditation = resume,
                onClick = { onStartClick(resume) },
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
        }
        if (meditations.isNotEmpty()) {
            Spacer(Modifier.height(SectionGap))
            SectionTitle(R.string.home_explore)
            HorizontalList {
                meditations.forEachIndexed { index, it -> MeditationChip(it, index, onClick = { onMeditationClick(it) }) }
            }
        }
        if (blogPosts.isNotEmpty()) {
            Spacer(Modifier.height(SectionGap))
            SectionTitle(R.string.home_read)
            Column(Modifier.padding(horizontal = ScreenPadding)) {
                blogPosts.take(3).forEach { PostRow(it, onClick = { onPostClick(it) }) }
            }
        }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = SerinType.SectionTitle,
        modifier = Modifier.padding(horizontal = ScreenPadding).padding(bottom = 14.dp),
    )
}

/** Yana kaydırılan satır; ilk ve son öğenin kenar boşluğu sayfayla aynı. */
@Composable
private fun HorizontalList(content: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            // Gölge kenarlarda kırpılmasın diye dikey boşluk veriliyor.
            .padding(horizontal = ScreenPadding, vertical = 4.dp),
        content = content,
    )
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
    ImageCard(meditation.image, meditation.id, onClick, modifier.fillMaxWidth().heightIn(min = 200.dp), phase = 0) {
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
private fun Hero(greeting: String) {
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
    Box(Modifier.fillMaxWidth().heightIn(min = 270.dp)) {
        Canvas(Modifier.matchParentSize()) {
            val center = Offset(
                size.width - 62.dp.toPx() + drift.dx.toPx() * progress,
                118.dp.toPx() + drift.dy.toPx() * progress,
            )
            val glowRadius = 200.dp.toPx()
            drawCircle(
                Brush.radialGradient(listOf(colors.accent.copy(alpha = 0.3f), Color.Transparent), center, glowRadius),
                glowRadius,
                center,
            )
            // Hilal: tam daireden, kaydırılmış ikinci daire çıkarılarak oyulur.
            val full = Path().apply { addOval(Rect(center, 34.dp.toPx())) }
            val cut = Path().apply { addOval(Rect(center + Offset((-14).dp.toPx(), (-6).dp.toPx()), 31.dp.toPx())) }
            drawPath(Path.combine(PathOperation.Difference, full, cut), colors.accent)
        }
        Column {
            SerinHeader()
            Spacer(Modifier.height(72.dp))
            Text(
                text = greeting,
                style = SerinType.Display.copy(textAlign = TextAlign.Start),
                modifier = Modifier.padding(horizontal = ScreenPadding),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_hero_line),
                style = SerinType.Caption.copy(fontSize = 16.sp, color = colors.textMuted),
                modifier = Modifier.padding(horizontal = ScreenPadding).widthIn(max = 280.dp.scaledByFont()),
            )
        }
    }
}

/** Hilalin yavaş süzülmesi; ölçü değişmez, yalnızca çizim kayar. */
private class MoonDrift(val periodMillis: Int, val dx: Dp, val dy: Dp)

private val HeroMoonDrift = MoonDrift(periodMillis = 9000, dx = (-14).dp, dy = 12.dp)

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenPreview() {
    SerinTheme {
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
