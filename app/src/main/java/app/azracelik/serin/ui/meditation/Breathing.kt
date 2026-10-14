package app.azracelik.serin.ui.meditation

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import app.azracelik.serin.R
import app.azracelik.serin.data.BreathPhase
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.ui.components.FitText
import app.azracelik.serin.ui.theme.SerinType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

/** Küre görselinin, kürenin boyutundan ne kadar küçük olduğu: çevresindeki ilerleme halkası için pay. */
internal val OrbImageInset = 36.dp

// Perde ve üstündeki yazı iki temada da aynıdır: görsel hep siyah-beyaz çizim olduğu için tema rengine
// bağlanmaz. Görselin en parlak yerinde bile yazı 6,5:1 kontrastla okunur.
private val VeilColor = Color(0xFF15131F).copy(alpha = 0.78f)
private val VeilText = Color(0xFFF6EFD6)

/**
 * [content] çalarken [pattern]'in ritminde hafifçe büyüyüp küçülür ve arkasında mor bir ışık yayılır:
 * nefes alırken büyür, tutarken olduğu yerde kalır, verirken küçülür. [showCue] açıksa görsel
 * koyulaşır ve evrenin adı ("Nefes al", "Tut", "Nefes ver") ile kalan saniye üstüne yazılır.
 * Duraklatılınca perde kalkar, küre yavaşça dinlenme hâline döner.
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
    var secondsLeft by remember { mutableIntStateOf(0) }
    // Perde evre sürdükçe iner, duraklatınca kalkar.
    val veil by animateFloatAsState(if (showCue && phase != null) 1f else 0f, tween(600), label = "breathVeil")
    LaunchedEffect(isPlaying, animationsEnabled, pattern) {
        if (isPlaying && animationsEnabled) {
            while (true) {
                for (step in pattern.steps) {
                    phase = step.phase
                    val millis = step.seconds * 1000
                    coroutineScope {
                        launch {
                            for (second in step.seconds downTo 1) {
                                secondsLeft = second
                                delay(1000)
                            }
                        }
                        when (step.phase) {
                            BreathPhase.Inhale -> breath.animateTo(1f, tween(millis, easing = BreathEasing))
                            BreathPhase.Exhale -> breath.animateTo(0f, tween(millis, easing = BreathEasing))
                            BreathPhase.HoldFull, BreathPhase.HoldEmpty -> delay(millis.toLong())
                        }
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
            contentAlignment = Alignment.Center,
        ) {
            content()
            // Perde görselle birlikte büyür; yoksa en geniş anında görselin kenarı perdenin dışında kalırdı.
            Box(
                Modifier
                    .requiredSize(size - OrbImageInset)
                    .clip(CircleShape)
                    .graphicsLayer { alpha = veil }
                    .background(VeilColor),
            )
        }
        // Yazı büyüyüp küçülen görselin dışında durur; kıpırdayan yazı okunmaz.
        if (showCue) {
            BreathCue(
                phase = phase,
                secondsLeft = secondsLeft,
                width = size - OrbImageInset - 24.dp,
                modifier = Modifier.graphicsLayer { alpha = veil },
            )
        }
    }
}

/** Perdenin üstündeki yazı: evrenin adı ve kalan saniye. Eski kelime önce kaybolur, yenisi sonra belirir. */
@Composable
private fun BreathCue(phase: BreathPhase?, secondsLeft: Int, width: Dp, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        // Ekran okuyucu evre değişince yeni kelimeyi okur.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(width)
                .height(44.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            AnimatedContent(
                targetState = phase?.cue,
                transitionSpec = { fadeIn(tween(300, delayMillis = 120)) togetherWith fadeOut(tween(120)) },
                label = "breathCue",
            ) { cue ->
                // Küçük kürede ve büyük yazı ayarında kelime kesilmek yerine küçülür.
                if (cue != null) FitText(stringResource(cue), SerinType.BreathCue.copy(color = VeilText))
            }
        }
        Text(
            text = stringResource(R.string.breath_seconds, secondsLeft),
            style = SerinType.BreathSeconds.copy(color = VeilText.copy(alpha = 0.85f)),
        )
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
