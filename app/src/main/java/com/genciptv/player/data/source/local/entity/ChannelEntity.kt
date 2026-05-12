package com.genciptv.player.data.source.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("playlistId"),
        Index("epgChannelId"),
        Index("groupTitle"),
    ],
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: Long,
    val name: String,
    val logoUrl: String? = null,
    val streamUrl: String,
    val groupTitle: String? = null,
    val epgChannelId: String? = null,
    val isHd: Boolean = false,
    val sortOrder: Int = 0,
    /**
     * Provider-defined category order. For Xtream this is the index of the
     * channel's category in `get_live_categories` response. For M3U it is the
     * index of the first channel seen in that group within the playlist file.
     * Lower values come first. Unknown categories use [Int.MAX_VALUE].
     */
    val groupSortOrder: Int = Int.MAX_VALUE,
)
