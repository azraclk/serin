package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import app.azracelik.serin.ui.components.bottomBarHeight
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.theme.SerinPurple
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun MeditationDetailScreen(
    meditation: Meditation,
    isPlaying: Boolean,
    progress: Float,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasAudio = meditation.audio != null
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomBarHeight()),
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
        Spacer(Modifier.height(70.dp))
        ProgressLine(progress)
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
            modifier = Modifier
                .size(50.dp)
                .alpha(if (hasAudio) 1f else 0.3f)
                .clip(CircleShape)
                .clickable(enabled = hasAudio, role = Role.Button, onClick = onPlayClick),
        )
    }
}

/** Figma'daki çizgi; çalınan kısım mor ile doluyor. */
@Composable
private fun ProgressLine(progress: Float) {
    Canvas(
        Modifier
            .width(236.dp)
            .height(75.dp),
    ) {
        val y = size.height / 2
        drawLine(Color.Black, Offset(0f, y), Offset(size.width, y), 2.dp.toPx(), StrokeCap.Round)
        if (progress > 0f) {
            drawLine(SerinPurple, Offset(0f, y), Offset(size.width * progress, y), 4.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun MeditationDetailScreenPreview() {
    SerinTheme {
        MeditationDetailScreen(PreviewContent.meditations[3], isPlaying = true, progress = 0.4f, onPlayClick = {})
    }
}
