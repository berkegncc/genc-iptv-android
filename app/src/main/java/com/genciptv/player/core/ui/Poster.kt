package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.PosterShape

/**
 * v2 poster placeholder — used for film & series posters when no real
 * artwork is available, and as a base layer behind real artwork.
 *
 * Visual stack (back to front):
 *  1. Hash-derived diagonal gradient — stable per [title], muted jewel tones.
 *  2. Film-grain noise hint (very subtle horizontal lines).
 *  3. Bottom vignette gradient (transparent → 55% black).
 *  4. AsyncImage on top if [posterUrl] is provided.
 *  5. Optional [label] in the top-left (e.g. "DEVAM", "YENİ").
 *  6. Serif title in the bottom-left (Instrument Serif, white).
 *  7. Mono [year] in the bottom-right.
 *
 * Pass `modifier = Modifier.fillMaxWidth()` (or any size override) to use
 * the poster inside a constrained parent (LazyRow item, grid cell, etc.). The
 * default falls back to [width] × [height] = 120 × 180 dp.
 */
@Composable
fun Poster(
    title: String,
    modifier: Modifier = Modifier,
    posterUrl: String? = null,
    year: Int? = null,
    label: String? = null,
    width: Dp = Dp.Unspecified,
    height: Dp = Dp.Unspecified,
    seed: Int = 0,
    /**
     * Whether to render the [title] inside the poster (bottom-left serif).
     * Cards that already display the title underneath the poster (e.g.
     * "Devam Et" rows) should set this to `false` so the artwork isn't
     * cluttered with redundant text.
     */
    showTitleOverlay: Boolean = true,
) {
    val brush = remember(title, seed) { hashedPosterGradient(title, seed) }
    // Apply explicit size only if both dimensions were given; otherwise the
    // caller's [modifier] (e.g. `fillMaxWidth().aspectRatio(2f/3f)`) takes over.
    val sizeMod = if (width != Dp.Unspecified && height != Dp.Unspecified) {
        Modifier.size(width = width, height = height)
    } else if (width != Dp.Unspecified) {
        Modifier.size(width = width, height = width * 1.5f)  // 2:3 default fallback
    } else Modifier

    val isCompact = width != Dp.Unspecified && width < 120.dp
    Box(
        modifier = modifier
            .then(sizeMod)
            .clip(PosterShape)
            .background(brush)
            .drawFilmGrain()
            .drawBottomVignette(),
    ) {
        if (!posterUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(posterUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
            )
            // Re-apply vignette on top of the image so the title remains legible.
            Box(modifier = Modifier.fillMaxSize().drawBottomVignette())
        }

        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    letterSpacing = 0.06.sp,
                    color = Color.White.copy(alpha = 0.70f),
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }

        if (showTitleOverlay) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (isCompact) 13.sp else 16.sp,
                    lineHeight = if (isCompact) 16.sp else 19.sp,
                    letterSpacing = (-0.01).sp,
                    color = Color.White.copy(alpha = 0.96f),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = if (year != null) 22.dp else 8.dp, top = 8.dp),
            )
        }

        if (year != null) {
            Text(
                text = year.toString(),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.55f),
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

// ── Visual effects ───────────────────────────────────────────────────────────

private fun Modifier.drawFilmGrain(opacity: Float = 0.08f): Modifier = drawWithContent {
    drawContent()
    val step = 2.5.dp.toPx()
    val lineHeight = 0.5.dp.toPx()
    val color = Color.White.copy(alpha = opacity)
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = color,
            topLeft = Offset(0f, y),
            size = Size(size.width, lineHeight),
        )
        y += step
    }
}

private fun Modifier.drawBottomVignette(): Modifier = drawWithContent {
    drawContent()
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.5f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.55f),
            ),
        ),
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

internal fun hashedPosterGradient(title: String, seed: Int = 0): Brush {
    val hash = if (seed != 0) seed
               else title.fold(0) { acc, c -> acc + c.code }
    val hue1 = (((hash * 53) % 360) + 360) % 360
    val hue2 = (hue1 + 40) % 360
    val top = Color.hsl(hue1.toFloat(), saturation = 0.45f, lightness = 0.20f)
    val bottom = Color.hsl(hue2.toFloat(), saturation = 0.40f, lightness = 0.10f)
    // Diagonal gradient — top-left → bottom-right.
    return Brush.linearGradient(
        colors = listOf(top, bottom),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun PosterLightPreview() {
    GencIptvTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            Poster(title = "Kelebeğin Rüyası", year = 2013)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun PosterDarkWithLabelPreview() {
    GencIptvTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            Poster(
                title = "Bir Başkadır",
                year = 2020,
                label = "DEVAM",
                width = 140.dp,
                height = 90.dp,
            )
        }
    }
}
