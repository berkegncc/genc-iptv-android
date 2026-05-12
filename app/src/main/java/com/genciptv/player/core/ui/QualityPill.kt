package com.genciptv.player.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LineStrong
import com.genciptv.player.core.designsystem.TextSecondary

/**
 * v2 quality indicator — flat hairline pill used to mark stream resolution
 * (HD, 4K) or other static qualities. Sits next to [CanliPill] in channel
 * rows and player overlays.
 *
 * Matches `tokens.js → QualityPill`: 0.5 dp border in [LineStrong], muted
 * secondary text colour, all-caps with mild letter-spacing.
 */
@Composable
fun QualityPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(shape)
            .border(width = 0.5.dp, color = LineStrong, shape = shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label.uppercase(),
            color = TextSecondary,
            fontFamily = GeistFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.06.em,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun QualityPillLightPreview() {
    GencIptvTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) { QualityPill("HD") }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun QualityPillDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) { QualityPill("4K") }
    }
}
