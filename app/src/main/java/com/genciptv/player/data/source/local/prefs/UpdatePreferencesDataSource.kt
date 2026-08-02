package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Bookkeeping for the in-app updater: when we last asked GitHub, and which
 * version the user waved away with "Daha sonra".
 */
class UpdatePreferencesDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    /** Epoch millis of the last *successful* check; 0 when never checked. */
    val lastCheckTimestamp: Flow<Long> = dataStore.data.map { it[KEY_LAST_CHECK] ?: 0L }

    /** Version the user dismissed, without the `v` prefix; empty when none. */
    val dismissedVersion: Flow<String> = dataStore.data.map { it[KEY_DISMISSED] ?: "" }

    suspend fun lastCheckTimestampOnce(): Long = lastCheckTimestamp.first()

    suspend fun dismissedVersionOnce(): String = dismissedVersion.first()

    suspend fun setLastCheckTimestamp(millis: Long) {
        dataStore.edit { it[KEY_LAST_CHECK] = millis }
    }

    suspend fun setDismissedVersion(version: String) {
        dataStore.edit { it[KEY_DISMISSED] = version }
    }

    companion object {
        const val FILE = "update_prefs"
        private val KEY_LAST_CHECK = longPreferencesKey("last_check_timestamp")
        private val KEY_DISMISSED = stringPreferencesKey("dismissed_version")
    }
}
