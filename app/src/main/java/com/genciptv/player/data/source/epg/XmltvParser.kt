package com.genciptv.player.data.source.epg

import com.genciptv.player.data.model.Program
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.TimeZone
import javax.inject.Inject

/**
 * Parses XMLTV streams into a [Sequence] of domain [Program] objects.
 *
 * Handles the classic XMLTV `<tv>` format:
 * ```
 * <tv>
 *   <channel id="...">...</channel>
 *   <programme start="20260415210000 +0300" stop="..." channel="...">
 *     <title>...</title>
 *     <desc>...</desc>
 *     <category>...</category>
 *   </programme>
 * </tv>
 * ```
 *
 * Times are converted to UTC epoch millis.
 */
interface XmltvParser {
    fun parse(input: InputStream, playlistId: Long): Sequence<Program>
}

class XmltvParserImpl @Inject constructor() : XmltvParser {

    override fun parse(input: InputStream, playlistId: Long): Sequence<Program> {
        // We build a list up-front because XmlPullParser is stateful and
        // consuming it lazily across Sequence boundaries is fragile.
        // Memory footprint is acceptable since EPG data is typically bounded
        // (a week of programs for ~1000 channels ≈ 50-100 MB uncompressed XML,
        // but only the parsed program data kept in RAM = ~10-20 MB worst case).
        return buildList<Program> {
            try {
                val factory = XmlPullParserFactory.newInstance().apply {
                    isNamespaceAware = false
                }
                val parser = factory.newPullParser()
                parser.setInput(input, null)

                var eventType = parser.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && parser.name.equals("programme", true)) {
                        readProgramme(parser, playlistId)?.let(::add)
                    }
                    eventType = parser.next()
                }
            } catch (_: XmlPullParserException) {
                // Skip malformed EPG — return whatever we parsed so far.
            }
        }.asSequence()
    }

    private fun readProgramme(parser: XmlPullParser, playlistId: Long): Program? {
        val startRaw = parser.getAttributeValue(null, "start") ?: return null
        val stopRaw = parser.getAttributeValue(null, "stop") ?: return null
        val channel = parser.getAttributeValue(null, "channel") ?: return null

        val startMillis = parseXmltvTime(startRaw) ?: return null
        val stopMillis = parseXmltvTime(stopRaw) ?: return null

        var title: String? = null
        var description: String? = null
        var category: String? = null

        var depth = 1
        while (depth > 0) {
            val event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break
            if (event == XmlPullParser.START_TAG) {
                depth++
                when (parser.name.lowercase()) {
                    "title" -> title = readText(parser)?.also { depth-- }
                    "desc" -> description = readText(parser)?.also { depth-- }
                    "category" -> category = readText(parser)?.also { depth-- }
                    else -> {
                        // skip unknown nested tag completely
                        skipTag(parser); depth--
                    }
                }
            } else if (event == XmlPullParser.END_TAG) {
                depth--
                if (depth == 0 && parser.name.equals("programme", true)) break
            }
        }

        return Program(
            channelEpgId = channel,
            playlistId = playlistId,
            title = title ?: "",
            description = description,
            startMillis = startMillis,
            stopMillis = stopMillis,
            category = category,
        )
    }

    private fun readText(parser: XmlPullParser): String? {
        // parser is currently at START_TAG; advance to TEXT and END_TAG
        var text: String? = null
        while (parser.next() != XmlPullParser.END_TAG) {
            when (parser.eventType) {
                XmlPullParser.TEXT -> text = parser.text
                XmlPullParser.END_DOCUMENT -> return text
            }
        }
        return text?.trim()
    }

    private fun skipTag(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    companion object {
        /**
         * Parse an XMLTV timestamp into UTC epoch millis.
         *
         * Accepted forms (all with optional trailing tz):
         *   `20260415210000 +0300`
         *   `20260415210000`  (assumed UTC)
         *   `20260415210000 +03:00`
         */
        internal fun parseXmltvTime(raw: String): Long? {
            val clean = raw.trim()
            if (clean.length < 14) return null

            return try {
                val y = clean.substring(0, 4).toInt()
                val mo = clean.substring(4, 6).toInt()
                val d = clean.substring(6, 8).toInt()
                val h = clean.substring(8, 10).toInt()
                val mi = clean.substring(10, 12).toInt()
                val s = clean.substring(12, 14).toInt()

                // Extract offset
                val tzPart = clean.substring(14).trim()
                val offsetMillis: Long = when {
                    tzPart.isEmpty() -> 0L
                    tzPart.startsWith("+") || tzPart.startsWith("-") -> {
                        val sign = if (tzPart[0] == '+') 1 else -1
                        val body = tzPart.substring(1).replace(":", "")
                        if (body.length < 4) 0L
                        else {
                            val oh = body.substring(0, 2).toIntOrNull() ?: 0
                            val om = body.substring(2, 4).toIntOrNull() ?: 0
                            sign * (oh * 3_600_000L + om * 60_000L)
                        }
                    }
                    else -> 0L
                }

                // Build UTC calendar
                val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.clear()
                cal.set(y, mo - 1, d, h, mi, s)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis - offsetMillis
            } catch (_: Exception) {
                null
            }
        }
    }
}
