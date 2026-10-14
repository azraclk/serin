package app.azracelik.serin.ui.meditation

import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.azracelik.serin.data.BreathPhase
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.ui.theme.SerinType
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.ui.theme.SerinTheme

/** Nefes gibi yavaş başlayıp yavaş biten geçiş. */
private val BreathEasing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/**
 * [content] çalarken [pattern]'in ritminde hafifçe büyüyüp küçülür ve arkasında mor bir ışık yayılır:
 * nefes alırken büyür, tutarken olduğu yerde kalır, verirken küçülür. [showCue] açıksa evrenin adı
 * ("Nefes al", "Tut", "Nefes ver") kürenin üstünde yazılır. Duraklatılınca yavaşça dinlenme hâline döner.
 */
@Composable
fun BreathingGlow(
    isPlaying: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    pattern: BreathingPattern = BreathingPattern.Calm,
    showCue: Boolean = false,
    content: @Composable () -> Unit,
) {
    val glow = SerinTheme.colors.accent
    val breath = remember { Animatable(0f) }
    // Işığın görünürlüğü: çalarken belirir, duraklatınca tamamen söner.
    val presence = remember { Animatable(0f) }
    val animationsEnabled = rememberAnimationsEnabled()
    LaunchedEffect(isPlaying, animationsEnabled) {
        presence.animateTo(if (isPlaying && animationsEnabled) 1f else 0f, tween(1500, easing = BreathEasing))
    }
    var phase by remember { mutableStateOf<BreathPhase?>(null) }
    LaunchedEffect(isPlaying, animationsEnabled, pattern) {
        if (isPlaying && animationsEnabled) {
            while (true) {
                for (step in pattern.steps) {
                    phase = step.phase
                    val millis = step.seconds * 1000
                    when (step.phase) {
                        BreathPhase.Inhale -> breath.animateTo(1f, tween(millis, easing = BreathEasing))
                        BreathPhase.Exhale -> breath.animateTo(0f, tween(millis, easing = BreathEasing))
                        BreathPhase.HoldFull, BreathPhase.HoldEmpty -> delay(millis.toLong())
                    }
                }
            }
        } else {
            phase = null
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
                        0.55f to glow.copy(alpha = 0.6f),
                        0.75f to glow.copy(alpha = 0.25f),
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
        if (showCue) {
            // Evre değişirken yazı yumuşakça yer değiştirir; duraklatılınca kaybolur.
            Crossfade(targetState = phase?.cue, animationSpec = tween(600), label = "breathCue") { cue ->
                if (cue != null) BreathCue(stringResource(cue))
            }
        }
    }
}

/** Kürenin üstünde, görselin rengi ne olursa olsun okunsun diye koyu bir zeminde durur. */
@Composable
private fun BreathCue(text: String) {
    Text(
        text = text,
        style = SerinType.PickTitle.copy(textAlign = TextAlign.Center),
        modifier = Modifier
            .clip(CircleShape)
            .background(SerinTheme.colors.background.copy(alpha = 0.7f))
            .padding(horizontal = 20.dp, vertical = 8.dp),
    )
}

/** Telefonun erişilebilirlik ayarlarında animasyonlar kapatıldıysa false döner. */
@Composable
private fun rememberAnimationsEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}
