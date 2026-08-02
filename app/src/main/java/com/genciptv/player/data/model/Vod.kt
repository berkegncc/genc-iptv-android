package com.genciptv.player.data.model

enum class VodKind { MOVIE, SERIES }

/**
 * A single movie OR a "series item" (series have child episodes).
 * For series items, [streamUrl] may be empty — child episodes hold the real urls.
 */
data class VodItem(
    val id: String,
    val playlistId: Long,
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String,
    val kind: VodKind,
    val year: Int? = null,
    val rating: Double? = null,
    val plot: String? = null,
    val genres: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val director: String? = null,
    val durationSecs: Int? = null,
    val categoryId: String? = null,
    /** Provider's "added" timestamp in epoch millis; 0 when unknown. */
    val addedAt: Long = 0L,
    /** Numeric Xtream stream id — the sort key behind "newest first". */
    val providerId: Long = 0L,
)

data class Series(
    val id: String,
    val playlistId: Long,
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val plot: String? = null,
    val year: Int? = null,
    val rating: Double? = null,
    val genres: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val categoryId: String? = null,
    /** Provider's "last_modified" timestamp in epoch millis; 0 when unknown. */
    val addedAt: Long = 0L,
    /** Numeric Xtream series id — the sort key behind "newest first". */
    val providerId: Long = 0L,
)

data class Episode(
    val id: String,
    val seriesId: String,
    val playlistId: Long,
    val season: Int,
    val episode: Int,
    val title: String,
    val streamUrl: String,
    val durationSecs: Int? = null,
    val plot: String? = null,
    val thumbnailUrl: String? = null,
)

data class VodCategory(
    val id: String,
    val playlistId: Long,
    val name: String,
    val kind: VodKind,
)
