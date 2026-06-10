package com.genciptv.player.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.GreenSoft
import com.genciptv.player.core.designsystem.IcBlueSoft
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LiveSoft
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.OrangeSoft
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.GencToggle
import com.genciptv.player.core.ui.GradientProfileCard
import com.genciptv.player.core.ui.readableContentWidth
import com.genciptv.player.core.ui.SettingGroupCard
import com.genciptv.player.core.ui.SettingRow
import com.genciptv.player.core.ui.SettingRowDivider
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.UserPreferences

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToPlaylistManager: () -> Unit,
    onNavigateToPlayerSettings: () -> Unit,
    onNavigateToSubtitleSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToVod: ((kind: String) -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onBack = onBack,
        onNavigateToPlaylistManager = onNavigateToPlaylistManager,
        onNavigateToPlayerSettings = onNavigateToPlayerSettings,
        onNavigateToSubtitleSettings = onNavigateToSubtitleSettings,
        onNavigateToThemeSettings = onNavigateToThemeSettings,
        onNavigateToHome = onNavigateToHome,
        onNavigateToChannels = onNavigateToChannels,
        onNavigateToGuide = onNavigateToGuide,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToVod = onNavigateToVod,
        onSetDisplayName = viewModel::setDisplayName,
        onToggleAutoUpdate = viewModel::toggleAutoUpdate,
        onToggleLoudness = viewModel::toggleLoudness,
        onTogglePip = viewModel::togglePip,
        onLogout = viewModel::logout,
    )
}

// ── Stateless content composable ──────────────────────────────────────────────

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onNavigateToPlaylistManager: () -> Unit,
    onNavigateToPlayerSettings: () -> Unit,
    onNavigateToSubtitleSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToVod: ((kind: String) -> Unit)? = null,
    onSetDisplayName: (String) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onToggleLoudness: (Boolean) -> Unit,
    onTogglePip: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    GencAdaptiveScaffold(
        current = GencNavItem.PROFILE,
        onItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> onNavigateToHome()
                GencNavItem.CHANNELS -> onNavigateToChannels()
                GencNavItem.MOVIES   -> onNavigateToVod?.invoke("MOVIE")
                GencNavItem.SERIES   -> onNavigateToVod?.invoke("SERIES")
                GencNavItem.PROFILE  -> { /* already here */ }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(innerPadding)
        ) {
            // Header
            ProfileHeader(onBack = onBack)

            // Scrollable content — capped to a readable width and centred on
            // tablets (no-op on phones where the screen is already narrower).
            Column(
                modifier = Modifier
                    .readableContentWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Gradient profile card
                val initials = buildInitials(uiState.user.displayName)
                GradientProfileCard(
                    name = uiState.user.displayName.ifBlank { "Kullanıcı" },
                    plan = uiState.planText,
                    initials = initials,
                    onEditClick = { showEditNameDialog = true },
                )

                Spacer(Modifier.height(20.dp))

                // ── Keşfet section — Guide + Favoriler ────────────────────────
                SectionTitle("Keşfet")
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = "⭐",
                        iconBgColor = OrangeSoft,
                        label = "Favoriler",
                        subtitle = "Kanallarım, filmlerim, dizilerim",
                        onClick = onNavigateToFavorites,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = "📅",
                        iconBgColor = IcBlueSoft,
                        label = "Program Rehberi",
                        subtitle = "Canlı TV EPG",
                        onClick = onNavigateToGuide,
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Section label
                SectionTitle("Playlist")
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = "📋",
                        iconBgColor = accent.soft,
                        label = "Playlist Yönetimi",
                        subtitle = "${uiState.playlistCount} playlist ekli",
                        onClick = onNavigateToPlaylistManager,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = "🔄",
                        iconBgColor = OrangeSoft,
                        label = "Otomatik Güncelleme",
                        subtitle = "Her 24 saatte bir",
                        trailing = {
                            GencToggle(
                                checked = uiState.user.autoUpdateEnabled,
                                onCheckedChange = onToggleAutoUpdate,
                            )
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle("Oynatıcı")
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = "🎥",
                        iconBgColor = accent.soft,
                        label = "Player Ayarları",
                        subtitle = "Varsayılan kalite, audio, decoder",
                        onClick = onNavigateToPlayerSettings,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = "📝",
                        iconBgColor = GreenSoft,
                        label = "Altyazı Görünümü",
                        subtitle = "Yazı tipi, renk, boyut",
                        onClick = onNavigateToSubtitleSettings,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = "🔊",
                        iconBgColor = OrangeSoft,
                        label = "Ses Normalleştirme",
                        subtitle = "Ses seviyesini dengele",
                        trailing = {
                            GencToggle(
                                checked = uiState.player.loudnessNormalization,
                                onCheckedChange = onToggleLoudness,
                            )
                        }
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = "📺",
                        iconBgColor = IcBlueSoft,
                        label = "Resim İçinde Resim",
                        subtitle = "Arka planda izle",
                        trailing = {
                            GencToggle(
                                checked = uiState.player.pictureInPicture,
                                onCheckedChange = onTogglePip,
                            )
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle("Görünüm")
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = "🌙",
                        iconBgColor = accent.soft,
                        label = "Tema & Renk",
                        subtitle = buildThemeSubtitle(uiState),
                        onClick = onNavigateToThemeSettings,
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle("Hesap")
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = "🚪",
                        iconBgColor = LiveSoft,
                        label = "Çıkış Yap",
                        onClick = { showLogoutDialog = true },
                        trailing = {
                            Text(
                                text = "›",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Live)
                            )
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Edit name dialog
    if (showEditNameDialog) {
        EditNameDialog(
            currentName = uiState.user.displayName,
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                onSetDisplayName(newName)
                showEditNameDialog = false
            }
        )
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Çıkış yapmak istediğinize emin misiniz? Aktif playlist silinecek.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("Çıkış Yap", color = Live)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .border(width = 1.dp, color = Border, shape = androidx.compose.ui.graphics.RectangleShape)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Back button
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

        // Title centered
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = TextPrimary,
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            color = TextTertiary,
        ),
    )
}

// ── Edit name dialog ──────────────────────────────────────────────────────────

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentName) }
    val accent = LocalAccentPalette.current.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İsmi Düzenle") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Görünen İsim") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text) },
                enabled = text.isNotBlank(),
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

internal fun buildInitials(name: String): String {
    if (name.isBlank()) return "K"
    val parts = name.trim().split("\\s+".toRegex())
    return if (parts.size >= 2) {
        "${parts[0].first()}${parts[1].first()}".uppercase()
    } else {
        parts[0].first().toString().uppercase()
    }
}

@Composable
private fun buildThemeSubtitle(uiState: ProfileUiState): String {
    // We don't have direct access to AppearancePreferences here; show static
    return "Açık · ${LocalAccentPalette.current.label}"
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ProfileContentPreview() {
    GencIptvTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = UserPreferences(displayName = "Mehmet Kaya", autoUpdateEnabled = true),
                player = PlayerPreferences(loudnessNormalization = false, pictureInPicture = true),
                playlistCount = 2,
                planText = "✨ Premium Plan · Son: 15 Nis 2026",
            ),
            onBack = {},
            onNavigateToPlaylistManager = {},
            onNavigateToPlayerSettings = {},
            onNavigateToSubtitleSettings = {},
            onNavigateToThemeSettings = {},
            onNavigateToHome = {},
            onNavigateToChannels = {},
            onNavigateToGuide = {},
            onNavigateToFavorites = {},
            onNavigateToVod = {},
            onSetDisplayName = {},
            onToggleAutoUpdate = {},
            onToggleLoudness = {},
            onTogglePip = {},
            onLogout = {},
        )
    }
}
