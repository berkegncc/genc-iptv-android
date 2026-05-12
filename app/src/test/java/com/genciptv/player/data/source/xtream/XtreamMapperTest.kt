package com.genciptv.player.data.source.xtream

import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.PlaylistType
import com.genciptv.player.data.source.xtream.dto.XtreamAuthResponse
import com.genciptv.player.data.source.xtream.dto.XtreamEpgEntryDto
import com.genciptv.player.data.source.xtream.dto.XtreamLiveStreamDto
import com.genciptv.player.data.source.xtream.dto.XtreamServerInfoDto
import com.genciptv.player.data.source.xtream.dto.XtreamUserInfoDto
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class XtreamMapperTest {

    private val testPlaylist = Playlist(
        id = 42L,
        name = "Test Provider",
        type = PlaylistType.XTREAM,
        url = "http://example.com:8080",
        username = "alice",
        password = "secret",
        isActive = true,
    )

    // ── URL builder ───────────────────────────────────────────────────────────

    @Test
    fun `playerApi builds correct URL and strips trailing slash`() {
        assertEquals(
            "http://host:1234/player_api.php",
            XtreamUrlBuilder.playerApi("http://host:1234/"),
        )
        assertEquals(
            "http://host:1234/player_api.php",
            XtreamUrlBuilder.playerApi("http://host:1234"),
        )
    }

    @Test
    fun `liveStream builds HLS url with credentials and stream id`() {
        val url = XtreamUrlBuilder.liveStream(
            serverBase = "http://host:1234",
            username = "u",
            password = "p",
            streamId = 999,
        )
        assertEquals("http://host:1234/live/u/p/999.m3u8", url)
    }

    @Test
    fun `vodStream builds movie url with container extension`() {
        val url = XtreamUrlBuilder.vodStream(
            serverBase = "http://host:1234/",
            username = "u",
            password = "p",
            streamId = 101,
            ext = "mkv",
        )
        assertEquals("http://host:1234/movie/u/p/101.mkv", url)
    }

    // ── Auth / user info ──────────────────────────────────────────────────────

    @Test
    fun `toUserInfo converts active xtream user`() {
        val auth = XtreamAuthResponse(
            userInfo = XtreamUserInfoDto(
                username = "alice",
                status = "Active",
                expDate = "1800000000",
                isTrial = "0",
                maxConnections = "2",
            ),
            serverInfo = XtreamServerInfoDto(url = "host", port = "8080"),
        )
        val info = XtreamMapper.toUserInfo(auth)
        assertNotNull(info)
        assertEquals("alice", info!!.username)
        assertEquals("Active", info.status)
        assertEquals(1800000000L * 1000L, info.expDateMillis)
        assertFalse(info.isTrial)
        assertEquals(2, info.maxConnections)
    }

    @Test
    fun `toUserInfo handles trial flag correctly`() {
        val auth = XtreamAuthResponse(
            userInfo = XtreamUserInfoDto(
                username = "bob",
                status = "Active",
                isTrial = "1",
            )
        )
        val info = XtreamMapper.toUserInfo(auth)
        assertTrue(info!!.isTrial)
    }

    @Test
    fun `toUserInfo returns null when username missing`() {
        val auth = XtreamAuthResponse(
            userInfo = XtreamUserInfoDto(username = null)
        )
        assertNull(XtreamMapper.toUserInfo(auth))
    }

    // ── Live stream → channel ────────────────────────────────────────────────

    @Test
    fun `toChannel builds proper channel with stream url`() {
        val dto = XtreamLiveStreamDto(
            name = "TRT 1 HD",
            streamId = 42,
            streamIcon = "http://logo.example/trt1.png",
            epgChannelId = "trt1.tr",
            categoryId = JsonPrimitive("1"),
        )
        val channel = XtreamMapper.toChannel(dto, testPlaylist, 0)
        assertNotNull(channel)
        channel!!
        assertEquals("42:42", channel.id)
        assertEquals(42L, channel.playlistId)
        assertEquals("TRT 1 HD", channel.name)
        assertEquals(
            "http://example.com:8080/live/alice/secret/42.m3u8",
            channel.streamUrl,
        )
        assertEquals("trt1.tr", channel.epgChannelId)
        assertTrue(channel.isHd)
    }

    @Test
    fun `toChannel marks 1080 as HD`() {
        val dto = XtreamLiveStreamDto(
            name = "Fox 1080",
            streamId = 1,
        )
        val channel = XtreamMapper.toChannel(dto, testPlaylist, 0)
        assertTrue(channel!!.isHd)
    }

    @Test
    fun `toChannel returns null when credentials missing`() {
        val bad = testPlaylist.copy(username = null)
        val dto = XtreamLiveStreamDto(name = "X", streamId = 1)
        assertNull(XtreamMapper.toChannel(dto, bad, 0))
    }

    // ── Base64 decoding for short EPG ─────────────────────────────────────────

    @Test
    fun `decodeBase64Jvm decodes a base64 title`() {
        val plain = "Şampiyonlar Ligi"
        val encoded = Base64.getEncoder().encodeToString(plain.toByteArray(Charsets.UTF_8))
        assertEquals(plain, XtreamMapper.decodeBase64Jvm(encoded))
    }

    @Test
    fun `decodeBase64Jvm returns null for blank`() {
        assertNull(XtreamMapper.decodeBase64Jvm(null))
        assertNull(XtreamMapper.decodeBase64Jvm(""))
    }

    @Test
    fun `toProgram parses EPG entry with base64 fields`() {
        val title = "Ana Haber"
        val desc = "Haberler"
        val titleB64 = Base64.getEncoder().encodeToString(title.toByteArray())
        val descB64 = Base64.getEncoder().encodeToString(desc.toByteArray())
        val dto = XtreamEpgEntryDto(
            id = "1",
            epgId = "trt1.tr",
            title = titleB64,
            description = descB64,
            startTimestamp = "1800000000",
            stopTimestamp = "1800003600",
        )
        val program = XtreamMapper.toProgram(dto, playlistId = 42L, useAndroidBase64 = false)
        assertNotNull(program)
        program!!
        assertEquals("Ana Haber", program.title)
        assertEquals("Haberler", program.description)
        assertEquals("trt1.tr", program.channelEpgId)
        assertEquals(1800000000L * 1000L, program.startMillis)
    }

    @Test
    fun `toProgram returns null when timestamps missing`() {
        val dto = XtreamEpgEntryDto(
            id = "1",
            epgId = "x",
            startTimestamp = null,
            stopTimestamp = null,
        )
        assertNull(XtreamMapper.toProgram(dto, playlistId = 1L, useAndroidBase64 = false))
    }

    // ── JSON helpers ──────────────────────────────────────────────────────────

    @Test
    fun `jsonElementToDoubleOrNull parses numbers and numeric strings`() {
        assertEquals(4.5, XtreamMapper.jsonElementToDoubleOrNull(JsonPrimitive(4.5)))
        assertEquals(7.0, XtreamMapper.jsonElementToDoubleOrNull(JsonPrimitive("7.0")))
        assertNull(XtreamMapper.jsonElementToDoubleOrNull(JsonPrimitive("not a num")))
        assertNull(XtreamMapper.jsonElementToDoubleOrNull(null))
    }
}
