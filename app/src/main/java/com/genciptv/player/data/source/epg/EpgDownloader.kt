package com.genciptv.player.data.source.epg

import com.genciptv.player.data.model.Program
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject

/**
 * Downloads and parses an XMLTV EPG file. Handles GZip automatically
 * (either via Content-Encoding or by URL suffix `.gz`).
 */
class EpgDownloader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val parser: XmltvParser,
) {

    /**
     * Download, decompress (if needed), and parse an XMLTV file at [url].
     * Returns the list of parsed programs for [playlistId].
     */
    fun downloadAndParse(url: String, playlistId: Long): List<Program> {
        val request = Request.Builder().url(url).get().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("EPG HTTP ${response.code}: ${response.message}")
            }
            val body = response.body ?: throw IOException("Empty EPG response body")
            val raw: InputStream = body.byteStream()
            // OkHttp transparent gzip sets Content-Encoding="identity" on the
            // decoded stream — only wrap manually if URL suggests .gz and the
            // client didn't already decompress.
            val stream: InputStream =
                if (url.endsWith(".gz", ignoreCase = true) &&
                    response.header("Content-Encoding")?.equals("gzip", true) != true
                ) {
                    GZIPInputStream(raw)
                } else {
                    raw
                }

            stream.use { parser.parse(it, playlistId).toList() }
                .let { return it }
        }
    }
}
