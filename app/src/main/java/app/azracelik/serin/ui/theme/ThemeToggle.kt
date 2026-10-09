package app.azracelik.serin.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

private const val PrefsName = "serin_theme"
private const val KeyDark = "dark"

/** Kullanıcının seçtiği tema; seçim yapılmadıysa [override] null olur ve varsayılan koyu tema kullanılır. */
class ThemePreference(context: Context) {
    private val prefs = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)

    var override: Boolean? by mutableStateOf(if (prefs.contains(KeyDark)) prefs.getBoolean(KeyDark, false) else null)
        private set

    fun setDark(dark: Boolean) {
        override = dark
        prefs.edit().putBoolean(KeyDark, dark).apply()
    }
}

/** Üst çubuktaki gece/gündüz düğmesinin durumu ve tıklaması. */
class ThemeToggle(val isDark: Boolean, val onToggle: () -> Unit)

val LocalThemeToggle = staticCompositionLocalOf<ThemeToggle?> { null }
