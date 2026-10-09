package app.azracelik.serin.ui.adaptive

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Material'ın pencere genişliği sınıfları: telefon dikey, küçük tablet ya da yatay telefon, tablet. */
enum class WidthClass {
    Compact,
    Medium,
    Expanded;

    companion object {
        fun of(width: Dp) = when {
            width < 600.dp -> Compact
            width < 840.dp -> Medium
            else -> Expanded
        }
    }
}

/** Yatay telefonlar [Compact] sayılır: dikey alan dar olduğu için yerleşim sıkılaşır. */
enum class HeightClass {
    Compact,
    Regular;

    companion object {
        fun of(height: Dp) = if (height < 480.dp) Compact else Regular
    }
}

/**
 * Uygulamanın içinde bulunduğu pencerenin ölçüleri; bölünmüş ekran ve katlanır cihazlarda pencere
 * ekrandan küçük olabilir, bu yüzden ekran değil pencere ölçülür.
 *
 * [contentWidth], menü ve kesim/çubuk boşlukları düşüldükten sonra ekranların kullanabildiği genişliktir.
 */
@Immutable
class WindowSize(
    val width: WidthClass,
    val height: HeightClass,
    val contentWidth: Dp,
    val windowHeight: Dp,
) {
    /** Menü yana yerleşir: orta ve geniş pencerede, ayrıca dar ama alçak (yatay küçük telefon) pencerede. */
    val useRail get() = usesRail(width, height)

    /** Yatay telefon: alçak pencere. Dikey alan kıt, yatay alan göreceli bol. */
    val isLandscapeShort get() = height == HeightClass.Compact

    /**
     * İçerik [maxContent]'ten genişse iki yana eşit boşluk bırakır, değilse [min] kadar.
     * Kaydırılan listeler genişliği kısıtlamak yerine bu değeri kenar boşluğu olarak alır; böylece
     * kenarlardaki boş alandan da kaydırılabilir.
     */
    fun sidePadding(maxContent: Dp, min: Dp): Dp = ((contentWidth - maxContent) / 2).coerceAtLeast(min)

    companion object {
        fun usesRail(width: WidthClass, height: HeightClass) =
            width != WidthClass.Compact || height == HeightClass.Compact
    }

    /** [available] genişliğe, en az [minCell] genişliğinde kaç sütun sığar; [max] ile sınırlıdır. */
    fun columns(available: Dp, minCell: Dp, spacing: Dp, max: Int, min: Int = 1): Int =
        ((available + spacing) / (minCell + spacing)).toInt().coerceIn(min, max)
}

/** Telefon dikey düzeni; pencere ölçülmeden önce (önizleme, testler) kullanılan varsayılan. */
val LocalWindowSize = compositionLocalOf {
    WindowSize(WidthClass.Compact, HeightClass.Regular, contentWidth = 393.dp, windowHeight = 852.dp)
}
