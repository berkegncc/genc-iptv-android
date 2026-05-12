package com.genciptv.player.data.source.m3u

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    private val parser = M3uParserImpl()

    @Test
    fun `parses standard EXTINF entry with attributes`() {
        val sample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="trt1.tr" tvg-name="TRT 1" tvg-logo="https://example.com/trt1.png" group-title="Ulusal",TRT 1 HD
            http://stream.example.com/trt1.m3u8
        """.trimIndent()

        val entries = parser.parse(sample).toList()

        assertEquals(1, entries.size)
        val e = entries[0]
        assertEquals("TRT 1 HD", e.displayName)
        assertEquals("http://stream.example.com/trt1.m3u8", e.url)
        assertEquals("trt1.tr", e.tvgId)
        assertEquals("TRT 1", e.tvgName)
        assertEquals("https://example.com/trt1.png", e.tvgLogo)
        assertEquals("Ulusal", e.groupTitle)
    }

    @Test
    fun `detects HD from display name`() {
        val sample = """
            #EXTM3U
            #EXTINF:-1,Kanal D HD
            http://k.com/1.ts
            #EXTINF:-1,Kanal 7 1080p
            http://k.com/2.ts
            #EXTINF:-1,A Haber
            http://k.com/3.ts
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(3, entries.size)
        assertTrue(
            "first entry should look HD",
            entries[0].displayName.contains("HD"),
        )
    }

    @Test
    fun `parses multiple entries with mixed attributes`() {
        val sample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="a" group-title="A",Channel A
            http://a.com/1
            #EXTINF:-1 tvg-id="b",Channel B
            http://b.com/1
            #EXTINF:-1,Channel C
            http://c.com/1
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(3, entries.size)
        assertEquals("a", entries[0].tvgId)
        assertEquals("A", entries[0].groupTitle)
        assertEquals("b", entries[1].tvgId)
        assertNull(entries[1].groupTitle)
        assertNull(entries[2].tvgId)
    }

    @Test
    fun `handles EXTVLCOPT user-agent for next entry only`() {
        val sample = """
            #EXTM3U
            #EXTVLCOPT:http-user-agent=CustomUA/1.0
            #EXTINF:-1,Channel With UA
            http://x.com/1
            #EXTINF:-1,Channel Without UA
            http://x.com/2
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(2, entries.size)
        assertEquals("CustomUA/1.0", entries[0].userAgent)
        assertNull(entries[1].userAgent)
    }

    @Test
    fun `ignores comments and blank lines`() {
        val sample = """
            #EXTM3U

            # some comment
            #EXTINF:-1,Only Channel
            http://x.com/1


        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(1, entries.size)
        assertEquals("Only Channel", entries[0].displayName)
    }

    @Test
    fun `display name can contain commas when quoted values present`() {
        // Display name is after the last unquoted comma on EXTINF line
        val sample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="x,y" group-title="News, Sports",Breaking News
            http://x.com/1
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(1, entries.size)
        assertEquals("Breaking News", entries[0].displayName)
        assertEquals("x,y", entries[0].tvgId)
        assertEquals("News, Sports", entries[0].groupTitle)
    }

    @Test
    fun `empty playlist produces no entries`() {
        assertEquals(0, parser.parse("#EXTM3U\n").toList().size)
        assertEquals(0, parser.parse("").toList().size)
    }

    @Test
    fun `entry without preceding EXTINF is skipped`() {
        val sample = """
            #EXTM3U
            http://orphan.com/1
            #EXTINF:-1,Real Entry
            http://real.com/1
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertEquals(1, entries.size)
        assertEquals("Real Entry", entries[0].displayName)
    }

    @Test
    fun `tvg-logo with empty string becomes null`() {
        val sample = """
            #EXTM3U
            #EXTINF:-1 tvg-logo="",Test
            http://x.com/1
        """.trimIndent()

        val entries = parser.parse(sample).toList()
        assertNull(entries[0].tvgLogo)
    }

    @Test
    fun `large-ish playlist parses all entries`() {
        val sb = StringBuilder("#EXTM3U\n")
        repeat(100) { i ->
            sb.append("#EXTINF:-1 tvg-id=\"ch$i\" group-title=\"Group${i % 5}\",Channel $i\n")
            sb.append("http://stream.example.com/$i.m3u8\n")
        }
        val entries = parser.parse(sb.toString()).toList()
        assertEquals(100, entries.size)
        assertNotNull(entries[50].tvgId)
        assertEquals("Channel 50", entries[50].displayName)
    }
}
