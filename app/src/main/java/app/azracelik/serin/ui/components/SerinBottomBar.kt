package app.azracelik.serin.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.ui.theme.SerinTheme
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
private val DiscSize = 64.dp
private val DiscRing = 5.dp
private val ItemWidth = 116.dp
private val IconSize = 24.dp
/** İkonun menü bileşeninin tepesine göre konumu: menüde dururken ve disk içinde ortalıyken. */
private val RestingIconTop = BumpHeight + 19.dp
private val RaisedIconTop = (DiscSize - IconSize) / 2

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
    val colors = SerinTheme.colors
    BoxWithConstraints(modifier.fillMaxWidth().height(bottomBarHeight())) {
        fun centerOf(tab: SerinTab) = maxWidth * (tab.centerX / DesignWidth)

        Box(
            Modifier
                .offset(y = BumpHeight)
                .fillMaxWidth()
                .height(maxHeight - BumpHeight)
                .background(colors.bar)
                .innerShadow(RectangleShape, SerinDropShadow),
        )
        // Seçim diski menünün üstüne çıkar; zemin renginde halka, menüde oyuk izlenimi verir.
        val discX by animateDpAsState(centerOf(selected) - DiscSize / 2, label = "selection")
        Box(
            Modifier
                .offset(x = discX)
                .size(DiscSize)
                .clip(CircleShape)
                .background(colors.bar)
                .border(DiscRing, colors.background, CircleShape),
        )
        SerinTab.entries.forEach { tab ->
            val isSelected = tab == selected
            // 0 = menüde duruyor, 1 = diskin içinde yükselmiş.
            val lift by animateFloatAsState(if (isSelected) 1f else 0f, label = "lift")
            val iconTop = RestingIconTop + (RaisedIconTop - RestingIconTop) * lift
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(x = centerOf(tab) - ItemWidth / 2)
                    .width(ItemWidth)
                    .height(BumpHeight + SlabHeight)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) }),
            ) {
                Spacer(Modifier.height(iconTop))
                Image(
                    painter = painterResource(tab.icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colors.onBar),
                    modifier = Modifier.size(IconSize).alpha(0.7f + 0.3f * lift),
                )
                // Etiket sabit yükseklikte kalır: ikon yükselirken altındaki boşluk büyür.
                Spacer(Modifier.height(RestingIconTop - iconTop + 2.dp))
                FitText(
                    text = stringResource(tab.label),
                    style = SerinType.NavLabel.copy(
                        color = colors.onBar.copy(alpha = if (isSelected) 1f else 0.75f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    // Sığma hesabında yalnızca genişlik sayılsın; yükseklik menünün dışına taşabilir.
                    modifier = Modifier.wrapContentHeight(unbounded = true),
                )
            }
        }
    }
}
