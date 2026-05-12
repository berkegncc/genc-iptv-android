package com.genciptv.player.data.source.tmdb.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * TMDb v3 API DTOs. Only the fields we actually consume are declared; unknown
 * keys are ignored by the shared [kotlinx.serialization.json.Json] config.
 */

@Serializable
data class TmdbSearchResponse(
    val page: Int = 1,
    val results: List<TmdbMovieResultDto> = emptyList(),
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class TmdbMovieResultDto(
    val id: Int,
    val title: String? = null,
    @SerialName("original_title") val originalTitle: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    val popularity: Double? = null,
)

@Serializable
data class TmdbTvSearchResponse(
    val page: Int = 1,
    val results: List<TmdbTvResultDto> = emptyList(),
    @SerialName("total_results") val totalResults: Int = 0,
)

@Serializable
data class TmdbTvResultDto(
    val id: Int,
    val name: String? = null,
    @SerialName("original_name") val originalName: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    val popularity: Double? = null,
)

/** Same shape on `/movie/{id}/credits` and `/tv/{id}/credits` — reused for both. */
@Serializable
data class TmdbCreditsResponse(
    val id: Int,
    val cast: List<TmdbCastDto> = emptyList(),
)

@Serializable
data class TmdbCastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    /** TMDb returns a billing order; lower = more prominent. */
    val order: Int = 0,
)
