package com.genciptv.player.data.source.local.entity

import androidx.room.Entity

@Entity(
    tableName = "favorites",
    primaryKeys = ["targetId", "targetType"],
)
data class FavoriteEntity(
    val targetId: String,
    /** "CHANNEL", "MOVIE", or "SERIES" */
    val targetType: String,
    val addedAt: Long,
)

@Entity(
    tableName = "continue_watching",
    primaryKeys = ["targetId", "targetType"],
)
data class ContinueWatchingEntity(
    val targetId: String,
    val targetType: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    /**
     * For SERIES rows: the episode the user should resume on. Lets us key the
     * row by series id (so the same series doesn't appear once per episode)
     * while still routing the tap to the right episode player. `null` for
     * movies and channels.
     */
    val resumeEpisodeId: String? = null,
)
