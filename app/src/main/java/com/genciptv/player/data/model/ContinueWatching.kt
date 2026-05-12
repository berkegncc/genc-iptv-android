package com.genciptv.player.data.model

/**
 * "Kaldığın Yerden" entry. For live channels, [durationMs] may be 0.
 *
 * For SERIES, [targetId] holds the series id (so the entry collapses to one
 * row per series — no matter which episode the user last watched), and
 * [resumeEpisodeId] holds the episode that should resume on tap. For movies
 * and channels [resumeEpisodeId] is null and [targetId] alone is enough.
 */
data class ContinueWatching(
    val targetId: String,
    val targetType: FavoriteTargetType,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    val resumeEpisodeId: String? = null,
)
