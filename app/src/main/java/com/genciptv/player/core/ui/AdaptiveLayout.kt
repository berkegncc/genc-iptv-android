package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.TextTertiary

/**
 * Adaptive layout helpers shared across screens for tablet / large-screen
 * optimisation. Phones (Compact) keep their current full-bleed behaviour;
 * these utilities only change anything once there's extra horizontal room.
 *
 * See [com.genciptv.player.core.designsystem.WindowSize] for the breakpoints.
 */

/** Default content cap for reading/settings surfaces. */
val DefaultReadableWidth: Dp = 640.dp

/** Slightly wider cap for detail screens (poster + meta + episodes). */
val DetailReadableWidth: Dp = 720.dp

/**
 * Constrains an element to [max] width and centres it horizontally within its
 * parent. On Compact widths the parent is narrower than [max] so this is a
 * no-op (the element simply fills the width); on tablets it stops content from
 * stretching into unreadable line lengths.
 *
 * Apply to the scrolling content container of settings / onboarding / single
 * pane detail screens.
 */
fun Modifier.readableContentWidth(max: Dp = DefaultReadableWidth): Modifier =
    this
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = max)

/**
 * Centred, width-capped [Column]. Convenience wrapper around
 * [readableContentWidth] for call sites that build a fresh column.
 */
@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = DefaultReadableWidth,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.readableContentWidth(maxWidth),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/** Which pane of a [TwoPaneRow] keeps a fixed width; the other one fills. */
enum class TwoPaneSide { Start, End }

/**
 * Master-detail split for Expanded screens: one pane has a fixed width, the
 * other fills the remaining space, separated by a hairline divider. Call sites
 * gate this on [com.genciptv.player.core.designsystem.WindowSize.isExpanded]
 * and fall back to single-pane navigation on smaller widths.
 *
 * - Channels: list of categories is the fixed start pane, channels fill the end.
 *   `fixedSide = Start`.
 * - VOD: the poster grid fills the start, the detail panel is the fixed end.
 *   `fixedSide = End`.
 */
@Composable
fun TwoPaneRow(
    startPane: @Composable () -> Unit,
    endPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    fixedSide: TwoPaneSide = TwoPaneSide.Start,
    fixedWidth: Dp = 360.dp,
) {
    Row(modifier = modifier.fillMaxSize()) {
        if (fixedSide == TwoPaneSide.Start) {
            Box(modifier = Modifier.width(fixedWidth).fillMaxHeight()) { startPane() }
            VerticalDivider(thickness = 1.dp, color = Border)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { endPane() }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) { startPane() }
            VerticalDivider(thickness = 1.dp, color = Border)
            Box(modifier = Modifier.width(fixedWidth).fillMaxHeight()) { endPane() }
        }
    }
}

/**
 * Empty state for the detail pane of a [TwoPaneRow] when nothing is selected.
 */
@Composable
fun DetailPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Bg)
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextTertiary,
            ),
        )
    }
}
