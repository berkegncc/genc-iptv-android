package com.genciptv.player.feature.profile.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
                _uiState.update { it.copy(error = "Aktif ayarlanamadı: ${e.message}") }
            }
        }
    }

    fun delete(playlist: Playlist) {
        viewModelScope.launch {
            try {
                playlistRepository.delete(playlist)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Silinemedi: ${e.message}") }
            }
        }
    }

    fun sync(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingIds = it.syncingIds + id) }
            try {
                playlistRepository.sync(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Senkronizasyon başarısız: ${e.message}") }
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
            "unknownhost" in msg || "no address" in msg -> "Sunucuya ulaşılamadı."
            "timeout" in msg || "connect" in msg -> "Bağlantı zaman aşımı."
            "empty" in msg || "no channel" in msg -> "Boş playlist."
            "401" in msg || "auth" in msg -> "Kimlik doğrulama başarısız."
            else -> "Hata: ${e.message ?: "bilinmeyen"}"
        }
    }
}
