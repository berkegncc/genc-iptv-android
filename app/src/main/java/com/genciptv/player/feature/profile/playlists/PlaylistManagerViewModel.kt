package com.genciptv.player.feature.profile.playlists

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.R
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistManagerUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val syncingIds: Set<Long> = emptySet(),
    val error: String? = null,
    val showAddSheet: Boolean = false,
)

@HiltViewModel
class PlaylistManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistManagerUiState())
    val uiState: StateFlow<PlaylistManagerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            playlistRepository.observeAll().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
    }

    fun setActive(id: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.setActive(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = context.getString(R.string.errors_playlist_set_active_failed, e.message ?: "")) }
            }
        }
    }

    fun delete(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.delete(playlist)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = context.getString(R.string.errors_playlist_delete_failed, e.message ?: "")) }
            }
        }
    }

    fun sync(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingIds = it.syncingIds + id) }
            try {
                playlistRepository.sync(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = context.getString(R.string.errors_playlist_sync_failed, e.message ?: "")) }
            } finally {
                _uiState.update { it.copy(syncingIds = it.syncingIds - id) }
            }
        }
    }

    fun addM3u(name: String, url: String, epgUrl: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                playlistRepository.addM3u(
                    name = name.ifBlank { "Playlist" },
                    url = url.trim(),
                    epgUrl = epgUrl?.ifBlank { null },
                )
                _uiState.update { it.copy(isLoading = false, showAddSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
            }
        }
    }

    fun addXtream(name: String, serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                playlistRepository.addXtream(
                    name = name.ifBlank { "Xtream" },
                    serverUrl = serverUrl.trim(),
                    username = username.trim(),
                    password = password,
                )
                _uiState.update { it.copy(isLoading = false, showAddSheet = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = mapError(e)) }
            }
        }
    }

    fun showAddSheet() = _uiState.update { it.copy(showAddSheet = true, error = null) }
    fun hideAddSheet() = _uiState.update { it.copy(showAddSheet = false, error = null) }
    fun dismissError() = _uiState.update { it.copy(error = null) }

    private fun mapError(e: Exception): String {
        val msg = (e.message ?: "").lowercase()
        return when {
            "unknownhost" in msg || "no address" in msg -> context.getString(R.string.errors_playlist_host_unreachable)
            "timeout" in msg || "connect" in msg -> context.getString(R.string.errors_playlist_connection_timeout)
            "empty" in msg || "no channel" in msg -> context.getString(R.string.errors_playlist_empty)
            "401" in msg || "auth" in msg -> context.getString(R.string.errors_playlist_auth_failed)
            else -> context.getString(
                R.string.errors_playlist_generic,
                e.message ?: context.getString(R.string.errors_unknown),
            )
        }
    }
}
