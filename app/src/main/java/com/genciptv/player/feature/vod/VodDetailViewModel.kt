package com.genciptv.player.feature.vod

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.TmdbRepository
import com.genciptv.player.data.repository.VodRepository
import com.genciptv.player.data.source.xtream.XtreamApi
import com.genciptv.player.data.source.xtream.XtreamUrlBuilder
import com.genciptv.player.data.source.local.dao.EpisodeDao
import com.genciptv.player.data.source.local.entity.EpisodeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodRepository: VodRepository,
    private val favoriteRepository: FavoriteRepository,
    private val playlistRepository: PlaylistRepository,
    private val tmdbRepository: TmdbRepository,
    private val xtreamApi: XtreamApi,
    private val episodeDao: EpisodeDao,
) : ViewModel() {

    // The id argument from navigation: "{playlistId}:movie:{streamId}" or "{playlistId}:series:{seriesId}"
    private val vodId: String = savedStateHandle["id"] ?: ""

    private val _state = MutableStateFlow(VodDetailUiState())

    // ── Determine content type and load ───────────────────────────────────────

    private val isSeries: Boolean = vodId.contains(":series:")

    private val favoriteType: FavoriteTargetType =
        if (isSeries) FavoriteTargetType.SERIES else FavoriteTargetType.MOVIE

    private val isFavoriteFlow = favoriteRepository.observeIsFavorite(vodId, favoriteType)

    private val episodesFlow = if (isSeries) {
        episodeDao.observeForSeries(vodId).flatMapLatest { entities ->
            flowOf(entities.map { it.toDomainEpisode() })
        }
    } else {
        flowOf(emptyList())
    }

    val uiState: StateFlow<VodDetailUiState> = combine(
        _state,
        isFavoriteFlow,
        episodesFlow,
    ) { state, isFavorite, episodes ->
        state.copy(isFavorite = isFavorite, episodes = episodes)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VodDetailUiState.INITIAL,
    )

    init {
        loadContent()
    }

    // ── Load content ──────────────────────────────────────────────────────────

    private fun loadContent() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                if (isSeries) {
                    val series = vodRepository.getSeriesById(vodId)
                    _state.update { it.copy(series = series, isLoading = false) }
                    // Trigger episode loading for Xtream series
                    if (series != null) {
                        loadSeriesEpisodes(series.id)
                        loadTmdbCastForSeries(series)
                        loadSimilarSeries(series)
                    }
                } else {
                    val movie = vodRepository.getVodById(vodId)
                    _state.update { it.copy(movie = movie, isLoading = false) }
                    if (movie != null) {
                        loadTmdbCast(movie)
                        loadSimilarMovies(movie)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Fetch TMDb cast (with profile photos) for a movie. Runs as a side-effect of
     * [loadContent]; failures are swallowed and leave [VodDetailUiState.castWithPhotos]
     * empty so the screen falls back to the Xtream name list.
     */
    private fun loadTmdbCast(movie: VodItem) {
        viewModelScope.launch {
            _state.update { it.copy(isCastLoading = true) }
            val cast = withContext(Dispatchers.IO) {
                tmdbRepository.fetchMovieCast(title = movie.title, year = movie.year)
            }
            _state.update { it.copy(castWithPhotos = cast, isCastLoading = false) }
        }
    }

    private fun loadTmdbCastForSeries(series: Series) {
        viewModelScope.launch {
            _state.update { it.copy(isCastLoading = true) }
            val cast = withContext(Dispatchers.IO) {
                tmdbRepository.fetchSeriesCast(title = series.title, year = series.year)
            }
            _state.update { it.copy(castWithPhotos = cast, isCastLoading = false) }
        }
    }

    /**
     * Fetch up to 15 other movies in the same Xtream category (locally — no
     * network). Falls back to an empty list when the movie has no category id
     * or the playlist hasn't synced yet.
     */
    private fun loadSimilarMovies(movie: VodItem) {
        val categoryId = movie.categoryId
        if (categoryId.isNullOrBlank()) return
        viewModelScope.launch {
            val similar = runCatching {
                vodRepository
                    .observeMovies(playlistId = movie.playlistId, categoryId = categoryId, query = "")
                    .first()
                    .filter { it.id != movie.id }
                    .take(15)
            }.getOrDefault(emptyList())
            _state.update { it.copy(similarMovies = similar) }
        }
    }

    /** Mirror of [loadSimilarMovies] for series — same Xtream category, current series excluded. */
    private fun loadSimilarSeries(series: Series) {
        val categoryId = series.categoryId
        if (categoryId.isNullOrBlank()) return
        viewModelScope.launch {
            val similar = runCatching {
                vodRepository
                    .observeSeries(playlistId = series.playlistId, categoryId = categoryId, query = "")
                    .first()
                    .filter { it.id != series.id }
                    .take(15)
            }.getOrDefault(emptyList())
            _state.update { it.copy(similarSeries = similar) }
        }
    }

    // ── Load episodes from Xtream API ─────────────────────────────────────────

    fun loadSeriesEpisodes(seriesId: String) {
        // Extract raw series id (numeric part after last ':')
        val rawSeriesId = seriesId.substringAfterLast(':')
        // Extract playlist id (numeric part before first ':')
        val playlistIdStr = seriesId.substringBefore(':')
        val playlistId = playlistIdStr.toLongOrNull() ?: return

        viewModelScope.launch {
            _state.update { it.copy(isEpisodesLoading = true) }
            try {
                val playlist = playlistRepository.getById(playlistId) ?: return@launch
                val username = playlist.username ?: return@launch
                val password = playlist.password ?: return@launch
                val apiUrl = XtreamUrlBuilder.playerApi(playlist.url)

                val info = withContext(Dispatchers.IO) {
                    xtreamApi.getSeriesInfo(apiUrl, username, password, seriesId = rawSeriesId)
                }

                val episodesJson = info.episodes as? JsonObject ?: run {
                    _state.update { it.copy(isEpisodesLoading = false) }
                    return@launch
                }

                val episodes = mutableListOf<EpisodeEntity>()
                episodesJson.forEach { (seasonKey, episodesInSeason) ->
                    val seasonNumber = seasonKey.toIntOrNull() ?: return@forEach
                    (episodesInSeason as? JsonArray)?.forEach { episodeJson ->
                        runCatching {
                            val obj = episodeJson.jsonObject
                            val episodeId = obj["id"]?.jsonPrimitive?.content ?: return@runCatching
                            val title = obj["title"]?.jsonPrimitive?.content ?: "Bölüm $episodeId"
                            val ext = obj["container_extension"]?.jsonPrimitive?.content ?: "mp4"
                            val episodeNum = obj["episode_num"]?.jsonPrimitive?.intOrNull ?: 0
                            val plot = obj["info"]?.jsonObject?.get("plot")?.jsonPrimitive?.content

                            // Build stream URL
                            val streamUrl = XtreamUrlBuilder.seriesStream(
                                serverBase = playlist.url,
                                username = username,
                                password = password,
                                episodeId = episodeId,
                                ext = ext,
                            )

                            episodes.add(
                                EpisodeEntity(
                                    id = "${playlistId}:ep:$episodeId",
                                    seriesId = seriesId,
                                    playlistId = playlistId,
                                    season = seasonNumber,
                                    episode = episodeNum,
                                    title = title,
                                    streamUrl = streamUrl,
                                    plot = plot,
                                )
                            )
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    episodeDao.insertAll(episodes)
                }

            } catch (e: Exception) {
                // Episodes are optional; don't surface the error to the main state
            } finally {
                _state.update { it.copy(isEpisodesLoading = false) }
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun toggleFavorite() {
        viewModelScope.launch {
            favoriteRepository.toggle(vodId, favoriteType)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun EpisodeEntity.toDomainEpisode(): Episode = Episode(
        id = id,
        seriesId = seriesId,
        playlistId = playlistId,
        season = season,
        episode = episode,
        title = title,
        streamUrl = streamUrl,
        durationSecs = durationSecs,
        plot = plot,
        thumbnailUrl = thumbnailUrl,
    )
}
