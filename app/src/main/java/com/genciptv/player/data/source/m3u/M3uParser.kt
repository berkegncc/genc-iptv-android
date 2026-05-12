package com.genciptv.player.data.source.m3u

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * Parses `.m3u` / `.m3u8` playlist streams into a lazy [Sequence] of [M3uEntry].
 *
 * Supports:
 *  - `#EXTM3U` header (ignored)
 *  - `#EXTINF:<duration> tvg-id="..." tvg-name="..." tvg-logo="..." group-title="...",<display name>`
 *  - `#EXTVLCOPT:http-user-agent=...` (applies to the NEXT entry only)
 *  - `#EXTVLCOPT:http-referrer=...`
 *  - Comments (`#` lines not listed above) and blank lines are skipped.
 *
 * Output is a [Sequence] so callers can consume entries lazily without holding
 * the whole (possibly 10K+) playlist in memory.
 */
interface M3uParser {
    fun parse(input: InputStream): Sequence<M3uEntry>
    fun parse(text: String): Sequence<M3uEntry>
}

class M3uParserImpl @Inject constructor() : M3uParser {

    override fun parse(input: InputStream): Sequence<M3uEntry> {
        val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
        return readerToSequence(reader)
    }

    override fun parse(text: String): Sequence<M3uEntry> =
        readerToSequence(text.reader().buffered())

    private fun readerToSequence(reader: BufferedReader): Sequence<M3uEntry> = sequence {
        var pendingExtInf: ExtInfLine? = null
        var pendingUserAgent: String? = null
        var pendingReferer: String? = null

        reader.useLines { lines ->
            for (raw in lines) {
                val line = raw.trim()
                if (line.isEmpty()) continue

                when {
                    line.startsWith("#EXTM3U", ignoreCase = true) -> {
                        // header — ignore
                    }
                    line.startsWith("#EXTINF:", ignoreCase = true) -> {
                        pendingExtInf = parseExtInf(line)
                    }
                    line.startsWith("#EXTVLCOPT:", ignoreCase = true) -> {
                        val body = line.substringAfter(":").trim()
                        when {
                            body.startsWith("http-user-agent=", ignoreCase = true) ->
                                pendingUserAgent = body.substringAfter("=").trim()
                            body.startsWith("http-referrer=", ignoreCase = true) ||
                                body.startsWith("http-referer=", ignoreCase = true) ->
                                pendingReferer = body.substringAfter("=").trim()
                        }
                    }
                    line.startsWith("#") -> {
                        // other directive — ignore
                    }
                    else -> {
                        val ext = pendingExtInf
                        if (ext != null) {
                            yield(
                                M3uEntry(
                                    displayName = ext.displayName,
                                    url = line,
                                    tvgId = ext.attrs["tvg-id"]?.takeIf { it.isNotBlank() },
                                    tvgName = ext.attrs["tvg-name"]?.takeIf { it.isNotBlank() },
                                    tvgLogo = ext.attrs["tvg-logo"]?.takeIf { it.isNotBlank() },
                                    groupTitle = ext.attrs["group-title"]?.takeIf { it.isNotBlank() },
                                    duration = ext.duration,
                                    userAgent = pendingUserAgent,
                                    referer = pendingReferer,
                                ),
                            )
                        }
                        pendingExtInf = null
                        pendingUserAgent = null
                        pendingReferer = null
                    }
                }
            }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private data class ExtInfLine(
        val duration: Int,
        val attrs: Map<String, String>,
        val displayName: String,
    )

    /**
     * Parse one `#EXTINF:...` line.
     *
     * Format: `#EXTINF:<duration>[ key="value"]*,<display name>`
     *
     * Display name is everything after the LAST comma that isn't inside quotes.
     * Attributes are key=value pairs where value is usually quoted.
     */
    private fun parseExtInf(line: String): ExtInfLine {
        val body = line.substringAfter(":")

        // Split body into "header" (duration + attrs) and "name" by finding the
        // first top-level comma (not inside quotes).
        val headerEnd = findTopLevelComma(body)
        val header = if (headerEnd < 0) body else body.substring(0, headerEnd)
        val displayName = if (headerEnd < 0) "" else body.substring(headerEnd + 1).trim()

        // First token is duration (may be "-1" or seconds).
        val durationToken = header.takeWhile { it != ' ' && it != '\t' }
        val duration = durationToken.toIntOrNull() ?: -1

        // Remaining is attr list.
        val attrStart = header.indexOfFirst { it == ' ' || it == '\t' }
        val attrRegion = if (attrStart < 0) "" else header.substring(attrStart + 1)
        val attrs = parseAttributes(attrRegion)

        return ExtInfLine(duration, attrs, displayName)
    }

    /** Index of the first comma not enclosed in double quotes, or -1. */
    private fun findTopLevelComma(s: String): Int {
        var inQuotes = false
        for (i in s.indices) {
            val c = s[i]
            if (c == '"') inQuotes = !inQuotes
            else if (c == ',' && !inQuotes) return i
        }
        return -1
    }

    /**
     * Parse space-separated `key="value"` or `key=value` pairs.
     * Values may contain spaces when quoted.
     */
    private fun parseAttributes(region: String): Map<String, String> {
        if (region.isBlank()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        var i = 0
        val n = region.length
        while (i < n) {
            // Skip whitespace
            while (i < n && region[i].isWhitespace()) i++
            if (i >= n) break

            // Read key until '='
            val keyStart = i
            while (i < n && region[i] != '=' && !region[i].isWhitespace()) i++
            if (i >= n || region[i] != '=') break
            val key = region.substring(keyStart, i).lowercase()
            i++ // skip '='

            // Read value: quoted or bare
            val value: String
            if (i < n && region[i] == '"') {
                i++ // skip opening quote
                val vStart = i
                while (i < n && region[i] != '"') i++
                value = region.substring(vStart, i)
                if (i < n) i++ // skip closing quote
            } else {
                val vStart = i
                while (i < n && !region[i].isWhitespace()) i++
                value = region.substring(vStart, i)
            }
            result[key] = value
        }
        return result
    }
}
