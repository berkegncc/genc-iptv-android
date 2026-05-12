package com.genciptv.player.data.source.m3u

/**
 * A single parsed entry from an M3U playlist (one channel).
 *
 * Extracted from the `#EXTINF:-1 ...` attribute line + its URL line.
 */
data class M3uEntry(
    val displayName: String,
    val url: String,
    val tvgId: String? = null,
    val tvgName: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null,
    val duration: Int = -1,
    /** Per-entry User-Agent override from #EXTVLCOPT:http-user-agent=... */
    val userAgent: String? = null,
    /** Per-entry HTTP Referer override from #EXTVLCOPT:http-referrer=... */
    val referer: String? = null,
)
