package com.genciptv.player.data.model

/**
 * A single EPG program entry for a channel.
 * Times are stored as epoch millis (UTC).
 */
data class Program(
    val id: Long = 0,
    val channelEpgId: String,
    val playlistId: Long,
    val title: String,
    val description: String? = null,
    val startMillis: Long,
    val stopMillis: Long,
    val category: String? = null,
)
