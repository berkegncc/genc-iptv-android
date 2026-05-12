package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.IcBlueSoft
import com.genciptv.player.core.designsystem.LiveSoft
import com.genciptv.player.core.designsystem.OrangeSoft
import com.genciptv.player.core.designsystem.Surface

private val GroupRadius = RoundedCornerShape(16.dp)

/**
 * Container for settings rows, matching `.set-group` CSS:
 *   - White surface background, 16dp radius, 1.5dp border
 *   - Children stacked vertically; SettingRow draws its own 1dp border-bottom
 *     (last row's border is covered by the outer rounded clip naturally)
 */
@Composable
fun SettingGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GroupRadius)
            .background(Surface)
            .border(width = 1.5.dp, color = Border, shape = GroupRadius)
    ) {
        content()
    }
}

/** Thin divider used between rows inside [SettingGroupCard]. */
@Composable
fun SettingRowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Border)
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SettingGroupCardPreview() {
    GencIptvTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingGroupCard {
                SettingRow(
                    icon = "📋",
                    iconBgColor = LocalAccentPalette().soft,
                    label = "Playlist Yönetimi",
                    subtitle = "2 playlist aktif"
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🔄",
                    iconBgColor = OrangeSoft,
                    label = "Otomatik Güncelleme",
                    trailing = { GencToggle(checked = true, onCheckedChange = {}) }
                )
                SettingRowDivider()
                SettingRow(
                    icon = "☁️",
                    iconBgColor = IcBlueSoft,
                    label = "Bulut Senkronizasyonu",
                    trailing = { GencToggle(checked = false, onCheckedChange = {}) }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SettingGroupCardSingleRowPreview() {
    GencIptvTheme {
        SettingGroupCard(modifier = Modifier.padding(16.dp)) {
            SettingRow(
                icon = "🚪",
                iconBgColor = LiveSoft,
                label = "Çıkış Yap"
            )
        }
    }
}

// Workaround helper to access LocalAccentPalette outside @Composable context
@Composable
private fun LocalAccentPalette() = com.genciptv.player.core.designsystem.LocalAccentPalette.current
