package com.genciptv.player.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.genciptv.player.data.source.local.entity.EpisodeEntity
import com.genciptv.player.data.source.local.entity.SeriesEntity
import com.genciptv.player.data.source.local.entity.VodCategoryEntity
import com.genciptv.player.data.source.local.entity.VodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VodDao {

    @Query(
        """
        SELECT * FROM vod_items
        WHERE playlistId = :playlistId
          AND kind = :kind
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:query = '' OR title LIKE '%' || :query || '%')
        ORDER BY
          CASE WHEN :categoryId IS NULL THEN id END DESC,
          title ASC
        """
    )
    // "Tümü" view (categoryId = null): newest first by id (ids are
    // monotonically increasing per Xtream stream_id, so id DESC ≈ newest
    // first — same heuristic as observeLatest on the home screen).
    // Inside a specific category: alphabetical, which is what users expect
    // when browsing within a topic.
    fun observeByKind(
        playlistId: Long,
        kind: String,
        categoryId: String? = null,
        query: String = "",
    ): Flow<List<VodEntity>>

    @Query("SELECT * FROM vod_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VodEntity>)

    @Query("DELETE FROM vod_items WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("SELECT * FROM vod_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<VodEntity>

    @Query("SELECT * FROM vod_items WHERE playlistId = :playlistId AND kind = :kind ORDER BY id DESC LIMIT :limit")
    fun observeLatest(playlistId: Long, kind: String, limit: Int = 10): Flow<List<VodEntity>>

    @Query("UPDATE vod_items SET posterUrl = :url WHERE id = :id")
    suspend fun updatePosterUrl(id: String, url: String)
}

@Dao
interface SeriesDao {
    @Query("""
        SELECT * FROM series
        WHERE playlistId = :playlistId
          AND (:query = '' OR title LIKE '%' || :query || '%')
          AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY
          CASE WHEN :categoryId IS NULL THEN id END DESC,
          title ASC
    """)
    // "Tümü" view: newest first by id; inside a specific category:
    // alphabetical. Mirrors VodDao.observeByKind for consistency.
    fun observeByPlaylist(
        playlistId: Long,
        query: String = "",
        categoryId: String? = null,
    ): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    @Query("DELETE FROM series WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("SELECT * FROM series WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<SeriesEntity>

    @Query("SELECT * FROM series WHERE playlistId = :playlistId ORDER BY id DESC LIMIT :limit")
    fun observeLatest(playlistId: Long, limit: Int = 10): Flow<List<SeriesEntity>>

    @Query("UPDATE series SET posterUrl = :url WHERE id = :id")
    suspend fun updatePosterUrl(id: String, url: String)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY season, episode")
    fun observeForSeries(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)
}

@Dao
interface VodCategoryDao {
    @Query("SELECT * FROM vod_categories WHERE playlistId = :playlistId AND kind = :kind ORDER BY name ASC")
    fun observeByKind(playlistId: Long, kind: String): Flow<List<VodCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<VodCategoryEntity>)

    @Query("DELETE FROM vod_categories WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("DELETE FROM vod_categories WHERE playlistId = :playlistId AND kind = :kind")
    suspend fun deleteByPlaylistAndKind(playlistId: Long, kind: String)
}
