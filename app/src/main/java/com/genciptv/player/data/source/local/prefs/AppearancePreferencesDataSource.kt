package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.genciptv.player.data.model.AppearancePreferences
import com.genciptv.player.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppearancePreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val flow: Flow<AppearancePreferences> = dataStore.data.map { p ->
        AppearancePreferences(
            themeMode = runCatching {
                ThemeMode.valueOf(p[KEY_THEME_MODE] ?: ThemeMode.LIGHT.name)
            }.getOrDefault(ThemeMode.LIGHT),
            accentKey = p[KEY_ACCENT] ?: "PURPLE",
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setAccentKey(key: String) {
        dataStore.edit { it[KEY_ACCENT] = key }
    }

    companion object {
        const val FILE = "appearance_prefs"
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_ACCENT = stringPreferencesKey("accent_key")
    }
}
