package com.genciptv.player.data.repository

import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.Favorite
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.source.local.dao.ContinueWatchingDao
import com.genciptv.player.data.source.local.dao.FavoriteDao
import com.genciptv.player.data.source.local.entity.FavoriteEntity
import com.genciptv.player.data.source.local.mapper.toDomain
import com.genciptv.player.data.source.local.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface FavoriteRepository {
    fun observeAll(): Flow<List<Favorite>>
    fun observeByType(type: FavoriteTargetType): Flow<List<Favorite>>
    fun observeIsFavorite(targetId: String, type: FavoriteTargetType): Flow<Boolean>
    suspend fun toggle(targetId: String, type: FavoriteTargetType)
}

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
) : FavoriteRepository {

    override fun observeAll(): Flow<List<Favorite>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: FavoriteTargetType): Flow<List<Favorite>> =
        dao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    override fun observeIsFavorite(targetId: String, type: FavoriteTargetType): Flow<Boolean> =
        dao.observeIsFavorite(targetId, type.name)

    override suspend fun toggle(targetId: String, type: FavoriteTargetType) {
        if (dao.isFavorite(targetId, type.name)) {
            dao.deleteBy(targetId, type.name)
        } else {
            dao.upsert(
                FavoriteEntity(
                    targetId = targetId,
                    targetType = type.name,
                    addedAt = System.currentTimeMillis(),
                )
            )
        }
    }
}

interface ContinueWatchingRepository {
    fun observeAll(): Flow<List<ContinueWatching>>
    fun observeLatest(limit: Int = 10): Flow<List<ContinueWatching>>
    fun observeByType(type: FavoriteTargetType): Flow<List<ContinueWatching>>
    suspend fun upsert(item: ContinueWatching)
    suspend fun remove(targetId: String, type: FavoriteTargetType)
    suspend fun deleteAllChannels()
}

@Singleton
class ContinueWatchingRepositoryImpl @Inject constructor(
    private val dao: ContinueWatchingDao,
) : ContinueWatchingRepository {

    override fun observeAll(): Flow<List<ContinueWatching>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeLatest(limit: Int): Flow<List<ContinueWatching>> =
        dao.observeLatest(limit).map { list -> list.map { it.toDomain() } }

    override fun observeByType(type: FavoriteTargetType): Flow<List<ContinueWatching>> =
        dao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(item: ContinueWatching) {
        dao.upsert(item.toEntity())
    }

    override suspend fun remove(targetId: String, type: FavoriteTargetType) {
        dao.deleteBy(targetId, type.name)
    }

    override suspend fun deleteAllChannels() {
        dao.deleteAllChannels()
    }
}
