package com.genciptv.player.data.source.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Room type converters. All `List<String>` columns are stored as JSON text.
 * Enum columns are stored as their `name` strings via typed converters.
 */
class DbTypeConverters {

    // ── List<String> ↔ JSON ───────────────────────────────────────────────────
    @TypeConverter
    fun stringListToJson(list: List<String>?): String? =
        list?.let { json.encodeToString(ListSerializer(String.serializer()), it) }

    @TypeConverter
    fun jsonToStringList(text: String?): List<String>? =
        text?.let { json.decodeFromString(ListSerializer(String.serializer()), it) }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}
