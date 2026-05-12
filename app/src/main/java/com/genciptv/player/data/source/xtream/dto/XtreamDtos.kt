package com.genciptv.player.data.source.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Response from `player_api.php?username=X&password=Y` (no action).
 * Contains [userInfo] and [serverInfo] blocks.
 */
@Serializable
data class XtreamAuthResponse(
    @SerialName("user_info") val userInfo: XtreamUserInfoDto? = null,
    @SerialName("server_info") val serverInfo: XtreamServerInfoDto? = null,
)

@Serializable
data class XtreamUserInfoDto(
    val username: String? = null,
    val password: String? = null,
    /** "Active", "Expired", "Disabled", ... */
    val status: String? = null,
    /** Unix seconds as string or null. */
    @SerialName("exp_date") val expDate: String? = null,
    /** "1" or "0". */
    @SerialName("is_trial") val isTrial: String? = null,
    /** String in some servers. */
    @SerialName("max_connections") val maxConnections: String? = null,
    val auth: Int? = null,
    val message: String? = null,
)

@Serializable
data class XtreamServerInfoDto(
    val url: String? = null,
    val port: String? = null,
    @SerialName("https_port") val httpsPort: String? = null,
    @SerialName("server_protocol") val serverProtocol: String? = null,
    val timezone: String? = null,
    @SerialName("time_now") val timeNow: String? = null,
)

// ── Categories ────────────────────────────────────────────────────────────────

@Serializable
data class XtreamCategoryDto(
    @SerialName("category_id") val categoryId: JsonElement,
    @SerialName("category_name") val categoryName: String,
    @SerialName("parent_id") val parentId: JsonElement? = null,
) {
    /** Category id as string regardless of JSON type (string or number). */
    val categoryIdString: String
        get() = (categoryId as? kotlinx.serialization.json.JsonPrimitive)?.content
            ?: categoryId.toString()
}

// ── Live Streams ──────────────────────────────────────────────────────────────

@Serializable
data class XtreamLiveStreamDto(
    val num: JsonElement? = null,
    val name: String,
    @SerialName("stream_type") val streamType: String? = null,
    @SerialName("stream_id") val streamId: Int,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("epg_channel_id") val epgChannelId: String? = null,
    val added: String? = null,
    /** JsonElement because some Xtream servers return number, others string. */
    @SerialName("category_id") val categoryId: JsonElement? = null,
    @SerialName("custom_sid") val customSid: String? = null,
    @SerialName("tv_archive") val tvArchive: Int? = null,
    @SerialName("direct_source") val directSource: String? = null,
    @SerialName("tv_archive_duration") val tvArchiveDuration: JsonElement? = null,
) {
    /** Normalised category id as string, or null. */
    val categoryIdString: String?
        get() = (categoryId as? kotlinx.serialization.json.JsonPrimitive)?.content
}

// ── VOD ───────────────────────────────────────────────────────────────────────

@Serializable
data class XtreamVodDto(
    val num: JsonElement? = null,
    val name: String,
    @SerialName("stream_type") val streamType: String? = null,
    @SerialName("stream_id") val streamId: Int,
    @SerialName("stream_icon") val streamIcon: String? = null,
    /** Some providers use `cover` instead of or alongside `stream_icon`. */
    val cover: String? = null,
    val rating: JsonElement? = null,
    @SerialName("rating_5based") val rating5: JsonElement? = null,
    val added: String? = null,
    /** JsonElement because some Xtream servers return number, others string. */
    @SerialName("category_id") val categoryId: JsonElement? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("custom_sid") val customSid: String? = null,
    @SerialName("direct_source") val directSource: String? = null,
    val plot: String? = null,
) {
    val categoryIdString: String?
        get() = (categoryId as? kotlinx.serialization.json.JsonPrimitive)?.content
}

@Serializable
data class XtreamVodInfoResponse(
    val info: XtreamVodInfoDto? = null,
    @SerialName("movie_data") val movieData: XtreamMovieDataDto? = null,
)

@Serializable
data class XtreamVodInfoDto(
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    val duration: String? = null,
    @SerialName("duration_secs") val durationSecs: Int? = null,
    val rating: JsonElement? = null,
    val releasedate: String? = null,
    @SerialName("movie_image") val movieImage: String? = null,
    @SerialName("backdrop_path") val backdropPath: JsonElement? = null,
    val youtube_trailer: String? = null,
)

@Serializable
data class XtreamMovieDataDto(
    @SerialName("stream_id") val streamId: Int? = null,
    val name: String? = null,
    val added: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
)

// ── Series ────────────────────────────────────────────────────────────────────

@Serializable
data class XtreamSeriesDto(
    val num: JsonElement? = null,
    val name: String,
    @SerialName("series_id") val seriesId: Int,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    val rating: JsonElement? = null,
    @SerialName("rating_5based") val rating5: JsonElement? = null,
    @SerialName("backdrop_path") val backdropPath: JsonElement? = null,
    @SerialName("episode_run_time") val episodeRunTime: String? = null,
    @SerialName("category_id") val categoryId: JsonElement? = null,
) {
    val categoryIdString: String?
        get() = (categoryId as? kotlinx.serialization.json.JsonPrimitive)?.content
}

@Serializable
data class XtreamSeriesInfoResponse(
    val info: XtreamSeriesInfoDto? = null,
    val episodes: JsonElement? = null,
)

@Serializable
data class XtreamSeriesInfoDto(
    val name: String? = null,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    val releaseDate: String? = null,
    val rating: JsonElement? = null,
)

// ── EPG ───────────────────────────────────────────────────────────────────────

@Serializable
data class XtreamShortEpgResponse(
    @SerialName("epg_listings") val listings: List<XtreamEpgEntryDto> = emptyList(),
)

@Serializable
data class XtreamEpgEntryDto(
    val id: String? = null,
    @SerialName("epg_id") val epgId: String? = null,
    /** Base64-encoded UTF-8 string. Decode before use. */
    val title: String? = null,
    val lang: String? = null,
    val start: String? = null,
    val end: String? = null,
    /** Base64-encoded UTF-8 string. Decode before use. */
    val description: String? = null,
    @SerialName("channel_id") val channelId: String? = null,
    @SerialName("start_timestamp") val startTimestamp: String? = null,
    @SerialName("stop_timestamp") val stopTimestamp: String? = null,
)
