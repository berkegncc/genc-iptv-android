package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Live

/**
 * Badge size variants matching different HTML usages:
 *   SMALL   — .live-badge inside channel thumb (radius 3dp)
 *   MEDIUM  — .feat-badge featured banner badge (radius 20dp)
 *   COMPACT — .vid-live-badge player top-right (radius 4dp)
 */
enum class LiveBadgeVariant { SMALL, MEDIUM, COMPACT }

private data class BadgeSpec(
    val hPad: Dp,
    val vPad: Dp,
    val radius: Dp,
    val fontSize: Float,
    val dotSize: Dp
)

private fun specFor(variant: LiveBadgeVariant) = when (variant) {
    LiveBadgeVariant.SMALL   -> BadgeSpec(hPad = 5.dp,  vPad = 1.dp, radius = 3.dp,  fontSize = 6.5f, dotSize = 5.dp)
    LiveBadgeVariant.MEDIUM  -> BadgeSpec(hPad = 8.dp,  vPad = 2.dp, radius = 20.dp, fontSize = 7f,   dotSize = 5.dp)
    LiveBadgeVariant.COMPACT -> BadgeSpec(hPad = 7.dp,  vPad = 3.dp, radius = 4.dp,  fontSize = 7f,   dotSize = 4.dp)
}

@Composable
fun LiveBadge(
    modifier: Modifier = Modifier,
    variant: LiveBadgeVariant = LiveBadgeVariant.SMALL
) {
    val spec = specFor(variant)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(spec.radius))
            .background(Live)
            .padding(horizontal = spec.hPad, vertical = spec.vPad)
    ) {
        LivePulseDot(size = spec.dotSize)
        Spacer(Modifier.width(4.dp))
        Text(
            text = "CANLI",
            color = Color.White,
            fontSize = spec.fontSize.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.07.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun LiveBadgeSmallPreview() {
    GencIptvTheme {
        LiveBadge(
            variant = LiveBadgeVariant.SMALL,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun LiveBadgeMediumPreview() {
    GencIptvTheme {
        LiveBadge(
            variant = LiveBadgeVariant.MEDIUM,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
private fun LiveBadgeCompactPreview() {
    GencIptvTheme {
        LiveBadge(
            variant = LiveBadgeVariant.COMPACT,
            modifier = Modifier.padding(8.dp)
        )
    }
}
