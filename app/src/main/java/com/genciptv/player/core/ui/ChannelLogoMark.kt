package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.genciptv.player.core.designsystem.BgElev2
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LineStrong
import com.genciptv.player.core.designsystem.TextTertiary

/**
 * v2 channel logo placeholder.
 *
 * - **Real logo present** ([logoUrl] non-null): renders the AsyncImage cleanly.
 *   Provider logos often have transparent backgrounds, so we render nothing
 *   behind the image — overlaying initials would bleed through and look messy.
 * - **No logo** ([logoUrl] null/blank): hairline-bordered tile with monospace
 *   initials in [TextTertiary] — communicates "we tried, no asset".
 */
@Composable
fun ChannelLogoMark(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    logoUrl: String? = null,
) {
    val initials = remember(name) { extractInitials(name) }
    val shape = RoundedCornerShape(8.dp)

    if (!logoUrl.isNullOrBlank()) {
        // Clean AsyncImage with no backdrop. Transparent logos must show only
        // the artwork, never initials underneath.
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(logoUrl)
                .crossfade(true)
                .build(),
            contentDescription = name,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size)
                .clip(shape),
        )
    } else {
        // No logo — hairline tile with mono initials
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(BgElev2)
                .border(width = 0.5.dp, color = LineStrong, shape = shape),
        ) {
            Text(
                text = initials,
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.04.sp,
                    color = TextTertiary,
                ),
            )
        }
    }
}

private fun extractInitials(name: String): String =
    name.trim().split(Regex("\\s+")).take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { name.take(2).uppercase() }

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun ChannelLogoMarkDarkWithLogoPreview() {
    GencIptvTheme(darkTheme = true) {
        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            ChannelLogoMark(name = "beIN Sports 1", logoUrl = "https://example.com/logo.png", size = 44.dp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun ChannelLogoMarkLightNoLogoPreview() {
    GencIptvTheme(darkTheme = false) {
        Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            ChannelLogoMark(name = "FOX TV", logoUrl = null, size = 44.dp)
        }
    }
}
