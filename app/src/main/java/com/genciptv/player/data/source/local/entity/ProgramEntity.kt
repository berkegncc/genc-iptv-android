package com.genciptv.player.data.source.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "programs",
    indices = [
        Index("playlistId"),
        Index("channelEpgId"),
        Index("startMillis"),
        Index("stopMillis"),
        Index(value = ["playlistId", "channelEpgId", "startMillis"]),
    ],
)
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelEpgId: String,
    val playlistId: Long,
    val title: String,
    val description: String? = null,
    val startMillis: Long,
    val stopMillis: Long,
    val category: String? = null,
)
