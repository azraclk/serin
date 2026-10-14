package app.azracelik.serin.ui.meditation

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.BreathPhase
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Nefes gibi yavaş başlayıp yavaş biten geçiş. */
private val BreathEasing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

/**
 * Seansın nefes durumu: küre [breath]'i, ritim şeridi de [step] ile [stepProgress]'i okur. Durumu
 * [rememberBreathingState] sürer; ikisi de aynı kareyi gösterir.
 */
@Stable
class BreathingState {
    /** 0 nefes verilmiş, 1 nefes alınmış. */
    val breath = Animatable(0f)

    /** Şu anki evrenin ritimdeki sırası; seans çalmıyorsa null. */
    var step by mutableStateOf<Int?>(null)
        private set

    /** Şu anki evrenin ne kadarının geçtiği, 0..1. */
    val stepProgress = Animatable(0f)

    /** [pattern]'i çalışma iptal edilene kadar tekrarlar: alırken büyür, tutarken kalır, verirken küçülür. */
    internal suspend fun run(pattern: BreathingPattern) {
        while (true) {
            pattern.steps.forEachIndexed { index, breathStep ->
                step = index
                stepProgress.snapTo(0f)
                val millis = breathStep.seconds * 1000
                coroutineScope {
                    launch { stepProgress.animateTo(1f, tween(millis, easing = LinearEasing)) }
                    when (breathStep.phase) {
                        BreathPhase.Inhale -> breath.animateTo(1f, tween(millis, easing = BreathEasing))
                        BreathPhase.Exhale -> breath.animateTo(0f, tween(millis, easing = BreathEasing))
                        BreathPhase.HoldFull, BreathPhase.HoldEmpty -> delay(millis.toLong())
                    }
                }
            }
        }
    }

    /** Duraklatılınca yavaşça dinlenme hâline döner. */
    internal suspend fun rest() {
        step = null
        breath.animateTo(0f, tween(1200, easing = BreathEasing))
    }
}

/** [isPlaying] iken [pattern]'in ritminde ilerleyen nefes durumu. */
@Composable
fun rememberBreathingState(isPlaying: Boolean, pattern: BreathingPattern): BreathingState {
    val state = remember { BreathingState() }
    val animationsEnabled = rememberAnimationsEnabled()
    LaunchedEffect(isPlaying, animationsEnabled, pattern) {
        if (isPlaying && animationsEnabled) state.run(pattern) else state.rest()
    }
    return state
}

/**
 * [content] çalarken [state]'in ritminde hafifçe büyüyüp küçülür ve arkasında mor bir ışık yayılır:
 * nefes alırken büyür, tutarken olduğu yerde kalır, verirken küçülür. Evrelerin adı kürenin
 * üstüne yazılmaz; resim ne olursa olsun okunsun diye [BreathingRhythm]'de ayrı durur.
 */
@Composable
fun BreathingGlow(
    isPlaying: Boolean,
    size: Dp,
    state: BreathingState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val glow = SerinTheme.colors.accent
    // Işığın görünürlüğü: çalarken belirir, duraklatınca tamamen söner.
    val presence = remember { Animatable(0f) }
    val animationsEnabled = rememberAnimationsEnabled()
    LaunchedEffect(isPlaying, animationsEnabled) {
        presence.animateTo(if (isPlaying && animationsEnabled) 1f else 0f, tween(1500, easing = BreathEasing))
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.requiredSize(size)) {
        // Görselden taşan, kenarlara doğru kaybolan ışık.
        Box(
            Modifier
                .requiredSize(size * 1.6f)
                .graphicsLayer {
                    val value = state.breath.value
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
                scaleX = 1f + 0.025f * state.breath.value
                scaleY = scaleX
            },
        ) {
            content()
        }
    }
}

/** Ritim şeridinin en çok genişliği; [BreathingRhythm] bundan geniş olmaz. */
private val RhythmMaxWidth = 240.dp

/**
 * Kürenin altında evrenin adı ("Nefes al", "Tut", "Nefes ver") ve altında bütün ritim durur: her evre
 * süresiyle orantılı bir parça, içinde olduğumuz parça dolarak ilerler. Yazı düz zeminde olduğu için
 * resmin rengi ne olursa olsun okunur. Evrenin yeri sabit; değişirken yerleşim kaymaz.
 */
@Composable
fun BreathingRhythm(state: BreathingState, pattern: BreathingPattern, modifier: Modifier = Modifier) {
    val colors = SerinTheme.colors
    val steps = pattern.steps
    val current = state.step
    val summary = steps.map { "${stringResource(it.phase.cue)} ${stringResource(R.string.breath_seconds, it.seconds)}" }
        .joinToString(", ")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = RhythmMaxWidth)
            .fillMaxWidth(),
    ) {
        // Ekran okuyucu evre değişince yeni kelimeyi okur.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(30.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            // Önce eski kelime kaybolur, sonra yenisi belirir; ikisi hiçbir zaman üst üste binmez.
            AnimatedContent(
                targetState = current?.let { steps.getOrNull(it)?.phase?.cue },
                transitionSpec = { fadeIn(tween(300, delayMillis = 120)) togetherWith fadeOut(tween(120)) },
                label = "breathCue",
            ) { cue ->
                if (cue != null) Text(stringResource(cue), style = SerinType.BreathCue.copy(color = colors.text))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = summary },
        ) {
            steps.forEachIndexed { index, step ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(step.seconds.toFloat())) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .drawBehind {
                                val fraction = when {
                                    current == null || index > current -> 0f
                                    index < current -> 1f
                                    else -> state.stepProgress.value
                                }
                                drawRect(colors.accent, size = Size(size.width * fraction, size.height))
                            },
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.breath_seconds, step.seconds),
                        style = SerinType.SessionTime.copy(
                            color = if (index == current) colors.text else colors.textMuted,
                        ),
                    )
                }
            }
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
