package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.SessionState
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.formatDuration
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

/** Küre (en çok 260dp), aralık (56dp) ve kontroller (en çok 360dp) yan yana sığsın diye gereken genişlik. */
private val SideBySideMinWidth = 560.dp

@Composable
fun MeditationDetailScreen(
    meditation: Meditation,
    isPlaying: Boolean,
    session: SessionState,
    onLengthSelect: (Int) -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSize = LocalWindowSize.current
    val colors = SerinTheme.colors
    val pageModifier = modifier
        .fillMaxSize()
        .background(colors.background)

    // Küre ve kontroller yan yana ancak yeterli genişlik varsa sığar; yoksa alt alta dizilir ve kayar.
    if (windowSize.isLandscapeShort && windowSize.contentWidth >= SideBySideMinWidth) {
        // Yatay telefon: dikey alan kıt, yatay alan bol; küre solda, kontroller sağda durur.
        val orbSize = (windowSize.windowHeight - 140.dp).coerceIn(150.dp, 260.dp)
        Column(pageModifier) {
            SerinHeader()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(56.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = contentBottomPadding()),
            ) {
                BreathingGlow(isPlaying = isPlaying, size = orbSize) {
                    MoonOrb(meditation = meditation, progress = session.progress, orbSize = orbSize)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 360.dp),
                ) {
                    SessionControls(meditation, isPlaying, session, onLengthSelect, onPlayClick, gap = 0.6f)
                }
            }
        }
        return
    }

    // Küre, başlığın ve kontrollerin ölçülmesinden sonra kalan boşluğa göre boyutlanır; böylece oynat
    // düğmesi, yazı boyutu ve ekran ne olursa olsun ilk ekranda alt menünün üstünde kalır. Boşluk
    // en küçük küreye bile yetmiyorsa sayfa kayar.
    val gap = when {
        windowSize.windowHeight < 600.dp -> 0.3f
        windowSize.windowHeight < 720.dp -> 0.6f
        else -> 1f
    }
    val minOrb = when {
        windowSize.windowHeight < 600.dp -> 100.dp
        gap < 1f -> 140.dp
        else -> 180.dp
    }
    val orbRange = minOrb..(if (windowSize.useRail) 380.dp else 300.dp)
    val bottomPadding = contentBottomPadding()
    BoxWithConstraints(pageModifier) {
        val viewportHeight = maxHeight - bottomPadding
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding),
        ) {
            OrbLayout(
                viewportHeight = viewportHeight,
                orbRange = orbRange,
                orbMaxWidth = windowSize.contentWidth - 40.dp,
                header = {
                    Column {
                        SerinHeader()
                        Spacer(Modifier.height((40 * gap).dp))
                    }
                },
                orb = { orbSize ->
                    BreathingGlow(isPlaying = isPlaying, size = orbSize) {
                        MoonOrb(meditation = meditation, progress = session.progress, orbSize = orbSize)
                    }
                },
                controls = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height((36 * gap).dp))
                        SessionControls(meditation, isPlaying, session, onLengthSelect, onPlayClick, gap)
                    }
                },
            )
        }
    }
}

/**
 * Üstte [header], ortada kürenin kendisi, altta [controls] diziler. Önce [header] ve [controls] ölçülür;
 * küre, [viewportHeight] içinde kalan boşluğa sığacak en büyük boyutta ([orbRange] ile sınırlı) çizilir.
 */
@Composable
private fun OrbLayout(
    viewportHeight: Dp,
    orbRange: ClosedRange<Dp>,
    orbMaxWidth: Dp,
    header: @Composable () -> Unit,
    orb: @Composable (Dp) -> Unit,
    controls: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    SubcomposeLayout(modifier.fillMaxWidth()) { constraints ->
        val loose = constraints.copy(minHeight = 0)
        val headerPlaceable = subcompose(OrbSlot.Header, header).first().measure(loose)
        val controlsPlaceable = subcompose(OrbSlot.Controls, controls).first().measure(loose)
        // Düğme alt menüye yapışmasın diye küçük bir nefes payı bırakılır.
        val free = viewportHeight.roundToPx() - headerPlaceable.height - controlsPlaceable.height -
            OrbBreathingRoom.roundToPx()
        val orbSize = free.toDp()
            .coerceIn(orbRange.start, orbRange.endInclusive)
            .coerceAtMost(orbMaxWidth)
        val orbPlaceable = subcompose(OrbSlot.Orb) { orb(orbSize) }.first().measure(loose.copy(minWidth = 0))
        val height = headerPlaceable.height + orbPlaceable.height + controlsPlaceable.height
        layout(constraints.maxWidth, height) {
            headerPlaceable.place((constraints.maxWidth - headerPlaceable.width) / 2, 0)
            orbPlaceable.place((constraints.maxWidth - orbPlaceable.width) / 2, headerPlaceable.height)
            controlsPlaceable.place(
                (constraints.maxWidth - controlsPlaceable.width) / 2,
                headerPlaceable.height + orbPlaceable.height,
            )
        }
    }
}

private enum class OrbSlot { Header, Orb, Controls }

private val OrbBreathingRoom = 12.dp

/** Başlık, kalan süre, süre seçimi ve oynat düğmesi; [gap] aralıkları kısa ekranlarda daraltır. */
@Composable
private fun SessionControls(
    meditation: Meditation,
    isPlaying: Boolean,
    session: SessionState,
    onLengthSelect: (Int) -> Unit,
    onPlayClick: () -> Unit,
    gap: Float,
) {
    val hasAudio = meditation.audio != null
    val colors = SerinTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = meditation.title,
            style = SerinType.DetailTitle,
            // Başlıklar içerikten geldiği için uzun olanlar ikinci satıra geçer.
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = formatDuration(session.remainingMs),
            style = SerinType.SessionClock.copy(color = colors.textMuted),
        )
        Spacer(Modifier.height((28 * gap).dp))
        SessionLengthPicker(selected = session.lengthMinutes, onSelect = onLengthSelect)
        Spacer(Modifier.height((28 * gap).dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .alpha(if (hasAudio) 1f else 0.3f)
                .clip(CircleShape)
                .background(colors.accent)
                .clickable(enabled = hasAudio, role = Role.Button, onClick = onPlayClick),
        ) {
            Image(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                colorFilter = ColorFilter.tint(colors.onAccent),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/** Meditasyon görseli yuvarlak bir ay gibi durur; çevresindeki halka seansın ilerlemesini gösterir. */
@Composable
private fun MoonOrb(meditation: Meditation, progress: Float, orbSize: Dp) {
    val colors = SerinTheme.colors
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(orbSize)) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(colors.outline, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            if (progress > 0f) {
                drawArc(
                    colors.accent, -90f, 360f * progress, false, Offset(inset, inset), arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        RemoteImage(
            url = meditation.image,
            fallback = bundledImage(meditation.id),
            modifier = Modifier
                .size(orbSize - 36.dp)
                .clip(CircleShape),
        )
    }
}

/** Seans süresi seçimi: ay evreleri, 5 dk hilal, 10 dk yarım, 20 dk dolunay. */
@Composable
private fun SessionLengthPicker(selected: Int, onSelect: (Int) -> Unit) {
    val colors = SerinTheme.colors
    val lengths = MeditationSession.LengthsMinutes
    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        lengths.forEachIndexed { index, minutes ->
            val isSelected = minutes == selected
            val tint = if (isSelected) colors.accent else colors.textMuted
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(CardShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(minutes) })
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                MoonPhase(lit = (index + 1f) / lengths.size, color = tint, modifier = Modifier.size(22.dp))
                Text(
                    text = stringResource(R.string.session_minutes, minutes),
                    style = SerinType.SessionChip.copy(color = if (isSelected) colors.text else colors.textMuted),
                )
            }
        }
    }
}

/** [lit] 0..1: aydınlık kısmın oranı. Gölge, zemin renginde bir daire olarak kayar. */
@Composable
private fun MoonPhase(lit: Float, color: Color, modifier: Modifier = Modifier) {
    val background = SerinTheme.colors.background
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawCircle(color, r, center)
        if (lit < 1f) {
            drawCircle(background, r, Offset(center.x - 2 * r * lit, center.y))
            drawCircle(color.copy(alpha = 0.35f), r - 0.5.dp.toPx(), center, style = Stroke(1.dp.toPx()))
        }
    }
}

@AdaptivePreviews
@Composable
private fun MeditationDetailScreenPreview() {
    PreviewWindow(SerinTab.Meditation) {
        MeditationDetailScreen(
            meditation = PreviewContent.meditations[3],
            isPlaying = true,
            session = SessionState(lengthMs = 600_000, elapsedMs = 240_000),
            onLengthSelect = {},
            onPlayClick = {},
        )
    }
}
