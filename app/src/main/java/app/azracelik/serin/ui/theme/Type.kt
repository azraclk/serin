package app.azracelik.serin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R

@OptIn(ExperimentalTextApi::class)
private fun variable(weight: Int) = Font(
    R.font.inter_variable,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Gövde ve küçük metinlerin yazı tipi; hiyerarşi boyut ve ağırlıkla kurulur. */
val Inter = FontFamily(variable(300), variable(400), variable(500), variable(700))

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: Int) = Font(
    R.font.fraunces_variable,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight),
        // Büyük başlıklara göre ayarlı kontrast, hafif yumuşatılmış uçlar; "wonk" kapalı.
        FontVariation.Setting("opsz", 48f),
        FontVariation.Setting("SOFT", 50f),
        FontVariation.Setting("WONK", 0f),
    ),
)

/** Yalnızca ana başlıkların yazı tipi. */
val Fraunces = FontFamily(fraunces(300), fraunces(400))

/** Ana başlıklar: Fraunces, gümüş yansıma; hafif ağırlık gece teması için yeterince sakin. */
@Composable
@ReadOnlyComposable
private fun heading() = TextStyle(
    brush = SerinTheme.colors.silver,
    fontFamily = Fraunces,
    fontWeight = FontWeight.Light,
)

/** Ana başlık sayılmayan, yazı tipi Inter kalan hafif metinler. */
private val Light = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Light)

/** Renk belirtilmez; metinler temanın rengini alır (bkz. SerinTheme). */
private val Base = TextStyle(fontFamily = Inter)

/**
 * Metin stilleri; boyutlar yalnızca 12, 14, 16, 20, 24, 36 ve 44sp'den seçilir.
 * Yeni bir stil gerekiyorsa bu kademelerden birini kullanın.
 */
object SerinType {
    val Logo @Composable @ReadOnlyComposable get() = heading().copy(fontSize = 24.sp)
    val Display @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 36.sp, textAlign = TextAlign.Center)
    val Splash @Composable @ReadOnlyComposable get() =
        Display.copy(lineHeight = 40.sp, letterSpacing = 1.8.sp)
    val SectionTitle @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 20.sp, fontWeight = FontWeight.Normal)
    val PickTitle @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 24.sp, lineHeight = 30.sp)
    val Caption = Base.copy(fontSize = 14.sp, letterSpacing = 0.5.sp)
    val CardLabel = Base.copy(fontSize = 20.sp, textAlign = TextAlign.Center)
    val DetailTitle @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 24.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center)
    // Satır yüksekliği Figma'daki 40sp; yazı küçülünce (FitText) onunla birlikte küçülsün diye em.
    val NavLabel = Base.copy(fontSize = 12.sp, lineHeight = (40f / 12f).em, letterSpacing = 0.05.em)

    // Blog yazısı detay ekranı; Figma'da yok, mevcut stile göre tasarlandı.
    val PostRowTitle @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Normal)
    val PostTitle @Composable @ReadOnlyComposable get() =
        heading().copy(fontSize = 24.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
    val PostBody = Base.copy(fontSize = 16.sp, lineHeight = 26.sp)
    // Yazı içi ara başlık; ana başlık değil, Inter kalır. Rengi altın yansımalı.
    val PostHeading @Composable @ReadOnlyComposable get() =
        Light.copy(brush = SerinTheme.colors.gold, fontSize = 20.sp, lineHeight = 28.sp)
    val PostSubheading = Base.copy(fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)

    // Seans süresi ve mini player.
    val SessionChip = Base.copy(fontSize = 14.sp, letterSpacing = 0.5.sp)
    val SessionClock = Light.copy(fontSize = 44.sp, letterSpacing = 1.sp)
    val SessionTime = Base.copy(fontSize = 12.sp, letterSpacing = 0.6.sp)
    val MiniTitle = Base.copy(fontSize = 16.sp, lineHeight = 20.sp)
}

/** Material bileşenlerinin varsayılan Roboto'ya düşmemesi için tüm stiller Inter'a çevrilir. */
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Inter),
        displayMedium = displayMedium.copy(fontFamily = Inter),
        displaySmall = displaySmall.copy(fontFamily = Inter),
        headlineLarge = headlineLarge.copy(fontFamily = Inter),
        headlineMedium = headlineMedium.copy(fontFamily = Inter),
        headlineSmall = headlineSmall.copy(fontFamily = Inter),
        titleLarge = titleLarge.copy(fontFamily = Inter),
        titleMedium = titleMedium.copy(fontFamily = Inter),
        titleSmall = titleSmall.copy(fontFamily = Inter),
        bodyLarge = bodyLarge.copy(fontFamily = Inter),
        bodyMedium = bodyMedium.copy(fontFamily = Inter),
        bodySmall = bodySmall.copy(fontFamily = Inter),
        labelLarge = labelLarge.copy(fontFamily = Inter),
        labelMedium = labelMedium.copy(fontFamily = Inter),
        labelSmall = labelSmall.copy(fontFamily = Inter),
    )
}
