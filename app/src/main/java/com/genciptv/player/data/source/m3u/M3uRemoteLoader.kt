package com.genciptv.player.data.source.m3u

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

/**
 * Downloads an M3U playlist from [url] via OkHttp and streams it into
 * the injected [M3uParser]. The returned sequence is backed by the network
 * response body, so it MUST be consumed in a single pass and within the
 * calling [load] scope.
 *
 * For batching into Room, collect the sequence in chunks in the repository.
 */
class M3uRemoteLoader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val parser: M3uParser,
) {

    /**
     * Download the playlist at [url] and materialise all entries into a list.
     * Prefer [loadStream] for very large playlists.
     */
    fun load(url: String, userAgent: String? = null): List<M3uEntry> {
        return loadStream(url, userAgent) { it.toList() }
    }

    /**
     * Stream the playlist, exposing a [Sequence] to [block]. The network call
     * and resource cleanup happen inside this method.
     */
    fun <R> loadStream(
        url: String,
        userAgent: String? = null,
        block: (Sequence<M3uEntry>) -> R,
    ): R {
        val requestBuilder = Request.Builder().url(url).get()
        if (userAgent != null) requestBuilder.header("User-Agent", userAgent)

        okHttpClient.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("M3U HTTP ${response.code}: ${response.message}")
            }
            val body = response.body ?: throw IOException("Empty M3U response body")
            body.byteStream().use { stream ->
                val seq = parser.parse(stream)
                return block(seq)
            }
        }
    }
}
