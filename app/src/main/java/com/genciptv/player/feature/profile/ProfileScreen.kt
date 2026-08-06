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
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LocalAccentPalette
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
import com.genciptv.player.feature.update.UpdateDialogHost
import com.genciptv.player.feature.update.rememberUpdateViewModel
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.UserPreferences
import com.genciptv.player.core.util.AppLanguage
import com.genciptv.player.feature.profile.language.LanguageDialog
import com.genciptv.player.feature.profile.sync.SyncNetworkDialog
import androidx.compose.material.icons.outlined.Language

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

    // Shared with Ana Sayfa (Activity-scoped), so a download started there is
    // still running — and still visible — if the user wanders in here.
    val updateViewModel = rememberUpdateViewModel()
    UpdateDialogHost(updateViewModel)

    ProfileContent(
        uiState = uiState,
        appVersion = updateViewModel.currentVersion,
        onCheckUpdates = { updateViewModel.check(force = true) },
        onBack = onBack,
        onNavigateToPlaylistManager = onNavigateToPlaylistManager,
        onSetSyncOverWifiOnly = viewModel::setSyncOverWifiOnly,
        onNavigateToPlayerSettings = onNavigateToPlayerSettings,
        onNavigateToSubtitleSettings = onNavigateToSubtitleSettings,
        onNavigateToThemeSettings = onNavigateToThemeSettings,
        onNavigateToHome = onNavigateToHome,
        onNavigateToChannels = onNavigateToChannels,
        onNavigateToGuide = onNavigateToGuide,
        onNavigateToFavorites = onNavigateToFavorites,
        onNavigateToVod = onNavigateToVod,
        onSetDisplayName = viewModel::setDisplayName,
        onSetTmdbApiKey = viewModel::setTmdbApiKey,
        onToggleLoudness = viewModel::toggleLoudness,
        onTogglePip = viewModel::togglePip,
        onLogout = viewModel::logout,
    )
}

// ── Stateless content composable ──────────────────────────────────────────────

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    /** Running versionName, shown in the "Hakkında" group. */
    appVersion: String = "",
    /** User-initiated check — reports "you're up to date" when there is nothing. */
    onCheckUpdates: () -> Unit = {},
    onBack: () -> Unit,
    onNavigateToPlaylistManager: () -> Unit,
    onSetSyncOverWifiOnly: (Boolean) -> Unit,
    onNavigateToPlayerSettings: () -> Unit,
    onNavigateToSubtitleSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToVod: ((kind: String) -> Unit)? = null,
    onSetDisplayName: (String) -> Unit,
    onSetTmdbApiKey: (String) -> Unit = {},
    onToggleLoudness: (Boolean) -> Unit,
    onTogglePip: (Boolean) -> Unit,
    onLogout: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSyncNetworkDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showTmdbKeyDialog by remember { mutableStateOf(false) }
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
                    name = uiState.user.displayName.ifBlank { stringResource(R.string.profile_default_display_name) },
                    plan = uiState.planText,
                    initials = initials,
                    onEditClick = { showEditNameDialog = true },
                )

                Spacer(Modifier.height(20.dp))

                // ── Keşfet section — Guide + Favoriler ────────────────────────
                SectionTitle(stringResource(R.string.profile_section_discover))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.Outlined.StarBorder,
                        label = stringResource(R.string.term_favorites),
                        subtitle = stringResource(R.string.profile_favorites_subtitle),
                        onClick = onNavigateToFavorites,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.CalendarMonth,
                        label = stringResource(R.string.profile_guide_label),
                        subtitle = stringResource(R.string.profile_guide_subtitle),
                        onClick = onNavigateToGuide,
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Section label
                SectionTitle(stringResource(R.string.profile_section_playlist))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
                        label = stringResource(R.string.profile_playlist_management_label),
                        subtitle = pluralStringResource(
                            R.plurals.profile_playlist_count,
                            uiState.playlistCount,
                            uiState.playlistCount,
                        ),
                        onClick = onNavigateToPlaylistManager,
                    )
                    SettingRowDivider()
                    // Subtitle carries the current choice, matching the Language
                    // row above it — the value is the answer, so it belongs
                    // where the reader is already looking.
                    SettingRow(
                        icon = Icons.Outlined.Wifi,
                        label = stringResource(R.string.profile_sync_network_label),
                        subtitle = stringResource(
                            if (uiState.user.syncOverWifiOnly) R.string.profile_sync_option_wifi
                            else R.string.profile_sync_option_any
                        ),
                        onClick = { showSyncNetworkDialog = true },
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle(stringResource(R.string.profile_section_player))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.Outlined.Tune,
                        label = stringResource(R.string.profile_player_settings_label),
                        subtitle = stringResource(R.string.profile_player_settings_subtitle),
                        onClick = onNavigateToPlayerSettings,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.ClosedCaption,
                        label = stringResource(R.string.profile_subtitle_settings_label),
                        subtitle = stringResource(R.string.profile_subtitle_settings_subtitle),
                        onClick = onNavigateToSubtitleSettings,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.GraphicEq,
                        label = stringResource(R.string.profile_loudness_label),
                        subtitle = stringResource(R.string.profile_loudness_subtitle),
                        trailing = {
                            GencToggle(
                                checked = uiState.player.loudnessNormalization,
                                onCheckedChange = onToggleLoudness,
                            )
                        }
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.PictureInPictureAlt,
                        label = stringResource(R.string.profile_pip_label),
                        subtitle = stringResource(R.string.profile_pip_subtitle),
                        trailing = {
                            GencToggle(
                                checked = uiState.player.pictureInPicture,
                                onCheckedChange = onTogglePip,
                            )
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle(stringResource(R.string.profile_section_appearance))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.Outlined.Palette,
                        label = stringResource(R.string.profile_theme_color_label),
                        subtitle = buildThemeSubtitle(uiState),
                        onClick = onNavigateToThemeSettings,
                    )
                    // Sits under Appearance rather than in its own section: it
                    // is one row, and a section heading over a single item
                    // reads as an empty promise of more.
                    val currentLanguage = AppLanguage.current(LocalContext.current)
                    SettingRow(
                        icon = Icons.Outlined.Language,
                        label = stringResource(R.string.language_title),
                        subtitle = when (currentLanguage) {
                            AppLanguage.SYSTEM -> stringResource(R.string.language_subtitle_system)
                            AppLanguage.TURKISH -> stringResource(R.string.language_option_turkish)
                            AppLanguage.ENGLISH -> stringResource(R.string.language_option_english)
                        },
                        onClick = { showLanguageDialog = true },
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle(stringResource(R.string.profile_section_about))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.Outlined.FileDownload,
                        label = stringResource(R.string.profile_check_updates_label),
                        subtitle = stringResource(R.string.profile_check_updates_subtitle),
                        onClick = onCheckUpdates,
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.Movie,
                        label = stringResource(R.string.profile_tmdb_key_label),
                        subtitle = if (uiState.user.tmdbApiKey.isBlank()) {
                            stringResource(R.string.profile_tmdb_key_subtitle_empty)
                        } else {
                            stringResource(R.string.profile_tmdb_key_subtitle_set)
                        },
                        onClick = { showTmdbKeyDialog = true },
                        trailing = {
                            Text(
                                text = if (uiState.user.tmdbApiKey.isBlank()) stringResource(R.string.action_add) else "✓",
                                style = TextStyle(
                                    fontFamily = GeistMonoFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (uiState.user.tmdbApiKey.isBlank()) {
                                        TextTertiary
                                    } else {
                                        accent.primary
                                    },
                                ),
                            )
                        },
                    )
                    SettingRowDivider()
                    SettingRow(
                        icon = Icons.Outlined.Info,
                        label = stringResource(R.string.profile_version_label),
                        trailing = {
                            Text(
                                text = if (appVersion.isBlank()) "—" else "v$appVersion",
                                style = TextStyle(
                                    fontFamily = GeistMonoFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.02.sp,
                                    color = TextTertiary,
                                ),
                            )
                        },
                    )
                }

                Spacer(Modifier.height(20.dp))

                SectionTitle(stringResource(R.string.profile_section_account))
                Spacer(Modifier.height(8.dp))
                SettingGroupCard {
                    SettingRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        tint = Live,
                        label = stringResource(R.string.profile_logout_label),
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

    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }

    if (showSyncNetworkDialog) {
        SyncNetworkDialog(
            wifiOnly = uiState.user.syncOverWifiOnly,
            onSelect = onSetSyncOverWifiOnly,
            onDismiss = { showSyncNetworkDialog = false },
        )
    }

    if (showTmdbKeyDialog) {
        TmdbKeyDialog(
            currentKey = uiState.user.tmdbApiKey,
            onDismiss = { showTmdbKeyDialog = false },
            onSave = { key ->
                onSetTmdbApiKey(key)
                showTmdbKeyDialog = false
            },
        )
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
            title = { Text(stringResource(R.string.profile_logout_label)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLogout()
                        showLogoutDialog = false
                    }
                ) {
                    Text(stringResource(R.string.profile_logout_label), color = Live)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(onBack: () -> Unit) {
    // A hairline along the bottom edge only. `Modifier.border` outlines all
    // four sides, which boxed the title in a full rectangle — the heaviest
    // shape on the screen and the first thing the eye landed on.
    Column(modifier = Modifier.fillMaxWidth().background(Surface)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Back button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                modifier = Modifier.size(20.dp),
                tint = TextPrimary,
            )
        }

        // Title centered
        Text(
            text = stringResource(R.string.term_settings),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                letterSpacing = (-0.01).sp,
                color = TextPrimary,
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Border)
        )
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = GeistMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 0.12.sp,
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
        title = { Text(stringResource(R.string.profile_edit_name_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.profile_display_name_label)) },
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
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
internal fun buildInitials(name: String): String {
    if (name.isBlank()) return stringResource(R.string.profile_default_initials)
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
    return stringResource(R.string.profile_theme_subtitle, stringResource(LocalAccentPalette.current.labelRes))
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun ProfileContentPreview() {
    GencIptvTheme {
        ProfileContent(
            uiState = ProfileUiState(
                user = UserPreferences(displayName = "Mehmet Kaya"),
                player = PlayerPreferences(loudnessNormalization = false, pictureInPicture = true),
                playlistCount = 2,
                planText = "✨ Premium Plan · Son: 15 Nis 2026",
            ),
            onBack = {},
            onNavigateToPlaylistManager = {},
            onSetSyncOverWifiOnly = {},
            onNavigateToPlayerSettings = {},
            onNavigateToSubtitleSettings = {},
            onNavigateToThemeSettings = {},
            onNavigateToHome = {},
            onNavigateToChannels = {},
            onNavigateToGuide = {},
            onNavigateToFavorites = {},
            onNavigateToVod = {},
            onSetDisplayName = {},
            onToggleLoudness = {},
            onTogglePip = {},
            onLogout = {},
        )
    }
}

/**
 * Lets the user supply their own TMDb key.
 *
 * Published builds carry no key — one baked into the APK would be extractable
 * by anyone who downloads it, and every install would then share a single
 * quota. So actor photos and the poster fallback are opt-in: paste a key here
 * and they switch on, clear it and they switch off. The key never leaves the
 * device.
 */
@Composable
private fun TmdbKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val accent = LocalAccentPalette.current
    var text by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text(
                text = stringResource(R.string.profile_tmdb_key_label),
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.profile_tmdb_key_dialog_body),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.profile_tmdb_key_placeholder),
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary),
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = GeistMonoFamily,
                        color = TextPrimary,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent.primary,
                        unfocusedBorderColor = Border,
                        cursorColor = accent.primary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.trim()) }) {
                Text(
                    text = stringResource(R.string.action_save),
                    style = MaterialTheme.typography.labelLarge.copy(color = accent.primary),
                )
            }
        },
        dismissButton = {
            Row {
                // Clearing is the way to switch the TMDb features back off.
                if (currentKey.isNotBlank()) {
                    TextButton(onClick = { onSave("") }) {
                        Text(
                            text = stringResource(R.string.action_remove),
                            style = MaterialTheme.typography.labelLarge.copy(color = Live),
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.profile_tmdb_key_dismiss),
                        style = MaterialTheme.typography.labelLarge.copy(color = TextTertiary),
                    )
                }
            }
        },
    )
}
