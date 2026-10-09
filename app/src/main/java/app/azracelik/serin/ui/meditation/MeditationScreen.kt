package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.CardBorderWidth
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.FitText
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.serinShadow
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

@Composable
fun MeditationScreen(
    meditations: List<Meditation>,
    onMeditationClick: (Meditation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(21.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp),
            contentPadding = PaddingValues(start = 11.dp, end = 11.dp, bottom = contentBottomPadding()),
        ) {
            items(meditations, key = { it.id }) { meditation ->
                MeditationCard(meditation, onClick = { onMeditationClick(meditation) })
            }
        }
    }
}

@Composable
private fun MeditationCard(meditation: Meditation, onClick: () -> Unit) {
    val colors = SerinTheme.colors
    Box(
        Modifier
            .height(261.dp)
            .serinShadow()
            .clip(CardShape)
            .background(colors.surface)
            .border(CardBorderWidth, colors.outline, CardShape)
            .clickable(onClick = onClick),
    ) {
        RemoteImage(
            url = meditation.image,
            fallback = bundledImage(meditation.id),
            alpha = 0.25f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(colors.overlay))
        FitText(
            text = meditation.label,
            style = SerinType.CardLabel.copy(shadow = serinTextShadow()),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 6.dp, start = 8.dp, end = 8.dp),
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun MeditationScreenPreview() {
    SerinTheme { MeditationScreen(PreviewContent.meditations, onMeditationClick = {}) }
}
