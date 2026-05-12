package com.genciptv.player.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genciptv.player.data.model.CategoryChannelCount
import com.genciptv.player.data.source.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query(
        """
        SELECT * FROM channels
        WHERE playlistId = :playlistId
        ORDER BY groupSortOrder ASC, sortOrder ASC, name ASC
        """
    )
    fun observeByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query(
        """
        SELECT * FROM channels
        WHERE playlistId = :playlistId
          AND (:query = '' OR name LIKE '%' || :query || '%')
          AND (:category IS NULL OR groupTitle = :category)
        ORDER BY groupSortOrder ASC, sortOrder ASC, name ASC
        """
    )
    fun searchByPlaylist(
        playlistId: Long,
        query: String,
        category: String?,
    ): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ChannelEntity?>

    /**
     * Distinct group titles in provider-preferred order. Uses
     * [ChannelEntity.groupSortOrder] which for Xtream comes from the index of
     * the category in `get_live_categories`, and for M3U from the order each
     * group first appears in the playlist file.
     * Unknown groups (Int.MAX_VALUE) fall to the bottom.
     */
    @Query(
        """
        SELECT groupTitle FROM channels
        WHERE playlistId = :playlistId AND groupTitle IS NOT NULL
        GROUP BY groupTitle
        ORDER BY MIN(groupSortOrder) ASC, groupTitle ASC
        """
    )
    fun observeCategories(playlistId: Long): Flow<List<String>>

    /**
     * Categories with a channel count per group, used by the vertical category
     * picker on the Channels screen. Maps directly to the [CategoryChannelCount]
     * data class via the column aliases `name` and `count`.
     */
    @Query(
        """
        SELECT groupTitle AS name, COUNT(*) AS count FROM channels
        WHERE playlistId = :playlistId AND groupTitle IS NOT NULL
        GROUP BY groupTitle
        ORDER BY MIN(groupSortOrder) ASC, groupTitle ASC
        """
    )
    fun observeCategoriesWithCount(playlistId: Long): Flow<List<CategoryChannelCount>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY sortOrder ASC LIMIT :limit")
    fun observePopular(playlistId: Long, limit: Int = 10): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun countByPlaylist(playlistId: Long): Int

    @Query("SELECT * FROM channels WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ChannelEntity>
}
