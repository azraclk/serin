package app.azracelik.serin.ui.meditation

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.ui.theme.SerinPurple

/** Nefes alma ve verme süresi; tam bir nefes 8 saniye. */
private const val BreathHalfMillis = 4000

/** Nefes gibi yavaş başlayıp yavaş biten geçiş. */
private val BreathEasing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/**
 * [content] çalarken nefes ritminde hafifçe büyüyüp küçülür ve arkasında mor bir ışık yayılır.
 * Duraklatılınca bulunduğu yerden yavaşça dinlenme hâline döner.
 */
@Composable
fun BreathingGlow(
    isPlaying: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val breath = remember { Animatable(0f) }
    // Işığın görünürlüğü: çalarken belirir, duraklatınca tamamen söner.
    val presence = remember { Animatable(0f) }
    val animationsEnabled = rememberAnimationsEnabled()
    LaunchedEffect(isPlaying, animationsEnabled) {
        presence.animateTo(if (isPlaying && animationsEnabled) 1f else 0f, tween(1500, easing = BreathEasing))
    }
    LaunchedEffect(isPlaying, animationsEnabled) {
        if (isPlaying && animationsEnabled) {
            while (true) {
                breath.animateTo(1f, tween(BreathHalfMillis, easing = BreathEasing))
                breath.animateTo(0f, tween(BreathHalfMillis, easing = BreathEasing))
            }
        } else {
            breath.animateTo(0f, tween(1200, easing = BreathEasing))
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.requiredSize(size)) {
        // Görselden taşan, kenarlara doğru kaybolan ışık.
        Box(
            Modifier
                .requiredSize(size * 1.6f)
                .graphicsLayer {
                    val value = breath.value
                    scaleX = 1f + 0.1f * value
                    scaleY = scaleX
                    alpha = presence.value * (0.35f + 0.65f * value)
                }
                .background(
                    // Görselin kenarına kadar dolu, dışarı doğru kaybolur.
                    Brush.radialGradient(
                        0.55f to SerinPurple.copy(alpha = 0.6f),
                        0.75f to SerinPurple.copy(alpha = 0.25f),
                        1f to Color.Transparent,
                    ),
                ),
        )
        Box(
            Modifier.graphicsLayer {
                scaleX = 1f + 0.025f * breath.value
                scaleY = scaleX
            },
        ) {
            content()
        }
    }
}

/** Telefonun erişilebilirlik ayarlarında animasyonlar kapatıldıysa false döner. */
@Composable
private fun rememberAnimationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}
