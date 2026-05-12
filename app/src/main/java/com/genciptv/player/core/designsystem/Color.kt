package com.genciptv.player.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Semantic colour tokens — sourced from the active GencColors in the
// CompositionLocal set by GencIptvTheme.
//
// All existing imports like `import com.genciptv.player.core.designsystem.Bg`
// continue to work unchanged because these are still top-level properties in
// this package. The only difference is that they are now `@Composable`
// getters, so the value reflects the current theme (light or dark) at runtime.
//
// New v2 tokens (BgElev, BgElev2, Line, LineStrong, Copper, CopperDim, etc.)
// are added below for the new design system. They are accessed the same way.
// ─────────────────────────────────────────────────────────────────────────────

// ── Background & Surface ─────────────────────────────────────────────────────

val Bg: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.bg

val Surface: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.surface

val Surface2: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.surface2

val Border: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.border

// ── New v2 surfaces / lines ──────────────────────────────────────────────────

/** Cards, sheet bodies — semantic alias of [Surface]. */
val BgElev: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.bgElev

/** Raised content within sheets, input fields — semantic alias of [Surface2]. */
val BgElev2: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.bgElev2

/** Modal scrim / overlay backdrop. */
val BgScrim: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.bgScrim

/** Hairline divider (0.5 px equivalent) — subtler than [Border]. */
val Line: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.line

/** Strong line / card border (1.5 px stroke equivalent) — same as [Border]. */
val LineStrong: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.lineStrong

// ── Text ─────────────────────────────────────────────────────────────────────

val TextPrimary: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.textPrimary

val TextSecondary: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.textSecondary

val TextTertiary: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.textTertiary

// ── Status ────────────────────────────────────────────────────────────────────

val Live: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.live

val LiveSoft: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.liveSoft

/** Live indicator background — semantic alias of [LiveSoft]. */
val LiveBg: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.liveBg

val GreenOk: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.green

val GreenSoft: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.greenSoft

val Orange: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.orange

val OrangeSoft: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.orangeSoft

val IcBlueSoft: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.icBlueSoft

// ── Brand & Semantic ─────────────────────────────────────────────────────────

/** Warm copper — used for ratings, "watching" progress, brand punctuation. */
val Copper: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.copper

/** Dimmer copper — inactive states. */
val CopperDim: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.copperDim

/** Destructive / error accent. */
val Danger: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.danger

/** Confirmation / success accent — same as [GreenOk]. */
val Success: Color
    @Composable @ReadOnlyComposable
    get() = LocalGencColors.current.success
