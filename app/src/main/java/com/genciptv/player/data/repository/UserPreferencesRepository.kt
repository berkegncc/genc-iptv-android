package com.genciptv.player.data.repository

import com.genciptv.player.data.model.AppearancePreferences
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.ThemeMode
import com.genciptv.player.data.model.UserPreferences
import com.genciptv.player.data.source.local.prefs.AppearancePreferencesDataSource
import com.genciptv.player.data.source.local.prefs.PlayerPreferencesDataSource
import com.genciptv.player.data.source.local.prefs.RecentChannelsDataSource
import com.genciptv.player.data.source.local.prefs.SubtitleStyleDataSource
import com.genciptv.player.data.source.local.prefs.UserPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade for all user-facing preferences across DataStore files.
 * Provides stable Flow<T> outputs and suspend mutators for the ViewModels.
 */
interface UserPreferencesRepository {
    val appearance: Flow<AppearancePreferences>
    val player: Flow<PlayerPreferences>
    val subtitles: Flow<SubtitleStyle>
    val user: Flow<UserPreferences>

    /** Local-only LRU list of recently-opened live channel IDs (max 15). */
    val recentChannels: Flow<List<String>>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentKey(key: String)
    suspend fun updatePlayer(block: (PlayerPreferences) -> PlayerPreferences)
    suspend fun updateSubtitleStyle(style: SubtitleStyle)
    suspend fun resetSubtitleStyle()

    suspend fun setDisplayName(name: String)
    suspend fun setOnboardingCompleted(done: Boolean)
    suspend fun setActivePlaylistId(id: Long)
    suspend fun setAutoUpdateEnabled(enabled: Boolean)

    suspend fun addRecentChannel(id: String)
    suspend fun clearRecentChannels()
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val appearanceDs: AppearancePreferencesDataSource,
    private val playerDs: PlayerPreferencesDataSource,
    private val subtitleDs: SubtitleStyleDataSource,
    private val userDs: UserPreferencesDataSource,
    private val recentChannelsDs: RecentChannelsDataSource,
) : UserPreferencesRepository {

    override val appearance: Flow<AppearancePreferences> = appearanceDs.flow
    override val player: Flow<PlayerPreferences> = playerDs.flow
    override val subtitles: Flow<SubtitleStyle> = subtitleDs.flow
    override val user: Flow<UserPreferences> = userDs.flow
    override val recentChannels: Flow<List<String>> = recentChannelsDs.flow

    override suspend fun setThemeMode(mode: ThemeMode) = appearanceDs.setThemeMode(mode)
    override suspend fun setAccentKey(key: String) = appearanceDs.setAccentKey(key)

    override suspend fun updatePlayer(block: (PlayerPreferences) -> PlayerPreferences) =
        playerDs.update(block)

    override suspend fun updateSubtitleStyle(style: SubtitleStyle) = subtitleDs.update(style)
    override suspend fun resetSubtitleStyle() = subtitleDs.reset()

    override suspend fun setDisplayName(name: String) = userDs.setDisplayName(name)
    override suspend fun setOnboardingCompleted(done: Boolean) = userDs.setOnboardingCompleted(done)
    override suspend fun setActivePlaylistId(id: Long) = userDs.setActivePlaylistId(id)
    override suspend fun setAutoUpdateEnabled(enabled: Boolean) = userDs.setAutoUpdateEnabled(enabled)

    override suspend fun addRecentChannel(id: String) = recentChannelsDs.addRecent(id)
    override suspend fun clearRecentChannels() = recentChannelsDs.clear()
}
