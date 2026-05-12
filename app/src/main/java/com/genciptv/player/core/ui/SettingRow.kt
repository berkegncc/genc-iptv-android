package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.IcBlueSoft
import com.genciptv.player.core.designsystem.LiveSoft
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.OrangeSoft
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary

private val IconRadius = RoundedCornerShape(8.dp)

/**
 * Single row inside a settings group card, matching `.set-row` CSS.
 *
 * Structure:
 *   [32dp icon box] | [label + optional subtitle] | [trailing slot: ›, pill, toggle, custom]
 *
 * The [trailing] composable slot lets callers pass GencToggle, Text("›"), or a pill.
 */
@Composable
fun SettingRow(
    icon: String,
    iconBgColor: Color,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {
        Text(
            text = "›",
            style = MaterialTheme.typography.bodyLarge.copy(color = TextTertiary)
        )
    }
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        // Icon box — 32×32dp rounded
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(IconRadius)
                .background(iconBgColor)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.width(10.dp))

        // Text column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary)
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Trailing slot
        trailing()
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowChevronPreview() {
    GencIptvTheme {
        val accent = LocalAccentPalette.current.soft
        SettingRow(
            icon = "📋",
            iconBgColor = accent,
            label = "Playlist Yönetimi",
            subtitle = "2 playlist aktif"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowTogglePreview() {
    GencIptvTheme {
        SettingRow(
            icon = "🔄",
            iconBgColor = OrangeSoft,
            label = "Otomatik Güncelleme",
            trailing = { GencToggle(checked = true, onCheckedChange = {}) }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingRowVariantsPreview() {
    GencIptvTheme {
        Column {
            SettingRow(icon = "🔔", iconBgColor = LiveSoft, label = "Bildirimler")
            SettingRow(icon = "🌐", iconBgColor = IcBlueSoft, label = "Ağ Ayarları", subtitle = "Wi-Fi bağlı")
            SettingRow(icon = "🗑️", iconBgColor = Surface2, label = "Önbelleği Temizle")
        }
    }
}
