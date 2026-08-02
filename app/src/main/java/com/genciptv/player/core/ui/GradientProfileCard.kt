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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextTertiary

private val CardRadius = RoundedCornerShape(16.dp)

/**
 * Identity block at the top of Settings.
 *
 * Was a saturated accent-gradient banner with white text and an emoji pencil —
 * the loudest thing on the screen and the main reason it read as a template.
 * Now it speaks the same language as the rest of the app: serif name, mono
 * plan line, accent used as a quiet tint rather than a full-bleed fill.
 */
@Composable
fun GradientProfileCard(
    name: String,
    plan: String,
    initials: String,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
) {
    val palette = LocalAccentPalette.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(CardRadius)
            // A soft accent wash rather than a solid gradient: enough to mark
            // this as the identity block, not enough to shout.
            .background(
                Brush.horizontalGradient(
                    listOf(palette.soft, palette.soft.copy(alpha = 0.35f)),
                )
            )
            .border(width = 1.dp, color = palette.mid, shape = CardRadius)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(palette.primary),
        ) {
            Text(
                text = initials.take(2).uppercase(),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    letterSpacing = 0.04.sp,
                    // The accent is bright in dark mode and deep in light mode,
                    // so the initials flip to keep contrast either way.
                    color = if (palette.isDark) TextPrimary else androidx.compose.ui.graphics.Color.White,
                ),
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.01).sp,
                    color = TextPrimary,
                ),
                maxLines = 1,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = plan.uppercase(),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 0.08.sp,
                    color = TextTertiary,
                ),
                maxLines = 1,
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onEditClick),
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Adı düzenle",
                tint = palette.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun ProfileCardPreview() {
    GencIptvTheme {
        GradientProfileCard(
            name = "Mehmet Kaya",
            plan = "Premium Plan · Son: 07 Ağu 2026",
            initials = "MK",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun ProfileCardDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        GradientProfileCard(
            name = "Ahmet Yılmaz",
            plan = "Ücretsiz Plan",
            initials = "AY",
            modifier = Modifier.padding(16.dp),
        )
    }
}
