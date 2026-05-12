package com.genciptv.player.data.source.local.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local-only LRU list of recently-opened live channel IDs.
 * Max [MAX_ENTRIES] entries. Uses DataStore; never leaves the device.
 * This is NOT ContinueWatching — there is no position/duration.
 */
class RecentChannelsDataSource(
    private val dataStore: DataStore<Preferences>,
) {
    val flow: Flow<List<String>> = dataStore.data.map { p ->
        val raw = p[KEY] ?: ""
        if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    suspend fun addRecent(channelId: String) {
        dataStore.edit { prefs ->
            val current = (prefs[KEY] ?: "").split("|").filter { it.isNotBlank() }
            val deduped = (listOf(channelId) + current.filter { it != channelId })
                .take(MAX_ENTRIES)
            prefs[KEY] = deduped.joinToString("|")
        }
    }

    suspend fun clear() {
        dataStore.edit { it.remove(KEY) }
    }

    companion object {
        const val FILE = "recent_channels"
        const val MAX_ENTRIES = 15
        private val KEY = stringPreferencesKey("recent_channel_ids")
    }
}
