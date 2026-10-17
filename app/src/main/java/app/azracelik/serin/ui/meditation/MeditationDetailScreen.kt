package app.azracelik.serin.ui.meditation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.requiredSize
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.data.AudioStatus
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.ui.components.DownloadButton
import app.azracelik.serin.ui.components.FavoriteButton
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.playback.SessionState
import app.azracelik.serin.playback.sessionVolume
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.formatDuration
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

/** Dinleme sayfasının sınama etiketi; süre artık burada seçilmediği için sayfayı başka bir şey ayırt etmez. */
const val MeditationDetailTag = "meditationDetail"

/** Küre (en çok 260dp), aralık (56dp) ve kontroller (en çok 360dp) yan yana sığsın diye gereken genişlik. */
private val SideBySideMinWidth = 560.dp

@Composable
fun MeditationDetailScreen(
    meditation: Meditation,
    isPlaying: Boolean,
    isBuffering: Boolean,
    session: SessionState,
    isFavorite: Boolean,
    downloadStatus: AudioStatus?,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Yalnızca nefes meditasyonunda verilir: seçili ritim ve seçici. */
    breathing: BreathingChoice? = null,
    /** Sessiz modda başlık ve düğmeler soluklaşır, süre yalnızca dakika olarak görünür. */
    quiet: Boolean = false,
    /** Bu hafta dinlenen dakika; seans bitince özet satırında gösterilir. */
    weekMinutes: Int = 0,
) {
    val windowSize = LocalWindowSize.current
    val colors = SerinTheme.colors
    val chrome = animateFloatAsState(if (quiet) 0f else 1f, tween(QuietFadeMillis), label = "chrome")
    // Işık sesle birlikte açılıp kısılır. Süre saniyede bir güncellendiği için bir saniye ilerisi hedeflenir,
    // doğrusal geçiş de tam o saniyede biter.
    val light by animateFloatAsState(
        targetValue = if (isPlaying) {
            sessionVolume(minOf(session.elapsedMs + LightLeadMillis, session.lengthMs), session.lengthMs)
        } else {
            0f
        },
        animationSpec = tween(LightLeadMillis.toInt(), easing = LinearEasing),
        label = "light",
    )
    val finished = session.completed && !isPlaying
    // Halka da ışık gibi saniyede bir değil akıcı ilerler: bir sonraki saniyeye doğru doğrusal gider.
    val ringProgress by animateFloatAsState(
        targetValue = when {
            finished -> 1f
            isPlaying -> minOf(session.elapsedMs + LightLeadMillis, session.lengthMs).toFloat() / session.lengthMs.coerceAtLeast(1)
            else -> session.progress
        },
        animationSpec = tween(LightLeadMillis.toInt(), easing = LinearEasing),
        label = "ring",
    )
    val haptics = rememberBreathHaptics(enabled = breathing?.haptics == true)
    val pageModifier = modifier
        .fillMaxSize()
        .background(colors.background)
        .testTag(MeditationDetailTag)

    // Küre ve kontroller yan yana ancak yeterli genişlik varsa sığar; yoksa alt alta dizilir ve kayar.
    if (windowSize.isLandscapeShort && windowSize.contentWidth >= SideBySideMinWidth) {
        // Yatay telefon: dikey alan kıt, yatay alan bol; küre solda, kontroller sağda durur.
        val orbSize = (windowSize.windowHeight - 140.dp).coerceIn(150.dp, 260.dp)
        Column(pageModifier) {
            SerinHeader(Modifier.graphicsLayer { alpha = chrome.value })
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(56.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = contentBottomPadding()),
            ) {
                BreathingGlow(
                    isPlaying = isPlaying,
                    size = orbSize,
                    pattern = breathing?.selected ?: BreathingPattern.Calm,
                    showCue = breathing != null,
                    haptics = haptics,
                    light = light,
                ) {
                    MoonOrb(meditation = meditation, progress = ringProgress, orbSize = orbSize)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 360.dp),
                ) {
                    SessionControls(
                        meditation, isPlaying, isBuffering, session, isFavorite, downloadStatus,
                        onPlayClick, onFavoriteToggle, onDownloadClick, quiet, finished, weekMinutes, chrome, gap = 0.6f,
                    )
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
                        SerinHeader(Modifier.graphicsLayer { alpha = chrome.value })
                        Spacer(Modifier.height((40 * gap).dp))
                    }
                },
                orb = { orbSize ->
                    BreathingGlow(
                        isPlaying = isPlaying,
                        size = orbSize,
                        pattern = breathing?.selected ?: BreathingPattern.Calm,
                        showCue = breathing != null,
                        haptics = haptics,
                        light = light,
                    ) {
                        MoonOrb(meditation = meditation, progress = ringProgress, orbSize = orbSize)
                    }
                },
                controls = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height((36 * gap).dp))
                        SessionControls(
                            meditation, isPlaying, isBuffering, session, isFavorite, downloadStatus,
                            onPlayClick, onFavoriteToggle, onDownloadClick, quiet, finished, weekMinutes, chrome, gap,
                        )
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

private enum class ClockMode { Full, Quiet, Done }

private val OrbBreathingRoom = 12.dp

/** Işık, ses seviyesinin bu kadar ilerisini hedefler (süre saniyede bir güncellenir). */
private const val LightLeadMillis = 1_000L

/** Yükleme halkası, bekleme bu kadar sürerse gösterilir. */
private const val LoadingRingDelayMillis = 400L

/** Başlık, kalan süre ve oynat düğmesi; [gap] aralıkları kısa ekranlarda daraltır. */
@Composable
private fun SessionControls(
    meditation: Meditation,
    isPlaying: Boolean,
    isBuffering: Boolean,
    session: SessionState,
    isFavorite: Boolean,
    downloadStatus: AudioStatus?,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    quiet: Boolean,
    finished: Boolean,
    weekMinutes: Int,
    chrome: State<Float>,
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
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .graphicsLayer { alpha = 0.55f + 0.45f * chrome.value },
        )
        // Kısa ekranda yer kıt; açıklama saat ve düğmelere yer bıraksın diye gizlenir. Seans bitince aynı yerde
        // özet görünür, böylece sayfanın yerleşimi değişmez.
        if ((meditation.description != null || finished) && gap >= 0.6f) {
            Spacer(Modifier.height(6.dp))
            Crossfade(
                targetState = finished,
                animationSpec = tween(QuietFadeMillis),
                label = "note",
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .graphicsLayer { alpha = chrome.value },
            ) { isFinished ->
                Text(
                    text = if (isFinished) {
                        stringResource(R.string.session_done_summary, session.lengthMinutes, weekMinutes)
                    } else {
                        meditation.description.orEmpty()
                    },
                    style = SerinType.Caption.copy(color = colors.textMuted, textAlign = TextAlign.Center),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Saniyeler her saniye göz çeker; sessiz modda yalnızca kalan dakika görünür. Seans bitince saatin
        // yerinde "Tamamlandı" yazar.
        val clock = when {
            finished -> ClockMode.Done
            quiet -> ClockMode.Quiet
            else -> ClockMode.Full
        }
        Crossfade(targetState = clock, animationSpec = tween(QuietFadeMillis), label = "clock") { mode ->
            when (mode) {
                ClockMode.Done -> Text(
                    text = stringResource(R.string.session_done),
                    style = SerinType.SessionClock.copy(color = colors.accent, fontSize = 36.sp),
                    maxLines = 1,
                )
                ClockMode.Quiet -> Text(
                    text = stringResource(R.string.session_minutes, session.remainingMinutes),
                    style = SerinType.SessionClock.copy(color = colors.textMuted),
                )
                ClockMode.Full -> Text(
                    text = formatDuration(session.remainingMs),
                    style = SerinType.SessionClock.copy(color = colors.textMuted),
                )
            }
        }
        Spacer(Modifier.height((28 * gap).dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Sesi olmayan meditasyonda indirme düğmesinin yeri boş kalır; oynat düğmesi ortada durur.
            if (hasAudio) {
                DownloadButton(downloadStatus, onDownloadClick, Modifier.graphicsLayer { alpha = chrome.value })
            } else {
                Spacer(Modifier.size(48.dp))
            }
            // Ses hızlı hazırlanırsa halka hiç çıkmaz; yalnızca bekleme uzayınca belirir.
            var showLoading by remember { mutableStateOf(false) }
            LaunchedEffect(isBuffering) {
                if (isBuffering) delay(LoadingRingDelayMillis)
                showLoading = isBuffering
            }
            val active = isPlaying || isBuffering
            // Duraklat düğmesi sessiz modda da silik görünür; yeri bilinsin diye tamamen kaybolmaz.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp).graphicsLayer { alpha = 0.3f + 0.7f * chrome.value },
            ) {
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
                        painter = painterResource(if (active) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = stringResource(if (active) R.string.pause else R.string.play),
                        colorFilter = ColorFilter.tint(colors.onAccent),
                        modifier = Modifier.size(32.dp),
                    )
                }
                if (showLoading) {
                    // Düğmenin dışında dönen halka; yerleşimi büyütmez.
                    CircularProgressIndicator(
                        color = colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.requiredSize(76.dp),
                    )
                }
            }
            FavoriteButton(isFavorite, onFavoriteToggle, Modifier.graphicsLayer { alpha = chrome.value })
        }
    }
}

/**
 * Meditasyon görseli yuvarlak bir ay gibi durur; çevresindeki ince halka seansın ilerlemesini gösterir.
 * Halkanın ucunda küçük bir ay noktası durur; ilerleme yalnızca o noktanın yerinden okunur.
 */
@Composable
private fun MoonOrb(meditation: Meditation, progress: Float, orbSize: Dp) {
    val colors = SerinTheme.colors
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(orbSize)) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val headRadius = 4.dp.toPx()
            // Uçtaki nokta halkanın dışına taşmasın diye halka içeri çekilir.
            val inset = headRadius
            val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
            drawArc(colors.outline, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            if (progress > 0f) {
                drawArc(
                    colors.accent, -90f, 360f * progress, false, Offset(inset, inset), arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
                val angle = Math.toRadians((-90f + 360f * progress).toDouble())
                val radius = arcSize.width / 2
                val head = Offset(
                    center.x + radius * kotlin.math.cos(angle).toFloat(),
                    center.y + radius * kotlin.math.sin(angle).toFloat(),
                )
                drawCircle(colors.accent.copy(alpha = 0.25f), headRadius * 2, head)
                drawCircle(colors.accent, headRadius, head)
            }
        }
        RemoteImage(
            url = meditation.image,
            fallback = bundledImage(meditation.id),
            modifier = Modifier
                .size(orbSize - OrbImageInset)
                .clip(CircleShape),
        )
    }
}

@AdaptivePreviews
@Composable
private fun MeditationDetailScreenPreview() {
    PreviewWindow(SerinTab.Meditation) {
        MeditationDetailScreen(
            meditation = PreviewContent.meditations[3],
            isPlaying = true,
            isBuffering = false,
            session = SessionState(lengthMs = 600_000, elapsedMs = 240_000),
            isFavorite = true,
            downloadStatus = AudioStatus.Downloaded,
            onPlayClick = {},
            onFavoriteToggle = {},
            onDownloadClick = {},
        )
    }
}
