package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.genciptv.player.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val flow: Flow<UserPreferences> = dataStore.data.map { p ->
        UserPreferences(
            displayName = p[KEY_DISPLAY_NAME] ?: "",
            onboardingCompleted = p[KEY_ONBOARDING_DONE] ?: false,
            activePlaylistId = p[KEY_ACTIVE_PLAYLIST] ?: -1L,
            autoUpdateEnabled = p[KEY_AUTO_UPDATE] ?: true,
        )
    }

    suspend fun setDisplayName(name: String) {
        dataStore.edit { it[KEY_DISPLAY_NAME] = name }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setActivePlaylistId(id: Long) {
        dataStore.edit { it[KEY_ACTIVE_PLAYLIST] = id }
    }

    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_UPDATE] = enabled }
    }

    companion object {
        const val FILE = "user_prefs"
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val KEY_ACTIVE_PLAYLIST = longPreferencesKey("active_playlist_id")
        private val KEY_AUTO_UPDATE = booleanPreferencesKey("auto_update_enabled")
    }
}
