package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.GencIptvTheme

/**
 * 16:9 detail hero — used at the top of film/series detail screens.
 *
 * Renders, back to front:
 *  1. Hash-derived diagonal gradient (stable per [title]).
 *  2. AsyncImage with the real backdrop on top, if [backdropUrl] is provided.
 *  3. Subtle film-grain noise.
 *  4. Vertical gradient that fades into the page background ([Bg]) at the
 *     bottom, so the hero blends seamlessly into the screen content underneath.
 *
 * [content] is a `BoxScope` slot for overlay UI — back button, title row,
 * favourite toggle. The slot is the only thing the caller owns; the hero
 * art and bottom fade are managed here.
 */
@Composable
fun Backdrop(
    title: String,
    modifier: Modifier = Modifier,
    backdropUrl: String? = null,
    height: Dp = 220.dp,
    seed: Int = 0,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val brush = remember(title, seed) { hashedBackdropGradient(title, seed) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush),
    ) {
        if (!backdropUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backdropUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Film grain
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    val step = 2.5.dp.toPx()
                    val lineHeight = 0.5.dp.toPx()
                    val grainColor = Color.White.copy(alpha = 0.05f)
                    var y = 0f
                    while (y < size.height) {
                        drawRect(
                            color = grainColor,
                            topLeft = Offset(0f, y),
                            size = Size(size.width, lineHeight),
                        )
                        y += step
                    }
                }
        )

        // Bottom fade into page background — uses Bg so it adapts to theme.
        val fadeColor = Bg
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.30f to Color.Transparent,
                            1f to fadeColor,
                        ),
                    ),
                ),
        )

        content()
    }
}

internal fun hashedBackdropGradient(title: String, seed: Int = 0): Brush {
    val hash = if (seed != 0) seed
               else title.fold(0) { acc, c -> acc + c.code }
    val hue1 = (((hash * 53) % 360) + 360) % 360
    val hue2 = (hue1 + 40) % 360
    val top = Color.hsl(hue1.toFloat(), saturation = 0.45f, lightness = 0.18f)
    val bottom = Color.hsl(hue2.toFloat(), saturation = 0.35f, lightness = 0.08f)
    return Brush.linearGradient(
        colors = listOf(top, bottom),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun BackdropDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        Backdrop(title = "Çukur", height = 220.dp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun BackdropLightPreview() {
    GencIptvTheme(darkTheme = false) {
        Backdrop(title = "Bir Başkadır", height = 220.dp)
    }
}
