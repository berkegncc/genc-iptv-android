package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LiveSoft
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary

private val LogoBoxRadius = RoundedCornerShape(9.dp)
private val PillRadius    = RoundedCornerShape(20.dp)

/**
 * List item for the Kanallar screen matching `.ch-list-item` CSS.
 *
 * Structure: [42dp logo box] | [name + programme column] | [star + CANLI/HD pill]
 * Bottom: 1dp border divider.
 */
@Composable
fun ChannelListItem(
    name: String,
    program: String,
    isFavorite: Boolean,
    thumbBrush: Brush,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    isLive: Boolean = false,
    isHd: Boolean = false,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    val accent = LocalAccentPalette.current.primary
    val accentSoft = LocalAccentPalette.current.soft

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 9.dp)
        ) {
            // Channel logo box — 48×48dp. When logoUrl is present, show a
            // clean light background so the logo stands on its own (no noisy
            // gradient). When missing, keep the colourful gradient + dashed
            // placeholder for visual variety.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(LogoBoxRadius)
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
                            .size(42.dp)
                            .padding(3.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.25f))
                            .border(
                                width = 1.5.dp,
                                color = Color.White.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
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
            }

            Spacer(Modifier.width(8.dp))

            // Right column: star + pill
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                        tint = if (isFavorite) accent else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                when {
                    isLive -> Pill(label = "CANLI", bgColor = LiveSoft, textColor = Live)
                    isHd   -> Pill(label = "HD",    bgColor = accentSoft, textColor = accent)
                }
            }
        }

        // Bottom divider — 1dp border (HTML: border-bottom: 1px solid var(--border))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
    }
}

@Composable
private fun Pill(label: String, bgColor: Color, textColor: Color) {
    Text(
        text = label,
        color = textColor,
        fontSize = 6.5f.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.07.sp,
        modifier = Modifier
            .clip(PillRadius)
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val listPreviewBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
)

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ChannelListItemLiveFavoritePreview() {
    GencIptvTheme {
        ChannelListItem(
            name = "TRT 1",
            program = "Ana Haber Bülteni",
            isFavorite = true,
            isLive = true,
            thumbBrush = listPreviewBrush,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ChannelListItemHdPreview() {
    GencIptvTheme {
        ChannelListItem(
            name = "NTV Spor 4K",
            program = "Süper Lig Özetleri",
            isFavorite = false,
            isHd = true,
            thumbBrush = Brush.linearGradient(listOf(Color(0xFF43e97b), Color(0xFF38f9d7))),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
