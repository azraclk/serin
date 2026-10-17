package app.azracelik.serin.ui.meditation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.delay

/** Dinleme sayfasına bu kadar süre dokunulmazsa arayüz soluklaşır. */
private const val QuietIdleMillis = 8_000L

/** Arayüzün solma ve geri gelme süresi. */
const val QuietFadeMillis = 1_200

/**
 * Dinlerken gözü yormamak için: sayfaya bir süre dokunulmazsa başlık, düğmeler ve alt menü soluklaşır,
 * geriye küre, ilerleme halkası ve silik bir süre kalır. Dokunuş her şeyi geri getirir.
 */
class QuietMode {
    var isQuiet by mutableStateOf(false)
        private set

    /** Her dokunuşta artar; bekleme sayacı buna bakıp baştan başlar. */
    var touches by mutableIntStateOf(0)
        private set

    fun touch() {
        touches++
        isQuiet = false
    }

    internal fun fall() {
        isQuiet = true
    }

    internal fun reset() {
        isQuiet = false
    }
}

/**
 * [enabled] iken dokunulmadan [QuietIdleMillis] geçerse [quiet]'ı sessize alır. Sayfadan çıkılınca,
 * duraklatılınca ya da [enabled] kapanınca arayüz hemen geri gelir.
 */
@Composable
fun QuietEffect(quiet: QuietMode, enabled: Boolean) {
    LaunchedEffect(enabled, quiet.touches) {
        quiet.reset()
        if (enabled) {
            delay(QuietIdleMillis)
            quiet.fall()
        }
    }
}

/** Dokunuşlar tüketilmeden izlenir; yalnızca sessiz modun bekleme sayacını sıfırlar. */
fun Modifier.trackQuietTouches(quiet: QuietMode): Modifier = pointerInput(quiet) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type == PointerEventType.Press) quiet.touch()
        }
    }
}

/**
 * Soluk arayüze yanlışlıkla basılmasın: sessizken ilk dokunuş yalnızca arayüzü geri getirir. İçeriğin
 * üstüne, [trackQuietTouches] ile izlenen aynı kapsayıcıya konur.
 */
@Composable
fun QuietShield(quiet: QuietMode) {
    if (quiet.isQuiet) {
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } })
    }
}

/**
 * Sessiz mod yalnızca animasyonlar açıkken ve ekran okuyucu kullanılmıyorsa çalışır: animasyonları
 * kapatanlar arayüzün kendiliğinden kaybolmasını istemez, ekran okuyucuyla gezenler de neyin nerede
 * olduğunu görmeden dokunamaz.
 */
@Composable
fun rememberQuietAllowed(): Boolean {
    val animations = rememberAnimationsEnabled()
    val manager = LocalContext.current.getSystemService(AccessibilityManager::class.java)
    return animations && manager?.isTouchExplorationEnabled != true
}
