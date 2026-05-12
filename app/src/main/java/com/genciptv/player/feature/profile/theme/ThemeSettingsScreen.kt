package com.genciptv.player.feature.profile.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.AccentPalette
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.data.model.AppearancePreferences
import com.genciptv.player.data.model.ThemeMode

@Composable
fun ThemeSettingsScreen(
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val appearance by viewModel.appearance.collectAsStateWithLifecycle()

    ThemeSettingsContent(
        appearance = appearance,
        onBack = onBack,
        onSetThemeMode = viewModel::setThemeMode,
        onSetAccentKey = viewModel::setAccentKey,
    )
}

@Composable
fun ThemeSettingsContent(
    appearance: AppearancePreferences,
    onBack: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetAccentKey: (String) -> Unit,
) {
    val currentAccent = LocalAccentPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .border(width = 1.dp, color = Border, shape = androidx.compose.ui.graphics.RectangleShape)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Surface2)
                    .clickable(onClick = onBack)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    modifier = Modifier.size(18.dp),
                    tint = TextPrimary,
                )
            }
            Text(
                text = "Görünüm & Tema",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Mode switcher
            Text(
                text = "TEMA MODU",
                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
            )
            Spacer(Modifier.height(8.dp))
            ThemeModeSwitcher(
                current = appearance.themeMode,
                onSelect = onSetThemeMode,
            )

            Spacer(Modifier.height(20.dp))

            // Theme preview — pass the enum key, not the resolved accent.
            ThemePreviewCard(
                accent = currentAccent.key,
                isDark = when (appearance.themeMode) {
                    ThemeMode.DARK -> true
                    else -> false
                },
            )

            Spacer(Modifier.height(20.dp))

            // Accent swatches
            Text(
                text = "ACCENT RENGİ",
                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
            )
            Spacer(Modifier.height(12.dp))

            // 4x2 grid — use LazyVerticalGrid in non-scrollable mode
            val palettes = AccentPalette.entries
            for (row in palettes.chunked(4)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { palette ->
                        AccentSwatch(
                            palette = palette,
                            isSelected = appearance.accentKey == palette.name,
                            onClick = { onSetAccentKey(palette.name) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Fill remaining cells if row has less than 4
                    repeat(4 - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(20.dp))

            // Icon preview (display-only)
            Text(
                text = "UYGULAMA İKONU ÖNİZLEME",
                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf(AccentPalette.PURPLE, AccentPalette.RED, AccentPalette.BLUE, AccentPalette.GREEN)
                    .forEach { palette ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(palette.primary, palette.gradientEnd)
                                        )
                                    )
                            ) {
                                Text(text = "📺", fontSize = 22.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = palette.label,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                            )
                        }
                    }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Theme mode switcher ───────────────────────────────────────────────────────

@Composable
private fun ThemeModeSwitcher(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val modes = listOf(ThemeMode.LIGHT to "Açık", ThemeMode.DARK to "Karanlık", ThemeMode.SYSTEM to "Sistem")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface2)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = current == mode
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (isSelected) Modifier.shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Surface else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                )
            }
        }
    }
}

// ── Theme preview card ────────────────────────────────────────────────────────

@Composable
private fun ThemePreviewCard(
    accent: AccentPalette,
    isDark: Boolean,
) {
    val bg = if (isDark) Color(0xFF0E0F18) else Bg
    val surf = if (isDark) Color(0xFF12131A) else Surface
    val textMain = if (isDark) Color(0xFFF0F1FA) else TextPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = 1.dp, color = Border, shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            // Mini header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(accent.primary, accent.gradientEnd))
                    )
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Genç IPTV",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Mini content rows
            repeat(2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(surf)
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accent.soft)
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .height(12.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isDark) Color(0xFF2A2B3D) else Surface2)
                    )
                }
                if (it == 0) Spacer(Modifier.height(4.dp))
            }
        }
    }
}

// ── Accent swatch ─────────────────────────────────────────────────────────────

@Composable
private fun AccentSwatch(
    palette: AccentPalette,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = com.genciptv.player.core.designsystem.LocalGencColors.current.isDark
    val swatchTop = if (isDark) palette.dark else palette.light
    val swatchBottom = if (isDark) palette.light else palette.dark
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) palette.dark else Border,
                shape = RoundedCornerShape(13.dp),
            )
            .then(
                if (isSelected) Modifier.shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(13.dp),
                    spotColor = palette.glow,
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .background(Surface)
    ) {
        // Two-stop gradient (light + dark variants of this accent)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.linearGradient(listOf(swatchTop, swatchBottom))
                )
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(vertical = 5.dp)
        ) {
            Text(
                text = palette.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isSelected) palette.dark else TextSecondary,
                    fontSize = 10.sp,
                ),
            )
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ThemeSettingsPreview() {
    GencIptvTheme {
        ThemeSettingsContent(
            appearance = AppearancePreferences(themeMode = ThemeMode.LIGHT, accentKey = "PURPLE"),
            onBack = {},
            onSetThemeMode = {},
            onSetAccentKey = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0F18)
@Composable
private fun ThemeSettingsDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        ThemeSettingsContent(
            appearance = AppearancePreferences(themeMode = ThemeMode.DARK, accentKey = "TEAL"),
            onBack = {},
            onSetThemeMode = {},
            onSetAccentKey = {},
        )
    }
}
