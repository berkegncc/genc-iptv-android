package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextTertiary

private val RailWidth = 80.dp
private val IndicatorShape = RoundedCornerShape(14.dp)

/**
 * Tablet / wide-screen side navigation rail. Mirrors [GencBottomNav]'s
 * 5 destinations but stacks them vertically along the screen edge so there's
 * room for a richer content area beside it.
 */
@Composable
fun GencNavRail(
    current: GencNavItem?,
    onItemClick: (GencNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary
    val accentSoft = LocalAccentPalette.current.soft

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .width(RailWidth)
            .fillMaxHeight()
            .background(Surface)
            .border(width = 1.dp, color = Border)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
    ) {
        GencNavItem.entries.forEach { item ->
            val isActive = item == current
            val iconTint = if (isActive) accent else TextTertiary
            val labelColor = if (isActive) accent else TextTertiary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { onItemClick(item) }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 52.dp, height = 32.dp)
                        .clip(IndicatorShape)
                        .background(if (isActive) accentSoft else Color.Transparent),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(color = labelColor),
                    maxLines = 1,
                )
            }
        }
    }
}
