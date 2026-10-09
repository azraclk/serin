package app.azracelik.serin.ui.theme

import androidx.compose.material3.Typography
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
    R.font.dmsans_variable,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Uygulamadaki tek yazı tipi; hiyerarşi boyut ve ağırlıkla kurulur. */
val DmSans = FontFamily(variable(300), variable(400), variable(500), variable(700))

/** Başlıklar; hafif ağırlık gece teması için yeterince sakin. */
private val Heading = TextStyle(fontFamily = DmSans, fontWeight = FontWeight.Light)

/** Renk belirtilmez; metinler temanın rengini alır (bkz. SerinTheme). */
private val Base = TextStyle(fontFamily = DmSans)

/**
 * Metin stilleri; boyutlar yalnızca 12, 14, 16, 20, 24, 36 ve 44sp'den seçilir.
 * Yeni bir stil gerekiyorsa bu kademelerden birini kullanın.
 */
object SerinType {
    val Logo = Heading.copy(fontSize = 24.sp)
    val Display = Heading.copy(fontSize = 36.sp, textAlign = TextAlign.Center)
    val Splash = Display.copy(lineHeight = 40.sp, letterSpacing = 1.8.sp)
    val SectionTitle = Heading.copy(fontSize = 20.sp, fontWeight = FontWeight.Normal)
    val PickTitle = Heading.copy(fontSize = 24.sp, lineHeight = 30.sp)
    val Caption = Base.copy(fontSize = 14.sp, letterSpacing = 0.5.sp)
    val CardLabel = Base.copy(fontSize = 20.sp, textAlign = TextAlign.Center)
    val DetailTitle = Heading.copy(fontSize = 24.sp, letterSpacing = 2.sp, textAlign = TextAlign.Center)
    // Satır yüksekliği Figma'daki 40sp; yazı küçülünce (FitText) onunla birlikte küçülsün diye em.
    val NavLabel = Base.copy(fontSize = 12.sp, lineHeight = (40f / 12f).em, letterSpacing = 0.05.em)

    // Blog yazısı detay ekranı; Figma'da yok, mevcut stile göre tasarlandı.
    val PostRowTitle = Heading.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Normal)
    val PostTitle = Heading.copy(fontSize = 24.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
    val PostBody = Base.copy(fontSize = 16.sp, lineHeight = 26.sp)
    val PostHeading = Heading.copy(fontSize = 20.sp, lineHeight = 28.sp)
    val PostSubheading = Base.copy(fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)

    // Seans süresi ve mini player.
    val SessionChip = Base.copy(fontSize = 14.sp, letterSpacing = 0.5.sp)
    val SessionClock = Heading.copy(fontSize = 44.sp, letterSpacing = 1.sp)
    val SessionTime = Base.copy(fontSize = 12.sp, letterSpacing = 0.6.sp)
    val MiniTitle = Base.copy(fontSize = 16.sp, lineHeight = 20.sp)
}

/** Material bileşenlerinin varsayılan Roboto'ya düşmemesi için tüm stiller DM Sans'a çevrilir. */
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = DmSans),
        displayMedium = displayMedium.copy(fontFamily = DmSans),
        displaySmall = displaySmall.copy(fontFamily = DmSans),
        headlineLarge = headlineLarge.copy(fontFamily = DmSans),
        headlineMedium = headlineMedium.copy(fontFamily = DmSans),
        headlineSmall = headlineSmall.copy(fontFamily = DmSans),
        titleLarge = titleLarge.copy(fontFamily = DmSans),
        titleMedium = titleMedium.copy(fontFamily = DmSans),
        titleSmall = titleSmall.copy(fontFamily = DmSans),
        bodyLarge = bodyLarge.copy(fontFamily = DmSans),
        bodyMedium = bodyMedium.copy(fontFamily = DmSans),
        bodySmall = bodySmall.copy(fontFamily = DmSans),
        labelLarge = labelLarge.copy(fontFamily = DmSans),
        labelMedium = labelMedium.copy(fontFamily = DmSans),
        labelSmall = labelSmall.copy(fontFamily = DmSans),
    )
}
