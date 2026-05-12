package com.genciptv.player.data.repository

import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodCategory
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.data.source.local.dao.EpisodeDao
import com.genciptv.player.data.source.local.dao.SeriesDao
import com.genciptv.player.data.source.local.dao.VodCategoryDao
import com.genciptv.player.data.source.local.dao.VodDao
import com.genciptv.player.data.source.local.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface VodRepository {
    fun observeMovies(playlistId: Long, categoryId: String? = null, query: String = ""): Flow<List<VodItem>>
    fun observeSeries(playlistId: Long, categoryId: String? = null, query: String = ""): Flow<List<Series>>
    fun observeEpisodes(seriesId: String): Flow<List<Episode>>
    fun observeCategories(playlistId: Long, kind: VodKind): Flow<List<VodCategory>>
    fun observeLatestMovies(playlistId: Long, limit: Int = 10): Flow<List<VodItem>>
    fun observeLatestSeries(playlistId: Long, limit: Int = 10): Flow<List<Series>>

    suspend fun getVodById(id: String): VodItem?
    suspend fun getSeriesById(id: String): Series?
    suspend fun getEpisodeById(id: String): Episode?
    suspend fun getMoviesByIds(ids: List<String>): List<VodItem>
    suspend fun getSeriesByIds(ids: List<String>): List<Series>
}

@Singleton
class VodRepositoryImpl @Inject constructor(
    private val vodDao: VodDao,
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao,
    private val categoryDao: VodCategoryDao,
) : VodRepository {

    override fun observeMovies(
        playlistId: Long,
        categoryId: String?,
        query: String,
    ): Flow<List<VodItem>> =
        vodDao.observeByKind(playlistId, VodKind.MOVIE.name, categoryId, query)
            .map { list -> list.map { it.toDomain() } }

    override fun observeSeries(
        playlistId: Long,
        categoryId: String?,
        query: String,
    ): Flow<List<Series>> =
        seriesDao.observeByPlaylist(playlistId, query, categoryId)
            .map { list -> list.map { it.toDomain() } }

    override fun observeEpisodes(seriesId: String): Flow<List<Episode>> =
        episodeDao.observeForSeries(seriesId)
            .map { list -> list.map { it.toDomain() } }

    override fun observeCategories(playlistId: Long, kind: VodKind): Flow<List<VodCategory>> =
        categoryDao.observeByKind(playlistId, kind.name)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getVodById(id: String): VodItem? =
        vodDao.getById(id)?.toDomain()

    override suspend fun getSeriesById(id: String): Series? =
        seriesDao.getById(id)?.toDomain()

    override suspend fun getEpisodeById(id: String): Episode? =
        episodeDao.getById(id)?.toDomain()

    override suspend fun getMoviesByIds(ids: List<String>): List<VodItem> =
        if (ids.isEmpty()) emptyList()
        else vodDao.getByIds(ids).map { it.toDomain() }

    override suspend fun getSeriesByIds(ids: List<String>): List<Series> =
        if (ids.isEmpty()) emptyList()
        else seriesDao.getByIds(ids).map { it.toDomain() }

    override fun observeLatestMovies(playlistId: Long, limit: Int): Flow<List<VodItem>> =
        vodDao.observeLatest(playlistId, VodKind.MOVIE.name, limit)
            .map { list -> list.map { it.toDomain() } }

    override fun observeLatestSeries(playlistId: Long, limit: Int): Flow<List<Series>> =
        seriesDao.observeLatest(playlistId, limit)
            .map { list -> list.map { it.toDomain() } }
}
