package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.playback.SessionState
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.formatDuration
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun MeditationDetailScreen(
    meditation: Meditation,
    isPlaying: Boolean,
    session: SessionState,
    onLengthSelect: (Int) -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAudio = meditation.audio != null
    val colors = SerinTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentBottomPadding()),
    ) {
        SerinHeader()
        Spacer(Modifier.height(50.dp))
        BreathingGlow(isPlaying = isPlaying, size = 300.dp) {
            RemoteImage(
                url = meditation.image,
                fallback = bundledImage(meditation.id),
                modifier = Modifier
                    .size(300.dp)
                    .serinShadow()
                    .clip(CardShape),
            )
        }
        Spacer(Modifier.height(51.dp))
        Text(
            text = meditation.title,
            style = SerinType.DetailTitle.copy(shadow = serinTextShadow()),
            // Başlıklar içerikten geldiği için uzun olanlar ikinci satıra geçer.
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(32.dp))
        SessionLengthPicker(selected = session.lengthMinutes, onSelect = onLengthSelect)
        Spacer(Modifier.height(14.dp))
        ProgressLine(session.progress, session.remainingMs)
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
            colorFilter = ColorFilter.tint(colors.text),
            modifier = Modifier
                .size(50.dp)
                .alpha(if (hasAudio) 1f else 0.3f)
                .clip(CircleShape)
                .clickable(enabled = hasAudio, role = Role.Button, onClick = onPlayClick),
        )
    }
}

/** Seans süresi seçimi: 5 / 10 / 20 dk. */
@Composable
private fun SessionLengthPicker(selected: Int, onSelect: (Int) -> Unit) {
    val colors = SerinTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MeditationSession.LengthsMinutes.forEach { minutes ->
            val isSelected = minutes == selected
            Text(
                text = stringResource(R.string.session_minutes, minutes),
                style = SerinType.SessionChip.copy(color = if (isSelected) colors.onAccent else colors.accent),
                modifier = Modifier
                    .clip(ChipShape)
                    .background(if (isSelected) colors.accent else Color.Transparent)
                    .border(1.5.dp, colors.accent, ChipShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(minutes) })
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

private val ChipShape = RoundedCornerShape(50)

/** Figma'daki çizgi; seansın geçen kısmı mor ile dolar, altında kalan süre yazar. */
@Composable
private fun ProgressLine(progress: Float, remainingMs: Long) {
    val colors = SerinTheme.colors
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(236.dp)
            .height(75.dp),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height / 2
            drawLine(colors.track, Offset(0f, y), Offset(size.width, y), 2.dp.toPx(), StrokeCap.Round)
            if (progress > 0f) {
                drawLine(colors.accent, Offset(0f, y), Offset(size.width * progress, y), 4.dp.toPx(), StrokeCap.Round)
            }
        }
        Text(
            text = formatDuration(remainingMs),
            style = SerinType.SessionTime.copy(color = colors.textMuted),
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun MeditationDetailScreenPreview() {
    SerinTheme {
        MeditationDetailScreen(
            meditation = PreviewContent.meditations[3],
            isPlaying = true,
            session = SessionState(lengthMs = 600_000, elapsedMs = 240_000),
            onLengthSelect = {},
            onPlayClick = {},
        )
    }
}
