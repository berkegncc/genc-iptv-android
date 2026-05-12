package com.genciptv.player.data.repository

import com.genciptv.player.data.model.CategoryChannelCount
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.source.local.dao.ChannelDao
import com.genciptv.player.data.source.local.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ChannelRepository {
    fun observeByPlaylist(playlistId: Long): Flow<List<Channel>>
    fun search(playlistId: Long, query: String, category: String?): Flow<List<Channel>>
    fun observeCategories(playlistId: Long): Flow<List<String>>
    fun observeCategoriesWithCount(playlistId: Long): Flow<List<CategoryChannelCount>>
    fun observePopular(playlistId: Long, limit: Int = 10): Flow<List<Channel>>
    suspend fun getById(id: String): Channel?
    fun observeById(id: String): Flow<Channel?>
    suspend fun getByIds(ids: List<String>): List<Channel>
}

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val dao: ChannelDao,
) : ChannelRepository {

    override fun observeByPlaylist(playlistId: Long): Flow<List<Channel>> =
        dao.observeByPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    override fun search(playlistId: Long, query: String, category: String?): Flow<List<Channel>> =
        dao.searchByPlaylist(playlistId, query, category)
            .map { list -> list.map { it.toDomain() } }

    override fun observeCategories(playlistId: Long): Flow<List<String>> =
        dao.observeCategories(playlistId)

    override fun observeCategoriesWithCount(playlistId: Long): Flow<List<CategoryChannelCount>> =
        dao.observeCategoriesWithCount(playlistId)

    override fun observePopular(playlistId: Long, limit: Int): Flow<List<Channel>> =
        dao.observePopular(playlistId, limit).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Channel? = dao.getById(id)?.toDomain()

    override fun observeById(id: String): Flow<Channel?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getByIds(ids: List<String>): List<Channel> =
        if (ids.isEmpty()) emptyList()
        else dao.getByIds(ids).map { it.toDomain() }
}
