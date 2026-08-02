package com.genciptv.player.core.ui

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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary

/**
 * Single row inside a settings group card.
 *
 *   [20dp icon] | [label + optional subtitle] | [trailing: ›, toggle, value…]
 *
 * The icon is a vector drawn in a single colour — no tinted box behind it.
 * Emoji in coloured tiles is what the settings screens used to do, and it read
 * as a template: emoji render differently per device, sit off the text
 * baseline, and carry weights that fight the rest of the type. Five different
 * pastel backgrounds on one screen also meant colour signalled nothing. Here
 * colour is reserved for meaning — [tint] defaults to the accent, and callers
 * override it only where it says something, like a destructive action.
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color? = null,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {
        Text(
            text = "›",
            style = TextStyle(
                fontFamily = GeistFamily,
                fontSize = 18.sp,
                color = TextTertiary,
            ),
        )
    },
) {
    val iconTint = tint ?: LocalAccentPalette.current.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (tint != null) tint else TextPrimary,
                ),
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary,
                    ),
                    maxLines = 1,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        trailing()
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowChevronPreview() {
    GencIptvTheme {
        SettingRow(
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
            label = "Playlist Yönetimi",
            subtitle = "2 playlist aktif",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowTogglePreview() {
    GencIptvTheme {
        SettingRow(
            icon = Icons.Filled.Autorenew,
            label = "Otomatik Güncelleme",
            trailing = { GencToggle(checked = true, onCheckedChange = {}) },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowVariantsPreview() {
    GencIptvTheme {
        Column {
            SettingRow(icon = Icons.Filled.Notifications, label = "Bildirimler")
            SettingRow(
                icon = Icons.Outlined.Wifi,
                label = "Ağ Ayarları",
                subtitle = "Wi-Fi bağlı",
            )
            SettingRow(icon = Icons.Outlined.DeleteOutline, label = "Önbelleği Temizle")
        }
    }
}
