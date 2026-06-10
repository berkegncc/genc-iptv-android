package com.genciptv.player.feature.vodplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.repository.ContinueWatchingRepository
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.TmdbRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.data.repository.VodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VodPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodRepository: VodRepository,
    private val continueWatchingRepository: ContinueWatchingRepository,
    private val favoriteRepository: FavoriteRepository,
    private val tmdbRepository: TmdbRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val subtitleStyle: StateFlow<SubtitleStyle> = userPreferencesRepository.subtitles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubtitleStyle.Default,
        )

    /**
     * User's preferred audio track language (ISO 639-1). Null/blank when the
     * user hasn't picked one — in that case [VodPlayerScreen] leaves ExoPlayer's
     * track selector alone and the stream's default language plays. When set
     * (e.g. "tr"), the player forces tracks tagged with that language.
     */
    val preferredAudioLang: StateFlow<String?> = userPreferencesRepository.player
        .map { it.preferredAudioLang?.takeIf { lang -> lang.isNotBlank() } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val userAgent: StateFlow<String?> = userPreferencesRepository.player
        .map { it.userAgent?.takeIf { ua -> ua.isNotBlank() } }
        .distinctUntilChanged()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    // Route args — exactly one of these is set
    private val initialVodId: String?     = savedStateHandle["vodId"]
    private val initialEpisodeId: String? = savedStateHandle["episodeId"]

    private val _uiState = MutableStateFlow(VodPlayerUiState.INITIAL)
    private val _isFavorite = MutableStateFlow(false)

    private val _playbackSpeed = MutableStateFlow(1f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    val uiState: StateFlow<VodPlayerUiState> = combine(
        _uiState,
        _isFavorite,
    ) { state, fav ->
        state.copy(isFavorite = fav)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VodPlayerUiState.INITIAL,
    )

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    init {
        viewModelScope.launch { loadContent() }
    }

    private suspend fun loadContent() {
        try {
            when {
                initialVodId != null -> loadMovie(initialVodId)
                initialEpisodeId != null -> loadEpisode(initialEpisodeId)
                else -> _uiState.value = VodPlayerUiState(isLoading = false, error = "İçerik bulunamadı")
            }
        } catch (e: Exception) {
            _uiState.value = VodPlayerUiState(isLoading = false, error = e.message)
        }
    }

    // ── Movie path ───────────────────────────────────────────────────────────

    private suspend fun loadMovie(vodId: String) {
        val movie = withContext(Dispatchers.IO) { vodRepository.getVodById(vodId) }
        if (movie == null) {
            _uiState.value = VodPlayerUiState(isLoading = false, error = "Film bulunamadı")
            return
        }
        val savedPos = withContext(Dispatchers.IO) {
            continueWatchingRepository.observeByType(FavoriteTargetType.MOVIE)
                .first()
                .firstOrNull { it.targetId == vodId }?.positionMs ?: 0L
        }

        _uiState.value = VodPlayerUiState(
            title = movie.title,
            subtitle = null,
            streamUrl = movie.streamUrl,
            initialPositionMs = savedPos,
            isLoading = false,
            targetId = vodId,
            isMovie = true,
            posterUrl = movie.posterUrl,
            movie = movie,
        )

        // Observe favorite state
        viewModelScope.launch {
            favoriteRepository.observeIsFavorite(vodId, FavoriteTargetType.MOVIE)
                .collect { _isFavorite.value = it }
        }

        // Kick off TMDb cast + similar in parallel — non-blocking
        loadMovieExtras(movie)
    }

    private fun loadMovieExtras(movie: VodItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCastLoading = true) }
            val cast = withContext(Dispatchers.IO) {
                tmdbRepository.fetchMovieCast(movie.title, movie.year)
            }
            _uiState.update { it.copy(castWithPhotos = cast, isCastLoading = false) }
        }
        val categoryId = movie.categoryId
        if (!categoryId.isNullOrBlank()) {
            viewModelScope.launch {
                val similar = runCatching {
                    vodRepository
                        .observeMovies(playlistId = movie.playlistId, categoryId = categoryId, query = "")
                        .first()
                        .filter { it.id != movie.id }
                        .take(15)
                }.getOrDefault(emptyList())
                _uiState.update { it.copy(similarMovies = similar) }
            }
        }
    }

    // ── Episode path ─────────────────────────────────────────────────────────

    private suspend fun loadEpisode(episodeId: String) {
        val episode = withContext(Dispatchers.IO) { vodRepository.getEpisodeById(episodeId) }
        if (episode == null) {
            _uiState.value = VodPlayerUiState(isLoading = false, error = "Bölüm bulunamadı")
            return
        }
        val series = withContext(Dispatchers.IO) { vodRepository.getSeriesById(episode.seriesId) }
        val cwPos = withContext(Dispatchers.IO) {
            continueWatchingRepository.observeByType(FavoriteTargetType.SERIES)
                .first()
                .firstOrNull { it.targetId == episodeId }?.positionMs ?: 0L
        }

        _uiState.value = VodPlayerUiState(
            title = series?.title ?: episode.title,
            subtitle = "S${episode.season} · B${episode.episode} — ${episode.title}",
            streamUrl = episode.streamUrl,
            initialPositionMs = cwPos,
            isLoading = false,
            targetId = episodeId,
            isMovie = false,
            posterUrl = series?.posterUrl ?: episode.thumbnailUrl,
            series = series,
            episode = episode,
            selectedSeason = episode.season,
        )

        // Observe series favorite state — episodes track favouritism at the
        // series level (the Devam Et card shows the series).
        viewModelScope.launch {
            favoriteRepository.observeIsFavorite(episode.seriesId, FavoriteTargetType.SERIES)
                .collect { _isFavorite.value = it }
        }

        // Stream the full episode list for the series so the panel can show
        // a season picker + the episode rail underneath the player.
        viewModelScope.launch {
            vodRepository.observeEpisodes(episode.seriesId).collect { episodes ->
                _uiState.update { it.copy(seriesEpisodes = episodes) }
            }
        }
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    fun selectSeason(season: Int) {
        _uiState.update { it.copy(selectedSeason = season) }
    }

    /**
     * Switch to a different episode of the same series. Saves the current
     * episode's position first so the user can resume it later, then loads the
     * new episode's data + saved position. The screen's `LaunchedEffect` on
     * [VodPlayerUiState.streamUrl] picks up the change and re-prepares ExoPlayer.
     */
    fun switchToEpisode(newEpisodeId: String, currentPositionMs: Long, currentDurationMs: Long) {
        val current = _uiState.value
        if (current.targetId == newEpisodeId) return  // already playing

        // Persist the current episode's position before changing streams
        if (current.targetId.isNotBlank() && current.streamUrl.isNotBlank()) {
            savePosition(currentPositionMs, currentDurationMs)
        }

        viewModelScope.launch {
            val newEpisode = withContext(Dispatchers.IO) {
                current.seriesEpisodes.firstOrNull { it.id == newEpisodeId }
                    ?: vodRepository.getEpisodeById(newEpisodeId)
            } ?: return@launch

            val savedPos = withContext(Dispatchers.IO) {
                continueWatchingRepository.observeByType(FavoriteTargetType.SERIES)
                    .first()
                    .firstOrNull { it.targetId == newEpisodeId }?.positionMs ?: 0L
            }

            _uiState.update { state ->
                state.copy(
                    streamUrl = newEpisode.streamUrl,
                    initialPositionMs = savedPos,
                    targetId = newEpisodeId,
                    subtitle = "S${newEpisode.season} · B${newEpisode.episode} — ${newEpisode.title}",
                    posterUrl = state.series?.posterUrl ?: newEpisode.thumbnailUrl,
                    episode = newEpisode,
                    selectedSeason = newEpisode.season,
                )
            }
        }
    }

    /** Called every ~15 s and on dispose. */
    fun savePosition(positionMs: Long, durationMs: Long) {
        val state = _uiState.value
        if (state.streamUrl.isBlank() || state.targetId.isBlank()) return
        val targetType = if (state.isMovie) FavoriteTargetType.MOVIE else FavoriteTargetType.SERIES

        // For series we key the row by SERIES id (so each series collapses to
        // one Devam Et entry no matter which episodes were watched) and stash
        // the current episode id in `resumeEpisodeId` so a tap routes to that
        // episode's player. If the series object hasn't loaded yet (rare —
        // savePosition fires every ~15s, the series load completes during the
        // first frame), bail out: writing with a fallback id would create a
        // duplicate row that gets re-keyed on the next save.
        val seriesId = state.series?.id
        if (!state.isMovie && seriesId == null) return
        val rowTargetId = if (state.isMovie) state.targetId else seriesId!!
        val resumeEpisodeId = if (state.isMovie) null else state.targetId

        // < 10 s remaining → drop the entry so it doesn't show in "Devam Et"
        val nearlyDone = durationMs > 0L && (durationMs - positionMs) < 10_000L

        viewModelScope.launch {
            if (nearlyDone) {
                continueWatchingRepository.remove(rowTargetId, targetType)
            } else {
                continueWatchingRepository.upsert(
                    ContinueWatching(
                        targetId = rowTargetId,
                        targetType = targetType,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        updatedAt = System.currentTimeMillis(),
                        title = state.title,
                        subtitle = state.subtitle,
                        thumbnailUrl = state.posterUrl,
                        resumeEpisodeId = resumeEpisodeId,
                    )
                )
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.isMovie) {
                state.movie?.id?.let { favoriteRepository.toggle(it, FavoriteTargetType.MOVIE) }
            } else {
                state.series?.id?.let { favoriteRepository.toggle(it, FavoriteTargetType.SERIES) }
            }
        }
    }
}
