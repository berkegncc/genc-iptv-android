package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextTertiary

private val WrapShape = RoundedCornerShape(20.dp)
private val NavHeight = 60.dp

/**
 * Updated 5-item bottom navigation:
 *   Ana Sayfa · Kanallar · Filmler · Diziler · Profil
 *
 * Guide and Favoriler are accessed from the Profile screen.
 * [current] can be null — when null, no item is highlighted (used for drill-in
 * screens like Guide/Favoriler that sit under Profile conceptually).
 */
enum class GencNavItem(
    val label: String,
    val icon: ImageVector,
) {
    HOME(label = "Ana Sayfa", icon = Icons.Default.Home),
    CHANNELS(label = "Kanallar", icon = Icons.Default.Tv),
    MOVIES(label = "Filmler", icon = Icons.Default.Movie),
    SERIES(label = "Diziler", icon = Icons.Default.VideoLibrary),
    PROFILE(label = "Profil", icon = Icons.Default.Person);
}

@Composable
fun GencBottomNav(
    current: GencNavItem?,
    onItemClick: (GencNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary
    val accentSoft = LocalAccentPalette.current.soft

    Surface(
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        color = com.genciptv.player.core.designsystem.Surface,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                ambientColor = Color(0xFF12131A).copy(alpha = 0.06f),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp, bottom = 14.dp, start = 4.dp, end = 4.dp)
                .height(NavHeight),
        ) {
            GencNavItem.entries.forEach { item ->
                val isActive = item == current
                val iconTint = if (isActive) accent else TextTertiary
                val labelColor = if (isActive) accent else TextTertiary

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemClick(item) }
                        .padding(top = 8.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 40.dp, height = 25.dp)
                            .clip(WrapShape)
                            .background(if (isActive) accentSoft else Color.Transparent),
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Spacer(Modifier.width(0.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(color = labelColor),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencBottomNavHomePreview() {
    GencIptvTheme {
        GencBottomNav(
            current = GencNavItem.HOME,
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencBottomNavMoviesPreview() {
    GencIptvTheme {
        GencBottomNav(
            current = GencNavItem.MOVIES,
            onItemClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencBottomNavNoActivePreview() {
    GencIptvTheme {
        GencBottomNav(
            current = null,
            onItemClick = {},
        )
    }
}
