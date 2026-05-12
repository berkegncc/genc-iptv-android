package com.genciptv.player.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.PosterEnricher
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val vodRepository: VodRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val posterEnricher: PosterEnricher,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val activePlaylistFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }
        .distinctUntilChanged()

    // Debounced, trimmed query — emits only when worth re-searching
    private val debouncedQuery = _query
        .map { it.trim() }
        .debounce(250)
        .distinctUntilChanged()

    private val channelsFlow = combine(activePlaylistFlow, debouncedQuery) { pid, q -> pid to q }
        .flatMapLatest { (pid, q) ->
            if (pid <= 0L || q.length < SearchUiState.MIN_QUERY_LENGTH) flowOf(emptyList())
            else channelRepository.search(pid, q, null)
                .map { it.take(SearchUiState.MAX_RESULTS_PER_SECTION) }
        }

    private val moviesFlow = combine(activePlaylistFlow, debouncedQuery) { pid, q -> pid to q }
        .flatMapLatest { (pid, q) ->
            if (pid <= 0L || q.length < SearchUiState.MIN_QUERY_LENGTH) flowOf(emptyList())
            else vodRepository.observeMovies(pid, null, q)
                .map { it.take(SearchUiState.MAX_RESULTS_PER_SECTION) }
        }
        .onEach { posterEnricher.enrichMovies(it) }

    private val seriesFlow = combine(activePlaylistFlow, debouncedQuery) { pid, q -> pid to q }
        .flatMapLatest { (pid, q) ->
            if (pid <= 0L || q.length < SearchUiState.MIN_QUERY_LENGTH) flowOf(emptyList())
            else vodRepository.observeSeries(pid, null, q)
                .map { it.take(SearchUiState.MAX_RESULTS_PER_SECTION) }
        }
        .onEach { posterEnricher.enrichSeries(it) }

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        channelsFlow,
        moviesFlow,
        seriesFlow,
    ) { q, channels, movies, series ->
        SearchUiState(
            query = q,
            channels = channels,
            movies = movies,
            series = series,
            isSearching = false,
        )
    }
        .flowOn(Dispatchers.Default)
        .catch { emit(SearchUiState.INITIAL) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.INITIAL,
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun clearQuery() {
        _query.value = ""
    }
}
