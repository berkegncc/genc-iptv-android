package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.CategoryGradient
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.LocalGencColors
import com.genciptv.player.core.designsystem.categoryGradientFor

/**
 * v2 category picker card — used on the Kanallar entry screen.
 *
 * Visual:
 *  - Dark theme: vivid Dusk gradient ([CategoryGradient.from] → [.to]).
 *  - Light theme: subtle washed-out tint ([CategoryGradient.tint]).
 *  - Always: oversized translucent glyph in the bottom-right corner that
 *    serves as silent identity for the category (◐ ◑ ◓ ◒ ● ◗ ◖).
 *  - Always: subtle film-grain overlay.
 *  - Top-left: monospaced index ("01", "02", …).
 *  - Bottom-left: serif category [name] + "X kanal" subtitle.
 *
 * Use in a 2-column LazyVerticalGrid; the card maintains a 1 : 0.85 aspect
 * ratio so two side-by-side cards balance with surrounding row spacing.
 */
@Composable
fun CategoryCard(
    name: String,
    channelCount: Int,
    index: Int,
    modifier: Modifier = Modifier,
    gradient: CategoryGradient = categoryGradientFor(index - 1),
    onClick: () -> Unit = {},
) {
    val isLight = !LocalGencColors.current.isDark
    val brush = if (isLight) {
        // Light: gradient from the tint colour to a slightly darker shade of itself
        Brush.linearGradient(
            colors = listOf(
                gradient.tint,
                gradient.tint.copy(alpha = 0.87f),
            ),
        )
    } else {
        Brush.linearGradient(colors = listOf(gradient.from, gradient.to))
    }

    val titleColor = if (isLight) Color(0xFF14120E) else Color.White
    val indexColor = if (isLight) gradient.from else Color.White.copy(alpha = 0.60f)
    val subtitleColor = if (isLight) gradient.from else Color.White.copy(alpha = 0.70f)
    val glyphColor = if (isLight) gradient.from.copy(alpha = 0.18f)
                     else gradient.to.copy(alpha = 0.32f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 0.85f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .drawFilmGrain(opacity = if (isLight) 0.04f else 0.07f),
    ) {
        // Oversized corner glyph
        Text(
            text = gradient.glyph,
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 140.sp,
                lineHeight = 140.sp,
                color = glyphColor,
            ),
            // Push the glyph past the bottom-right corner so only the upper-left
            // portion bleeds in. `offset` accepts negative values; `padding`
            // does not (Compose validates non-negative).
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 30.dp),
        )

        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            // Top-left: index
            Text(
                text = index.toString().padStart(2, '0'),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    letterSpacing = 0.08.sp,
                    color = indexColor,
                ),
            )

            // Bottom-left: name + count
            Column {
                Text(
                    text = name,
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        lineHeight = 23.sp,
                        letterSpacing = (-0.015).sp,
                        color = titleColor,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$channelCount kanal",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = subtitleColor,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun Modifier.drawFilmGrain(opacity: Float = 0.05f): Modifier = drawWithContent {
    drawContent()
    val step = 2.5.dp.toPx()
    val lineHeight = 0.5.dp.toPx()
    val color = Color.White.copy(alpha = opacity)
    var y = 0f
    while (y < size.height) {
        drawRect(
            color = color,
            topLeft = Offset(0f, y),
            size = Size(size.width, lineHeight),
        )
        y += step
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun CategoryCardDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(16.dp)) {
            CategoryCard(name = "Spor", channelCount = 42, index = 2)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun CategoryCardLightPreview() {
    GencIptvTheme(darkTheme = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            CategoryCard(name = "Belgesel", channelCount = 36, index = 4)
        }
    }
}
