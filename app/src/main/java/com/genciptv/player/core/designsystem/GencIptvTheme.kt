package com.genciptv.player.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// ── Material3 colour schemes — bridge between [GencColors]/[ResolvedAccent]
//    and the slots Material3 widgets expect (Button, Card, etc.). ─────────────

private fun gencLightColors(accent: ResolvedAccent, c: GencColors) = lightColorScheme(
    primary            = accent.primary,
    onPrimary          = Color.White,
    primaryContainer   = accent.soft,
    onPrimaryContainer = accent.primary,
    secondary          = c.copper,
    onSecondary        = Color.White,
    background         = c.bg,
    onBackground       = c.textPrimary,
    surface            = c.surface,
    onSurface          = c.textPrimary,
    surfaceVariant     = c.surface2,
    onSurfaceVariant   = c.textSecondary,
    outline            = c.lineStrong,
    error              = c.danger,
    onError            = Color.White,
)

private fun gencDarkColors(accent: ResolvedAccent, c: GencColors) = darkColorScheme(
    primary            = accent.primary,
    onPrimary          = Color(0xFF0E1213),
    primaryContainer   = accent.primary.copy(alpha = 0.22f),
    onPrimaryContainer = accent.primary,
    secondary          = c.copper,
    onSecondary        = Color(0xFF0E1213),
    background         = c.bg,
    onBackground       = c.textPrimary,
    surface            = c.surface,
    onSurface          = c.textPrimary,
    surfaceVariant     = c.surface2,
    onSurfaceVariant   = c.textSecondary,
    outline            = c.lineStrong,
    error              = c.danger,
    onError            = Color(0xFF0E1213),
)

/**
 * Root theme wrapper for the Genç IPTV app.
 *
 * Drives, via CompositionLocals, every screen's:
 *   - colour tokens   ([LocalGencColors]; updated for v2 — warm bone / deep ink + teal)
 *   - accent palette  ([LocalAccentPalette] now provides a [ResolvedAccent], so
 *                      `LocalAccentPalette.current.primary` reflects the active
 *                      light or dark variant of the user's chosen accent)
 *   - typography      ([GencTypography] — Geist sans + Geist Mono + Instrument Serif)
 *   - shapes          ([GencShapes] — chip/button/card/sheet plus [PosterShape])
 *
 * No call site needs to change for dark theme to take effect; the property
 * getters in `Color.kt` and the resolved accent take care of switching.
 */
@Composable
fun GencIptvTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentPalette: AccentPalette = AccentPalette.TEAL,
    content: @Composable () -> Unit,
) {
    val gencColors = if (darkTheme) GencColors.Dark else GencColors.Light
    val resolvedAccent = accentPalette.resolve(isDark = darkTheme)

    val colorScheme = if (darkTheme) {
        gencDarkColors(resolvedAccent, gencColors)
    } else {
        gencLightColors(resolvedAccent, gencColors)
    }

    CompositionLocalProvider(
        LocalAccentPalette provides resolvedAccent,
        LocalGencColors provides gencColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = GencTypography,
            shapes      = GencShapes,
            content     = content,
        )
    }
}
