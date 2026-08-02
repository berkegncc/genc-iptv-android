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

    /**
     * v3 → v4: `vod_items` / `series` gain `addedAt` (provider timestamp, epoch
     * millis) and `providerId` (numeric Xtream id). "Son Eklenen" rows used to
     * sort on the TEXT primary key, which SQLite compares lexicographically —
     * so `"1:series:999"` outranked `"1:series:3500"` and the series row on the
     * home screen never changed. Backfill `providerId` from the id suffix so
     * the ordering is correct immediately, without waiting for a re-sync;
     * `addedAt` fills in on the next sync.
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vod_items ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE vod_items ADD COLUMN providerId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE series ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE series ADD COLUMN providerId INTEGER NOT NULL DEFAULT 0")
            db.execSQL(
                """
                UPDATE vod_items
                SET providerId = CAST(substr(id, instr(id, ':movie:') + 7) AS INTEGER)
                WHERE instr(id, ':movie:') > 0
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE series
                SET providerId = CAST(substr(id, instr(id, ':series:') + 8) AS INTEGER)
                WHERE instr(id, ':series:') > 0
                """.trimIndent()
            )
        }
    }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        )
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            // A missing migration wipes every table rather than crashing.
            //
            // Deliberate: this app is sideloaded, so a version that crashes on
            // launch locks the user out with no way back. Losing data is at
            // least recoverable. But be clear about the cost — `playlists`
            // holds the Xtream server, username and password, so the user must
            // type their subscription details in again; "just hit Refresh" is
            // not an option once that row is gone.
            //
            // So: whenever an @Entity changes, bump AppDatabase.version and add
            // the Migration above. See CLAUDE.md.
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
