package app.azracelik.serin.ui.meditation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toSize
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.ui.components.RemoteImage
import kotlin.math.roundToInt

/** Küre, seçim sayfasından dinleme sayfasındaki yerine bu sürede uçar. */
private const val FlightMillis = 650

/** Sayfa, küre yolun bir kısmını aldıktan sonra belirmeye başlar. */
const val FlightPageDelayMillis = 250

/**
 * Seçim sayfasındaki kürenin dinleme sayfasındaki küreye dönüşmesi. Seçim sayfası ayrı bir pencerede
 * olduğu için ortak öğe geçişi kullanılamaz; bunun yerine küre ekranın üstünde çizilir ve konumlar
 * ekran koordinatlarında verilir.
 */
class OrbFlight(val meditation: Meditation, val from: Rect)

/** Ekran koordinatlarında kapladığı alanı bildirir. Konum değiştikçe yeniden bildirir. */
fun Modifier.reportScreenBounds(onBounds: (Rect) -> Unit): Modifier = onGloballyPositioned {
    onBounds(Rect(it.positionOnScreen(), it.size.toSize()))
}

/**
 * [flight]'ın kürenin görselini [target]'taki yerine taşır. [target] dinleme sayfası yerleşince belli olur;
 * o zamana kadar küre başladığı yerde bekler. Varınca [onLanded] çağrılır ve görsel kaldırılmalıdır.
 */
@Composable
fun OrbFlightOverlay(
    flight: OrbFlight,
    target: () -> Rect?,
    onLanded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    val progress = remember(flight) { Animatable(0f) }
    val destination = target()
    LaunchedEffect(flight, destination != null) {
        if (destination != null) {
            progress.animateTo(1f, tween(FlightMillis, easing = FastOutSlowInEasing))
            onLanded()
        }
    }
    val rect = lerp(flight.from, destination ?: flight.from, progress.value)
    val density = LocalDensity.current
    Box(modifier.fillMaxSize().onGloballyPositioned { origin = it.positionOnScreen() }) {
        RemoteImage(
            url = flight.meditation.image,
            fallback = bundledImage(flight.meditation.id),
            modifier = Modifier
                .offset { IntOffset((rect.left - origin.x).roundToInt(), (rect.top - origin.y).roundToInt()) }
                .size(with(density) { rect.width.toDp() }, with(density) { rect.height.toDp() })
                .clip(CircleShape),
        )
    }
}
