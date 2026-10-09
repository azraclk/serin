package app.azracelik.serin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import app.azracelik.serin.ui.theme.LocalThemeToggle
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.ThemeToggle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R
import app.azracelik.serin.ui.theme.SerinShadow
import app.azracelik.serin.ui.theme.SerinType
import androidx.compose.ui.graphics.Shadow as TextShadow

val CardShape = RoundedCornerShape(16.dp)
val CardBorderWidth = 3.dp

/** Figma: drop shadow 0 4 4 rgba(0,0,0,0.25). */
val SerinDropShadow = Shadow(radius = 4.dp, color = SerinShadow, offset = DpOffset(0.dp, 4.dp))

fun Modifier.serinShadow(shape: Shape = CardShape) = dropShadow(shape, SerinDropShadow)

/** Figma: text-shadow 0 4 4 rgba(0,0,0,0.25). */
@Composable
fun serinTextShadow(): TextShadow = with(LocalDensity.current) {
    TextShadow(color = SerinShadow, offset = Offset(0f, 4.dp.toPx()), blurRadius = 4.dp.toPx())
}

/**
 * Figma'daki sabit metin genişliği. Normal yazı boyutunda tasarımdaki satır kırılımları korunur;
 * telefonda yazı büyütüldükçe genişlik de aynı oranda artar.
 */
@Composable
fun Dp.scaledByFont(): Dp = this * LocalDensity.current.fontScale

/** Tek satırlık kısa etiket; büyük yazı boyutunda sığmazsa kesilmek yerine küçülür. */
@Composable
fun FitText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    // En küçük boyut dp cinsinden; sp olsaydı büyük yazı ayarında o da büyür ve metin yine sığmazdı.
    val minFontSize = with(LocalDensity.current) { 8.dp.toSp() }
    BasicText(
        text = text,
        style = style.copy(color = style.color.takeOrElse { LocalContentColor.current }),
        maxLines = 1,
        // Kaydırma kapalı: uzun bir kelime alt satıra bölünmez, taşma genişlikten ölçülür.
        softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = minFontSize, maxFontSize = style.fontSize, stepSize = 0.5.sp),
        modifier = modifier,
    )
}

/**
 * Tüm ekranların üstündeki "serin" yazısı; durum çubuğunun altında 40dp yer kaplar.
 * Sağ uçta gece/gündüz düğmesi bulunur (splash'te [showThemeToggle] kapalıdır).
 */
@Composable
fun SerinHeader(modifier: Modifier = Modifier, showThemeToggle: Boolean = true) {
    Box(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 11.dp),
    ) {
        Text(
            text = stringResource(R.string.logo),
            style = SerinType.Logo,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        val toggle = LocalThemeToggle.current
        if (showThemeToggle && toggle != null) {
            ThemeToggleButton(toggle, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp))
        }
    }
}

/** Gece temasında güneş, açık temada hilal çizer; dokununca öbür temaya geçer. */
@Composable
private fun ThemeToggleButton(toggle: ThemeToggle, modifier: Modifier = Modifier) {
    val color = SerinTheme.colors.text
    Canvas(
        modifier
            .requiredSize(48.dp)
            .clip(CircleShape)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(if (toggle.isDark) R.string.theme_to_light else R.string.theme_to_dark),
                onClick = toggle.onToggle,
            ),
    ) {
        val c = center
        if (toggle.isDark) {
            drawCircle(color, 5.dp.toPx(), c)
            for (i in 0 until 8) {
                val a = i * PI.toFloat() / 4
                val dir = Offset(cos(a), sin(a))
                drawLine(color, c + dir * 8.dp.toPx(), c + dir * 11.dp.toPx(), 1.8.dp.toPx(), StrokeCap.Round)
            }
        } else {
            val full = Path().apply { addOval(Rect(c, 9.dp.toPx())) }
            val cut = Path().apply { addOval(Rect(c + Offset((-4).dp.toPx(), (-3).dp.toPx()), 8.dp.toPx())) }
            drawPath(Path.combine(PathOperation.Difference, full, cut), color)
        }
    }
}
