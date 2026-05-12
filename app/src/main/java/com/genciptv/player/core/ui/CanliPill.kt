package com.genciptv.player.core.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LiveBg

/**
 * v2 live indicator — teal dot with a 1.4 s breathing pulse, framed by a
 * tinted background and a 0.5 dp hairline border in the live colour.
 *
 * The pulse scales the inner dot between 0.85 and 1.0 with a synchronised
 * fade so it reads as a heartbeat without becoming visually noisy.
 *
 * Replaces the old `LiveBadge` for the redesigned screens. The colour comes
 * from [Live] / [LiveBg] which are theme-aware (logo-faithful teal in both
 * light and dark themes).
 */
@Composable
fun CanliPill(
    modifier: Modifier = Modifier,
    label: String = "CANLI",
) {
    val transition = rememberInfiniteTransition(label = "canli-pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "canli-pulse-scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "canli-pulse-alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .background(LiveBg)
            .border(width = 0.5.dp, color = Live.copy(alpha = 0.40f), shape = CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Live.copy(alpha = alpha)),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = Live,
            fontFamily = GeistFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.em,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun CanliPillLightPreview() {
    GencIptvTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) { CanliPill() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun CanliPillDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) { CanliPill() }
    }
}
