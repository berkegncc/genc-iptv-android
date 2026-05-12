package com.genciptv.player.feature.syncing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the auto-sync gate shown on app open when the active playlist is
 * stale (>6h since [com.genciptv.player.data.model.Playlist.lastSyncedAt]).
 *
 * Fail-open: if sync fails (network, provider down, etc.) we still advance
 * to Home rather than trapping the user on a loading screen with no escape.
 * They can pull-to-refresh to retry.
 */
@HiltViewModel
class SyncingViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    enum class Phase { Syncing, Done }

    private val _phase = MutableStateFlow(Phase.Syncing)
    val phase: StateFlow<Phase> = _phase.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                playlistRepository.getActive()?.let { active ->
                    playlistRepository.sync(active.id)
                }
            } catch (e: Exception) {
                Log.e("GencIPTV/AutoSync", "Auto-sync failed; proceeding to Home", e)
            } finally {
                _phase.value = Phase.Done
            }
        }
    }
}
