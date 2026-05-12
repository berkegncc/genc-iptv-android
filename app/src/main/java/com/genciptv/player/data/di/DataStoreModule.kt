package com.genciptv.player.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.genciptv.player.data.source.local.prefs.AppearancePreferencesDataSource
import com.genciptv.player.data.source.local.prefs.PlayerPreferencesDataSource
import com.genciptv.player.data.source.local.prefs.RecentChannelsDataSource
import com.genciptv.player.data.source.local.prefs.SubtitleStyleDataSource
import com.genciptv.player.data.source.local.prefs.UserPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppearanceDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlayerDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SubtitleDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecentChannelsDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private fun createStore(
        context: Context,
        fileName: String,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile(fileName) },
        )

    @Provides @Singleton @AppearanceDataStore
    fun provideAppearanceStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createStore(context, AppearancePreferencesDataSource.FILE)

    @Provides @Singleton @PlayerDataStore
    fun providePlayerStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createStore(context, PlayerPreferencesDataSource.FILE)

    @Provides @Singleton @SubtitleDataStore
    fun provideSubtitleStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createStore(context, SubtitleStyleDataSource.FILE)

    @Provides @Singleton @UserDataStore
    fun provideUserStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createStore(context, UserPreferencesDataSource.FILE)

    @Provides @Singleton
    fun provideAppearanceDataSource(
        @AppearanceDataStore store: DataStore<Preferences>,
    ): AppearancePreferencesDataSource = AppearancePreferencesDataSource(store)

    @Provides @Singleton
    fun providePlayerDataSource(
        @PlayerDataStore store: DataStore<Preferences>,
    ): PlayerPreferencesDataSource = PlayerPreferencesDataSource(store)

    @Provides @Singleton
    fun provideSubtitleDataSource(
        @SubtitleDataStore store: DataStore<Preferences>,
    ): SubtitleStyleDataSource = SubtitleStyleDataSource(store)

    @Provides @Singleton
    fun provideUserDataSource(
        @UserDataStore store: DataStore<Preferences>,
    ): UserPreferencesDataSource = UserPreferencesDataSource(store)

    @Provides @Singleton @RecentChannelsDataStore
    fun provideRecentChannelsStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createStore(context, RecentChannelsDataSource.FILE)

    @Provides @Singleton
    fun provideRecentChannelsDataSource(
        @RecentChannelsDataStore store: DataStore<Preferences>,
    ): RecentChannelsDataSource = RecentChannelsDataSource(store)
}
