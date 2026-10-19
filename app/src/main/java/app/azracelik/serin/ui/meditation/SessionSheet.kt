package app.azracelik.serin.ui.meditation

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.data.BreathingPattern
import app.azracelik.serin.data.Meditation
import app.azracelik.serin.data.bundledImage
import app.azracelik.serin.playback.MeditationSession
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.components.RemoteImage
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType
import kotlinx.coroutines.delay
import java.util.Date

private const val ClockRefreshMillis = 30_000L

/** Üstteki küre; Başla'ya basınca dinleme sayfasındaki küreye dönüşür. */
private val SheetOrbSize = 72.dp

/** Seçilen ayın dolması ve halesinin açılması bu kadar sürer. */
private const val MoonGlowMillis = 700

/** Seçilen ay, kendi evresinden dolunaya giden yolun bu kadarını doldurur. */
private const val SelectedFill = 0.4f

/** Hale yarıçapı ay çapının bu katıdır; en koyu yeri [GlowAlpha] kadar görünür. */
private const val GlowRadiusFactor = 0.9f
private const val GlowAlpha = 0.35f

/** [SessionSheet]'teki Başla düğmesinin sınama etiketi; ana sayfada da "Başla" yazan bir düğme var. */
const val SessionStartTag = "sessionStart"

/**
 * Meditasyona girmeden önce açılan seçim sayfası: süre (ve nefes meditasyonunda ritim) burada seçilir,
 * dinleme sayfası yalnızca dinlemeye kalır. Seçilen süre ve üstteki kürenin ekran koordinatlarındaki yeri
 * [onStart] ile verilir; küre dinleme sayfasındaki yerine uçar. Başla'dan sonra sayfa [SheetDissolveMillis]
 * boyunca erir, küre yerinde kalır, sonra [onDismiss] çağrılır. Vazgeçilirse önceki süre korunur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSheet(
    meditation: Meditation,
    initialMinutes: Int,
    onStart: (minutes: Int, orbOnScreen: Rect?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    /** Yalnızca nefes meditasyonunda verilir: seçili ritim ve seçici. */
    breathing: BreathingChoice? = null,
) {
    val colors = SerinTheme.colors
    var minutes by remember { mutableIntStateOf(initialMinutes) }
    var orbBounds by remember { mutableStateOf<Rect?>(null) }
    // Sayfa uzun süre açık kalırsa bitiş saati kaymasın diye yarım dakikada bir tazelenir.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(ClockRefreshMillis)
            now = System.currentTimeMillis()
        }
    }
    // Başla'ya basılınca sayfa anında kaybolmaz: küre dinleme sayfasına uçmaya hazırlanırken geri kalanı erir.
    var leaving by remember { mutableStateOf(false) }
    val dissolve by animateFloatAsState(if (leaving) 0f else 1f, tween(SheetDissolveMillis), label = "sheetDissolve")
    LaunchedEffect(leaving) {
        if (leaving) {
            delay(SheetDissolveMillis.toLong())
            onDismiss()
        }
    }
    ModalBottomSheet(
        onDismissRequest = { if (!leaving) onDismiss() },
        modifier = modifier,
        // Kısa ekranda (yatay telefon) içerik kayar; sayfa yarım açık takılıp kalmasın.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface.copy(alpha = dissolve),
        contentColor = colors.text,
        scrimColor = BottomSheetDefaults.ScrimColor.let { it.copy(alpha = it.alpha * dissolve) },
        dragHandle = { BottomSheetDefaults.DragHandle(Modifier.graphicsLayer { alpha = dissolve }) },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        ) {
            RemoteImage(
                url = meditation.image,
                fallback = bundledImage(meditation.id),
                modifier = Modifier
                    .size(SheetOrbSize)
                    .reportScreenBounds { orbBounds = it }
                    .clip(CircleShape),
            )
            Spacer(Modifier.height(16.dp))
            // Küre kalır, geri kalan her şey Başla'ya basılınca erir.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = dissolve },
            ) {
                Text(text = meditation.title, style = SerinType.DetailTitle, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.session_prompt),
                    style = SerinType.Caption.copy(color = colors.textMuted),
                )
                Spacer(Modifier.height(12.dp))
                SessionLengthPicker(selected = minutes, onSelect = { minutes = it }, moonSize = 28.dp, shade = colors.surface)
                Spacer(Modifier.height(12.dp))
                // Seçimin ne anlama geldiğini söyler; telefonun 12/24 saat ayarına uyar.
                val endsAt = DateFormat.getTimeFormat(LocalContext.current).format(Date(now + minutes * 60_000L))
                Text(
                    text = stringResource(R.string.session_ends_at, endsAt),
                    style = SerinType.Caption.copy(color = colors.textMuted),
                )
                if (breathing != null) {
                    Spacer(Modifier.height(16.dp))
                    BreathingPatternPicker(breathing)
                }
                Spacer(Modifier.height(24.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable(role = Role.Button, enabled = !leaving, onClick = {
                            leaving = true
                            onStart(minutes, orbBounds)
                        })
                        .testTag(SessionStartTag),
                ) {
                    Text(stringResource(R.string.home_start), style = SerinType.SessionChip.copy(color = colors.onAccent))
                }
            }
        }
    }
}

/** Nefes meditasyonunda seçili ritim, titreşim tercihi ve bunları değiştiren işlevler. */
class BreathingChoice(
    val selected: BreathingPattern,
    val onSelect: (BreathingPattern) -> Unit,
    val haptics: Boolean,
    val onHapticsChange: (Boolean) -> Unit,
)

/**
 * Nefes ritmi seçimi; seans süresi seçicisiyle aynı dilde, yalnızca yazıdan oluşur. Telefon titreşebiliyorsa
 * ritimlerin yanında bir de titreşim anahtarı vardır; ritimlerden biri seçilir, titreşim ise açılıp kapanır.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BreathingPatternPicker(choice: BreathingChoice) {
    val colors = SerinTheme.colors
    val canVibrate = rememberCanVibrate()
    // Büyük yazı ayarında chip'ler alt satıra geçer.
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BreathingPattern.entries.forEach { pattern ->
            val isSelected = pattern == choice.selected
            Text(
                text = stringResource(pattern.label),
                style = SerinType.SessionChip.copy(color = if (isSelected) colors.onAccent else colors.textMuted),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) colors.accent else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { choice.onSelect(pattern) })
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        if (canVibrate) {
            Text(
                text = stringResource(R.string.breath_haptics),
                style = SerinType.SessionChip.copy(color = if (choice.haptics) colors.onAccent else colors.textMuted),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (choice.haptics) colors.accent else Color.Transparent)
                    .toggleable(value = choice.haptics, role = Role.Switch, onValueChange = choice.onHapticsChange)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
}

/** Seans süresi seçimi: ay evreleri; süre uzadıkça ay dolar, en uzunu dolunaydır. */
@Composable
private fun SessionLengthPicker(
    selected: Int,
    onSelect: (Int) -> Unit,
    moonSize: Dp,
    /** Ayın gölge kısmının rengi; seçicinin durduğu zeminle aynı olmalı. */
    shade: Color,
) {
    val colors = SerinTheme.colors
    val lengths = MeditationSession.LengthsMinutes
    val haptic = LocalHapticFeedback.current
    // Beş seçenek dar telefonlara (320dp) sığsın diye aralık küçük tutulur.
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        lengths.forEachIndexed { index, minutes ->
            val isSelected = minutes == selected
            // Seçilen ay hafifçe büyüyüp parlar; seçim hem görülür hem hissedilir.
            val tint by animateColorAsState(if (isSelected) colors.accent else colors.textMuted, label = "moonTint")
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.18f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "moonScale",
            )
            // Seçilen ay yavaşça dolar ve çevresinde ışık belirir; seçilmeyenler kendi evresine döner.
            val phase = (index + 1f) / lengths.size
            val lit by animateFloatAsState(
                targetValue = if (isSelected) phase + (1f - phase) * SelectedFill else phase,
                animationSpec = tween(MoonGlowMillis),
                label = "moonLit",
            )
            val glow by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = tween(MoonGlowMillis),
                label = "moonGlow",
            )
            // Hale, ay kutusunun dışına taşar; kırpılan düğme alanının dışında çizilmesi için bir kutuya sarılır.
            Box(
                Modifier.drawBehind {
                    if (glow > 0f) {
                        val center = Offset(size.width / 2, 6.dp.toPx() + moonSize.toPx() / 2)
                        val radius = moonSize.toPx() * GlowRadiusFactor
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(colors.accent.copy(alpha = GlowAlpha * glow), Color.Transparent),
                                center = center,
                                radius = radius,
                            ),
                            radius = radius,
                            center = center,
                        )
                    }
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(CardShape)
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = {
                                if (!isSelected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(minutes)
                            },
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    MoonPhase(
                        lit = lit,
                        color = tint,
                        shade = shade,
                        modifier = Modifier.size(moonSize).graphicsLayer { scaleX = scale; scaleY = scale },
                    )
                    Text(
                        text = stringResource(R.string.session_minutes, minutes),
                        style = SerinType.SessionChip.copy(color = if (isSelected) colors.text else colors.textMuted),
                    )
                }
            }
        }
    }
}

/** [lit] 0..1: aydınlık kısmın oranı. Gölge, [shade] renginde bir daire olarak kayar. */
@Composable
private fun MoonPhase(lit: Float, color: Color, shade: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val r = size.minDimension / 2
        drawCircle(color, r, center)
        if (lit < 1f) {
            drawCircle(shade, r, Offset(center.x - 2 * r * lit, center.y))
            drawCircle(color.copy(alpha = 0.35f), r - 0.5.dp.toPx(), center, style = Stroke(1.dp.toPx()))
        }
    }
}
