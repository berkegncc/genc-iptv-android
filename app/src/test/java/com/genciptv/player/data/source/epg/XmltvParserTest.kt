package com.genciptv.player.data.source.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XmltvParserTest {

    private val parser = XmltvParserImpl()

    @Test
    fun `parses simple xmltv programme`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tv>
              <channel id="trt1.tr"><display-name>TRT 1</display-name></channel>
              <programme start="20260415210000 +0000" stop="20260415220000 +0000" channel="trt1.tr">
                <title>Ana Haber</title>
                <desc>Günün öne çıkan haberleri</desc>
                <category>News</category>
              </programme>
            </tv>
        """.trimIndent()

        val programs = parser.parse(xml.byteInputStream(), playlistId = 1L).toList()

        assertEquals(1, programs.size)
        val p = programs[0]
        assertEquals("trt1.tr", p.channelEpgId)
        assertEquals(1L, p.playlistId)
        assertEquals("Ana Haber", p.title)
        assertEquals("Günün öne çıkan haberleri", p.description)
        assertEquals("News", p.category)
    }

    @Test
    fun `parses multiple programmes with different channels`() {
        val xml = """
            <tv>
              <programme start="20260415200000 +0000" stop="20260415210000 +0000" channel="a">
                <title>A1</title>
              </programme>
              <programme start="20260415210000 +0000" stop="20260415220000 +0000" channel="a">
                <title>A2</title>
              </programme>
              <programme start="20260415210000 +0000" stop="20260415220000 +0000" channel="b">
                <title>B1</title>
              </programme>
            </tv>
        """.trimIndent()

        val programs = parser.parse(xml.byteInputStream(), playlistId = 5L).toList()
        assertEquals(3, programs.size)
        val aEntries = programs.filter { it.channelEpgId == "a" }
        assertEquals(2, aEntries.size)
    }

    @Test
    fun `parseXmltvTime handles offset correctly`() {
        // 21:00 local time in +03:00 = 18:00 UTC
        val withOffset = XmltvParserImpl.parseXmltvTime("20260415210000 +0300")
        val asUtc = XmltvParserImpl.parseXmltvTime("20260415180000 +0000")
        assertNotNull(withOffset)
        assertNotNull(asUtc)
        assertEquals(asUtc, withOffset)
    }

    @Test
    fun `parseXmltvTime handles missing offset as UTC`() {
        val noOffset = XmltvParserImpl.parseXmltvTime("20260415120000")
        val withZero = XmltvParserImpl.parseXmltvTime("20260415120000 +0000")
        assertEquals(withZero, noOffset)
    }

    @Test
    fun `parseXmltvTime returns null for malformed input`() {
        assertEquals(null, XmltvParserImpl.parseXmltvTime("not a time"))
        assertEquals(null, XmltvParserImpl.parseXmltvTime("2026"))
    }

    @Test
    fun `programme start and stop converted to epoch millis`() {
        val xml = """
            <tv>
              <programme start="20260101000000 +0000" stop="20260101010000 +0000" channel="x">
                <title>Midnight Show</title>
              </programme>
            </tv>
        """.trimIndent()

        val programs = parser.parse(xml.byteInputStream(), playlistId = 1L).toList()
        assertEquals(1, programs.size)
        // 2026-01-01 00:00:00 UTC = 1767225600000 ms
        assertEquals(1767225600000L, programs[0].startMillis)
        assertEquals(1767225600000L + 3600_000L, programs[0].stopMillis)
    }

    @Test
    fun `missing title produces empty string program`() {
        val xml = """
            <tv>
              <programme start="20260415210000 +0000" stop="20260415220000 +0000" channel="x">
              </programme>
            </tv>
        """.trimIndent()

        val programs = parser.parse(xml.byteInputStream(), playlistId = 1L).toList()
        assertEquals(1, programs.size)
        assertEquals("", programs[0].title)
    }

    @Test
    fun `malformed xml returns empty list gracefully`() {
        val xml = "<tv><programme start='bad' channel='x'><title>Nope"
        val programs = parser.parse(xml.byteInputStream(), playlistId = 1L).toList()
        assertTrue(programs.isEmpty())
    }
}
