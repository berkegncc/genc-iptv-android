package com.genciptv.player.core.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette

private val TrackWidth  = 38.dp
private val TrackHeight = 22.dp
private val KnobSize    = 18.dp
private val TrackRadius = RoundedCornerShape(11.dp)

/**
 * Custom toggle matching `.toggle` / `.toggle.on` / `.toggle.off` CSS.
 *
 * - 38×22dp rounded pill track, on = accent, off = border colour
 * - 18dp white circle knob with shadow, animated slide (150ms)
 */
@Composable
fun GencToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val accent = LocalAccentPalette.current.primary
    val effectiveAccent = if (enabled) accent else Border
    val trackColor = if (checked) effectiveAccent else Border

    // Knob x-offset: off = 2dp (left), on = 18dp (right)
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 2.dp,
        animationSpec = tween(durationMillis = 150),
        label = "knobOffset"
    )

    Box(
        modifier = modifier
            .size(width = TrackWidth, height = TrackHeight)
            .clip(TrackRadius)
            .background(trackColor)
            .then(
                if (enabled) Modifier.clickable { onCheckedChange(!checked) }
                else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = knobOffset)
                .size(KnobSize)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencToggleOffPreview() {
    GencIptvTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kapalı")
            Spacer(Modifier.width(12.dp))
            GencToggle(checked = false, onCheckedChange = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencToggleOnPreview() {
    GencIptvTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Açık")
            Spacer(Modifier.width(12.dp))
            GencToggle(checked = true, onCheckedChange = {})
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GencToggleInteractivePreview() {
    GencIptvTheme {
        var on by remember { mutableStateOf(false) }
        Column(modifier = Modifier.padding(16.dp)) {
            GencToggle(checked = on, onCheckedChange = { on = it })
            Spacer(Modifier.height(4.dp))
            Text(if (on) "Açık" else "Kapalı")
        }
    }
}
