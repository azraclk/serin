package app.azracelik.serin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val SerinPurple = Color(0xFF756AB6)

val SerinShadow = Color.Black.copy(alpha = 0.25f)

@Immutable
data class SerinColors(
    val background: Color,
    /** Kartların zemini. */
    val surface: Color,
    val text: Color,
    /** Uzun okuma metni; saf renkten biraz daha yumuşak. */
    val textSoft: Color,
    /** Kalan süre gibi ikincil bilgiler. */
    val textMuted: Color,
    /** Çerçeveler, başlıklar, bağlantılar, ilerleme. */
    val accent: Color,
    /** Kart çerçeveleri; gece temasında vurgu rengini harcamaz. */
    val outline: Color,
    /** Seçili düğme gibi [accent] zemin üstündeki metin. */
    val onAccent: Color,
    /** Ana sayfa kartlarının dolgusu. */
    val accentSoft: Color,
    /** Meditasyon kartlarında görselin üstündeki renk katmanı. */
    val overlay: Color,
    /** Alt menü ve seçim dairesi. */
    val bar: Color,
    val onBar: Color,
    /** İlerleme çizgisinin boş kısmı. */
    val track: Color,
    val isDark: Boolean,
)

/** Figma'daki açık tema. */
val LightColors = SerinColors(
    background = Color.White,
    surface = Color.White,
    text = Color.Black,
    textSoft = Color.Black.copy(alpha = 0.85f),
    textMuted = Color.Black.copy(alpha = 0.55f),
    accent = SerinPurple,
    outline = SerinPurple,
    onAccent = Color.White,
    accentSoft = SerinPurple.copy(alpha = 0.25f),
    overlay = SerinPurple.copy(alpha = 0.2f),
    bar = SerinPurple,
    onBar = Color.White,
    track = Color.Black,
    isDark = false,
)

private val NightText = Color(0xFFEEEBF8)
private val NightLavender = Color(0xFFA99FE3)
/** Gece temasındaki tek sıcak vurgu: sönük ay kremi. */
private val MoonCream = Color(0xFFE8D9A8)

/** Gece teması: koyu çivit zemin, lavanta yüzeyler, ay kremi vurgu. */
val DarkColors = SerinColors(
    background = Color(0xFF15131F),
    surface = Color(0xFF211D33),
    text = NightText,
    textSoft = NightText.copy(alpha = 0.85f),
    textMuted = NightText.copy(alpha = 0.6f),
    accent = MoonCream,
    outline = NightLavender.copy(alpha = 0.45f),
    onAccent = Color(0xFF15131F),
    accentSoft = NightLavender.copy(alpha = 0.16f),
    overlay = SerinPurple.copy(alpha = 0.3f),
    bar = Color(0xFF332C5C),
    onBar = NightText,
    track = NightText.copy(alpha = 0.7f),
    isDark = true,
)

val LocalSerinColors = staticCompositionLocalOf { LightColors }
