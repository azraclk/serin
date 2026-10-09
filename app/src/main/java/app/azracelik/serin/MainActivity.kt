package app.azracelik.serin

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import app.azracelik.serin.ui.theme.LocalThemeToggle
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.ThemePreference
import app.azracelik.serin.ui.theme.ThemeToggle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreference = ThemePreference(applicationContext)
        setContent {
            val dark = themePreference.override ?: isSystemInDarkTheme()
            // Sistem çubuklarının ikonları temaya göre koyu (açık tema) ya da açık (gece teması) olur.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }
            SerinTheme(darkTheme = dark) {
                CompositionLocalProvider(
                    LocalThemeToggle provides ThemeToggle(dark) { themePreference.setDark(!dark) },
                ) {
                    SerinApp()
                }
            }
        }
    }
}
