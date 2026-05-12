package com.genciptv.player.data.model

/**
 * A live TV channel from a [Playlist].
 *
 * @property id globally unique id, format: `"{playlistId}:{streamId-or-slug}"`
 * @property epgChannelId used to join with [Program] via EPG xml tvg-id
 */
data class Channel(
    val id: String,
    val playlistId: Long,
    val name: String,
    val logoUrl: String? = null,
    val streamUrl: String,
    val groupTitle: String? = null,
    val epgChannelId: String? = null,
    val isHd: Boolean = false,
    val sortOrder: Int = 0,
    /** Provider-defined category order; lower values first, `Int.MAX_VALUE` for unknown. */
    val groupSortOrder: Int = Int.MAX_VALUE,
)
