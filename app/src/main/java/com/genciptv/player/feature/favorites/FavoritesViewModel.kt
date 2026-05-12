package com.genciptv.player.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val channelRepository: ChannelRepository,
    private val vodRepository: VodRepository,
) : ViewModel() {

    // ── Per-type favorite flows with bulk lookup ──────────────────────────────

    private val favoriteChannelsFlow = favoriteRepository
        .observeByType(FavoriteTargetType.CHANNEL)
        .map { favs ->
            channelRepository.getByIds(favs.map { it.targetId })
        }

    private val favoriteMoviesFlow = favoriteRepository
        .observeByType(FavoriteTargetType.MOVIE)
        .map { favs ->
            vodRepository.getMoviesByIds(favs.map { it.targetId })
        }

    private val favoriteSeriesFlow = favoriteRepository
        .observeByType(FavoriteTargetType.SERIES)
        .map { favs ->
            vodRepository.getSeriesByIds(favs.map { it.targetId })
        }

    // ── Local tab selection ───────────────────────────────────────────────────
    //
    // Must be a StateFlow so the UI re-collects when the tab changes — a plain
    // `var` here was the bug: `selectTab()` mutated it but the `combine` below
    // never noticed, so the screen kept rendering the old tab.

    private val _selectedTab = MutableStateFlow(0)

    // ── Combined UI state ─────────────────────────────────────────────────────

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoriteChannelsFlow,
        favoriteMoviesFlow,
        favoriteSeriesFlow,
        _selectedTab,
    ) { channels, movies, series, selectedTab ->
        FavoritesUiState(
            selectedTab = selectedTab,
            favoriteChannels = channels,
            favoriteMovies = movies,
            favoriteSeries = series,
            isLoading = false,
        )
    }
    .catch { emit(FavoritesUiState(isLoading = false)) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavoritesUiState.INITIAL,
    )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun unfavorite(targetId: String, type: FavoriteTargetType) {
        viewModelScope.launch {
            favoriteRepository.toggle(targetId, type)
        }
    }
}
