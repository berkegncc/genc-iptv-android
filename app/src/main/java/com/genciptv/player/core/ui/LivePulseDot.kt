package com.genciptv.player.core.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Live

/**
 * Animated pulsing dot matching the HTML `.live-pulse` / `@keyframes blink` spec:
 *   - 5–6dp circle, live red
 *   - Opacity oscillates 1.0 ↔ 0.3, 1.2s cycle via rememberInfiniteTransition
 */
@Composable
fun LivePulseDot(
    modifier: Modifier = Modifier,
    size: Dp = 5.dp
) {
    val transition = rememberInfiniteTransition(label = "livePulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "livePulseAlpha"
    )

    Box(
        modifier = modifier
            .size(size)
            .alpha(alpha)
            .clip(CircleShape)
            .background(Live)
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun LivePulseDotPreview() {
    GencIptvTheme {
        LivePulseDot()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun LivePulseDotLargePreview() {
    GencIptvTheme {
        LivePulseDot(size = 10.dp)
    }
}
