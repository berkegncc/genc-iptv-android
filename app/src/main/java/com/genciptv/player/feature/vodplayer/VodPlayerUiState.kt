package com.genciptv.player.feature.vodplayer

import com.genciptv.player.data.model.CastMember
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem

data class VodPlayerUiState(
    val title: String = "",
    val subtitle: String? = null,
    val streamUrl: String = "",
    val initialPositionMs: Long = 0L,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    /** targetId used for ContinueWatching upsert */
    val targetId: String = "",
    val isMovie: Boolean = true,
    /** Poster URL stored on the ContinueWatching row so the "Devam Et" card can render a banner. */
    val posterUrl: String? = null,

    // ── Movie panel context ─────────────────────────────────────────────────
    /** Full movie metadata — used by the bottom panel for plot / genres / rating. */
    val movie: VodItem? = null,
    /** TMDb cast (with profile photos). Empty until the network call resolves. */
    val castWithPhotos: List<CastMember> = emptyList(),
    val isCastLoading: Boolean = false,
    /** Other movies in the same Xtream category — for the "Benzer Filmler" rail. */
    val similarMovies: List<VodItem> = emptyList(),

    // ── Series episode panel context ────────────────────────────────────────
    /** Series metadata for the episode currently playing. */
    val series: Series? = null,
    /** The episode currently playing (matches [targetId] when [isMovie] is false). */
    val episode: Episode? = null,
    /** All episodes of [series], used to build the season list and the row below. */
    val seriesEpisodes: List<Episode> = emptyList(),
    /** Which season the user is currently browsing in the panel. Defaults to the
     *  current episode's season; updated by the season-picker sheet. */
    val selectedSeason: Int? = null,
) {
    /** Distinct season numbers in [seriesEpisodes], ascending. */
    val availableSeasons: List<Int>
        get() = seriesEpisodes.map { it.season }.distinct().sorted()

    /** Episodes belonging to [selectedSeason], sorted by episode number. */
    val episodesInSelectedSeason: List<Episode>
        get() = if (selectedSeason != null) {
            seriesEpisodes.filter { it.season == selectedSeason }.sortedBy { it.episode }
        } else emptyList()

    /**
     * The episode that follows [episode] in chronological order (next episode
     * in the same season, then the first episode of the next season). `null`
     * when this is a movie, when the episode hasn't loaded yet, or when the
     * current episode is the series finale. Drives the "next episode" button
     * in the player overlay.
     */
    val nextEpisode: Episode?
        get() {
            val current = episode ?: return null
            val sorted = seriesEpisodes.sortedWith(
                compareBy({ it.season }, { it.episode }),
            )
            val idx = sorted.indexOfFirst { it.id == current.id }
            return if (idx in 0 until sorted.lastIndex) sorted[idx + 1] else null
        }

    companion object {
        val INITIAL = VodPlayerUiState()
    }
}
