package com.genciptv.player.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.genciptv.player.R

/**
 * Google Fonts provider. Falls back to the system sans-serif if Play Services
 * fonts are unavailable (e.g. on non-GMS devices). The font_certs.xml array
 * matches the standard GMS fonts provider certificate.
 */
private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ─────────────────────────────────────────────────────────────────────────────
// New v2 typography — Geist (sans), Geist Mono (codes/timestamps),
// Instrument Serif (film/series titles, the "editorial moment").
// ─────────────────────────────────────────────────────────────────────────────

private val geistFontName = GoogleFont("Geist")
private val geistMonoFontName = GoogleFont("Geist Mono")
private val instrumentSerifFontName = GoogleFont("Instrument Serif")

/** Geist — primary UI sans-serif. */
val GeistFamily = FontFamily(
    Font(googleFont = geistFontName, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = geistFontName, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = geistFontName, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = geistFontName, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
)

/** Geist Mono — codes, time/runtime, status badges. */
val GeistMonoFamily = FontFamily(
    Font(googleFont = geistMonoFontName, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = geistMonoFontName, fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = geistMonoFontName, fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = geistMonoFontName, fontProvider = googleFontsProvider, weight = FontWeight.Bold),
)

/** Instrument Serif — film & series titles, greeting, the editorial moment. */
val InstrumentSerifFamily = FontFamily(
    Font(googleFont = instrumentSerifFontName, fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(
        googleFont = instrumentSerifFontName,
        fontProvider = googleFontsProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Backwards-compatibility aliases — existing screens import OutfitFamily,
// PlusJakartaFamily, NunitoFamily directly. Pointing them at Geist now means
// every existing call site immediately picks up the new font without any
// touch-up. Phase 2/3 of the redesign will replace these references with the
// canonical names (GeistFamily, etc.) screen by screen.
// ─────────────────────────────────────────────────────────────────────────────

@Deprecated(
    message = "Replaced by GeistFamily in v2 design system.",
    replaceWith = ReplaceWith("GeistFamily"),
)
val OutfitFamily: FontFamily = GeistFamily

@Deprecated(
    message = "Replaced by GeistFamily in v2 design system.",
    replaceWith = ReplaceWith("GeistFamily"),
)
val PlusJakartaFamily: FontFamily = GeistFamily

@Deprecated(
    message = "Replaced by GeistFamily in v2 design system.",
    replaceWith = ReplaceWith("GeistFamily"),
)
val NunitoFamily: FontFamily = GeistFamily

// ─────────────────────────────────────────────────────────────────────────────
// Type scale — directly mirrors tokens.js `type.scale`.
// Material3 only ships fixed slots, so the v2 named tokens map onto them as:
//
//   Material3            v2 tokens.js name      Family / weight / size
//   ───────────────────  ─────────────────────  ────────────────────────
//   displayLarge         display                 Serif 400 32/36
//   displayMedium        display (smaller)       Serif 400 28/32
//   displaySmall         display (smallest)      Serif 400 24/28
//   headlineLarge        title1                  Sans 600 22/28
//   headlineMedium       title2                  Sans 600 18/24
//   headlineSmall        title3                  Sans 600 16/22
//   titleLarge           title2                  Sans 600 18/24
//   titleMedium          title3                  Sans 600 16/22
//   titleSmall           bodyMed                 Sans 500 14/20
//   bodyLarge            body                    Sans 400 14/20
//   bodyMedium           body                    Sans 400 14/20
//   bodySmall            caption                 Sans 500 12/16
//   labelLarge           bodyMed                 Sans 500 14/20
//   labelMedium          caption                 Sans 500 12/16
//   labelSmall           micro                   Sans 600 10/12 (uppercase)
//
// The mono and serif scales are not bound to Material slots — feature code
// references [GeistMonoFamily] / [InstrumentSerifFamily] directly when the
// editorial moment calls for it.
// ─────────────────────────────────────────────────────────────────────────────

val GencTypography = Typography(
    // Display — serif, the editorial moment
    displayLarge = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = InstrumentSerifFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.015).sp,
    ),

    // Headline — sans, screen titles
    headlineLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.005).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.003).sp,
    ),

    // Title — sans, section headings
    titleLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.005).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.003).sp,
    ),
    titleSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),

    // Body — sans
    bodyLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.005.sp,
    ),

    // Label — sans, labels and tags
    labelLarge = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.005.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = GeistFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.06.sp,  // ALL-CAPS micro labels
    ),
)
