package app.azracelik.serin.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
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
 * Sol uçta gece/gündüz düğmesi bulunur (splash'te [showThemeToggle] kapalıdır).
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
            ThemeToggleButton(toggle, Modifier.align(Alignment.CenterStart).padding(start = 14.dp))
        }
    }
}

/**
 * "Ufuk" ikonu: gündüz ufuktaki yarım güneş, gece aynı çizgiden yükselen hilal.
 * Tema değişince önce güneş ufka batar, sonra hilal çizginin ardından yükselir (ya da tersi).
 */
@Composable
private fun ThemeToggleButton(toggle: ThemeToggle, modifier: Modifier = Modifier) {
    val color = SerinTheme.colors.text
    val night by animateFloatAsState(
        targetValue = if (toggle.isDark) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "themeToggleHorizon",
    )
    val moon = remember { PathParser().parsePathString(HorizonMoonPath).toPath() }
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
        // Yarı yarıya bölünmüş süre: ilk yarıda güneş batar, ikincide hilal doğar.
        val sink = FastOutSlowInEasing.transform((night / 0.5f).coerceIn(0f, 1f))
        val rise = FastOutSlowInEasing.transform(((night - 0.5f) / 0.5f).coerceIn(0f, 1f))
        val stroke = Stroke(HorizonStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Çizim 24x24'lük bir birim alanında yapılır; 1 birim = 1 dp.
        translate(center.x - 12.dp.toPx(), center.y - 12.dp.toPx()) {
            scale(1.dp.toPx(), 1.dp.toPx(), Offset.Zero) {
                // Ufuk çizgisinin altına inen her şey kesilir; güneş ve ay çizginin ardından geçer.
                clipRect(0f, 0f, 24f, HorizonY) {
                    translate(0f, SunDrop * sink) {
                        drawArc(color, 180f, 180f, false, Offset(7f, HorizonY - 5f), Size(10f, 10f), style = stroke)
                        val rayColor = color.copy(alpha = 1f - sink)
                        for (deg in SunRayAngles) {
                            val a = deg * PI.toFloat() / 180f
                            val dir = Offset(cos(a), sin(a))
                            val c = Offset(12f, HorizonY)
                            drawLine(rayColor, c + dir * 7.5f, c + dir * 9.8f, HorizonStroke, StrokeCap.Round)
                        }
                    }
                    translate(0f, MoonDrop * (1f - rise)) {
                        withTransform({
                            translate(6.6f, 3.6f)
                            scale(MoonScale, MoonScale, Offset.Zero)
                        }) {
                            drawPath(
                                moon,
                                color.copy(alpha = (rise * 2f).coerceAtMost(1f)),
                                style = Stroke(HorizonStroke / MoonScale, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        }
                    }
                }
                drawLine(color, Offset(2.5f, HorizonY), Offset(21.5f, HorizonY), HorizonStroke, StrokeCap.Round)
                drawLine(color, Offset(7f, 20.6f), Offset(17f, 20.6f), HorizonStroke, StrokeCap.Round)
            }
        }
    }
}

private const val HorizonStroke = 1.8f
private const val HorizonY = 17f
/** Güneşin ufkun ardına tamamen gizlenmesi için inmesi gereken mesafe (ışınlar dahil). */
private const val SunDrop = 11f
private const val MoonDrop = 12f
private const val MoonScale = 0.55f
private val SunRayAngles = floatArrayOf(-90f, -50f, -130f, -15f, -165f)
private const val HorizonMoonPath = "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"
