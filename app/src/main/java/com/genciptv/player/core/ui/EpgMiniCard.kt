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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary

private val CardRadius = RoundedCornerShape(16.dp)

/**
 * EPG "Şu An Yayında" mini-card matching `.epg-mini` CSS.
 *
 * Structure:
 *   - Label row: LivePulseDot + "ŞU AN YAYINDA" uppercase tertiary text
 *   - Programme title (titleSmall primary)
 *   - Channel name (bodySmall secondary)
 *   - 3dp accent progress bar
 */
@Composable
fun EpgMiniCard(
    title: String,
    channelName: String,
    progressFraction: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val accent = LocalAccentPalette.current.primary

    Column(
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
            .clickable(onClick = onClick)
            .padding(11.dp)
    ) {
        // Label row
        Row(verticalAlignment = Alignment.CenterVertically) {
            LivePulseDot(size = 5.dp)
            Spacer(Modifier.width(5.dp))
            Text(
                text = "ŞU AN YAYINDA",
                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
            )
        }

        Spacer(Modifier.height(6.dp))

        // Programme title
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        // Channel name
        Text(
            text = channelName,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        // Progress bar — 3dp height matching .epg-bar / .epg-bar-fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun EpgMiniCardPreview() {
    GencIptvTheme {
        EpgMiniCard(
            title = "Ana Haber Bülteni",
            channelName = "TRT 1",
            progressFraction = 0.42f,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun EpgMiniCardLongTitlePreview() {
    GencIptvTheme {
        EpgMiniCard(
            title = "Türkiye Büyük Millet Meclisi Genel Kurul Toplantısı Canlı Yayını",
            channelName = "TRT Haber",
            progressFraction = 0.78f,
            modifier = Modifier.padding(16.dp)
        )
    }
}
