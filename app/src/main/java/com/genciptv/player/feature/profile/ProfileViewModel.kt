package com.genciptv.player.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.R
import com.genciptv.player.data.model.PlaylistType
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.genciptv.player.data.worker.SyncScheduler

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncScheduler: SyncScheduler,
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

    fun setTmdbApiKey(key: String) {
        viewModelScope.launch {
            userPreferencesRepository.setTmdbApiKey(key)
        }
    }

    /**
     * Saves the choice and re-declares the job, in that order — the scheduler
     * reads the preference back, so writing first is what makes the new
     * constraint the one that gets applied.
     */
    fun setSyncOverWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSyncOverWifiOnly(wifiOnly)
            syncScheduler.scheduleDailySync()
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
            PlaylistType.M3U -> context.getString(R.string.profile_plan_standard)
            PlaylistType.XTREAM -> {
                val userInfo = playlist.userInfo
                if (userInfo != null && userInfo.status.equals("Active", ignoreCase = true)) {
                    val expDate = userInfo.expDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
                        sdf.format(Date(millis))
                    }
                    if (expDate != null) {
                        context.getString(R.string.errors_profile_premium_active_until, expDate)
                    } else {
                        context.getString(R.string.errors_profile_premium_active)
                    }
                } else {
                    context.getString(R.string.errors_profile_plan_expired)
                }
            }
        }
    }
}
