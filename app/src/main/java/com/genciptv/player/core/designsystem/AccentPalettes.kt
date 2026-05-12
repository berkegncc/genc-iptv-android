package com.genciptv.player.core.designsystem

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 8 user-selectable accent palettes, sourced from the new tokens.js design system.
 * Each accent has an oklch-derived hex value tuned for AA contrast against both
 * the dark and light surface roles in [GencColors]. Chroma stays in the
 * 0.10–0.15 envelope so the 8 accents read as a family, not 8 unrelated stickers.
 *
 * Enum entry names (PURPLE, RED, …) are preserved from the v1 palette so the
 * `accentKey` value persisted to DataStore continues to resolve correctly after
 * the migration. The colour values and labels are refreshed.
 *
 * For consumers inside the theme, prefer [LocalAccentPalette] which provides a
 * [ResolvedAccent] — the colour fields there reflect the active light/dark
 * variant. The static [primary] etc. on the enum itself always return the light
 * variant; that's used for icon previews and selection swatches that render
 * the colour outside any theme context.
 */
enum class AccentPalette(
    val label: String,
    /** Light-theme variant. */
    val light: Color,
    /** Dark-theme variant — pulled brighter for AA on deep ink. */
    val dark: Color,
    /** Halo / focus ring — 20 % alpha of the dark variant. */
    val glow: Color,
) {
    PURPLE(
        label = "Mor",
        light = Color(0xFF6B4FBE),
        dark  = Color(0xFFA892F0),
        glow  = Color(0xFFA892F0).copy(alpha = 0.20f),
    ),
    RED(
        label = "Kırmızı",
        light = Color(0xFFC13E3E),
        dark  = Color(0xFFF08585),
        glow  = Color(0xFFF08585).copy(alpha = 0.20f),
    ),
    ORANGE(
        label = "Turuncu",
        light = Color(0xFFB8632A),
        dark  = Color(0xFFF0A06E),
        glow  = Color(0xFFF0A06E).copy(alpha = 0.20f),
    ),
    GREEN(
        label = "Yeşil",
        light = Color(0xFF3F8A4F),
        dark  = Color(0xFF7FCC8F),
        glow  = Color(0xFF7FCC8F).copy(alpha = 0.20f),
    ),
    BLUE(
        label = "Mavi",
        light = Color(0xFF3565C2),
        dark  = Color(0xFF7BA8F0),
        glow  = Color(0xFF7BA8F0).copy(alpha = 0.20f),
    ),
    PINK(
        label = "Pembe",
        light = Color(0xFFC13E8A),
        dark  = Color(0xFFF085C2),
        glow  = Color(0xFFF085C2).copy(alpha = 0.20f),
    ),
    TEAL(
        label = "Turkuaz",
        light = Color(0xFF0F8A7E),
        dark  = Color(0xFF3FD0BD),
        glow  = Color(0xFF3FD0BD).copy(alpha = 0.20f),
    ),
    VIOLET(
        label = "Eflatun",
        light = Color(0xFF8A3FB8),
        dark  = Color(0xFFC285F0),
        glow  = Color(0xFFC285F0).copy(alpha = 0.20f),
    );

    // ── Backwards-compatibility properties ───────────────────────────────────
    // These return the LIGHT variant always. They're used by call sites that
    // render the swatch outside a theme context (icon preview rows, selection
    // grid). For theme-aware access use [LocalAccentPalette.current.primary].

    /** Primary brand colour (light variant — for static usage outside theme). */
    val primary: Color get() = light

    /** Lighter end of the gradient — for the icon preview gradient. */
    val gradientEnd: Color get() = mixWithWhite(light, 0.25f)

    /** Tinted background — 12 % alpha of the primary. */
    val soft: Color get() = light.copy(alpha = 0.12f)

    /** Mid-tone — used for borders/dividers on tinted surfaces. */
    val mid: Color get() = light.copy(alpha = 0.30f)

    /** Resolve the accent against the active theme. Used by [GencIptvTheme]. */
    fun resolve(isDark: Boolean): ResolvedAccent {
        val active = if (isDark) dark else light
        return ResolvedAccent(
            key         = this,
            label       = label,
            primary     = active,
            soft        = active.copy(alpha = 0.12f),
            mid         = active.copy(alpha = 0.30f),
            gradientEnd = if (isDark) mixWithBlack(active, 0.25f) else mixWithWhite(active, 0.25f),
            glow        = glow,
            isDark      = isDark,
        )
    }
}

/**
 * The accent palette resolved against the active theme. Provided to the
 * composition by [GencIptvTheme] via [LocalAccentPalette]. All call sites that
 * read `LocalAccentPalette.current.primary` etc. get the variant matching the
 * current dark / light state automatically.
 */
data class ResolvedAccent(
    /** Back-reference to the enum entry that produced this resolution. */
    val key: AccentPalette,
    val label: String,
    /** Active accent colour (light or dark variant). */
    val primary: Color,
    /** Tinted background — 12 % alpha. */
    val soft: Color,
    /** Border / divider on tinted surfaces — 30 % alpha. */
    val mid: Color,
    /** Lighter (light theme) or darker (dark theme) end of the brand gradient. */
    val gradientEnd: Color,
    /** Halo / focus ring colour. */
    val glow: Color,
    /** Whether this resolution targets the dark theme. */
    val isDark: Boolean,
)

/**
 * Provides the [ResolvedAccent] for the active theme. The default value here
 * is only used when no [GencIptvTheme] wraps the composition — production
 * code always goes through [GencIptvTheme] so a real value is provided.
 */
val LocalAccentPalette = compositionLocalOf { AccentPalette.TEAL.resolve(isDark = false) }

// ── Internal helpers ─────────────────────────────────────────────────────────

private fun mixWithWhite(c: Color, t: Float): Color = Color(
    red   = c.red   + (1f - c.red)   * t,
    green = c.green + (1f - c.green) * t,
    blue  = c.blue  + (1f - c.blue)  * t,
    alpha = c.alpha,
)

private fun mixWithBlack(c: Color, t: Float): Color = Color(
    red   = c.red   * (1f - t),
    green = c.green * (1f - t),
    blue  = c.blue  * (1f - t),
    alpha = c.alpha,
)
