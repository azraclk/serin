package app.azracelik.serin.ui.meditation

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.azracelik.serin.data.BreathPhase

/** Bir titreşimin zamanları ve genlikleri; ikisi de [VibrationEffect.createWaveform]'a verilir. */
class HapticWave(
    /** Sırayla sessiz ve titreşimli süreler (ms); sessizle başlar. */
    val timings: LongArray,
    /** [timings] ile aynı sırada genlikler (0-255); sessiz süreler 0. */
    val amplitudes: IntArray,
)

private const val PulseMillis = 25L
private const val SoftestAmplitude = 40
private const val StrongestAmplitude = 150
private const val HoldAmplitude = 90
private const val HoldMillis = 60L

/**
 * [phase]'in [seconds] saniyelik titreşimi. Nefes alırken darbeler sıklaşır ve güçlenir (doluyor gibi),
 * verirken seyrekleşir ve zayıflar (boşalıyor gibi). Tutarken evrenin başında tek bir hafif tık gelir.
 * Boş bekleme sessizdir; titreşimin olmaması da bir işarettir. Titreşim yoksa null döner.
 */
internal fun hapticWave(phase: BreathPhase, seconds: Int): HapticWave? = when (phase) {
    BreathPhase.HoldEmpty -> null
    BreathPhase.HoldFull -> HapticWave(longArrayOf(0, HoldMillis), intArrayOf(0, HoldAmplitude))
    BreathPhase.Inhale -> pulses(seconds, rising = true)
    BreathPhase.Exhale -> pulses(seconds, rising = false)
}

private fun pulses(seconds: Int, rising: Boolean): HapticWave {
    val count = seconds * 2
    // Darbeler arasındaki boşluklar giderek kısalır (alırken) ya da uzar (verirken); darbeler evrenin
    // başından sonuna yayılır. Son darbeden sonra boşluk bırakılmaz, dalga son darbede biter.
    val gapCount = count - 1
    val weights = IntArray(gapCount) { if (rising) gapCount - it else it + 1 }
    val totalWeight = weights.sum()
    val timings = LongArray(count * 2)
    val amplitudes = IntArray(count * 2)
    for (i in 0 until count) {
        timings[i * 2] = if (i == 0) 0 else (seconds * 1000L - PulseMillis) * weights[i - 1] / totalWeight - PulseMillis
        timings[i * 2 + 1] = PulseMillis
        val progress = i / (count - 1f)
        val strength = if (rising) progress else 1f - progress
        amplitudes[i * 2 + 1] = (SoftestAmplitude + (StrongestAmplitude - SoftestAmplitude) * strength).toInt()
    }
    return HapticWave(timings, amplitudes)
}

/** Nefes evrelerini titreşimle belirtir; telefon ekranı açıkken çalışır. */
class BreathHaptics internal constructor(private val vibrator: Vibrator) {
    fun play(phase: BreathPhase, seconds: Int) {
        val wave = hapticWave(phase, seconds) ?: return
        val effect = if (vibrator.hasAmplitudeControl()) {
            VibrationEffect.createWaveform(wave.timings, wave.amplitudes, -1)
        } else {
            // Genliği ayarlanamayan titreşim motorunda yalnızca zamanlar geçerlidir.
            VibrationEffect.createWaveform(wave.timings, -1)
        }
        vibrate(effect)
    }

    fun cancel() = vibrator.cancel()

    // Meditasyona eşlik eden bir medya titreşimi sayılır; telefonun dokunma titreşimi ayarından etkilenmez.
    @Suppress("DEPRECATION")
    private fun vibrate(effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_MEDIA))
        } else {
            vibrator.vibrate(effect, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
        }
    }
}

private fun Context.vibratorOrNull(): Vibrator? {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    return vibrator?.takeIf { it.hasVibrator() }
}

/** Telefonda titreşim motoru var mı; yoksa ayar hiç gösterilmez. */
@Composable
fun rememberCanVibrate(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.vibratorOrNull() != null }
}

/** [enabled] ve telefon titreşebiliyorsa nefes titreşimi, yoksa null. */
@Composable
fun rememberBreathHaptics(enabled: Boolean): BreathHaptics? {
    val context = LocalContext.current
    return remember(context, enabled) {
        if (enabled) context.vibratorOrNull()?.let(::BreathHaptics) else null
    }
}
