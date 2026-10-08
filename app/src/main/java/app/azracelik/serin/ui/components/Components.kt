package app.azracelik.serin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
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

/** Tüm ekranların üstündeki "serin" yazısı; durum çubuğunun altında 40dp yer kaplar. */
@Composable
fun SerinHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.logo),
        style = SerinType.Logo,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 11.dp),
    )
}
