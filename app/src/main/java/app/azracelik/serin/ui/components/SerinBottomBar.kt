package app.azracelik.serin.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
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
) {
    Home(R.string.nav_home, R.drawable.ic_home),
    Meditation(R.string.nav_meditation, R.drawable.ic_meditation),
    Blog(R.string.nav_blog, R.drawable.ic_blog),
}

private val BarHeight = 64.dp
private val BarShape = RoundedCornerShape(32.dp)
private val BarSideMargin = 20.dp
private val BarBottomMargin = 12.dp
private val BarInnerPadding = 8.dp
private val ItemHeight = 48.dp
private val IconSize = 24.dp
/** Seçili sekme, etiketini de gösterdiği için diğerlerinden bu kadar kat geniş yer alır. */
private const val SelectedWeight = 1.9f

/** Alt menünün ekranın altından kapladığı yükseklik; kaydırılan içerik bu kadar boşluk bırakmalı. */
@Composable
fun bottomBarHeight(): Dp =
    BarHeight + BarBottomMargin + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** Ekranın altında yüzen hap biçimli menü; seçili sekme ikonunun yanında etiketini de gösterir. */
@Composable
fun SerinBottomBar(
    selected: SerinTab,
    onSelect: (SerinTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SerinTheme.colors
    Box(modifier.fillMaxWidth().height(bottomBarHeight())) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomBarHeight() - BarHeight)
                .padding(horizontal = BarSideMargin)
                .fillMaxWidth()
                .height(BarHeight)
                .serinShadow(BarShape)
                .clip(BarShape)
                .background(colors.bar)
                .padding(horizontal = BarInnerPadding),
        ) {
            SerinTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val weight by animateFloatAsState(if (isSelected) SelectedWeight else 1f, label = "tabWeight")
                val highlight by animateFloatAsState(if (isSelected) 0.2f else 0f, label = "tabHighlight")
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(weight)
                        .padding(horizontal = 2.dp)
                        .heightIn(min = ItemHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(colors.onBar.copy(alpha = highlight))
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) }),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(tab.icon),
                            contentDescription = stringResource(tab.label),
                            colorFilter = ColorFilter.tint(colors.onBar),
                            modifier = Modifier.size(IconSize),
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally(),
                            modifier = Modifier.weight(1f, fill = false),
                        ) {
                            Row {
                                Spacer(Modifier.width(6.dp))
                                FitText(
                                    text = stringResource(tab.label),
                                    style = SerinType.NavLabel.copy(color = colors.onBar, fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
