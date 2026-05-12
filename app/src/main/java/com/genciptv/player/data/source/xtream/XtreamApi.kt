package com.genciptv.player.data.source.xtream

import com.genciptv.player.data.source.xtream.dto.XtreamAuthResponse
import com.genciptv.player.data.source.xtream.dto.XtreamCategoryDto
import com.genciptv.player.data.source.xtream.dto.XtreamLiveStreamDto
import com.genciptv.player.data.source.xtream.dto.XtreamSeriesDto
import com.genciptv.player.data.source.xtream.dto.XtreamSeriesInfoResponse
import com.genciptv.player.data.source.xtream.dto.XtreamShortEpgResponse
import com.genciptv.player.data.source.xtream.dto.XtreamVodDto
import com.genciptv.player.data.source.xtream.dto.XtreamVodInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Xtream Codes "player_api.php" endpoints.
 *
 * Each call takes the full URL via `@Url` because the base URL is per-playlist
 * (users may have multiple providers with different hosts). A single Retrofit
 * client is reused; callers construct the absolute URL via [XtreamUrlBuilder].
 */
interface XtreamApi {

    @GET
    suspend fun getUserInfo(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
    ): XtreamAuthResponse

    @GET
    suspend fun getLiveCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories",
    ): List<XtreamCategoryDto>

    @GET
    suspend fun getLiveStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamLiveStreamDto>

    @GET
    suspend fun getVodCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories",
    ): List<XtreamCategoryDto>

    @GET
    suspend fun getVodStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamVodDto>

    @GET
    suspend fun getVodInfo(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: String,
    ): XtreamVodInfoResponse

    @GET
    suspend fun getSeriesCategories(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories",
    ): List<XtreamCategoryDto>

    @GET
    suspend fun getSeries(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null,
    ): List<XtreamSeriesDto>

    @GET
    suspend fun getSeriesInfo(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: String,
    ): XtreamSeriesInfoResponse

    @GET
    suspend fun getShortEpg(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_short_epg",
        @Query("stream_id") streamId: String,
        @Query("limit") limit: Int = 10,
    ): XtreamShortEpgResponse
}

/**
 * Helper to build the absolute `player_api.php` URL from a [serverBase].
 * [serverBase] is the raw URL the user entered (may or may not end in `/`).
 */
object XtreamUrlBuilder {
    fun playerApi(serverBase: String): String {
        val trimmed = serverBase.trimEnd('/')
        return "$trimmed/player_api.php"
    }

    /** URL for a live stream: `{base}/live/{u}/{p}/{streamId}.m3u8` */
    fun liveStream(serverBase: String, username: String, password: String, streamId: Int): String {
        val trimmed = serverBase.trimEnd('/')
        return "$trimmed/live/$username/$password/$streamId.m3u8"
    }

    /** URL for a VOD stream with container extension. */
    fun vodStream(
        serverBase: String,
        username: String,
        password: String,
        streamId: Int,
        ext: String = "mp4",
    ): String {
        val trimmed = serverBase.trimEnd('/')
        return "$trimmed/movie/$username/$password/$streamId.$ext"
    }

    /** URL for a series episode. */
    fun seriesStream(
        serverBase: String,
        username: String,
        password: String,
        episodeId: String,
        ext: String = "mp4",
    ): String {
        val trimmed = serverBase.trimEnd('/')
        return "$trimmed/series/$username/$password/$episodeId.$ext"
    }
}
