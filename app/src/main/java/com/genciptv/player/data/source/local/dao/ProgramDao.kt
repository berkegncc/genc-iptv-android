package com.genciptv.player.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genciptv.player.data.source.local.entity.ProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {

    /** Programs for a single channel within a time range (inclusive). */
    @Query(
        """
        SELECT * FROM programs
        WHERE playlistId = :playlistId
          AND channelEpgId = :channelEpgId
          AND stopMillis > :fromMillis
          AND startMillis < :toMillis
        ORDER BY startMillis ASC
        """
    )
    fun observeForChannelInRange(
        playlistId: Long,
        channelEpgId: String,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<List<ProgramEntity>>

    /** The program currently airing at [nowMillis] for each of [epgIds]. */
    @Query(
        """
        SELECT * FROM programs
        WHERE playlistId = :playlistId
          AND channelEpgId IN (:epgIds)
          AND startMillis <= :nowMillis
          AND stopMillis  >  :nowMillis
        """
    )
    fun observeNowForChannels(
        playlistId: Long,
        epgIds: List<String>,
        nowMillis: Long,
    ): Flow<List<ProgramEntity>>

    @Query(
        """
        SELECT * FROM programs
        WHERE playlistId = :playlistId
          AND channelEpgId = :channelEpgId
          AND startMillis > :afterMillis
        ORDER BY startMillis ASC
        LIMIT :limit
        """
    )
    suspend fun getUpcomingForChannel(
        playlistId: Long,
        channelEpgId: String,
        afterMillis: Long,
        limit: Int = 5,
    ): List<ProgramEntity>

    /** Programs for a SET of channels within a day time range — used by the EPG grid. */
    @Query(
        """
        SELECT * FROM programs
        WHERE playlistId = :playlistId
          AND channelEpgId IN (:epgIds)
          AND stopMillis > :fromMillis
          AND startMillis < :toMillis
        ORDER BY channelEpgId ASC, startMillis ASC
        """
    )
    fun observeForChannelsInRange(
        playlistId: Long,
        epgIds: List<String>,
        fromMillis: Long,
        toMillis: Long,
    ): Flow<List<ProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<ProgramEntity>)

    @Query("DELETE FROM programs WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("DELETE FROM programs WHERE playlistId = :playlistId AND stopMillis < :cutoffMillis")
    suspend fun deleteOlderThan(playlistId: Long, cutoffMillis: Long)
}
