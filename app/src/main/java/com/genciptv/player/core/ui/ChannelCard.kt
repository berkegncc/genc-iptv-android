package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary

private val CardWidth   = 110.dp
private val ThumbHeight = 64.dp
private val CardRadius  = RoundedCornerShape(16.dp)

/**
 * Channel card for the horizontal home-screen row.
 * Matches `.ch-card` / `.ch-thumb` / `.ch-info` from the HTML reference.
 *
 * Structure:
 *  - 110dp wide, 16dp radius card, 1.5dp border + shadow
 *  - 64dp gradient thumbnail with centred logo placeholder + optional LiveBadge
 *  - Info section: channel name, current programme, 2dp accent progress bar
 */
@Composable
fun ChannelCard(
    name: String,
    program: String,
    progressFraction: Float,
    thumbBrush: Brush,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    isLive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val accent = LocalAccentPalette.current.primary

    Column(
        modifier = modifier
            .width(CardWidth)
            .shadow(
                elevation = 4.dp,
                shape = CardRadius,
                ambientColor = Color(0xFF12131A).copy(alpha = 0.06f),
                spotColor = Color(0xFF12131A).copy(alpha = 0.04f)
            )
            .clip(CardRadius)
            .background(Surface)
            .border(width = 1.5.dp, color = Border, shape = CardRadius)
            .clickable(onClick = onClick)
    ) {
        // Thumbnail: solid light bg when logo is available (for clean logo
        // visibility), colourful gradient + placeholder when missing.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(ThumbHeight)
                .background(if (!logoUrl.isNullOrBlank()) Surface2 else Color.Transparent)
                .then(
                    if (logoUrl.isNullOrBlank()) Modifier.background(thumbBrush)
                    else Modifier
                )
        ) {
            if (!logoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ThumbHeight)
                        .padding(8.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
            if (isLive) {
                LiveBadge(
                    variant = LiveBadgeVariant.SMALL,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(5.dp)
                )
            }
        }

        // Info section
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = program,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Progress bar — 2dp height matching .ch-bar / .ch-bar-fill
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(accent)
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewBrush1 = Brush.linearGradient(
    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
)
private val previewBrush2 = Brush.linearGradient(
    colors = listOf(Color(0xFF43e97b), Color(0xFF38f9d7))
)

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ChannelCardLivePreview() {
    GencIptvTheme {
        ChannelCard(
            name = "TRT 1",
            program = "Ana Haber Bülteni",
            progressFraction = 0.42f,
            thumbBrush = previewBrush1,
            isLive = true,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ChannelCardNotLivePreview() {
    GencIptvTheme {
        ChannelCard(
            name = "NTV Spor",
            program = "Canlı Maç Yayını",
            progressFraction = 0.7f,
            thumbBrush = previewBrush2,
            isLive = false,
            modifier = Modifier.padding(8.dp)
        )
    }
}
