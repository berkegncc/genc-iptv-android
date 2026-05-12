package com.genciptv.player.feature.vod

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.data.repository.ContinueWatchingRepository
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.PosterEnricher
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VodListViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    private val continueWatchingRepository: ContinueWatchingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
    private val posterEnricher: PosterEnricher,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // ── Local mutable state ───────────────────────────────────────────────────

    private val _kind = MutableStateFlow(VodKind.MOVIE)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _query = MutableStateFlow("")
    private val _selectedCwIds =
        MutableStateFlow<Set<Pair<String, FavoriteTargetType>>>(emptySet())

    // ── Active playlist id ────────────────────────────────────────────────────

    private val activePlaylistIdFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }

    // ── Movies flow ───────────────────────────────────────────────────────────

    private val moviesFlow = combine(
        activePlaylistIdFlow,
        _selectedCategoryId,
        _query,
    ) { playlistId, categoryId, query ->
        Triple(playlistId, categoryId, query)
    }.flatMapLatest { (playlistId, categoryId, query) ->
        if (playlistId <= 0L) flowOf(emptyList())
        else vodRepository.observeMovies(playlistId, categoryId, query)
    }.onEach { posterEnricher.enrichMovies(it) }

    // ── Series flow ───────────────────────────────────────────────────────────

    private val seriesFlow = combine(
        activePlaylistIdFlow,
        _selectedCategoryId,
        _query,
    ) { playlistId, categoryId, query ->
        Triple(playlistId, categoryId, query)
    }.flatMapLatest { (playlistId, categoryId, query) ->
        if (playlistId <= 0L) flowOf(emptyList())
        else vodRepository.observeSeries(playlistId, categoryId, query)
    }.onEach { posterEnricher.enrichSeries(it) }

    // ── Categories flow ───────────────────────────────────────────────────────

    private val categoriesFlow = combine(
        activePlaylistIdFlow,
        _kind,
    ) { playlistId, kind ->
        Pair(playlistId, kind)
    }.flatMapLatest { (playlistId, kind) ->
        if (playlistId <= 0L) flowOf(emptyList())
        else vodRepository.observeCategories(playlistId, kind)
    }

    // ── In-progress items ─────────────────────────────────────────────────────

    private val inProgressMoviesFlow =
        continueWatchingRepository.observeByType(FavoriteTargetType.MOVIE)

    private val inProgressSeriesFlow =
        continueWatchingRepository.observeByType(FavoriteTargetType.SERIES)

    // ── Combined UI state ─────────────────────────────────────────────────────

    private val contentFlow = combine(
        moviesFlow,
        seriesFlow,
        categoriesFlow,
    ) { movies, series, categories ->
        Triple(movies, series, categories)
    }

    private val inProgressFlow = combine(
        inProgressMoviesFlow,
        inProgressSeriesFlow,
        _selectedCwIds,
    ) { movies, series, selected -> Triple(movies, series, selected) }

    val uiState: StateFlow<VodListUiState> = combine(
        _kind,
        contentFlow,
        _selectedCategoryId,
        _query,
        inProgressFlow,
    ) { kind, (movies, series, categories), selectedCategoryId, query, (inMovies, inSeries, selectedCwIds) ->
        VodListUiState(
            kind = kind,
            movies = movies,
            series = series,
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            query = query,
            isLoading = false,
            inProgressMovies = inMovies,
            inProgressSeries = inSeries,
            selectedCwIds = selectedCwIds,
        )
    }
    .catch { e -> emit(VodListUiState(isLoading = false, error = e.message)) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VodListUiState.INITIAL,
    )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun setKind(kind: VodKind) {
        _kind.value = kind
        _selectedCategoryId.value = null
        _query.value = ""
        // Selection mode shouldn't survive a tab switch — always start fresh on the new tab.
        _selectedCwIds.value = emptySet()
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    /** Toggle multi-select state for a "Devam Et" row (used by both long-press and selection-mode tap). */
    fun toggleCwSelection(item: ContinueWatching) {
        val key = item.targetId to item.targetType
        _selectedCwIds.update { current ->
            if (key in current) current - key else current + key
        }
    }

    /** Exit selection mode without removing anything. */
    fun clearCwSelection() {
        _selectedCwIds.value = emptySet()
    }

    /**
     * Remove all currently-selected "Devam Et" rows. Selection state is cleared first so the
     * top bar disappears immediately; the DB writes happen async on the I/O dispatcher used
     * by Room.
     */
    fun removeSelectedCw() {
        val ids = _selectedCwIds.value
        if (ids.isEmpty()) return
        _selectedCwIds.value = emptySet()
        viewModelScope.launch {
            try {
                ids.forEach { (targetId, type) ->
                    continueWatchingRepository.remove(targetId, type)
                }
            } catch (e: Exception) {
                Log.e("GencIPTV/VodList", "Removing continue-watching rows failed", e)
            }
        }
    }

    /** Pull-to-refresh: re-sync the active playlist (VOD + EPG). */
    fun refresh() {
        viewModelScope.launch {
            val active = playlistRepository.getActive() ?: return@launch
            _isRefreshing.value = true
            try {
                playlistRepository.sync(active.id)
            } catch (e: Exception) {
                Log.e("GencIPTV/VodList", "Refresh failed", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
