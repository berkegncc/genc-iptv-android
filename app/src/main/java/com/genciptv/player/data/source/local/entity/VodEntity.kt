package com.genciptv.player.data.source.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vod_items",
    indices = [Index("playlistId"), Index("categoryId"), Index("kind")],
)
data class VodEntity(
    @PrimaryKey val id: String,
    val playlistId: Long,
    val title: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val streamUrl: String,
    /** "MOVIE" or "SERIES" */
    val kind: String,
    val year: Int? = null,
    val rating: Double? = null,
    val plot: String? = null,
    /** Stored as JSON via DbTypeConverters. */
    val genres: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val director: String? = null,
    val durationSecs: Int? = null,
    val categoryId: String? = null,
)

@Entity(
    tableName = "series",
    indices = [Index("playlistId"), Index("categoryId")],
)
data class SeriesEntity(
    @PrimaryKey val id: String,
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
)

@Entity(
    tableName = "episodes",
    indices = [Index("seriesId"), Index("playlistId")],
)
data class EpisodeEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "vod_categories",
    indices = [Index("playlistId"), Index("kind")],
)
data class VodCategoryEntity(
    @PrimaryKey val id: String,
    val playlistId: Long,
    val name: String,
    /** "MOVIE" or "SERIES" */
    val kind: String,
)
