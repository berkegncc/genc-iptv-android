package com.genciptv.player.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.genciptv.player.data.source.local.dao.ChannelDao
import com.genciptv.player.data.source.local.dao.ContinueWatchingDao
import com.genciptv.player.data.source.local.dao.EpisodeDao
import com.genciptv.player.data.source.local.dao.FavoriteDao
import com.genciptv.player.data.source.local.dao.PlaylistDao
import com.genciptv.player.data.source.local.dao.ProgramDao
import com.genciptv.player.data.source.local.dao.SeriesDao
import com.genciptv.player.data.source.local.dao.VodCategoryDao
import com.genciptv.player.data.source.local.dao.VodDao
import com.genciptv.player.data.source.local.entity.ChannelEntity
import com.genciptv.player.data.source.local.entity.ContinueWatchingEntity
import com.genciptv.player.data.source.local.entity.EpisodeEntity
import com.genciptv.player.data.source.local.entity.FavoriteEntity
import com.genciptv.player.data.source.local.entity.PlaylistEntity
import com.genciptv.player.data.source.local.entity.ProgramEntity
import com.genciptv.player.data.source.local.entity.SeriesEntity
import com.genciptv.player.data.source.local.entity.VodCategoryEntity
import com.genciptv.player.data.source.local.entity.VodEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        ProgramEntity::class,
        VodEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        VodCategoryEntity::class,
        FavoriteEntity::class,
        ContinueWatchingEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(DbTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun programDao(): ProgramDao
    abstract fun vodDao(): VodDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun vodCategoryDao(): VodCategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun continueWatchingDao(): ContinueWatchingDao

    companion object {
        const val DB_NAME = "genciptv.db"
    }
}
