package com.genciptv.player.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colour tokens for the app, ported from the new tokens.js design system.
 *
 * Light theme: warm bone paper (#F6F2EC, 35° hue) with ink-black type — gives a
 * "documentary print" feel rather than a generic Material light grey.
 *
 * Dark theme: deep ink with a 4° teal-warm tint (#0E1213) — not pure black, so
 * the user accent and the logo-faithful teal "live" colour both render as warm
 * highlights against a slightly cool surface.
 *
 * Two visibly distinct ink-pen lines are exposed:
 * - [line] is a 0.5 px hairline (subtle dividers, internal structure)
 * - [lineStrong] is a 1 px / 1.5 px stroke equivalent (card borders, focus rings)
 *
 * Copper (#C68A5C dark / #A8693C light) is the brand-faithful warm punctuation,
 * separate from the user-selectable accent so it stays consistent across themes.
 */
data class GencColors(
    /** Primary surface — page background */
    val bg: Color,
    /** Secondary surface — cards, sheet bodies */
    val surface: Color,
    /** Tertiary surface — raised content within sheets, input fields */
    val surface2: Color,
    /** Strong divider / card border — equivalent to a 1.5 px stroke */
    val border: Color,
    /** Primary text colour */
    val textPrimary: Color,
    /** Secondary text colour — captions, supporting copy */
    val textSecondary: Color,
    /** Tertiary text colour — placeholders, disabled state, labels */
    val textTertiary: Color,
    /** Live indicator — logo-faithful teal, semantic only (NOT user accent) */
    val live: Color,
    /** Live indicator background — alpha-blended teal */
    val liveSoft: Color,
    /** Success accent */
    val green: Color,
    val greenSoft: Color,
    /** Warm copper — used for ratings, "watching" progress, brand punctuation */
    val orange: Color,
    val orangeSoft: Color,
    /** Blue-soft background for info icons */
    val icBlueSoft: Color,

    // ── New tokens added in the v2 design system ─────────────────────────────

    /** Card / sheet body — same as [surface] but exposed for the new naming. */
    val bgElev: Color,
    /** Raised content within sheets — same as [surface2] under the new naming. */
    val bgElev2: Color,
    /** Modal scrim / overlay backdrop */
    val bgScrim: Color,
    /** 0.5 px hairline divider — subtler than [border]. */
    val line: Color,
    /** 1 px / 1.5 px stroke equivalent — same as [border] under the new naming. */
    val lineStrong: Color,
    /** Warm punctuation — copper. */
    val copper: Color,
    /** Dimmer copper — used for inactive states. */
    val copperDim: Color,
    /** Live indicator background — semantic alias of [liveSoft]. */
    val liveBg: Color,
    /** Destructive / error accent. */
    val danger: Color,
    /** Confirmation / success accent — same as [green]. */
    val success: Color,
) {
    /** Convenience flag for code that needs to branch on theme. */
    val isDark: Boolean
        get() = bg == Dark.bg

    companion object {
        // ────────────────────────────────────────────────────────────────────
        // LIGHT — warm bone paper with ink-black type
        // ────────────────────────────────────────────────────────────────────
        val Light = GencColors(
            // Surfaces
            bg            = Color(0xFFF6F2EC),  // warm bone, 35° hue
            surface       = Color(0xFFFFFFFF),
            surface2      = Color(0xFFFBF8F2),
            border        = Color(0xFF14120E).copy(alpha = 0.16f),

            // Text
            textPrimary   = Color(0xFF14120E),  // ink black
            textSecondary = Color(0xFF5A564D),
            textTertiary  = Color(0xFF8A857A),

            // Status — live is logo-faithful teal pulled darker for AA on light
            live          = Color(0xFF0F8A7E),
            liveSoft      = Color(0xFF0F8A7E).copy(alpha = 0.10f),
            green         = Color(0xFF3F8A4F),
            greenSoft     = Color(0xFF3F8A4F).copy(alpha = 0.10f),
            orange        = Color(0xFFA8693C),  // copper
            orangeSoft    = Color(0xFFA8693C).copy(alpha = 0.10f),
            icBlueSoft    = Color(0xFFEEF6FF),

            // New tokens
            bgElev        = Color(0xFFFFFFFF),
            bgElev2       = Color(0xFFFBF8F2),
            bgScrim       = Color(0xFF14120E).copy(alpha = 0.32f),
            line          = Color(0xFF14120E).copy(alpha = 0.08f),
            lineStrong    = Color(0xFF14120E).copy(alpha = 0.16f),
            copper        = Color(0xFFA8693C),
            copperDim     = Color(0xFFC68A5C),
            liveBg        = Color(0xFF0F8A7E).copy(alpha = 0.10f),
            danger        = Color(0xFFC13E3E),
            success       = Color(0xFF3F8A4F),
        )

        // ────────────────────────────────────────────────────────────────────
        // DARK — deep ink with 4° teal-warm tint, copper as warmth signal
        // ────────────────────────────────────────────────────────────────────
        val Dark = GencColors(
            // Surfaces
            bg            = Color(0xFF0E1213),  // deep ink, 4° teal hue
            surface       = Color(0xFF161B1D),
            surface2      = Color(0xFF1E2426),
            border        = Color(0xFFFFFFFF).copy(alpha = 0.12f),

            // Text
            textPrimary   = Color(0xFFE8EDEC),
            textSecondary = Color(0xFF9DA8A6),
            textTertiary  = Color(0xFF6A7472),

            // Status — live is logo-faithful teal at full strength on dark
            live          = Color(0xFF3FD0BD),
            liveSoft      = Color(0xFF3FD0BD).copy(alpha = 0.12f),
            green         = Color(0xFF7FCC8F),
            greenSoft     = Color(0xFF7FCC8F).copy(alpha = 0.12f),
            orange        = Color(0xFFC68A5C),  // copper
            orangeSoft    = Color(0xFFC68A5C).copy(alpha = 0.12f),
            icBlueSoft    = Color(0xFF0F2038),

            // New tokens
            bgElev        = Color(0xFF161B1D),
            bgElev2       = Color(0xFF1E2426),
            bgScrim       = Color(0xFF080B0C).copy(alpha = 0.72f),
            line          = Color(0xFFFFFFFF).copy(alpha = 0.06f),
            lineStrong    = Color(0xFFFFFFFF).copy(alpha = 0.12f),
            copper        = Color(0xFFC68A5C),
            copperDim     = Color(0xFF8B6240),
            liveBg        = Color(0xFF3FD0BD).copy(alpha = 0.12f),
            danger        = Color(0xFFF08585),
            success       = Color(0xFF7FCC8F),
        )
    }
}

/** Provides the current [GencColors] variant throughout the composition. */
val LocalGencColors = staticCompositionLocalOf { GencColors.Light }
