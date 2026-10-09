package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.data.PreviewContent
import app.azracelik.serin.ui.components.FitText
import app.azracelik.serin.ui.components.ImageCard
import app.azracelik.serin.ui.components.SerinHeader
import app.azracelik.serin.ui.components.contentBottomPadding
import app.azracelik.serin.ui.components.serinTextShadow
import app.azracelik.serin.ui.components.AdaptivePreviews
import app.azracelik.serin.ui.components.PreviewWindow
import app.azracelik.serin.ui.components.SerinTab
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

private val GridMaxWidth = 1100.dp
private val MinCardWidth = 165.dp

/** Kartın en-boy oranı; telefonda eski 261dp'lik sabit yüksekliğe denk gelir. */
private const val CardAspectRatio = 0.67f

@Composable
fun MeditationScreen(
    meditations: List<Meditation>,
    onMeditationClick: (Meditation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSize = LocalWindowSize.current
    val sidePadding = windowSize.sidePadding(GridMaxWidth, min = 11.dp)
    // Kart genişliği telefondaki ölçüde kalır; pencere genişledikçe sütun sayısı artar.
    val columns = windowSize.columns(
        available = minOf(windowSize.contentWidth, GridMaxWidth) - 22.dp,
        minCell = MinCardWidth,
        spacing = 21.dp,
        max = 5,
        min = 2,
    )
    Column(modifier.fillMaxSize().background(SerinTheme.colors.background)) {
        SerinHeader(Modifier.padding(bottom = 15.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(21.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp),
            contentPadding = PaddingValues(start = sidePadding, end = sidePadding, bottom = contentBottomPadding()),
        ) {
            itemsIndexed(meditations, key = { _, it -> it.id }) { index, meditation ->
                MeditationCard(meditation, index, onClick = { onMeditationClick(meditation) })
            }
        }
    }
}

@Composable
private fun MeditationCard(meditation: Meditation, index: Int, onClick: () -> Unit) {
    ImageCard(meditation.image, meditation.id, onClick, Modifier.aspectRatio(CardAspectRatio), phase = index) {
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

@AdaptivePreviews
@Composable
private fun MeditationScreenPreview() {
    PreviewWindow(SerinTab.Meditation) { MeditationScreen(PreviewContent.meditations, onMeditationClick = {}) }
}
