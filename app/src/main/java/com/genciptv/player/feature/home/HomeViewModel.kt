package com.genciptv.player.feature.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.core.util.SyncPolicy.STALE_AFTER_MS
import com.genciptv.player.core.util.isConnectionMetered
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.PosterEnricher
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val channelRepository: ChannelRepository,
    private val vodRepository: VodRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    private val posterEnricher: PosterEnricher,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // ── Active playlist id ────────────────────────────────────────────────────

    private val activePlaylistFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }

    // ── Latest movies ─────────────────────────────────────────────────────────

    private val latestMoviesFlow = activePlaylistFlow.flatMapLatest { playlistId ->
        if (playlistId <= 0L) flowOf(emptyList())
        else vodRepository.observeLatestMovies(playlistId, limit = 10)
    }.onEach { posterEnricher.enrichMovies(it) }

    // ── Latest series ─────────────────────────────────────────────────────────

    private val latestSeriesFlow = activePlaylistFlow.flatMapLatest { playlistId ->
        if (playlistId <= 0L) flowOf(emptyList())
        else vodRepository.observeLatestSeries(playlistId, limit = 10)
    }.onEach { posterEnricher.enrichSeries(it) }

    // ── Recently-watched channels from local DataStore ────────────────────────

    private val recentChannelsFlow = userPreferencesRepository.recentChannels
        .flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf<List<Channel>>(emptyList())
            else {
                // We need to look up channel objects. channelRepository.getByIds is suspend,
                // so we use flatMapLatest with a flow that fetches once.
                activePlaylistFlow.flatMapLatest { _ ->
                    // Fetch channel objects — returns them in DB order; we'll reorder by ids
                    kotlinx.coroutines.flow.flow {
                        val channels = channelRepository.getByIds(ids)
                        // Restore LRU order from ids list
                        val byId = channels.associateBy { it.id }
                        emit(ids.mapNotNull { byId[it] })
                    }
                }
            }
        }

    // ── User name ─────────────────────────────────────────────────────────────

    private val userNameFlow = userPreferencesRepository.user.map { it.displayName }

    // ── Combined UI state ─────────────────────────────────────────────────────

    /**
     * True when the catalogue is stale and the user's "Yalnızca Wi-Fi" choice
     * is what is holding the refresh back. Recomputed whenever the playlist row
     * changes, so it clears itself as soon as a sync lands.
     */
    private val refreshHeldForWifiFlow = combine(
        userPreferencesRepository.user,
        playlistRepository.observeActive(),
    ) { prefs, active ->
        active != null &&
            System.currentTimeMillis() - active.lastSyncedAt > STALE_AFTER_MS &&
            prefs.syncOverWifiOnly &&
            appContext.isConnectionMetered()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        userNameFlow,
        latestMoviesFlow,
        latestSeriesFlow,
        recentChannelsFlow,
        refreshHeldForWifiFlow,
    ) { userName, movies, series, channels, heldForWifi ->
        HomeUiState(
            userName = userName,
            latestMovies = movies,
            latestSeries = series,
            recentChannels = channels,
            isLoading = false,
            selectedChipIndex = 0,
            refreshHeldForWifi = heldForWifi,
        )
    }
    .flowOn(Dispatchers.Default)
    .catch { emit(HomeUiState.INITIAL.copy(isLoading = false)) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.INITIAL,
    )

    fun selectChip(index: Int) {
        // Chip selection is visual-only — navigation is handled in the UI
    }

    /** Pull-to-refresh: re-sync the active playlist (channels + EPG). */
    fun refresh() {
        viewModelScope.launch {
            val active = playlistRepository.getActive() ?: return@launch
            _isRefreshing.value = true
            try {
                playlistRepository.sync(active.id)
            } catch (e: Exception) {
                Log.e("GencIPTV/Home", "Refresh failed", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
