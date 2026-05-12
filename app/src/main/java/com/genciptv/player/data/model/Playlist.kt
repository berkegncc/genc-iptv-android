package com.genciptv.player.data.model

/**
 * Type of playlist source.
 * M3U: raw playlist URL (optionally with external XMLTV EPG URL).
 * XTREAM: Xtream Codes API (server + username + password).
 */
enum class PlaylistType { M3U, XTREAM }

/**
 * A playlist configured by the user. One playlist = one content provider.
 * Multiple playlists may be stored but only one is [isActive] at a time.
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val type: PlaylistType,
    /** For M3U: the playlist URL. For Xtream: server base URL (e.g. http://host:port). */
    val url: String,
    val username: String? = null,
    val password: String? = null,
    /** External XMLTV EPG URL for M3U playlists (optional). */
    val epgUrl: String? = null,
    val isActive: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val channelCount: Int = 0,
    val userInfo: XtreamUserInfo? = null,
    /** Optional User-Agent override sent with all requests for this playlist. */
    val userAgent: String? = null,
)

/**
 * Xtream account info returned by `player_api.php?action=get_user_info`.
 * Null for M3U playlists.
 */
data class XtreamUserInfo(
    val username: String,
    val status: String,               // "Active", "Expired", "Disabled", ...
    val expDateMillis: Long? = null,
    val isTrial: Boolean = false,
    val maxConnections: Int? = null,
)
