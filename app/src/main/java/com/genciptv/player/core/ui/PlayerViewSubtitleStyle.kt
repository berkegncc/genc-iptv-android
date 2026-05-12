package com.genciptv.player.core.ui

import android.graphics.Typeface
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.genciptv.player.data.model.SubtitleEdgeType
import com.genciptv.player.data.model.SubtitleFontFamily
import com.genciptv.player.data.model.SubtitleFontStyle
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.SubtitleVerticalPosition

/**
 * media3's `SubtitleView` reports the default text size as ~5.33 % of view height.
 * We multiply this by the user's `textSizePercent` (50..200) to get a fractional
 * size relative to the player surface.
 */
private const val BASE_TEXT_SIZE_FRACTION = 0.0533f

/**
 * Apply the user's saved [SubtitleStyle] to this [PlayerView]'s embedded
 * `SubtitleView`. Idempotent — safe to call from `AndroidView.update` on every
 * recomposition.
 *
 * No-op if the player has no subtitle view (custom layouts that strip it out).
 */
fun PlayerView.applySubtitleStyle(style: SubtitleStyle) {
    val subtitleView = subtitleView ?: return

    val typefaceStyle = when (style.fontStyle) {
        SubtitleFontStyle.NORMAL -> Typeface.NORMAL
        SubtitleFontStyle.BOLD -> Typeface.BOLD
        SubtitleFontStyle.ITALIC -> Typeface.ITALIC
        SubtitleFontStyle.BOLD_ITALIC -> Typeface.BOLD_ITALIC
    }

    val baseTypeface: Typeface = when (style.fontFamily) {
        SubtitleFontFamily.SYSTEM -> Typeface.DEFAULT
        SubtitleFontFamily.SANS_SERIF -> Typeface.SANS_SERIF
        SubtitleFontFamily.SERIF -> Typeface.SERIF
        SubtitleFontFamily.MONOSPACE -> Typeface.MONOSPACE
        SubtitleFontFamily.CASUAL ->
            runCatching { Typeface.create("casual", typefaceStyle) }.getOrDefault(Typeface.SANS_SERIF)
        SubtitleFontFamily.CURSIVE ->
            runCatching { Typeface.create("cursive", typefaceStyle) }.getOrDefault(Typeface.SANS_SERIF)
    }
    val typeface: Typeface = Typeface.create(baseTypeface, typefaceStyle)

    val edgeType = when (style.edgeType) {
        SubtitleEdgeType.NONE -> CaptionStyleCompat.EDGE_TYPE_NONE
        SubtitleEdgeType.OUTLINE -> CaptionStyleCompat.EDGE_TYPE_OUTLINE
        SubtitleEdgeType.DROP_SHADOW -> CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW
        SubtitleEdgeType.RAISED -> CaptionStyleCompat.EDGE_TYPE_RAISED
        SubtitleEdgeType.DEPRESSED -> CaptionStyleCompat.EDGE_TYPE_DEPRESSED
    }

    subtitleView.setApplyEmbeddedStyles(false)
    subtitleView.setApplyEmbeddedFontSizes(false)
    subtitleView.setStyle(
        CaptionStyleCompat(
            /* foregroundColor = */ style.textColor,
            /* backgroundColor = */ style.backgroundColor,
            /* windowColor = */ style.windowColor,
            /* edgeType = */ edgeType,
            /* edgeColor = */ style.edgeColor,
            /* typeface = */ typeface,
        )
    )

    val sizeFraction = BASE_TEXT_SIZE_FRACTION * (style.textSizePercent.coerceIn(50, 200) / 100f)
    subtitleView.setFractionalTextSize(sizeFraction)

    // setBottomPaddingFraction(0f) → text at very bottom; 0.95f → near top.
    val bottomFraction = when (style.verticalPosition) {
        SubtitleVerticalPosition.BOTTOM -> 0.08f
        SubtitleVerticalPosition.CENTER -> 0.45f
        SubtitleVerticalPosition.TOP -> 0.85f
    }
    subtitleView.setBottomPaddingFraction(bottomFraction)
}
