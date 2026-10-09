package app.azracelik.serin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) = Font(
    res,
    FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

/** Gövde ve etiketler. */
val DmSans = FontFamily(
    variable(R.font.dmsans_variable, 400),
    variable(R.font.dmsans_variable, 500),
    variable(R.font.dmsans_variable, 700),
)

/** Başlıklar; hafif ağırlık gece teması için yeterince sakin. */
val Fraunces = FontFamily(
    variable(R.font.fraunces_variable, 300),
    variable(R.font.fraunces_variable, 400),
)

private val Heading = TextStyle(fontFamily = Fraunces, fontWeight = FontWeight.Light)

/** Renk belirtilmez; metinler temanın rengini alır (bkz. SerinTheme). */
private val Base = TextStyle(fontFamily = DmSans)

/** Figma'daki metin stilleri. */
object SerinType {
    val Logo = Heading.copy(fontSize = 24.sp)
    val Display = Heading.copy(fontSize = 36.sp, textAlign = TextAlign.Center)
    val Splash = Display.copy(lineHeight = 40.sp, letterSpacing = 1.8.sp)
    val SectionTitle = Heading.copy(fontSize = 20.sp, fontWeight = FontWeight.Normal)
    val PickTitle = Heading.copy(fontSize = 24.sp, lineHeight = 30.sp)
    val Caption = Base.copy(fontSize = 13.sp, letterSpacing = 0.5.sp)
    val CardLabel = Base.copy(fontSize = 20.sp, textAlign = TextAlign.Center)
    val HomeCard = Base.copy(fontSize = 16.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, hyphens = Hyphens.Auto)
    val BlogTitle = Heading.copy(fontSize = 24.sp, lineHeight = 50.sp, textAlign = TextAlign.Center, hyphens = Hyphens.Auto)
    val DetailTitle = Heading.copy(fontSize = 24.sp, letterSpacing = 6.sp, textAlign = TextAlign.Center)
    // Satır yüksekliği Figma'daki 40sp; yazı küçülünce (FitText) onunla birlikte küçülsün diye em.
    val NavLabel = Base.copy(fontSize = 12.sp, lineHeight = (40f / 12f).em, letterSpacing = 0.05.em)

    // Blog yazısı detay ekranı; Figma'da yok, mevcut stile göre tasarlandı.
    val PostTitle = Heading.copy(fontSize = 24.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
    val PostBody = Base.copy(fontSize = 16.sp, lineHeight = 26.sp)
    val PostHeading = Heading.copy(
        fontSize = 20.sp,
        lineHeight = 28.sp,
    )
    val PostSubheading = Base.copy(fontSize = 17.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold)

    // Seans süresi ve mini player.
    val SessionChip = Base.copy(fontSize = 14.sp, letterSpacing = 0.5.sp)
    val SessionTime = Base.copy(fontSize = 12.sp, letterSpacing = 0.6.sp)
    val MiniTitle = Base.copy(fontSize = 15.sp, lineHeight = 20.sp)
}

val Typography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = DmSans),
        bodyMedium = bodyMedium.copy(fontFamily = DmSans),
        bodySmall = bodySmall.copy(fontFamily = DmSans),
        titleLarge = titleLarge.copy(fontFamily = DmSans),
        titleMedium = titleMedium.copy(fontFamily = DmSans),
        labelLarge = labelLarge.copy(fontFamily = DmSans),
    )
}
