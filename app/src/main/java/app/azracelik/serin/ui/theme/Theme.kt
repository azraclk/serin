package app.azracelik.serin.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Varsayılan tema koyudur; gece teması [DarkColors]. */
@Composable
fun SerinTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        background = colors.background,
        onBackground = colors.text,
        surface = colors.surface,
        onSurface = colors.text,
    )
    CompositionLocalProvider(LocalSerinColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = Typography) {
            // Rengi belirtilmeyen tüm metinler temanın metin rengini alır.
            CompositionLocalProvider(LocalContentColor provides colors.text, content = content)
        }
    }
}

object SerinTheme {
    val colors: SerinColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSerinColors.current
}
