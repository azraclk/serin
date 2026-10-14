package app.azracelik.serin.data

import android.content.Context
import androidx.annotation.StringRes
import app.azracelik.serin.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Nefes meditasyonunun id'si; ritim seçici yalnızca bu meditasyonda gösterilir. */
const val BreathingMeditationId = "breathing"

/** Nefesin evreleri: önce alınır, tutulur, verilir, boş bırakılır. */
enum class BreathPhase(@StringRes val cue: Int) {
    Inhale(R.string.breath_inhale),
    HoldFull(R.string.breath_hold),
    Exhale(R.string.breath_exhale),
    HoldEmpty(R.string.breath_hold),
}

class BreathStep(val phase: BreathPhase, val seconds: Int)

/** Nefes ritmi; [steps] sırayla tekrarlanır. */
enum class BreathingPattern(@StringRes val label: Int, val steps: List<BreathStep>) {
    /** Varsayılan: tam nefes 8 saniye. */
    Calm(
        R.string.breath_calm,
        listOf(BreathStep(BreathPhase.Inhale, 4), BreathStep(BreathPhase.Exhale, 4)),
    ),

    /** Uykuya dalmaya yardım eden 4-7-8 nefesi. */
    FourSevenEight(
        R.string.breath_478,
        listOf(BreathStep(BreathPhase.Inhale, 4), BreathStep(BreathPhase.HoldFull, 7), BreathStep(BreathPhase.Exhale, 8)),
    ),

    /** Kutu nefesi: dört evre de dörder saniye. */
    Box(
        R.string.breath_box,
        listOf(
            BreathStep(BreathPhase.Inhale, 4),
            BreathStep(BreathPhase.HoldFull, 4),
            BreathStep(BreathPhase.Exhale, 4),
            BreathStep(BreathPhase.HoldEmpty, 4),
        ),
    );

    val cycleSeconds: Int get() = steps.sumOf { it.seconds }

    companion object {
        /** Kaydedilmiş ad tanınmıyorsa (örneğin sürüm değişince) varsayılan ritim. */
        fun fromName(name: String?): BreathingPattern = entries.find { it.name == name } ?: Calm
    }
}

private const val PrefsName = "serin_library"
private const val PatternKey = "breathing_pattern"

/** Kullanıcının seçtiği nefes ritmi. */
class BreathingPatternStore(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    private val _pattern = MutableStateFlow(BreathingPattern.fromName(prefs.getString(PatternKey, null)))
    val pattern: StateFlow<BreathingPattern> = _pattern.asStateFlow()

    fun select(pattern: BreathingPattern) {
        _pattern.value = pattern
        prefs.edit().putString(PatternKey, pattern.name).apply()
    }
}
