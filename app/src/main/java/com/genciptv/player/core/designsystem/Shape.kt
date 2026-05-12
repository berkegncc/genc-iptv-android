package com.genciptv.player.core.designsystem

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens — v2 design system.
 *
 * Radius philosophy is "architectural with a poster/player exception":
 *   chip   = 6dp   pills, chips, micro pills
 *   button = 10dp  primary / secondary buttons
 *   card   = 10dp  list cards, profile rows, channel rows
 *   sheet  = 16dp  bottom sheets, dialogs, panels
 *   poster = 18dp  posters, video surface, the "jewel moment"
 *
 * Material3's [Shapes] only ships five fixed slots, so we map them as best we
 * can. Anything that needs the poster radius references [PosterShape]
 * directly because Material has no semantic slot for it.
 */
val GencShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),    // chip
    small      = RoundedCornerShape(10.dp),   // button / card
    medium     = RoundedCornerShape(10.dp),   // larger card
    large      = RoundedCornerShape(16.dp),   // sheet
    extraLarge = RoundedCornerShape(50.dp),   // pill (fully rounded)
)

/**
 * Poster / video surface radius — 18dp. Used directly by [Poster],
 * [Backdrop], and the [VideoArea] components.
 */
val PosterShape = RoundedCornerShape(18.dp)

/** Sheet shape — only top corners rounded for bottom sheets. */
val SheetTopShape = RoundedCornerShape(
    topStart = 16.dp, topEnd = 16.dp,
    bottomStart = 0.dp, bottomEnd = 0.dp,
)

/** Pill / fully-rounded shape. */
val PillShape = RoundedCornerShape(CornerSize(50))
