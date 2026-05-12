package com.genciptv.player.core.player

import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

/**
 * Many IPTV panels gate, redirect, or change response payload based on
 * `User-Agent`. Default Android / OkHttp UAs frequently get an HTML error
 * page or a stripped manifest, while VLC's UA is universally accepted.
 * Setting it here cuts a large class of "manifest malformed" / "403" errors.
 */
private const val DEFAULT_USER_AGENT = "VLC/3.0.20 LibVLC/3.0.20"

/**
 * HTTP data source used by every MediaSource we build. Custom UA, follows
 * cross-protocol redirects (HTTP↔HTTPS — common with IPTV CDNs), and gives
 * the read a generous timeout so live streams don't trip on momentary
 * stalls.
 */
fun buildIptvDataSourceFactory(userAgent: String? = null): DataSource.Factory =
    DefaultHttpDataSource.Factory()
        .setUserAgent(userAgent?.takeIf { it.isNotBlank() } ?: DEFAULT_USER_AGENT)
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(15_000)
        .setReadTimeoutMs(20_000)

/**
 * Many IPTV panels expose the same channel both as `.m3u8` (HLS playlist) and
 * `.ts` (raw MPEG-TS). When ExoPlayer fails to parse the `.m3u8` payload — even
 * after retrying as a progressive download — switching to the `.ts` URL often
 * succeeds because the byte stream is identical but the extension makes
 * `DefaultExtractorsFactory` route directly to `TsExtractor`.
 *
 * Returns `null` if the URL doesn't carry a swappable extension (so the caller
 * knows the fallback chain is exhausted).
 */
fun swapToTsExtension(url: String): String? {
    val queryIdx = url.indexOf('?')
    val path = if (queryIdx >= 0) url.substring(0, queryIdx) else url
    val query = if (queryIdx >= 0) url.substring(queryIdx) else ""
    val newPath = when {
        path.endsWith(".m3u8", ignoreCase = true) -> path.dropLast(5) + ".ts"
        path.endsWith(".m3u",  ignoreCase = true) -> path.dropLast(4) + ".ts"
        path.endsWith(".ts",   ignoreCase = true) -> return null  // already TS
        else -> return null
    }
    return newPath + query
}

/**
 * Build the MediaSource appropriate for [url].
 *
 * - `.m3u8` URLs go through [HlsMediaSource] with `allowChunklessPreparation`
 *   enabled — this forgives playlists that skip preparation segments, a
 *   common quirk in IPTV-provider HLS.
 * - Everything else uses [ProgressiveMediaSource], which handles raw MPEG-TS,
 *   MP4, and other progressive formats via the default extractor set.
 *
 * When [forceProgressive] is true even `.m3u8` URLs are treated as raw
 * streams. This is the recovery path for `ERROR_CODE_PARSING_MANIFEST_MALFORMED`:
 * some providers serve raw TS while keeping the `.m3u8` extension, so
 * retrying as progressive often succeeds where HLS fails.
 */
fun buildIptvMediaSource(
    url: String,
    dataSourceFactory: DataSource.Factory,
    forceProgressive: Boolean = false,
): MediaSource {
    val item = MediaItem.fromUri(url)
    val looksLikeHls = !forceProgressive && url.contains(".m3u8", ignoreCase = true)
    return if (looksLikeHls) {
        HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(item)
    } else {
        ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(item)
    }
}
