package app.azracelik.serin.ui.reminder

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.azracelik.serin.R
import app.azracelik.serin.reminder.LocalReminder
import app.azracelik.serin.reminder.ReminderNotifier
import app.azracelik.serin.reminder.ReminderPreference
import app.azracelik.serin.reminder.ReminderSettings
import app.azracelik.serin.ui.components.CardShape
import app.azracelik.serin.ui.theme.SerinTheme
import app.azracelik.serin.ui.theme.SerinType

/**
 * Üst çubuktaki zil: hatırlatıcı açıksa dolu görünür. Dokununca [ReminderDialog] açılır.
 * Hatırlatıcı kaynağı verilmediyse (splash gibi) hiçbir şey çizmez.
 */
@Composable
fun ReminderButton(modifier: Modifier = Modifier) {
    val reminder = LocalReminder.current ?: return
    val colors = SerinTheme.colors
    var showDialog by remember { mutableStateOf(false) }
    val enabled = reminder.settings.enabled
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = { showDialog = true }),
    ) {
        Image(
            painter = painterResource(if (enabled) R.drawable.ic_bell_filled else R.drawable.ic_bell),
            contentDescription = stringResource(R.string.reminder_button),
            colorFilter = ColorFilter.tint(if (enabled) colors.accent else colors.text),
            modifier = Modifier.size(26.dp),
        )
    }
    if (showDialog) {
        ReminderDialog(reminder, onDismiss = { showDialog = false })
    }
}

/** Hatırlatıcıyı açıp kapatır ve saatini seçtirir; kaydedilene kadar ayar değişmez. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDialog(reminder: ReminderPreference, onDismiss: () -> Unit) {
    val colors = SerinTheme.colors
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(reminder.settings.enabled) }
    var permissionDenied by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        initialHour = reminder.settings.hour,
        initialMinute = reminder.settings.minute,
        is24Hour = true,
    )
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        enabled = granted
        permissionDenied = !granted
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.text,
        textContentColor = colors.text,
        shape = CardShape,
        title = { Text(stringResource(R.string.reminder_title), style = SerinType.PickTitle) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.reminder_switch), style = SerinType.Caption.copy(color = colors.text))
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            permissionDenied = false
                            if (on && ReminderNotifier.needsPermission(context)) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                enabled = on
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textMuted,
                            uncheckedTrackColor = colors.background,
                            uncheckedBorderColor = colors.outline,
                        ),
                    )
                }
                if (permissionDenied) {
                    Text(
                        stringResource(R.string.reminder_denied),
                        style = SerinType.Caption.copy(color = colors.textMuted),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (enabled) {
                    TimePicker(
                        state = timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = colors.background,
                            selectorColor = colors.accent,
                            clockDialSelectedContentColor = colors.onAccent,
                            clockDialUnselectedContentColor = colors.text,
                            timeSelectorSelectedContainerColor = colors.accent,
                            timeSelectorUnselectedContainerColor = colors.background,
                            timeSelectorSelectedContentColor = colors.onAccent,
                            timeSelectorUnselectedContentColor = colors.text,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                reminder.update(ReminderSettings(enabled, timeState.hour, timeState.minute))
                onDismiss()
            }) { Text(stringResource(R.string.reminder_save), color = colors.accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.reminder_cancel), color = colors.textMuted) }
        },
    )
}
