package com.genciptv.player.data.source.xtream

import android.util.Base64
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Playlist
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodCategory
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.data.model.XtreamUserInfo
import com.genciptv.player.data.source.xtream.dto.XtreamAuthResponse
import com.genciptv.player.data.source.xtream.dto.XtreamCategoryDto
import com.genciptv.player.data.source.xtream.dto.XtreamEpgEntryDto
import com.genciptv.player.data.source.xtream.dto.XtreamLiveStreamDto
import com.genciptv.player.data.source.xtream.dto.XtreamSeriesDto
import com.genciptv.player.data.source.xtream.dto.XtreamVodDto
import com.genciptv.player.data.source.xtream.dto.XtreamVodInfoResponse
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Pure functions converting Xtream DTOs to domain models.
 * Kept free of Android `Log` / Base64 where possible so they can be unit tested.
 */
object XtreamMapper {

    // ── Primitive helpers (testable without Android) ──────────────────────────

    internal fun jsonElementToDoubleOrNull(el: JsonElement?): Double? {
        if (el == null) return null
        return try {
            val prim = el as? JsonPrimitive ?: return null
            prim.doubleOrNull ?: prim.content.toDoubleOrNull()
        } catch (_: Exception) {
            null
        }
    }

    internal fun jsonElementToIntOrNull(el: JsonElement?): Int? {
        if (el == null) return null
        return try {
            val prim = el as? JsonPrimitive ?: return null
            prim.intOrNull ?: prim.content.toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    internal fun jsonElementToStringOrNull(el: JsonElement?): String? {
        if (el == null) return null
        return try {
            (el as? JsonPrimitive)?.content
        } catch (_: Exception) {
            null
        }
    }

    /** Safe Base64 decode. Returns null for empty/invalid input. */
    internal fun decodeBase64(text: String?): String? {
        if (text.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(text, Base64.DEFAULT)
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    /** Testable Base64 decoder using java.util.Base64 (JVM-only). */
    internal fun decodeBase64Jvm(text: String?): String? {
        if (text.isNullOrBlank()) return null
        return try {
            val bytes = java.util.Base64.getDecoder().decode(text.trim())
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    // ── User info / auth ──────────────────────────────────────────────────────

    fun toUserInfo(resp: XtreamAuthResponse): XtreamUserInfo? {
        val u = resp.userInfo ?: return null
        val username = u.username ?: return null
        return XtreamUserInfo(
            username = username,
            status = u.status ?: "Unknown",
            expDateMillis = u.expDate?.toLongOrNull()?.let { it * 1000L },
            isTrial = u.isTrial == "1",
            maxConnections = u.maxConnections?.toIntOrNull(),
        )
    }

    // ── Live streams → Channels ──────────────────────────────────────────────

    fun toChannel(
        dto: XtreamLiveStreamDto,
        playlist: Playlist,
        sortOrder: Int,
        categoryNameById: Map<String, String> = emptyMap(),
        categoryOrderById: Map<String, Int> = emptyMap(),
    ): Channel? {
        val username = playlist.username ?: return null
        val password = playlist.password ?: return null
        val streamUrl = XtreamUrlBuilder.liveStream(
            playlist.url, username, password, dto.streamId,
        )
        // Resolve human-readable category name from id; fall back to raw id if missing.
        val rawCatId = dto.categoryIdString
        val groupName = rawCatId?.let { categoryNameById[it] } ?: rawCatId
        // Category order comes from the provider's get_live_categories response.
        val groupOrder = rawCatId?.let { categoryOrderById[it] } ?: Int.MAX_VALUE
        return Channel(
            id = "${playlist.id}:${dto.streamId}",
            playlistId = playlist.id,
            name = dto.name,
            // Some providers return relative paths (`images/x.png`) for live
            // logos; normalise so Coil receives an absolute URL.
            logoUrl = normaliseImageUrl(dto.streamIcon, playlist.url),
            streamUrl = streamUrl,
            groupTitle = groupName,
            epgChannelId = dto.epgChannelId?.takeIf { it.isNotBlank() },
            isHd = dto.name.contains("HD", ignoreCase = true) ||
                dto.name.contains("1080", ignoreCase = true),
            sortOrder = sortOrder,
            groupSortOrder = groupOrder,
        )
    }

    // ── Categories ────────────────────────────────────────────────────────────

    fun toLiveCategoryGroup(dto: XtreamCategoryDto): String = dto.categoryName

    fun toVodCategory(
        dto: XtreamCategoryDto,
        playlistId: Long,
        kind: VodKind,
    ): VodCategory = VodCategory(
        id = "${playlistId}:${kind.name}:${dto.categoryIdString}",
        playlistId = playlistId,
        name = dto.categoryName,
        kind = kind,
    )

    // ── VOD items ─────────────────────────────────────────────────────────────

    /**
     * Normalise an image URL from Xtream. Some providers return relative paths
     * (e.g. `/images/logo.png` or `images/logo.png`). Prepend the server base
     * in those cases; leave absolute http(s) URLs alone.
     */
    internal fun normaliseImageUrl(raw: String?, serverBase: String): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
            return trimmed
        }
        val base = serverBase.trimEnd('/')
        val path = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return "$base$path"
    }

    fun toVodItem(
        dto: XtreamVodDto,
        playlist: Playlist,
    ): VodItem? {
        val username = playlist.username ?: return null
        val password = playlist.password ?: return null
        val ext = dto.containerExtension?.takeIf { it.isNotBlank() } ?: "mp4"
        val streamUrl = XtreamUrlBuilder.vodStream(
            playlist.url, username, password, dto.streamId, ext,
        )
        // Prefer stream_icon, fallback to cover. Normalise relative paths.
        val rawPoster = dto.streamIcon?.takeIf { it.isNotBlank() }
            ?: dto.cover?.takeIf { it.isNotBlank() }
        val posterUrl = normaliseImageUrl(rawPoster, playlist.url)
        val catId = dto.categoryIdString
        return VodItem(
            id = "${playlist.id}:movie:${dto.streamId}",
            playlistId = playlist.id,
            title = dto.name,
            posterUrl = posterUrl,
            streamUrl = streamUrl,
            kind = VodKind.MOVIE,
            rating = jsonElementToDoubleOrNull(dto.rating),
            plot = dto.plot,
            categoryId = catId?.let { "${playlist.id}:MOVIE:$it" },
        )
    }

    fun enrichVodItemWithInfo(
        base: VodItem,
        info: XtreamVodInfoResponse,
    ): VodItem {
        val i = info.info ?: return base
        val mData = info.movieData
        return base.copy(
            plot = i.plot ?: base.plot,
            year = i.releasedate?.take(4)?.toIntOrNull() ?: base.year,
            rating = jsonElementToDoubleOrNull(i.rating) ?: base.rating,
            posterUrl = i.movieImage?.takeIf { it.isNotBlank() } ?: base.posterUrl,
            backdropUrl = jsonElementToStringOrNull(i.backdropPath),
            genres = i.genre?.split(",", "/")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: base.genres,
            cast = i.cast?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: base.cast,
            director = i.director ?: base.director,
            durationSecs = i.durationSecs ?: base.durationSecs,
            streamUrl = if (mData?.containerExtension != null) base.streamUrl
            else base.streamUrl,
        )
    }

    // ── Series ────────────────────────────────────────────────────────────────

    fun toSeries(
        dto: XtreamSeriesDto,
        playlist: Playlist,
    ): Series {
        val catId = dto.categoryIdString
        return Series(
            id = "${playlist.id}:series:${dto.seriesId}",
            playlistId = playlist.id,
            title = dto.name,
            posterUrl = normaliseImageUrl(dto.cover, playlist.url),
            backdropUrl = normaliseImageUrl(jsonElementToStringOrNull(dto.backdropPath), playlist.url),
            plot = dto.plot,
            year = dto.releaseDate?.take(4)?.toIntOrNull(),
            rating = jsonElementToDoubleOrNull(dto.rating),
            genres = dto.genre?.split(",", "/")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: emptyList(),
            cast = dto.cast?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
                ?: emptyList(),
            categoryId = catId?.let { "${playlist.id}:SERIES:$it" },
        )
    }

    // ── Short EPG ─────────────────────────────────────────────────────────────

    fun toProgram(
        dto: XtreamEpgEntryDto,
        playlistId: Long,
        useAndroidBase64: Boolean = true,
    ): Program? {
        val start = dto.startTimestamp?.toLongOrNull()?.let { it * 1000L }
            ?: return null
        val stop = dto.stopTimestamp?.toLongOrNull()?.let { it * 1000L }
            ?: return null
        val channelEpg = dto.epgId ?: dto.channelId ?: return null
        val title = (if (useAndroidBase64) decodeBase64(dto.title) else decodeBase64Jvm(dto.title))
            ?: dto.title ?: ""
        val desc = (if (useAndroidBase64) decodeBase64(dto.description) else decodeBase64Jvm(dto.description))
            ?: dto.description
        return Program(
            channelEpgId = channelEpg,
            playlistId = playlistId,
            title = title,
            description = desc,
            startMillis = start,
            stopMillis = stop,
        )
    }
}
