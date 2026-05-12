package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextSecondary

private val ChipShape = RoundedCornerShape(50.dp)

/**
 * Horizontal scrollable chip row matching `.chips` / `.chip` / `.chip.active`.
 * Active chip = accent bg + white text + accent shadow.
 * Inactive chip = surface bg + 1.5dp border + secondary text.
 */
@Composable
fun CategoryChipRow(
    chips: List<String>,
    activeIndex: Int,
    onChipSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalAccentPalette.current.primary

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
    ) {
        itemsIndexed(chips) { index, chip ->
            val isActive = index == activeIndex
            val chipBg = if (isActive) accent else Surface
            val chipBorder = if (isActive) accent else Border
            val textColor = if (isActive) Color.White else TextSecondary

            val chipModifier = if (isActive) {
                Modifier
                    .padding(end = 8.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = ChipShape,
                        spotColor = accent.copy(alpha = 0.28f),
                        ambientColor = accent.copy(alpha = 0.1f)
                    )
                    .clip(ChipShape)
                    .background(chipBg)
                    .border(width = 1.5.dp, color = chipBorder, shape = ChipShape)
                    .clickable { onChipSelected(index) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            } else {
                Modifier
                    .padding(end = 8.dp)
                    .clip(ChipShape)
                    .background(chipBg)
                    .border(width = 1.5.dp, color = chipBorder, shape = ChipShape)
                    .clickable { onChipSelected(index) }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            }

            Text(
                text = chip,
                style = MaterialTheme.typography.titleSmall.copy(color = textColor),
                modifier = chipModifier
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val sampleChips = listOf("Tümü", "Canlı TV", "Filmler", "Diziler", "Spor", "Haberler")

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun CategoryChipRowPreview() {
    GencIptvTheme {
        CategoryChipRow(
            chips = sampleChips,
            activeIndex = 0,
            onChipSelected = {},
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun CategoryChipRowMiddleActivePreview() {
    GencIptvTheme {
        CategoryChipRow(
            chips = sampleChips,
            activeIndex = 2,
            onChipSelected = {},
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun CategoryChipRowInteractivePreview() {
    var selected by remember { mutableIntStateOf(0) }
    GencIptvTheme {
        CategoryChipRow(
            chips = sampleChips,
            activeIndex = selected,
            onChipSelected = { selected = it },
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
