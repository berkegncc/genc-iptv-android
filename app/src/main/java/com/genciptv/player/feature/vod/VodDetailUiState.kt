package com.genciptv.player.feature.vod

import com.genciptv.player.data.model.CastMember
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem

data class VodDetailUiState(
    val movie: VodItem? = null,
    val series: Series? = null,
    val episodes: List<Episode> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val isEpisodesLoading: Boolean = false,
    val error: String? = null,
    /** Cast enriched with profile photos (TMDb). Empty until TMDb resolves; UI falls back to [cast]. */
    val castWithPhotos: List<CastMember> = emptyList(),
    val isCastLoading: Boolean = false,
    /** Other movies in the same Xtream category — used by the "Benzer Filmler" bar. */
    val similarMovies: List<VodItem> = emptyList(),
    /** Other series in the same Xtream category — used by the "Benzer Diziler" bar. */
    val similarSeries: List<Series> = emptyList(),
) {
    val isSeries: Boolean get() = series != null
    val title: String get() = movie?.title ?: series?.title ?: ""
    val posterUrl: String? get() = movie?.posterUrl ?: series?.posterUrl
    val backdropUrl: String? get() = movie?.backdropUrl ?: series?.backdropUrl
    val plot: String? get() = movie?.plot ?: series?.plot
    val year: Int? get() = movie?.year ?: series?.year
    val rating: Double? get() = movie?.rating ?: series?.rating
    val genres: List<String> get() = movie?.genres ?: series?.genres ?: emptyList()
    val cast: List<String> get() = movie?.cast ?: series?.cast ?: emptyList()

    /**
     * Cast list to render in the UI — TMDb-enriched when available, otherwise
     * the raw Xtream string list mapped to name-only [CastMember]s. Always
     * non-null; may be empty if neither source has data.
     */
    val displayCast: List<CastMember>
        get() = castWithPhotos.ifEmpty {
            cast.map { CastMember(name = it) }
        }

    companion object {
        val INITIAL = VodDetailUiState()
    }
}
