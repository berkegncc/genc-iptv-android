package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette

private val BannerHeight = 152.dp
private val BannerRadius = RoundedCornerShape(16.dp)

private val DefaultFeaturedBrush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF3730A3),
        0.5f to Color(0xFF7C3AED),
        1f to Color(0xFFC026D3)
    )
)

private val DarkOverlayBrush = Brush.horizontalGradient(
    colorStops = arrayOf(
        0f to Color(0x0A0A1EB8),   // rgba(10,10,30,0.72) → approximate hex
        0.7f to Color.Transparent
    )
)

/**
 * Full-width featured banner card matching the HTML `.featured` / `.feat-*` CSS.
 *
 * Layers (bottom to top):
 *   1. Gradient background [thumbBrush]
 *   2. Dark overlay (rgba gradient left→transparent) for text legibility
 *   3. Bottom-left content: LiveBadge + Title + Subtitle
 *   4. Bottom-right: play FAB (40dp white circle + accent play arrow)
 */
@Composable
fun FeaturedBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    brush: Brush = DefaultFeaturedBrush,
    onPlayClick: () -> Unit = {}
) {
    val accent = LocalAccentPalette.current.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(BannerHeight)
            .shadow(
                elevation = 8.dp,
                shape = BannerRadius,
                ambientColor = Color(0xFF12131A).copy(alpha = 0.10f),
                spotColor = Color(0xFF12131A).copy(alpha = 0.04f)
            )
            .clip(BannerRadius)
    ) {
        // Layer 1: Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )

        // Layer 2: Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0A0A1E).copy(alpha = 0.72f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 600f
                    )
                )
        )

        // Decorative circle (subtle, top-right area — matches .feat-deco).
        // Compose padding disallows negatives, so we use offset to push the
        // circle partially outside the clipped banner bounds (CSS `top:-20px;right:-20px`).
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        // Layer 3: Bottom-left content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(13.dp)
        ) {
            LiveBadge(variant = LiveBadgeVariant.MEDIUM)
            Spacer(Modifier.height(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(color = Color.White),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White.copy(alpha = 0.55f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Layer 4: Play FAB — 38dp white circle, accent play triangle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(38.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f))
                .clickable(onClick = onPlayClick)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Oynat",
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun FeaturedBannerDefaultPreview() {
    GencIptvTheme {
        FeaturedBanner(
            title = "Süper Lig — Galatasaray vs Fenerbahçe",
            subtitle = "Canlı yayın • Stadyum: Rams Park",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun FeaturedBannerCustomBrushPreview() {
    GencIptvTheme(accentPalette = com.genciptv.player.core.designsystem.AccentPalette.TEAL) {
        FeaturedBanner(
            title = "Formula 1 — Monaco GP",
            subtitle = "Türkiye saati ile 16:00",
            brush = Brush.linearGradient(listOf(Color(0xFF0CA678), Color(0xFF20C997))),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
