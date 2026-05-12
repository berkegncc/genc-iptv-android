package com.genciptv.player.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.genciptv.player.data.source.local.AppDatabase
import com.genciptv.player.data.source.local.dao.ChannelDao
import com.genciptv.player.data.source.local.dao.ContinueWatchingDao
import com.genciptv.player.data.source.local.dao.EpisodeDao
import com.genciptv.player.data.source.local.dao.FavoriteDao
import com.genciptv.player.data.source.local.dao.PlaylistDao
import com.genciptv.player.data.source.local.dao.ProgramDao
import com.genciptv.player.data.source.local.dao.SeriesDao
import com.genciptv.player.data.source.local.dao.VodCategoryDao
import com.genciptv.player.data.source.local.dao.VodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * v2 → v3: add `resumeEpisodeId` column to `continue_watching` so series
     * rows can key by series id (one row per series) while still routing the
     * tap to the right episode. Stale SERIES rows from v2 had `targetId =
     * episodeId`, which would now collide with the new keying scheme — wipe
     * them so the user rebuilds them on the next watch. Movies and channels
     * (different `targetType`) stay intact.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE continue_watching ADD COLUMN resumeEpisodeId TEXT")
            db.execSQL("DELETE FROM continue_watching WHERE targetType = 'SERIES'")
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        )
            .addMigrations(MIGRATION_2_3)
            // Anything we forgot to migrate explicitly drops the DB; user just
            // re-syncs via the Refresh button in the Channels header.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()
    @Provides fun provideProgramDao(db: AppDatabase): ProgramDao = db.programDao()
    @Provides fun provideVodDao(db: AppDatabase): VodDao = db.vodDao()
    @Provides fun provideSeriesDao(db: AppDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideVodCategoryDao(db: AppDatabase): VodCategoryDao = db.vodCategoryDao()
    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideContinueWatchingDao(db: AppDatabase): ContinueWatchingDao =
        db.continueWatchingDao()
}
