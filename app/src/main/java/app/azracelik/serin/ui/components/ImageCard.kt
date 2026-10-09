package app.azracelik.serin.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.theme.SerinTheme
import kotlinx.coroutines.delay

/**
 * Görselin soluk arka plan olduğu, renk katmanlı kart; meditasyon ve ana sayfa kartlarının ortak hâli.
 * Alt kenarı koyulaşır, üstünden aralıklarla ışık geçer, basınca küçülüp yaylanır.
 *
 * [phase] ışık geçişinin başlangıç gecikmesini belirler; aynı ekrandaki kartlara farklı verin ki
 * hepsi aynı anda parlamasın. [shine] kapalıysa ışık geçişi hiç çalışmaz; çok kart olan
 * ekranlarda göz yormamak için yalnızca seçilen kartlarda açın.
 */
@Composable
fun ImageCard(
    imageUrl: String,
    imageId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    phase: Int = 0,
    shine: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = SerinTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPress",
    )
    // 1f: parıltı kartın dışında, görünmez. Süpürme aralıklarla sürekli tekrarlanır.
    val sweep = remember { Animatable(1f) }
    LaunchedEffect(shine) {
        if (!shine) return@LaunchedEffect
        delay(SweepStagger * (phase % SweepPhases))
        while (true) {
            sweep.snapTo(0f)
            sweep.animateTo(1f, tween(SweepMillis, easing = FastOutSlowInEasing))
            delay(SweepPauseMillis)
        }
    }
    Box(
        modifier
            // Ölçek gölgeden önce uygulanır; kart küçülürken gölgesi de onunla küçülür.
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .serinShadow()
            .clip(CardShape)
            .background(colors.surface)
            .border(CardBorderWidth, colors.outline, CardShape)
            .clickable(interactionSource = interaction, indication = LocalIndication.current, onClick = onClick),
    ) {
        RemoteImage(
            url = imageUrl,
            fallback = bundledImage(imageId),
            alpha = 0.25f,
            modifier = Modifier.matchParentSize(),
        )
        Box(Modifier.matchParentSize().background(colors.overlay))
        // Alt kenara doğru koyulaşan geçiş; kart zeminden ayrışır, derinlik kazanır.
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0.55f to Color.Transparent,
                    1f to BottomShade.copy(alpha = if (colors.isDark) 0.55f else 0.35f),
                ),
            ),
        )
        Box(Modifier.matchParentSize().drawBehind { drawSheen(sweep.value) })
        content()
    }
}

/** Alt geçişin rengi: saf siyah değil, temanın koyu çivit zemini. */
private val BottomShade = Color(0xFF15131F)

private const val SweepMillis = 1400
private const val SweepPauseMillis = 3200L
private const val SweepStagger = 450L

/** Başlangıç gecikmesinin tekrar ettiği kart sayısı. */
private const val SweepPhases = 6
private const val PressedScale = 0.96f

/** Soldan sağa, hafif eğik geçen parlak bant; [progress] 0'da kartın solunda, 1'de sağında. */
private fun DrawScope.drawSheen(progress: Float) {
    if (progress >= 1f) return
    val band = size.width * 0.9f
    val x = -band + (size.width + band) * progress
    drawRect(
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to Color.Transparent,
                0.45f to Color.White.copy(alpha = 0.28f),
                0.6f to Color.White.copy(alpha = 0.08f),
                1f to Color.Transparent,
            ),
            start = Offset(x, 0f),
            end = Offset(x + band, band * 0.4f),
        ),
    )
}
