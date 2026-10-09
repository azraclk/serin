package app.azracelik.serin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.azracelik.serin.ui.adaptive.HeightClass
import app.azracelik.serin.ui.adaptive.LocalWindowSize
import app.azracelik.serin.ui.adaptive.WidthClass
import app.azracelik.serin.ui.adaptive.WindowSize
import app.azracelik.serin.ui.theme.SerinTheme

/**
 * Ekran önizlemelerinin dört tipik pencere boyutu: telefon dikey ve yatay, tablet dikey ve yatay.
 * Ekranlar pencere boyutunu [PreviewWindow]'dan okuduğu için `@PreviewScreenSizes` yerine bu kullanılır.
 */
@Preview(name = "Phone", widthDp = 393, heightDp = 852)
@Preview(name = "Phone landscape", widthDp = 914, heightDp = 411)
@Preview(name = "Tablet portrait", widthDp = 800, heightDp = 1280)
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
annotation class AdaptivePreviews

/**
 * Önizleme penceresi: temayı, uygulamadaki gibi hesaplanan [WindowSize]'ı ve boyuta uygun menüyü
 * (alt menü ya da yan menü) sağlar; [content] menünün yanında kalan alana çizilir.
 */
@Composable
fun PreviewWindow(selected: SerinTab, content: @Composable () -> Unit) {
    SerinTheme {
        BoxWithConstraints(Modifier.fillMaxSize().background(SerinTheme.colors.background)) {
            val width = WidthClass.of(maxWidth)
            val height = HeightClass.of(maxHeight)
            val useRail = WindowSize.usesRail(width, height)
            val railInset = if (useRail) RailInset else 0.dp
            val windowSize = WindowSize(width, height, contentWidth = maxWidth - railInset, windowHeight = maxHeight)
            CompositionLocalProvider(LocalWindowSize provides windowSize) {
                Box(Modifier.fillMaxSize().padding(start = railInset)) { content() }
                if (useRail) {
                    SerinNavigationRail(selected, onSelect = {}, Modifier.align(Alignment.CenterStart))
                } else {
                    SerinBottomBar(selected, onSelect = {}, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}
