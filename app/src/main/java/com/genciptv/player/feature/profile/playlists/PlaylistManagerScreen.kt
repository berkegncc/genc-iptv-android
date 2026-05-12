package com.genciptv.player.feature.profile.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.PlaylistType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PlaylistManagerScreen(
    viewModel: PlaylistManagerViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlaylistManagerContent(
        uiState = uiState,
        onBack = onBack,
        onSetActive = viewModel::setActive,
        onDelete = viewModel::delete,
        onSync = viewModel::sync,
        onShowAddSheet = viewModel::showAddSheet,
        onHideAddSheet = viewModel::hideAddSheet,
        onAddM3u = viewModel::addM3u,
        onAddXtream = viewModel::addXtream,
        onDismissError = viewModel::dismissError,
    )
}

@Composable
fun PlaylistManagerContent(
    uiState: PlaylistManagerUiState,
    onBack: () -> Unit,
    onSetActive: (Long) -> Unit,
    onDelete: (Playlist) -> Unit,
    onSync: (Long) -> Unit,
    onShowAddSheet: () -> Unit,
    onHideAddSheet: () -> Unit,
    onAddM3u: (String, String, String?) -> Unit,
    onAddXtream: (String, String, String, String) -> Unit,
    onDismissError: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    var deleteCandidate by remember { mutableStateOf<Playlist?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddSheet,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Yeni Ekle") },
                containerColor = accent.primary,
                contentColor = androidx.compose.ui.graphics.Color.White,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Bg)
                .padding(innerPadding)
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
                    text = "Playlist Yönetimi",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (uiState.playlists.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    EmptyState(
                        title = "Henüz playlist eklenmemiş",
                        description = "Aşağıdaki + butonuna tıklayarak ekleyin",
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.playlists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            isSyncing = playlist.id in uiState.syncingIds,
                            onSetActive = { onSetActive(playlist.id) },
                            onSync = { onSync(playlist.id) },
                            onDelete = { deleteCandidate = playlist },
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    deleteCandidate?.let { playlist ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Playlist Sil") },
            text = { Text("\"${playlist.name}\" playlistini silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(playlist)
                    deleteCandidate = null
                }) {
                    Text("Sil", color = Live)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("İptal") }
            }
        )
    }

    // Error dialog
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Hata") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onDismissError) { Text("Tamam") }
            }
        )
    }

    // Add playlist bottom sheet
    if (uiState.showAddSheet) {
        AddPlaylistSheet(
            isLoading = uiState.isLoading,
            onDismiss = onHideAddSheet,
            onAddM3u = onAddM3u,
            onAddXtream = onAddXtream,
        )
    }
}

// ── Playlist card ─────────────────────────────────────────────────────────────

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isSyncing: Boolean,
    onSetActive: () -> Unit,
    onSync: () -> Unit,
    onDelete: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    var menuExpanded by remember { mutableStateOf(false) }
    val isActive = playlist.isActive
    val borderColor = if (isActive) accent.primary else Border
    val borderWidth = if (isActive) 2.dp else 1.5.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Type icon
        Text(
            text = if (playlist.type == PlaylistType.M3U) "📋" else "⚡",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.width(10.dp))

        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                if (isActive) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent.soft)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(color = accent.primary),
                        )
                    }
                }
            }
            Text(
                text = "${playlist.channelCount} kanal · ${formatRelativeTime(playlist.lastSyncedAt)}",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            )
        }

        // Sync indicator
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = accent.primary,
            )
            Spacer(Modifier.width(8.dp))
        }

        // Menu
        Box {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { menuExpanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Seçenekler",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Aktif yap") },
                    onClick = {
                        onSetActive()
                        menuExpanded = false
                    },
                    enabled = !isActive,
                )
                DropdownMenuItem(
                    text = { Text("Şimdi senkronize et") },
                    onClick = {
                        onSync()
                        menuExpanded = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Sil", color = Live) },
                    onClick = {
                        onDelete()
                        menuExpanded = false
                    },
                )
            }
        }
    }
}

private fun formatRelativeTime(millis: Long): String {
    if (millis == 0L) return "hiç senkronize edilmedi"
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "az önce"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} dk önce"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} sa önce"
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale("tr", "TR"))
            sdf.format(Date(millis))
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun PlaylistManagerPreview() {
    GencIptvTheme {
        PlaylistManagerContent(
            uiState = PlaylistManagerUiState(
                playlists = listOf(
                    Playlist(id = 1, name = "Türksat HD", type = PlaylistType.M3U, url = "http://example.com/list.m3u", isActive = true, channelCount = 423, lastSyncedAt = System.currentTimeMillis() - 3_600_000),
                    Playlist(id = 2, name = "Xtream Codes", type = PlaylistType.XTREAM, url = "http://example.com", username = "user", password = "pass", isActive = false, channelCount = 1200, lastSyncedAt = System.currentTimeMillis() - 86_400_000),
                )
            ),
            onBack = {},
            onSetActive = {},
            onDelete = {},
            onSync = {},
            onShowAddSheet = {},
            onHideAddSheet = {},
            onAddM3u = { _, _, _ -> },
            onAddXtream = { _, _, _, _ -> },
            onDismissError = {},
        )
    }
}
