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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import com.genciptv.player.core.designsystem.Orange
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary

private val CardRadius  = RoundedCornerShape(16.dp)
private val ThumbRadius = RoundedCornerShape(8.dp)

/**
 * "Kaldığın Yerden" (Continue Watching) card matching `.cw-item` CSS.
 *
 * Structure: [60×38dp thumb] | [title + subtitle + orange progress bar] | [28dp play button]
 */
@Composable
fun ContinueWatchingItem(
    title: String,
    subtitle: String,
    progressFraction: Float,
    thumbBrush: Brush,
    modifier: Modifier = Modifier,
    logoUrl: String? = null,
    onPlayClick: () -> Unit = {}
) {
    val accent = LocalAccentPalette.current.primary
    val accentSoft = LocalAccentPalette.current.soft

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = CardRadius,
                ambientColor = Color(0xFF12131A).copy(alpha = 0.06f),
                spotColor = Color(0xFF12131A).copy(alpha = 0.04f)
            )
            .clip(CardRadius)
            .background(Surface)
            .border(width = 1.5.dp, color = Border, shape = CardRadius)
            .padding(10.dp)
    ) {
        // Thumbnail — 64×40dp, solid light bg when logo exists, gradient otherwise
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 64.dp, height = 40.dp)
                .clip(ThumbRadius)
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
                        .size(width = 58.dp, height = 36.dp)
                        .padding(3.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 42.dp, height = 18.dp)
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

        Spacer(Modifier.width(9.dp))

        // Info column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(5.dp))

            // Orange progress bar — 2dp height (matching .cw-bar / .cw-bar-fill)
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
                        .background(Orange)
                )
            }
        }

        Spacer(Modifier.width(9.dp))

        // Play button — 28dp circle, accent-soft bg, accent play icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentSoft)
                .clickable(onClick = onPlayClick)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Oynat",
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ContinueWatchingItemPreview() {
    GencIptvTheme {
        ContinueWatchingItem(
            title = "Diriliş: Ertuğrul",
            subtitle = "Bölüm 142 · 23 dk kaldı",
            progressFraction = 0.62f,
            thumbBrush = Brush.linearGradient(listOf(Color(0xFFfa709a), Color(0xFFfee140))),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ContinueWatchingItemAlmostDonePreview() {
    GencIptvTheme {
        ContinueWatchingItem(
            title = "Kuruluş: Osman",
            subtitle = "Bölüm 58 · 5 dk kaldı",
            progressFraction = 0.92f,
            thumbBrush = Brush.linearGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2))),
            modifier = Modifier.padding(16.dp)
        )
    }
}
