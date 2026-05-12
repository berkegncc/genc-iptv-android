package com.genciptv.player.data.repository

import android.util.Log
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.source.epg.EpgDownloader
import com.genciptv.player.data.source.local.dao.ProgramDao
import com.genciptv.player.data.source.local.mapper.toDomain
import com.genciptv.player.data.source.local.mapper.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

interface EpgRepository {
    /** Programs currently airing on the given epg channels. */
    fun observeNow(
        playlistId: Long,
        epgIds: List<String>,
        nowMillis: Long = System.currentTimeMillis(),
    ): Flow<List<Program>>

    /** Programs for a single channel within the given time range. */
    fun observeForChannelInRange(
        playlistId: Long,
        channelEpgId: String,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<List<Program>>

    suspend fun getUpcoming(
        playlistId: Long,
        channelEpgId: String,
        afterMillis: Long = System.currentTimeMillis(),
        limit: Int = 5,
    ): List<Program>

    /**
     * All programs for [epgIds] within [dayStartMillis]..[dayEndMillis], grouped by epg channel id.
     * Used to populate the EPG grid for a selected day.
     */
    fun observeDayGrid(
        playlistId: Long,
        epgIds: List<String>,
        dayStartMillis: Long,
        dayEndMillis: Long,
    ): Flow<Map<String, List<Program>>>

    /** Download external XMLTV and store as EPG for the given playlist. */
    suspend fun syncFromXmltv(playlistId: Long, xmltvUrl: String)

    suspend fun clearOld(playlistId: Long, cutoffMillis: Long)
}

@Singleton
class EpgRepositoryImpl @Inject constructor(
    private val dao: ProgramDao,
    private val epgDownloader: EpgDownloader,
) : EpgRepository {

    override fun observeNow(
        playlistId: Long,
        epgIds: List<String>,
        nowMillis: Long,
    ): Flow<List<Program>> =
        dao.observeNowForChannels(playlistId, epgIds, nowMillis)
            .map { list -> list.map { it.toDomain() } }

    override fun observeForChannelInRange(
        playlistId: Long,
        channelEpgId: String,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<List<Program>> =
        dao.observeForChannelInRange(playlistId, channelEpgId, fromMillis, toMillis)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getUpcoming(
        playlistId: Long,
        channelEpgId: String,
        afterMillis: Long,
        limit: Int,
    ): List<Program> =
        dao.getUpcomingForChannel(playlistId, channelEpgId, afterMillis, limit)
            .map { it.toDomain() }

    override fun observeDayGrid(
        playlistId: Long,
        epgIds: List<String>,
        dayStartMillis: Long,
        dayEndMillis: Long,
    ): Flow<Map<String, List<Program>>> =
        dao.observeForChannelsInRange(playlistId, epgIds, dayStartMillis, dayEndMillis)
            .map { entities ->
                entities
                    .map { it.toDomain() }
                    .groupBy { it.channelEpgId }
            }

    override suspend fun syncFromXmltv(playlistId: Long, xmltvUrl: String): Unit =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "Downloading XMLTV for playlist=$playlistId from $xmltvUrl")
            val programs = epgDownloader.downloadAndParse(xmltvUrl, playlistId)
            val sampleEpgIds = programs.asSequence().map { it.channelEpgId }.distinct().take(5).toList()
            Log.i(
                TAG,
                "XMLTV parsed: ${programs.size} programs, " +
                    "${programs.map { it.channelEpgId }.distinct().size} distinct channelEpgIds " +
                    "(sample: $sampleEpgIds)",
            )
            val entities = programs.map { it.toEntity() }
            dao.deleteByPlaylist(playlistId)
            entities.chunked(500).forEach { dao.insertAll(it) }
            Log.i(TAG, "EPG insert complete for playlist=$playlistId")
        }

    override suspend fun clearOld(playlistId: Long, cutoffMillis: Long) =
        dao.deleteOlderThan(playlistId, cutoffMillis)

    private companion object {
        const val TAG = "GencIPTV/Epg"
    }
}
