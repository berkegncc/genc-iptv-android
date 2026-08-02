package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.genciptv.player.core.designsystem.GencLogo
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.WindowSize

private val RailWidth = 80.dp
private val ExpandedRailWidth = 96.dp
private val IndicatorShape = RoundedCornerShape(14.dp)

/**
 * Height one labelled rail item needs: 32dp indicator + 4dp gap + label +
 * 12dp of vertical padding. Used to decide whether the labels fit at all.
 */
private val LabelledItemHeight = 64.dp

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
    val isExpanded = WindowSize.isExpanded
    val railWidth = if (isExpanded) ExpandedRailWidth else RailWidth

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(railWidth)
            .fillMaxHeight()
            .background(Surface)
            .border(width = 1.dp, color = Border)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
    ) {
        // Brand mark anchors the rail on wide screens.
        GencLogo(size = if (isExpanded) 34.dp else 28.dp)
        Spacer(Modifier.height(if (isExpanded) 20.dp else 14.dp))

        // The five destinations share whatever height is left instead of
        // stacking directly under the logo. A tablet held in landscape gives
        // the rail far more height than the items need, and packed at the top
        // they left most of the rail visibly empty below them.
        //
        // When the rail is short the labels are dropped instead. A phone turned
        // sideways is also wide enough to get a rail, but only ~390dp tall, and
        // five labelled items do not fit in it — the last one used to fall off
        // the bottom edge, which quietly made Profil unreachable in landscape.
        // Icons alone need barely two thirds of the height and keep every
        // destination on screen.
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val showLabels = maxHeight >= LabelledItemHeight * GencNavItem.entries.size

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize(),
            ) {
                GencNavItem.entries.forEach { item ->
                    val isActive = item == current
                    val iconTint = if (isActive) accent else TextTertiary
                    val labelColor = if (isActive) accent else TextTertiary

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
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
                        if (showLabels) {
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
        }
    }
}
