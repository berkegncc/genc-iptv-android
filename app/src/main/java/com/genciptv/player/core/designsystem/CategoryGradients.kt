package com.genciptv.player.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * The "Dusk family" — 8 muted jewel-tone gradients sharing the same
 * lightness/chroma envelope so they read as siblings, not 8 stickers.
 *
 * Each entry carries:
 * - [from] / [to] — 2-stop dark gradient (used in dark theme + on top of dark
 *   surfaces like posters and category cards)
 * - [tint] — washed-out version (used in light theme as a subtle background)
 * - [glyph] — a unicode geometric symbol used as oversized corner decoration
 *   on the category card, providing identity without iconography
 *
 * Ported 1:1 from `tokens.js → categoryGradients`.
 */
data class CategoryGradient(
    val name: String,
    val from: Color,
    val to: Color,
    val tint: Color,
    val glyph: String,
)

val CategoryGradients: List<CategoryGradient> = listOf(
    CategoryGradient(
        name = "Teal",
        from = Color(0xFF1A3F3A),
        to = Color(0xFF2D6B5F),
        tint = Color(0xFFD9E8E4),
        glyph = "◐",
    ),
    CategoryGradient(
        name = "Copper",
        from = Color(0xFF3F2A1A),
        to = Color(0xFF7A4A2A),
        tint = Color(0xFFEDDFD0),
        glyph = "◑",
    ),
    CategoryGradient(
        name = "Plum",
        from = Color(0xFF3A1A35),
        to = Color(0xFF6B2D5A),
        tint = Color(0xFFE8D9E4),
        glyph = "◓",
    ),
    CategoryGradient(
        name = "Moss",
        from = Color(0xFF1F3A1A),
        to = Color(0xFF4A6B2D),
        tint = Color(0xFFDCE8D5),
        glyph = "◒",
    ),
    CategoryGradient(
        name = "Oxblood",
        from = Color(0xFF3F1A1F),
        to = Color(0xFF7A2D38),
        tint = Color(0xFFEDD5D9),
        glyph = "●",
    ),
    CategoryGradient(
        name = "Indigo",
        from = Color(0xFF1A2A3F),
        to = Color(0xFF2D4A7A),
        tint = Color(0xFFD5DCED),
        glyph = "◗",
    ),
    CategoryGradient(
        name = "Ochre",
        from = Color(0xFF3F351A),
        to = Color(0xFF7A6B2D),
        tint = Color(0xFFEDE7D0),
        glyph = "◖",
    ),
    CategoryGradient(
        name = "Slate",
        from = Color(0xFF1F2A35),
        to = Color(0xFF3F5260),
        tint = Color(0xFFD9DEE3),
        glyph = "◐",
    ),
)

/** Stable index lookup — wraps if [index] exceeds the list size. */
fun categoryGradientFor(index: Int): CategoryGradient =
    CategoryGradients[((index % CategoryGradients.size) + CategoryGradients.size) % CategoryGradients.size]
