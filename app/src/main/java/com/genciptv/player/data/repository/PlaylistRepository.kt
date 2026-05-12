package com.genciptv.player.data.repository

import android.util.Log
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.PlaylistType
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.data.source.local.dao.ChannelDao
import com.genciptv.player.data.source.local.dao.EpisodeDao
import com.genciptv.player.data.source.local.dao.PlaylistDao
import com.genciptv.player.data.source.local.dao.SeriesDao
import com.genciptv.player.data.source.local.dao.VodCategoryDao
import com.genciptv.player.data.source.local.dao.VodDao
import com.genciptv.player.data.source.local.entity.PlaylistEntity
import com.genciptv.player.data.source.local.mapper.toDomain
import com.genciptv.player.data.source.local.mapper.toEntity
import com.genciptv.player.data.source.m3u.M3uRemoteLoader
import com.genciptv.player.data.source.xtream.XtreamApi
import com.genciptv.player.data.source.xtream.XtreamMapper
import com.genciptv.player.data.source.xtream.XtreamUrlBuilder
import com.genciptv.player.data.source.local.entity.ChannelEntity
import com.genciptv.player.data.worker.SyncScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface PlaylistRepository {
    fun observeAll(): Flow<List<Playlist>>
    fun observeActive(): Flow<Playlist?>
    suspend fun getActive(): Playlist?
    suspend fun getById(id: Long): Playlist?
    suspend fun addM3u(
        name: String,
        url: String,
        epgUrl: String?,
        userAgent: String? = null,
    ): Long
    suspend fun addXtream(
        name: String,
        serverUrl: String,
        username: String,
        password: String,
    ): Long
    suspend fun setActive(id: Long)
    suspend fun delete(playlist: Playlist)
    suspend fun sync(playlistId: Long)
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val vodDao: VodDao,
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao,
    private val vodCategoryDao: VodCategoryDao,
    private val m3uLoader: M3uRemoteLoader,
    private val xtreamApi: XtreamApi,
    private val continueWatchingRepository: ContinueWatchingRepository,
    private val epgRepository: EpgRepository,
    private val syncScheduler: SyncScheduler,
) : PlaylistRepository {

    override fun observeAll(): Flow<List<Playlist>> =
        playlistDao.observeAll().map { list -> list.map(PlaylistEntity::toDomain) }

    override fun observeActive(): Flow<Playlist?> =
        playlistDao.observeActive().map { it?.toDomain() }

    override suspend fun getActive(): Playlist? =
        playlistDao.getActive()?.toDomain()

    override suspend fun getById(id: Long): Playlist? =
        playlistDao.getById(id)?.toDomain()

    override suspend fun addM3u(
        name: String,
        url: String,
        epgUrl: String?,
        userAgent: String?,
    ): Long = withContext(Dispatchers.IO) {
        val pending = Playlist(
            name = name,
            type = PlaylistType.M3U,
            url = url,
            epgUrl = epgUrl,
            userAgent = userAgent,
            isActive = false,
        )
        val id = playlistDao.upsert(pending.toEntity())
        sync(id)
        playlistDao.setActive(id)
        syncScheduler.scheduleDailySync()
        id
    }

    override suspend fun addXtream(
        name: String,
        serverUrl: String,
        username: String,
        password: String,
    ): Long = withContext(Dispatchers.IO) {
        val authUrl = XtreamUrlBuilder.playerApi(serverUrl)
        val auth = xtreamApi.getUserInfo(authUrl, username, password)
        val userInfo = XtreamMapper.toUserInfo(auth)

        val pending = Playlist(
            name = name,
            type = PlaylistType.XTREAM,
            url = serverUrl,
            username = username,
            password = password,
            userInfo = userInfo,
            isActive = false,
        )
        val id = playlistDao.upsert(pending.toEntity())
        sync(id)
        playlistDao.setActive(id)
        syncScheduler.scheduleDailySync()
        id
    }

    override suspend fun setActive(id: Long) = playlistDao.setActive(id)

    override suspend fun delete(playlist: Playlist) {
        playlistDao.delete(playlist.toEntity())
    }

    override suspend fun sync(playlistId: Long) = withContext(Dispatchers.IO) {
        val entity = playlistDao.getById(playlistId) ?: return@withContext
        val playlist = entity.toDomain()
        // Purge stale CHANNEL rows from ContinueWatching — live TV tracking is forbidden
        continueWatchingRepository.deleteAllChannels()
        when (playlist.type) {
            PlaylistType.M3U -> syncM3u(playlist)
            PlaylistType.XTREAM -> syncXtream(playlist)
        }
    }

    // ── Sync implementations ──────────────────────────────────────────────────

    private suspend fun syncM3u(playlist: Playlist) {
        val entries = m3uLoader.load(playlist.url, playlist.userAgent)
        if (entries.isEmpty()) {
            throw IllegalStateException("Empty playlist: no channels found")
        }
        // Track the order each group is first seen — that becomes its sort key.
        val groupOrder = LinkedHashMap<String, Int>()
        val channels: List<ChannelEntity> = entries.mapIndexed { i, e ->
            val group = e.groupTitle
            val groupIdx = if (group != null) {
                groupOrder.getOrPut(group) { groupOrder.size }
            } else {
                Int.MAX_VALUE
            }
            ChannelEntity(
                id = "${playlist.id}:${i}-${e.tvgId ?: e.displayName.hashCode()}",
                playlistId = playlist.id,
                name = e.displayName,
                logoUrl = e.tvgLogo,
                streamUrl = e.url,
                groupTitle = group,
                epgChannelId = e.tvgId,
                isHd = e.displayName.contains("HD", ignoreCase = true) ||
                    e.displayName.contains("1080", ignoreCase = true),
                sortOrder = i,
                groupSortOrder = groupIdx,
            )
        }
        channelDao.deleteByPlaylist(playlist.id)
        channels.chunked(500).forEach { channelDao.insertAll(it) }
        playlistDao.updateSyncStats(
            id = playlist.id,
            ts = System.currentTimeMillis(),
            count = channels.size,
        )
    }

    private suspend fun syncXtream(playlist: Playlist) {
        val username = playlist.username ?: return
        val password = playlist.password ?: return
        val apiUrl = XtreamUrlBuilder.playerApi(playlist.url)

        // Fetch live categories. Their ORDER in the response is the natural
        // provider-preferred order (premium categories first, etc). We capture
        // both: id → name and id → index so channels can be sorted by category.
        val categoryDtos = runCatching {
            xtreamApi.getLiveCategories(apiUrl, username, password)
        }.getOrDefault(emptyList())

        val categoryNameById: Map<String, String> =
            categoryDtos.associate { it.categoryIdString to it.categoryName }
        val categoryOrderById: Map<String, Int> =
            categoryDtos.mapIndexed { idx, cat -> cat.categoryIdString to idx }.toMap()

        val streams = xtreamApi.getLiveStreams(apiUrl, username, password)
        if (streams.isEmpty()) {
            throw IllegalStateException("Empty playlist: no channels in Xtream account")
        }
        val channels = streams.mapIndexedNotNull { i, dto ->
            XtreamMapper.toChannel(
                dto = dto,
                playlist = playlist,
                sortOrder = i,
                categoryNameById = categoryNameById,
                categoryOrderById = categoryOrderById,
            )?.toEntity()
        }
        channelDao.deleteByPlaylist(playlist.id)
        channels.chunked(500).forEach { channelDao.insertAll(it) }
        playlistDao.updateSyncStats(
            id = playlist.id,
            ts = System.currentTimeMillis(),
            count = channels.size,
        )

        // Diagnostic: how many live channels carry an epgChannelId? Without
        // it the Guide screen cannot match programmes to channels even when
        // the XMLTV file itself is valid. (Xtream providers vary widely on
        // how reliably they populate `epg_channel_id`.)
        val withEpgId = channels.count { !it.epgChannelId.isNullOrBlank() }
        val sampleEpgIds = channels.asSequence()
            .mapNotNull { it.epgChannelId?.takeIf { id -> id.isNotBlank() } }
            .distinct()
            .take(5)
            .toList()
        Log.i(
            "GencIPTV/Sync",
            "Xtream live sync: ${channels.size} channels, $withEpgId have epgChannelId " +
                "(sample: $sampleEpgIds)",
        )

        // VOD and Series sync — wrapped separately so failures don't abort live channels
        runCatching { syncXtreamVod(playlist, apiUrl, username, password) }
        runCatching { syncXtreamSeries(playlist, apiUrl, username, password) }

        // EPG sync via xmltv.php — optional; provider may not serve it
        runCatching {
            syncXtreamEpg(playlist, username, password)
            Log.i("GencIPTV/Sync", "EPG sync completed for playlist=${playlist.id}")
        }.onFailure { e ->
            Log.w("GencIPTV/Sync", "EPG sync failed (provider may not serve xmltv.php)", e)
        }
    }

    private suspend fun syncXtreamEpg(
        playlist: Playlist,
        username: String,
        password: String,
    ) {
        val xmltvUrl = "${playlist.url.trimEnd('/')}/xmltv.php?username=$username&password=$password"
        epgRepository.syncFromXmltv(playlist.id, xmltvUrl)
    }

    private suspend fun syncXtreamVod(
        playlist: Playlist,
        apiUrl: String,
        username: String,
        password: String,
    ) {
        val vodCategoriesDto = xtreamApi.getVodCategories(apiUrl, username, password)
        val vodCategoryEntities = vodCategoriesDto.map { dto ->
            XtreamMapper.toVodCategory(dto, playlist.id, VodKind.MOVIE).toEntity()
        }
        vodCategoryDao.deleteByPlaylistAndKind(playlist.id, VodKind.MOVIE.name)
        vodCategoryDao.insertAll(vodCategoryEntities)

        val vodStreams = xtreamApi.getVodStreams(apiUrl, username, password)
        val vodEntities = vodStreams.mapNotNull { dto ->
            XtreamMapper.toVodItem(dto, playlist)?.toEntity()
        }
        vodDao.deleteByPlaylist(playlist.id)
        vodEntities.chunked(500).forEach { vodDao.insertAll(it) }
    }

    private suspend fun syncXtreamSeries(
        playlist: Playlist,
        apiUrl: String,
        username: String,
        password: String,
    ) {
        val seriesCategoriesDto = xtreamApi.getSeriesCategories(apiUrl, username, password)
        val seriesCategoryEntities = seriesCategoriesDto.map { dto ->
            XtreamMapper.toVodCategory(dto, playlist.id, VodKind.SERIES).toEntity()
        }
        vodCategoryDao.deleteByPlaylistAndKind(playlist.id, VodKind.SERIES.name)
        vodCategoryDao.insertAll(seriesCategoryEntities)

        val seriesList = xtreamApi.getSeries(apiUrl, username, password)
        val seriesEntities = seriesList.map { dto ->
            XtreamMapper.toSeries(dto, playlist).toEntity()
        }
        seriesDao.deleteByPlaylist(playlist.id)
        seriesEntities.chunked(500).forEach { seriesDao.insertAll(it) }
    }
}
