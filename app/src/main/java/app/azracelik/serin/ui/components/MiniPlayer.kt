package app.azracelik.serin.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.playback.SessionState
import app.azracelik.serin.ui.theme.SerinPurple
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

private val MiniPlayerHeight = 64.dp
private val MiniPlayerGap = 8.dp
private val MiniPlayerShape = RoundedCornerShape(16.dp)

/** Mini player görünürken yüksekliği (boşluğuyla birlikte), görünmüyorken 0. */
val LocalMiniPlayerInset = compositionLocalOf { 0.dp }

/** Mini player'ın kapladığı alan. */
val MiniPlayerInset: Dp = MiniPlayerHeight + MiniPlayerGap

/** Kaydırılan içeriğin altta bırakması gereken boşluk: alt menü ve varsa mini player. */
@Composable
fun contentBottomPadding(): Dp = bottomBarHeight() + LocalMiniPlayerInset.current

/** 9:05 gibi dakika:saniye. */
fun formatDuration(ms: Long): String {
    val totalSeconds = (ms + 999) / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

/** Alt menünün üstünde, süren seansı gösteren küçük çalar. */
@Composable
fun MiniPlayer(
    meditation: Meditation,
    isPlaying: Boolean,
    session: SessionState,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = MiniPlayerGap)
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .serinShadow(MiniPlayerShape)
            .clip(MiniPlayerShape)
            .background(Color.White)
            .border(2.dp, SerinPurple, MiniPlayerShape)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 10.dp, end = 6.dp),
        ) {
            RemoteImage(
                url = meditation.image,
                fallback = bundledImage(meditation.id),
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = meditation.title,
                    style = SerinType.MiniTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = formatDuration(session.remainingMs), style = SerinType.SessionTime)
            }
            Image(
                painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onToggle)
                    .padding(8.dp)
                    .size(24.dp),
            )
            Image(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.close),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClose)
                    .padding(10.dp)
                    .size(20.dp),
            )
        }
        // Seansın geçen kısmı, kartın altında ince bir çizgi.
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(session.progress)
                .height(3.dp)
                .background(SerinPurple),
        )
    }
}

@Preview(widthDp = 393)
@Composable
private fun MiniPlayerPreview() {
    SerinTheme {
        MiniPlayer(
            meditation = PreviewContent.meditations[2],
            isPlaying = true,
            session = SessionState(lengthMs = 600_000, elapsedMs = 240_000),
            onClick = {},
            onToggle = {},
            onClose = {},
        )
    }
}
