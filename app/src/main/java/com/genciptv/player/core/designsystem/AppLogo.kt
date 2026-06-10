package com.genciptv.player.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.genciptv.player.R

/**
 * The Genç IPTV brand mark — silver G ring + copper bottom curve + teal play
 * triangle, all rendered from the resolution-independent vector at
 * `res/drawable/ic_logo_g_mark.xml`. Same artwork as the launcher icon and
 * splash screen, scaled to whatever [size] the caller asks for.
 */
@Composable
fun GencLogo(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.ic_logo_g_mark),
        contentDescription = "Genç IPTV Player",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size),
    )
}

// ── Backwards-compatible aliases ─────────────────────────────────────────────
//
// The redesign collapsed three distinct logo composables (`AppLogoIcon`,
// `AppLogoMark`, `AppLogoRow`) into a single brand mark since the new design
// system pairs the icon with separately-laid-out wordmark text rather than
// baking it into the asset. These wrappers stay so existing callers keep
// compiling; new screens should use [GencLogo] directly.

@Composable
fun AppLogoIcon(
    size: Dp = 30.dp,
    modifier: Modifier = Modifier,
) = GencLogo(size = size, modifier = modifier)

@Composable
fun AppLogoMark(
    height: Dp = 48.dp,
    modifier: Modifier = Modifier,
) = GencLogo(size = height, modifier = modifier)

@Composable
fun AppLogoRow(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") showText: Boolean = true,
    @Suppress("UNUSED_PARAMETER") showTagline: Boolean = true,
    height: Dp = 36.dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        GencLogo(size = height)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun GencLogoOnDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        GencLogo(size = 96.dp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun GencLogoOnLightPreview() {
    GencIptvTheme(darkTheme = false) {
        GencLogo(size = 96.dp)
    }
}
