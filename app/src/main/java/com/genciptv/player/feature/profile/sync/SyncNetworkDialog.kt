package com.genciptv.player.feature.profile.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary

/**
 * Which connections the daily catalogue refresh may use.
 *
 * A picker rather than a switch. "Wi-Fi only: off" states what is not
 * happening and leaves the reader to infer the rest — plausibly "it never
 * refreshes at all", which is the wrong inference and the expensive one to act
 * on. Two named options say what will happen either way, and let the settings
 * row show the current answer instead of a switch position.
 *
 * Deliberately no third "never" option: turning refreshes off entirely freezes
 * the catalogue, and months later that reads as the app being broken rather
 * than as a setting someone once chose. Pull-to-refresh covers anyone who
 * wants to control it by hand.
 */
@Composable
fun SyncNetworkDialog(
    wifiOnly: Boolean,
    onSelect: (wifiOnly: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElev,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.profile_sync_network_label),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                ),
            )
        },
        text = {
            Column {
                Option(
                    label = stringResource(R.string.profile_sync_option_any),
                    selected = !wifiOnly,
                    accent = accent,
                    onClick = { onSelect(false); onDismiss() },
                )
                Option(
                    label = stringResource(R.string.profile_sync_option_wifi),
                    selected = wifiOnly,
                    accent = accent,
                    onClick = { onSelect(true); onDismiss() },
                )
                Spacer(Modifier.height(10.dp))
                // The cost, stated once, where the choice is made — not as a
                // permanent caption under the settings row.
                Text(
                    text = stringResource(R.string.profile_sync_note),
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextSecondary,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_close),
                    style = TextStyle(fontFamily = GeistFamily, color = accent),
                )
            }
        },
    )
}

@Composable
private fun Option(
    label: String,
    selected: Boolean,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
