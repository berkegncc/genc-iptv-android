package com.genciptv.player.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.PlaylistType
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        userPreferencesRepository.user,
        userPreferencesRepository.player,
        playlistRepository.observeActive(),
        playlistRepository.observeAll(),
    ) { user, player, activePlaylist, allPlaylists ->
        val planText = buildPlanText(activePlaylist)
        ProfileUiState(
            user = user,
            player = player,
            activePlaylist = activePlaylist,
            playlistCount = allPlaylists.size,
            planText = planText,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(),
    )

    fun setDisplayName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayName(name.trim())
        }
    }

    fun toggleAutoUpdate(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoUpdateEnabled(enabled)
        }
    }

    fun toggleLoudness(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(loudnessNormalization = enabled) }
        }
    }

    fun togglePip(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(pictureInPicture = enabled) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(false)
            userPreferencesRepository.setActivePlaylistId(-1L)
        }
    }

    private fun buildPlanText(playlist: com.genciptv.player.data.model.Playlist?): String {
        if (playlist == null) return "—"
        return when (playlist.type) {
            PlaylistType.M3U -> "Standart"
            PlaylistType.XTREAM -> {
                val userInfo = playlist.userInfo
                if (userInfo != null && userInfo.status.equals("Active", ignoreCase = true)) {
                    val expText = userInfo.expDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
                        "Son: ${sdf.format(Date(millis))}"
                    } ?: ""
                    if (expText.isNotBlank()) "✨ Premium Plan · $expText" else "✨ Premium Plan"
                } else {
                    "⚠ Süresi Dolmuş"
                }
            }
        }
    }
}
