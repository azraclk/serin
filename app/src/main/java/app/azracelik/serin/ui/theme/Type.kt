package app.azracelik.serin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.azracelik.serin.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

val Merriweather = FontFamily(
    Font(R.font.merriweather_black_italic, FontWeight.Black, FontStyle.Italic),
)

/** Renk belirtilmez; metinler temanın rengini alır (bkz. SerinTheme). */
private val Base = TextStyle(fontFamily = Montserrat)

/** Figma'daki metin stilleri. */
object SerinType {
    val Logo = Base.copy(fontSize = 24.sp)
    val Display = Base.copy(fontSize = 36.sp, textAlign = TextAlign.Center)
    val Splash = Display.copy(lineHeight = 40.sp, letterSpacing = 1.8.sp)
    val CardLabel = Base.copy(fontSize = 20.sp, textAlign = TextAlign.Center)
    val HomeCard = Base.copy(fontSize = 16.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
    val BlogTitle = Base.copy(fontSize = 24.sp, lineHeight = 50.sp, textAlign = TextAlign.Center)
    val DetailTitle = Base.copy(fontSize = 24.sp, letterSpacing = 12.sp, textAlign = TextAlign.Center)
    val NavLabel = Base.copy(fontSize = 12.sp, lineHeight = 40.sp, letterSpacing = 0.6.sp)

    // Blog yazısı detay ekranı; Figma'da yok, mevcut stile göre tasarlandı.
    val PostTitle = Base.copy(fontSize = 24.sp, lineHeight = 34.sp, textAlign = TextAlign.Center)
    val PostBody = Base.copy(fontSize = 16.sp, lineHeight = 26.sp)
    val PostHeading = TextStyle(
        fontFamily = Merriweather,
        fontWeight = FontWeight.Black,
        fontStyle = FontStyle.Italic,
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
        bodyLarge = bodyLarge.copy(fontFamily = Montserrat),
        bodyMedium = bodyMedium.copy(fontFamily = Montserrat),
        bodySmall = bodySmall.copy(fontFamily = Montserrat),
        titleLarge = titleLarge.copy(fontFamily = Montserrat),
        titleMedium = titleMedium.copy(fontFamily = Montserrat),
        labelLarge = labelLarge.copy(fontFamily = Montserrat),
    )
}
