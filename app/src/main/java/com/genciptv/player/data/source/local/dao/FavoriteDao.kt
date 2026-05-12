package com.genciptv.player.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.genciptv.player.data.source.local.entity.ContinueWatchingEntity
import com.genciptv.player.data.source.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE targetType = :targetType ORDER BY addedAt DESC")
    fun observeByType(targetType: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE targetId = :targetId AND targetType = :targetType)")
    fun observeIsFavorite(targetId: String, targetType: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE targetId = :targetId AND targetType = :targetType)")
    suspend fun isFavorite(targetId: String, targetType: String): Boolean

    @Upsert
    suspend fun upsert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE targetId = :targetId AND targetType = :targetType")
    suspend fun deleteBy(targetId: String, targetType: String)
}

@Dao
interface ContinueWatchingDao {

    @Query("SELECT * FROM continue_watching ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ContinueWatchingEntity>>

    @Query("SELECT * FROM continue_watching ORDER BY updatedAt DESC LIMIT :limit")
    fun observeLatest(limit: Int): Flow<List<ContinueWatchingEntity>>

    @Upsert
    suspend fun upsert(entity: ContinueWatchingEntity)

    @Query("DELETE FROM continue_watching WHERE targetId = :targetId AND targetType = :targetType")
    suspend fun deleteBy(targetId: String, targetType: String)

    @Query("SELECT * FROM continue_watching WHERE targetType = :type ORDER BY updatedAt DESC")
    fun observeByType(type: String): Flow<List<ContinueWatchingEntity>>

    @Query("DELETE FROM continue_watching WHERE targetType = 'CHANNEL'")
    suspend fun deleteAllChannels()
}
