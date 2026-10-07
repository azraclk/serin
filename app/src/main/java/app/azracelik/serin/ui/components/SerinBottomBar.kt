package app.azracelik.serin.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.ui.theme.SerinPurple
import app.azracelik.serin.ui.theme.SerinType

enum class SerinTab(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    /** Figma'daki 393dp genişliğe göre ikonun yatay merkezi. */
    val centerX: Float,
) {
    Home(R.string.nav_home, R.drawable.ic_home, 80f),
    Meditation(R.string.nav_meditation, R.drawable.ic_meditation, 200f),
    Blog(R.string.nav_blog, R.drawable.ic_blog, 324f),
}

private const val DesignWidth = 393f
private val BumpHeight = 22.dp
private val SlabHeight = 80.dp
private val SelectionSize = 75.dp
private val ItemWidth = 96.dp

/** Alt menünün ekranın altından kapladığı yükseklik; kaydırılan içerik bu kadar boşluk bırakmalı. */
@Composable
fun bottomBarHeight(): Dp =
    BumpHeight + SlabHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
fun SerinBottomBar(
    selected: SerinTab,
    onSelect: (SerinTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth().height(bottomBarHeight())) {
        fun centerOf(tab: SerinTab) = maxWidth * (tab.centerX / DesignWidth)

        // Seçim dairesi menünün arkasında kalır, yalnızca üst kısmı taşar.
        val selectionX by animateDpAsState(centerOf(selected) - SelectionSize / 2, label = "selection")
        Image(
            painter = painterResource(R.drawable.nav_selection),
            contentDescription = null,
            modifier = Modifier.offset(x = selectionX).size(SelectionSize),
        )
        Box(
            Modifier
                .offset(y = BumpHeight)
                .fillMaxWidth()
                .height(maxHeight - BumpHeight)
                .background(SerinPurple)
                .innerShadow(RectangleShape, SerinDropShadow),
        )
        SerinTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(x = centerOf(tab) - ItemWidth / 2, y = BumpHeight)
                    .width(ItemWidth)
                    .height(SlabHeight)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) }),
            ) {
                Spacer(Modifier.height(19.dp))
                Image(
                    painter = painterResource(tab.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(tab.label),
                    style = SerinType.NavLabel,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.offset(y = 2.dp),
                )
            }
        }
    }
}
