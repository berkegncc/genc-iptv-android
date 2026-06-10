package com.genciptv.player.feature.profile.subtitle

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.IcBlueSoft
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.OrangeSoft
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.SettingGroupCard
import com.genciptv.player.core.ui.SettingRow
import com.genciptv.player.core.ui.SettingRowDivider
import com.genciptv.player.core.ui.readableContentWidth
import com.genciptv.player.data.model.SubtitleEdgeType
import com.genciptv.player.data.model.SubtitleFontFamily
import com.genciptv.player.data.model.SubtitleFontStyle
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.SubtitleVerticalPosition
import kotlin.math.roundToInt

// ── Label extensions for enums ────────────────────────────────────────────────

fun SubtitleFontFamily.label(): String = when (this) {
    SubtitleFontFamily.SYSTEM -> "Sistem"
    SubtitleFontFamily.SANS_SERIF -> "Sans-Serif"
    SubtitleFontFamily.SERIF -> "Serif"
    SubtitleFontFamily.MONOSPACE -> "Monospace"
    SubtitleFontFamily.CASUAL -> "Casual"
    SubtitleFontFamily.CURSIVE -> "Cursive"
}

fun SubtitleFontStyle.label(): String = when (this) {
    SubtitleFontStyle.NORMAL -> "Normal"
    SubtitleFontStyle.BOLD -> "Kalın"
    SubtitleFontStyle.ITALIC -> "İtalik"
    SubtitleFontStyle.BOLD_ITALIC -> "Kalın İtalik"
}

fun SubtitleEdgeType.label(): String = when (this) {
    SubtitleEdgeType.NONE -> "Yok"
    SubtitleEdgeType.OUTLINE -> "Dış Çizgi"
    SubtitleEdgeType.DROP_SHADOW -> "Gölge"
    SubtitleEdgeType.RAISED -> "Kabartma"
    SubtitleEdgeType.DEPRESSED -> "Batırma"
}

fun SubtitleVerticalPosition.label(): String = when (this) {
    SubtitleVerticalPosition.BOTTOM -> "Alt"
    SubtitleVerticalPosition.CENTER -> "Orta"
    SubtitleVerticalPosition.TOP -> "Üst"
}

// ── Color helpers ─────────────────────────────────────────────────────────────

fun Int.withAlpha(alpha01: Float): Int {
    val a = (alpha01 * 255).toInt().coerceIn(0, 255)
    return (a shl 24) or (this and 0x00FFFFFF)
}

fun Int.alphaPercent(): Int = ((this ushr 24) and 0xFF) * 100 / 255

fun Int.toComposeColor(): Color = Color(this.toLong() and 0xFFFFFFFFL)

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun SubtitleSettingsScreen(
    viewModel: SubtitleSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val style by viewModel.subtitleStyle.collectAsStateWithLifecycle()

    SubtitleSettingsContent(
        style = style,
        onBack = onBack,
        onUpdate = viewModel::update,
        onReset = viewModel::reset,
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@Composable
fun SubtitleSettingsContent(
    style: SubtitleStyle,
    onBack: () -> Unit,
    onUpdate: (SubtitleStyle) -> Unit,
    onReset: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    var showResetDialog by remember { mutableStateOf(false) }

    // Bottom sheet states
    var showFontFamilySheet by remember { mutableStateOf(false) }
    var showFontStyleSheet by remember { mutableStateOf(false) }
    var showEdgeTypeSheet by remember { mutableStateOf(false) }
    var showVerticalPosSheet by remember { mutableStateOf(false) }
    var showTextColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }
    var showWindowColorPicker by remember { mutableStateOf(false) }
    var showEdgeColorPicker by remember { mutableStateOf(false) }

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
                text = "Altyazı Görünümü",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center),
            )
            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text("Sıfırla", color = accent.primary)
            }
        }

        Column(
            modifier = Modifier
                .readableContentWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Live preview card
            SubtitlePreviewCard(style = style)

            Spacer(Modifier.height(20.dp))

            // Font section
            SectionLabel("Font")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🔤",
                    iconBgColor = accent.soft,
                    label = "Font Ailesi",
                    subtitle = style.fontFamily.label(),
                    onClick = { showFontFamilySheet = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🅱",
                    iconBgColor = IcBlueSoft,
                    label = "Font Stili",
                    subtitle = style.fontStyle.label(),
                    onClick = { showFontStyleSheet = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "📏",
                    iconBgColor = OrangeSoft,
                    label = "Metin Boyutu",
                    subtitle = "${style.textSizePercent}%",
                    trailing = {},
                )
                // Slider row
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Slider(
                        value = style.textSizePercent.toFloat(),
                        onValueChange = { onUpdate(style.copy(textSizePercent = it.roundToInt())) },
                        valueRange = 50f..200f,
                        steps = 29, // (200-50)/5 = 30 steps → steps param = 30-1 = 29
                        colors = SliderDefaults.colors(thumbColor = accent.primary, activeTrackColor = accent.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Colors section
            SectionLabel("Renkler")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🎨",
                    iconBgColor = accent.soft,
                    label = "Metin Rengi",
                    trailing = {
                        ColorSwatch(color = style.textColor.toComposeColor(), onClick = { showTextColorPicker = true })
                    }
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🔲",
                    iconBgColor = IcBlueSoft,
                    label = "Metin Opaklığı",
                    subtitle = "${style.textColor.alphaPercent()}%",
                    trailing = {},
                )
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Slider(
                        value = style.textColor.alphaPercent().toFloat(),
                        onValueChange = { pct ->
                            val newAlpha = pct / 100f
                            onUpdate(style.copy(textColor = style.textColor.withAlpha(newAlpha)))
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = accent.primary, activeTrackColor = accent.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SettingRowDivider()
                SettingRow(
                    icon = "🎨",
                    iconBgColor = OrangeSoft,
                    label = "Arka Plan Rengi",
                    trailing = {
                        ColorSwatch(color = style.backgroundColor.toComposeColor(), onClick = { showBgColorPicker = true })
                    }
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🔲",
                    iconBgColor = IcBlueSoft,
                    label = "Arka Plan Opaklığı",
                    subtitle = "${style.backgroundColor.alphaPercent()}%",
                    trailing = {},
                )
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Slider(
                        value = style.backgroundColor.alphaPercent().toFloat(),
                        onValueChange = { pct ->
                            val newAlpha = pct / 100f
                            onUpdate(style.copy(backgroundColor = style.backgroundColor.withAlpha(newAlpha)))
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = accent.primary, activeTrackColor = accent.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SettingRowDivider()
                SettingRow(
                    icon = "🪟",
                    iconBgColor = accent.soft,
                    label = "Pencere Rengi",
                    trailing = {
                        ColorSwatch(color = style.windowColor.toComposeColor(), onClick = { showWindowColorPicker = true })
                    }
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🪟",
                    iconBgColor = IcBlueSoft,
                    label = "Pencere Opaklığı",
                    subtitle = "${style.windowColor.alphaPercent()}%",
                    trailing = {},
                )
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Slider(
                        value = style.windowColor.alphaPercent().toFloat(),
                        onValueChange = { pct ->
                            val newAlpha = pct / 100f
                            onUpdate(style.copy(windowColor = style.windowColor.withAlpha(newAlpha)))
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(thumbColor = accent.primary, activeTrackColor = accent.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Edge section
            SectionLabel("Kenar")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🔲",
                    iconBgColor = accent.soft,
                    label = "Kenar Tipi",
                    subtitle = style.edgeType.label(),
                    onClick = { showEdgeTypeSheet = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🎨",
                    iconBgColor = IcBlueSoft,
                    label = "Kenar Rengi",
                    trailing = {
                        ColorSwatch(
                            color = style.edgeColor.toComposeColor(),
                            onClick = { if (style.edgeType != SubtitleEdgeType.NONE) showEdgeColorPicker = true },
                            enabled = style.edgeType != SubtitleEdgeType.NONE,
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            // Position section
            SectionLabel("Konum")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "📍",
                    iconBgColor = accent.soft,
                    label = "Dikey Konum",
                    subtitle = style.verticalPosition.label(),
                    onClick = { showVerticalPosSheet = true },
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Sıfırla") },
            text = { Text("Altyazı ayarlarını varsayılana döndürmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    onReset()
                    showResetDialog = false
                }) { Text("Sıfırla") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("İptal") }
            }
        )
    }

    // Font family sheet
    if (showFontFamilySheet) {
        RadioPickerSheet(
            title = "Font Ailesi",
            options = SubtitleFontFamily.entries.map { it to it.label() },
            selected = style.fontFamily,
            onSelect = { onUpdate(style.copy(fontFamily = it)); showFontFamilySheet = false },
            onDismiss = { showFontFamilySheet = false },
        )
    }

    // Font style sheet
    if (showFontStyleSheet) {
        RadioPickerSheet(
            title = "Font Stili",
            options = SubtitleFontStyle.entries.map { it to it.label() },
            selected = style.fontStyle,
            onSelect = { onUpdate(style.copy(fontStyle = it)); showFontStyleSheet = false },
            onDismiss = { showFontStyleSheet = false },
        )
    }

    // Edge type sheet
    if (showEdgeTypeSheet) {
        RadioPickerSheet(
            title = "Kenar Tipi",
            options = SubtitleEdgeType.entries.map { it to it.label() },
            selected = style.edgeType,
            onSelect = { onUpdate(style.copy(edgeType = it)); showEdgeTypeSheet = false },
            onDismiss = { showEdgeTypeSheet = false },
        )
    }

    // Vertical position sheet
    if (showVerticalPosSheet) {
        RadioPickerSheet(
            title = "Dikey Konum",
            options = SubtitleVerticalPosition.entries.map { it to it.label() },
            selected = style.verticalPosition,
            onSelect = { onUpdate(style.copy(verticalPosition = it)); showVerticalPosSheet = false },
            onDismiss = { showVerticalPosSheet = false },
        )
    }

    // Color pickers
    if (showTextColorPicker) {
        ColorPickerSheet(
            title = "Metin Rengi",
            currentRgb = style.textColor and 0x00FFFFFF,
            onSelect = { rgb ->
                val alpha = style.textColor ushr 24
                onUpdate(style.copy(textColor = (alpha shl 24) or rgb))
                showTextColorPicker = false
            },
            onDismiss = { showTextColorPicker = false },
        )
    }

    if (showBgColorPicker) {
        ColorPickerSheet(
            title = "Arka Plan Rengi",
            currentRgb = style.backgroundColor and 0x00FFFFFF,
            onSelect = { rgb ->
                val alpha = style.backgroundColor ushr 24
                onUpdate(style.copy(backgroundColor = (alpha shl 24) or rgb))
                showBgColorPicker = false
            },
            onDismiss = { showBgColorPicker = false },
            includeTransparent = true,
        )
    }

    if (showWindowColorPicker) {
        ColorPickerSheet(
            title = "Pencere Rengi",
            currentRgb = style.windowColor and 0x00FFFFFF,
            onSelect = { rgb ->
                val alpha = style.windowColor ushr 24
                onUpdate(style.copy(windowColor = (alpha shl 24) or rgb))
                showWindowColorPicker = false
            },
            onDismiss = { showWindowColorPicker = false },
            includeTransparent = true,
        )
    }

    if (showEdgeColorPicker) {
        ColorPickerSheet(
            title = "Kenar Rengi",
            currentRgb = style.edgeColor and 0x00FFFFFF,
            onSelect = { rgb ->
                val alpha = style.edgeColor ushr 24
                onUpdate(style.copy(edgeColor = (alpha shl 24) or rgb))
                showEdgeColorPicker = false
            },
            onDismiss = { showEdgeColorPicker = false },
        )
    }
}

// ── Subtitle preview card ─────────────────────────────────────────────────────

@Composable
private fun SubtitlePreviewCard(style: SubtitleStyle) {
    // Diagonal sunset gradient — covers the full lightness range (bright yellow →
    // dark navy) so any subtitle position lands on both light and dark zones at
    // once. The previous near-black gradient hid translucent dark backgrounds
    // entirely; opacity changes are now clearly visible against the bright tones.
    // `Brush.linearGradient` defaults to a top-left → bottom-right diagonal.
    val gradientBg = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD54F), // bright yellow (top-left)
            Color(0xFFFF7043), // orange
            Color(0xFFE91E63), // hot pink
            Color(0xFF5E35B1), // deep purple
            Color(0xFF1A237E), // dark navy (bottom-right)
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(gradientBg)
    ) {
        val verticalAlignment = when (style.verticalPosition) {
            SubtitleVerticalPosition.BOTTOM -> Alignment.BottomCenter
            SubtitleVerticalPosition.CENTER -> Alignment.Center
            SubtitleVerticalPosition.TOP -> Alignment.TopCenter
        }

        val sampleText = "Bu bir altyazı örneğidir.\nİkinci satır da burada görünür."

        val textColor = style.textColor.toComposeColor()
        val bgColor = style.backgroundColor.toComposeColor()
        val windowColor = style.windowColor.toComposeColor()
        val edgeColor = style.edgeColor.toComposeColor()

        val fontFamily = when (style.fontFamily) {
            SubtitleFontFamily.SYSTEM -> FontFamily.Default
            SubtitleFontFamily.SANS_SERIF -> FontFamily.SansSerif
            SubtitleFontFamily.SERIF -> FontFamily.Serif
            SubtitleFontFamily.MONOSPACE -> FontFamily.Monospace
            SubtitleFontFamily.CASUAL -> FontFamily.Cursive
            SubtitleFontFamily.CURSIVE -> FontFamily.Cursive
        }

        val fontWeight = when (style.fontStyle) {
            SubtitleFontStyle.BOLD, SubtitleFontStyle.BOLD_ITALIC -> FontWeight.Bold
            else -> FontWeight.Normal
        }

        val fontStyle = when (style.fontStyle) {
            SubtitleFontStyle.ITALIC, SubtitleFontStyle.BOLD_ITALIC -> FontStyle.Italic
            else -> FontStyle.Normal
        }

        val baseFontSize = 14.sp
        val fontSize = baseFontSize * (style.textSizePercent / 100f)

        val shadow: Shadow? = when (style.edgeType) {
            SubtitleEdgeType.DROP_SHADOW -> Shadow(
                color = edgeColor,
                offset = Offset(2f, 2f),
                blurRadius = 4f,
            )
            SubtitleEdgeType.RAISED -> Shadow(
                color = edgeColor,
                offset = Offset(-1f, -1f),
                blurRadius = 0f,
            )
            SubtitleEdgeType.DEPRESSED -> Shadow(
                color = edgeColor,
                offset = Offset(1f, 1f),
                blurRadius = 0f,
            )
            else -> null
        }

        // Window color box
        Box(
            contentAlignment = verticalAlignment,
            modifier = Modifier
                .fillMaxSize()
                .background(windowColor)
        ) {
            val vertPadding = if (style.verticalPosition == SubtitleVerticalPosition.BOTTOM) 12.dp else 0.dp
            Box(
                modifier = Modifier
                    .padding(
                        bottom = if (style.verticalPosition == SubtitleVerticalPosition.BOTTOM) 12.dp else 0.dp,
                        top = if (style.verticalPosition == SubtitleVerticalPosition.TOP) 12.dp else 0.dp,
                    )
            ) {
                if (style.edgeType == SubtitleEdgeType.OUTLINE) {
                    // Draw outline by rendering text 4 times offset
                    val offsets = listOf(
                        Offset(-1f, 0f), Offset(1f, 0f),
                        Offset(0f, -1f), Offset(0f, 1f),
                    )
                    offsets.forEach { off ->
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(
                                text = sampleText,
                                color = edgeColor,
                                fontFamily = fontFamily,
                                fontWeight = fontWeight,
                                fontStyle = fontStyle,
                                fontSize = fontSize,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = edgeColor,
                                        offset = off * 1.5f,
                                        blurRadius = 0f,
                                    )
                                ),
                                modifier = Modifier.background(bgColor),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .background(bgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = sampleText,
                        color = textColor,
                        fontFamily = fontFamily,
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        fontSize = fontSize,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = shadow),
                    )
                }
            }
        }
    }
}

// ── Radio picker bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> RadioPickerSheet(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            options.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(value) }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selected == value,
                        onClick = { onSelect(value) },
                        colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                    )
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Color picker bottom sheet ─────────────────────────────────────────────────

// Preset colors (RGB only, alpha managed separately)
private val PresetColors = listOf(
    "Beyaz" to 0x00FFFFFF,
    "Sarı" to 0x00FFEB3B,
    "Yeşil" to 0x0000E676,
    "Cyan" to 0x0000E5FF,
    "Mavi" to 0x002979FF,
    "Magenta" to 0x00D500F9,
    "Kırmızı" to 0x00FF1744,
    "Siyah" to 0x00000000,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerSheet(
    title: String,
    currentRgb: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    includeTransparent: Boolean = false,
) {
    val accent = LocalAccentPalette.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                PresetColors.forEach { (name, rgb) ->
                    val isSelected = currentRgb == rgb
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000 or rgb.toLong()))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accent.primary else Border,
                                    shape = CircleShape,
                                )
                                .clickable { onSelect(rgb) }
                        )
                    }
                }
            }
            if (includeTransparent) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { onSelect(0x00000000) }) {
                    Text("Şeffaf")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Color swatch composable ───────────────────────────────────────────────────

@Composable
private fun ColorSwatch(
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (enabled) color else color.copy(alpha = 0.3f))
            .border(width = 1.dp, color = Border, shape = CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
    )
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary),
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SubtitleSettingsPreview() {
    GencIptvTheme {
        SubtitleSettingsContent(
            style = SubtitleStyle.Default,
            onBack = {},
            onUpdate = {},
            onReset = {},
        )
    }
}
