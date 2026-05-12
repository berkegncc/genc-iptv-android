package com.genciptv.player.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** "M3U" or "XTREAM". */
    val type: String,
    val url: String,
    val username: String? = null,
    val password: String? = null,
    val epgUrl: String? = null,
    @ColumnInfo(defaultValue = "0") val isActive: Boolean = false,
    @ColumnInfo(defaultValue = "0") val lastSyncedAt: Long = 0L,
    @ColumnInfo(defaultValue = "0") val channelCount: Int = 0,
    val userAgent: String? = null,
    @Embedded(prefix = "xtream_") val xtreamUserInfo: XtreamUserInfoEmbedded? = null,
)

/**
 * Embedded Xtream user info inside PlaylistEntity.
 * All fields nullable so the whole embedded object can be null.
 */
data class XtreamUserInfoEmbedded(
    val username: String? = null,
    val status: String? = null,
    val expDateMillis: Long? = null,
    val isTrial: Boolean? = null,
    val maxConnections: Int? = null,
)
