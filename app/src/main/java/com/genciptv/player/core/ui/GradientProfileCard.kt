package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.OutfitFamily

private val CardRadius   = RoundedCornerShape(16.dp)
private val AvatarRadius = CircleShape
private val PlanRadius   = RoundedCornerShape(20.dp)

/**
 * Gradient profile card matching `.set-profile` CSS.
 *
 * Structure:
 *   - Gradient bg (accent → gradientEnd), 16dp radius, accent shadow
 *   - 48dp avatar circle (white22%, initials, border)
 *   - Name (titleSmall white) + "✨ Premium Plan" pill
 *   - Right: 30dp edit button circle (white18%)
 */
@Composable
fun GradientProfileCard(
    name: String,
    plan: String,
    initials: String,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {}
) {
    val palette = LocalAccentPalette.current
    val gradient = Brush.linearGradient(
        colors = listOf(palette.primary, palette.gradientEnd)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = CardRadius,
                spotColor = palette.primary.copy(alpha = 0.28f),
                ambientColor = palette.primary.copy(alpha = 0.12f)
            )
            .clip(CardRadius)
            .background(gradient)
            .padding(16.dp)
    ) {
        // Avatar circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(AvatarRadius)
                .background(Color.White.copy(alpha = 0.22f))
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.38f),
                    shape = AvatarRadius
                )
        ) {
            Text(
                text = initials.take(2).uppercase(),
                fontFamily = OutfitFamily,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.width(12.dp))

        // Name + plan column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(PlanRadius)
                    .background(Color.White.copy(alpha = 0.20f))
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            ) {
                Text(
                    text = plan,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.90f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.06.sp
                    )
                )
            }
        }

        // Edit button — 30dp circle, white18%
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(onClick = onEditClick)
        ) {
            Text(
                text = "✏️",
                fontSize = 12.sp
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GradientProfileCardPreview() {
    GencIptvTheme {
        GradientProfileCard(
            name = "Mehmet Kaya",
            plan = "✨ Premium Plan",
            initials = "MK",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun GradientProfileCardTealPreview() {
    GencIptvTheme(accentPalette = com.genciptv.player.core.designsystem.AccentPalette.TEAL) {
        GradientProfileCard(
            name = "Ahmet Yılmaz",
            plan = "🆓 Ücretsiz Plan",
            initials = "AY",
            modifier = Modifier.padding(16.dp)
        )
    }
}
