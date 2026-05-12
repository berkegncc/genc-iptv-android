package com.genciptv.player.feature.channels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.EpgRepository
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.feature.home.model.ChannelWithProgram
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val epgRepository: EpgRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)

    // ── Internal mutable search & filter state ────────────────────────────────

    private val _query = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _inCategoryView = MutableStateFlow(false)

    // ── Active playlist id ────────────────────────────────────────────────────

    private val activePlaylistFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }
        .distinctUntilChanged()

    // ── Channel search (react to query + category + playlist changes) ─────────

    private val channelSearchFlow = combine(
        activePlaylistFlow,
        _query,
        _selectedCategory,
    ) { playlistId, query, category -> Triple(playlistId, query, category) }
        .flatMapLatest { (playlistId, query, category) ->
            if (playlistId <= 0L) flowOf(emptyList())
            else channelRepository.search(playlistId, query, category)
        }

    // ── EPG join — produce ChannelWithProgram ─────────────────────────────────

    private val channelWithProgramFlow = combine(
        activePlaylistFlow,
        channelSearchFlow,
    ) { playlistId, channels -> Pair(playlistId, channels) }
        .flatMapLatest { (playlistId, channels) ->
            if (playlistId <= 0L || channels.isEmpty()) {
                flowOf(channels.map { ChannelWithProgram(it, null, 0f) })
            } else {
                val epgIds = channels.mapNotNull { it.epgChannelId }.filter { it.isNotBlank() }
                if (epgIds.isEmpty()) {
                    flowOf(channels.map { ChannelWithProgram(it, null, 0f) })
                } else {
                    epgRepository.observeNow(playlistId, epgIds)
                        .map { programs -> joinChannelsWithPrograms(channels, programs) }
                }
            }
        }

    // ── Category list (with per-category channel counts) ──────────────────────

    private val categoriesFlow = activePlaylistFlow.flatMapLatest { playlistId ->
        if (playlistId <= 0L) flowOf(emptyList())
        else channelRepository.observeCategoriesWithCount(playlistId)
    }

    // ── Favorite ids set ──────────────────────────────────────────────────────

    private val favoriteIdsFlow = favoriteRepository.observeByType(FavoriteTargetType.CHANNEL)
        .map { list -> list.map { it.targetId }.toSet() }

    // ── Featured "now" — pick first channel with a current program ────────────

    private val featuredNowFlow = channelWithProgramFlow.map { list ->
        list.firstOrNull { it.currentProgram != null }
    }

    // ── Combined UI state ─────────────────────────────────────────────────────

    val uiState: StateFlow<ChannelsUiState> = combine(
        channelWithProgramFlow,
        categoriesFlow,
        _query,
        _selectedCategory,
        combine(featuredNowFlow, favoriteIdsFlow, _inCategoryView) {
            featured, favIds, inCategory -> Triple(featured, favIds, inCategory)
        },
    ) { channels, categories, query, selectedCat, (featured, favIds, inCategory) ->
        ChannelsUiState(
            channels = channels,
            categories = categories,
            selectedCategory = selectedCat,
            query = query,
            featuredNow = featured,
            favoriteIds = favIds,
            isLoading = false,
            error = null,
            inCategoryView = inCategory,
        )
    }
        .flowOn(Dispatchers.Default)
        .catch { e -> emit(ChannelsUiState.INITIAL.copy(isLoading = false, error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChannelsUiState.INITIAL,
        )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun setQuery(q: String) {
        _query.value = q
    }

    /**
     * Enter the channel-list view for [category]. Pass `null` to view all
     * channels ("Tümü" card on the picker). Resets any active search query so
     * the destination view starts clean.
     */
    fun enterCategory(category: String?) {
        _selectedCategory.value = category
        _query.value = ""
        _inCategoryView.value = true
    }

    /** Pop back from the channel list to the vertical category picker. */
    fun backToCategories() {
        _inCategoryView.value = false
        _selectedCategory.value = null
        _query.value = ""
    }

    fun toggleFavorite(channelId: String) {
        viewModelScope.launch {
            favoriteRepository.toggle(channelId, FavoriteTargetType.CHANNEL)
        }
    }

    /** Re-download and re-parse the active playlist. Triggered from the header button. */
    fun resyncActivePlaylist() {
        viewModelScope.launch {
            val active = playlistRepository.getActive() ?: return@launch
            _isSyncing.value = true
            try {
                playlistRepository.sync(active.id)
            } catch (e: Exception) {
                Log.e("GencIPTV/Channels", "Resync failed", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    val isSyncing: StateFlow<Boolean> = _isSyncing

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun joinChannelsWithPrograms(
        channels: List<Channel>,
        programs: List<Program>,
    ): List<ChannelWithProgram> {
        val now = System.currentTimeMillis()
        val programByEpgId = programs.associateBy { it.channelEpgId }
        return channels.map { channel ->
            val program = channel.epgChannelId?.let { programByEpgId[it] }
            val progress = if (program != null) {
                val duration = (program.stopMillis - program.startMillis).coerceAtLeast(1L)
                val elapsed = (now - program.startMillis).coerceAtLeast(0L)
                (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            } else 0f
            ChannelWithProgram(channel, program, progress)
        }
    }
}
