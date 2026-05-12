package com.genciptv.player.feature.profile.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.IcBlueSoft
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.OrangeSoft
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.GencToggle
import com.genciptv.player.core.ui.SettingGroupCard
import com.genciptv.player.core.ui.SettingRow
import com.genciptv.player.core.ui.SettingRowDivider
import com.genciptv.player.data.model.DecoderPref
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.QualityPref

@Composable
fun PlayerSettingsScreen(
    viewModel: PlayerSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val player by viewModel.player.collectAsStateWithLifecycle()

    PlayerSettingsContent(
        player = player,
        onBack = onBack,
        onSetQuality = viewModel::setQuality,
        onSetDecoder = viewModel::setDecoder,
        onSetAudioLang = viewModel::setAudioLang,
        onToggleLoudness = viewModel::toggleLoudness,
        onSetUserAgent = viewModel::setUserAgent,
        onSetTrustAllCerts = viewModel::setTrustAllCerts,
        onTogglePip = viewModel::togglePip,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsContent(
    player: PlayerPreferences,
    onBack: () -> Unit,
    onSetQuality: (QualityPref) -> Unit,
    onSetDecoder: (DecoderPref) -> Unit,
    onSetAudioLang: (String?) -> Unit,
    onToggleLoudness: (Boolean) -> Unit,
    onSetUserAgent: (String?) -> Unit,
    onSetTrustAllCerts: (Boolean) -> Unit,
    onTogglePip: (Boolean) -> Unit,
) {
    val accent = LocalAccentPalette.current

    var showQualitySheet by remember { mutableStateOf(false) }
    var showDecoderSheet by remember { mutableStateOf(false) }
    var showAudioLangDialog by remember { mutableStateOf(false) }
    var showUserAgentDialog by remember { mutableStateOf(false) }
    var showSslWarningDialog by remember { mutableStateOf(false) }

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
                text = "Player Ayarları",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionLabel("Kalite & Codec")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🎥",
                    iconBgColor = accent.soft,
                    label = "Varsayılan Kalite",
                    subtitle = player.defaultQuality.label(),
                    onClick = { showQualitySheet = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🧩",
                    iconBgColor = IcBlueSoft,
                    label = "Decoder Tercihi",
                    subtitle = player.decoderPref.label(),
                    onClick = { showDecoderSheet = true },
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Ses & Dil")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🔊",
                    iconBgColor = OrangeSoft,
                    label = "Tercih Edilen Audio Dili",
                    subtitle = player.preferredAudioLang ?: "Yok",
                    onClick = { showAudioLangDialog = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "🔊",
                    iconBgColor = OrangeSoft,
                    label = "Ses Normalleştirme",
                    trailing = {
                        GencToggle(
                            checked = player.loudnessNormalization,
                            onCheckedChange = onToggleLoudness,
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Ağ")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "🌐",
                    iconBgColor = IcBlueSoft,
                    label = "User-Agent Override",
                    subtitle = player.userAgent ?: "Varsayılan",
                    onClick = { showUserAgentDialog = true },
                )
                SettingRowDivider()
                SettingRow(
                    icon = "⚠️",
                    iconBgColor = com.genciptv.player.core.designsystem.LiveSoft,
                    label = "SSL Doğrulamayı Atla",
                    subtitle = "DİKKAT: Güvensiz",
                    trailing = {
                        GencToggle(
                            checked = player.trustAllCerts,
                            onCheckedChange = { newValue ->
                                if (newValue) showSslWarningDialog = true
                                else onSetTrustAllCerts(false)
                            },
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            SectionLabel("Oynatma")
            Spacer(Modifier.height(8.dp))
            SettingGroupCard {
                SettingRow(
                    icon = "📺",
                    iconBgColor = accent.soft,
                    label = "Resim İçinde Resim",
                    trailing = {
                        GencToggle(
                            checked = player.pictureInPicture,
                            onCheckedChange = onTogglePip,
                        )
                    }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Quality picker sheet
    if (showQualitySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showQualitySheet = false },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Varsayılan Kalite",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                QualityPref.entries.forEach { q ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetQuality(q)
                                showQualitySheet = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = player.defaultQuality == q,
                            onClick = {
                                onSetQuality(q)
                                showQualitySheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                        )
                        Text(text = q.label(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Decoder picker sheet
    if (showDecoderSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showDecoderSheet = false },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Decoder Tercihi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                DecoderPref.entries.forEach { d ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetDecoder(d)
                                showDecoderSheet = false
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = player.decoderPref == d,
                            onClick = {
                                onSetDecoder(d)
                                showDecoderSheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                        )
                        Text(text = d.label(), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Audio lang dialog
    if (showAudioLangDialog) {
        var text by remember { mutableStateOf(player.preferredAudioLang ?: "") }
        AlertDialog(
            onDismissRequest = { showAudioLangDialog = false },
            title = { Text("Tercih Edilen Audio Dili") },
            text = {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Dil kodu (ISO 639-1)") },
                        placeholder = { Text("tr, en, de...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent.primary,
                            focusedLabelColor = accent.primary,
                            cursorColor = accent.primary,
                        ),
                    )
                    Text(
                        text = "Örnek: tr (Türkçe), en (İngilizce)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetAudioLang(text.ifBlank { null })
                    showAudioLangDialog = false
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showAudioLangDialog = false }) { Text("İptal") }
            }
        )
    }

    // User-Agent dialog
    if (showUserAgentDialog) {
        var text by remember { mutableStateOf(player.userAgent ?: "") }
        AlertDialog(
            onDismissRequest = { showUserAgentDialog = false },
            title = { Text("User-Agent Override") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("User-Agent") },
                    placeholder = { Text("VLC/3.0 (boş = varsayılan)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent.primary,
                        focusedLabelColor = accent.primary,
                        cursorColor = accent.primary,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetUserAgent(text.ifBlank { null })
                    showUserAgentDialog = false
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showUserAgentDialog = false }) { Text("İptal") }
            }
        )
    }

    // SSL warning dialog
    if (showSslWarningDialog) {
        AlertDialog(
            onDismissRequest = { showSslWarningDialog = false },
            title = { Text("Güvenlik Uyarısı") },
            text = {
                Text("Bu ayar man-in-the-middle saldırılarına açıktır. Yalnızca güvendiğiniz özel IPTV sunucularınız için kullanın. Devam etmek istiyor musunuz?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetTrustAllCerts(true)
                    showSslWarningDialog = false
                }) { Text("Devam Et", color = Live) }
            },
            dismissButton = {
                TextButton(onClick = { showSslWarningDialog = false }) { Text("İptal") }
            }
        )
    }
}

// ── Label extensions ──────────────────────────────────────────────────────────

fun QualityPref.label(): String = when (this) {
    QualityPref.AUTO -> "Otomatik"
    QualityPref.P1080 -> "1080p"
    QualityPref.P720 -> "720p"
    QualityPref.P480 -> "480p"
}

fun DecoderPref.label(): String = when (this) {
    DecoderPref.AUTO -> "Otomatik"
    DecoderPref.HW -> "Donanım"
    DecoderPref.SW -> "Yazılım"
}

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
private fun PlayerSettingsPreview() {
    GencIptvTheme {
        PlayerSettingsContent(
            player = PlayerPreferences(
                defaultQuality = QualityPref.AUTO,
                decoderPref = DecoderPref.AUTO,
                preferredAudioLang = "tr",
                loudnessNormalization = false,
                pictureInPicture = true,
                trustAllCerts = false,
            ),
            onBack = {},
            onSetQuality = {},
            onSetDecoder = {},
            onSetAudioLang = {},
            onToggleLoudness = {},
            onSetUserAgent = {},
            onSetTrustAllCerts = {},
            onTogglePip = {},
        )
    }
}
