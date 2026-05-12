package com.genciptv.player.data.source.tmdb

import com.genciptv.player.data.source.tmdb.dto.TmdbCreditsResponse
import com.genciptv.player.data.source.tmdb.dto.TmdbSearchResponse
import com.genciptv.player.data.source.tmdb.dto.TmdbTvSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * The Movie Database (TMDb) v3 endpoints we use. Auth is the v3 query-parameter
 * API key — passed by the repository, not hard-coded here.
 *
 * Image URLs returned by TMDb are paths only (`/abc.jpg`). Callers prepend
 * `https://image.tmdb.org/t/p/{size}` when consuming them.
 */
interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("year") year: Int? = null,
        @Query("language") language: String = "tr-TR",
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbSearchResponse

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "tr-TR",
    ): TmdbCreditsResponse

    @GET("search/tv")
    suspend fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("first_air_date_year") year: Int? = null,
        @Query("language") language: String = "tr-TR",
        @Query("include_adult") includeAdult: Boolean = false,
    ): TmdbTvSearchResponse

    @GET("tv/{id}/credits")
    suspend fun getTvCredits(
        @Path("id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "tr-TR",
    ): TmdbCreditsResponse
}
