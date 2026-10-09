package app.azracelik.serin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
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
    /** Ana başlıkların gümüş yansımalı dolgusu. */
    val silver: Brush,
    /** Vurgu metinlerinin altın yansımalı dolgusu. */
    val gold: Brush,
    val isDark: Boolean,
)

/** Işığı yakalayan parlak bantlı çapraz geçiş; durak sayısı ve konumu iki temada da aynı. */
private fun sheen(colors: List<Color>): Brush {
    val stops = colors.mapIndexed { i, c -> i / (colors.size - 1f) to c }
    return Brush.linearGradient(colorStops = stops.toTypedArray())
}

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
    // Beyaz zeminde açık gümüş görünmez; koyu çelikten başlayıp ortada yumuşakça parlar.
    silver = sheen(listOf(Color(0xFF1F1D33), Color(0xFF6C688F), Color(0xFF2A2744), Color(0xFF7A769C), Color(0xFF1F1D33))),
    gold = sheen(listOf(Color(0xFF4F4590), Color(0xFF8C80D6), Color(0xFF5A4F9E), Color(0xFF9A8FE0), Color(0xFF4F4590))),
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
    silver = sheen(listOf(Color(0xFFFFFFFF), Color(0xFFB4BAD9), Color(0xFFF6F4FF), Color(0xFF9AA1C8), Color(0xFFFFFFFF))),
    gold = sheen(listOf(Color(0xFFFBF0C8), Color(0xFFD9B05E), Color(0xFFF5E2A0), Color(0xFFC49A48), Color(0xFFFBF0C8))),
    isDark = true,
)

val LocalSerinColors = staticCompositionLocalOf { LightColors }
