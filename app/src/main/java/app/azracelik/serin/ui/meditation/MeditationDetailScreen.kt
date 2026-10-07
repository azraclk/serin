package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun MeditationDetailScreen(
    meditation: Meditation,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        RemoteImage(
            url = meditation.image,
            fallback = bundledImage(meditation.id),
            modifier = Modifier
                .size(300.dp)
                .serinShadow()
                .clip(CardShape),
        )
        Spacer(Modifier.height(51.dp))
        Text(
            text = meditation.title,
            style = SerinType.DetailTitle.copy(shadow = serinTextShadow()),
            maxLines = 1,
        )
        Spacer(Modifier.height(70.dp))
        Image(painter = painterResource(R.drawable.dash), contentDescription = null)
        Spacer(Modifier.height(8.dp))
        Image(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = stringResource(R.string.play),
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onPlayClick),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun MeditationDetailScreenPreview() {
    SerinTheme { MeditationDetailScreen(PreviewContent.meditations[3], onPlayClick = {}) }
}
