package com.genciptv.player.data.di

import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.ChannelRepositoryImpl
import com.genciptv.player.data.repository.ContinueWatchingRepository
import com.genciptv.player.data.repository.ContinueWatchingRepositoryImpl
import com.genciptv.player.data.repository.EpgRepository
import com.genciptv.player.data.repository.EpgRepositoryImpl
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.FavoriteRepositoryImpl
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.PlaylistRepositoryImpl
import com.genciptv.player.data.repository.TmdbRepository
import com.genciptv.player.data.repository.TmdbRepositoryImpl
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.data.repository.UserPreferencesRepositoryImpl
import com.genciptv.player.data.repository.VodRepository
import com.genciptv.player.data.repository.VodRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository

    @Binds @Singleton
    abstract fun bindEpgRepository(impl: EpgRepositoryImpl): EpgRepository

    @Binds @Singleton
    abstract fun bindVodRepository(impl: VodRepositoryImpl): VodRepository

    @Binds @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds @Singleton
    abstract fun bindContinueWatchingRepository(
        impl: ContinueWatchingRepositoryImpl,
    ): ContinueWatchingRepository

    @Binds @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl,
    ): UserPreferencesRepository

    @Binds @Singleton
    abstract fun bindTmdbRepository(impl: TmdbRepositoryImpl): TmdbRepository
}
