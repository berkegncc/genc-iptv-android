package com.genciptv.player.feature.vod

import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodCategory
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind

data class VodListUiState(
    val kind: VodKind = VodKind.MOVIE,
    val movies: List<VodItem> = emptyList(),
    val series: List<Series> = emptyList(),
    val categories: List<VodCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    /** In-progress movies (ContinueWatching entries for MOVIE type). */
    val inProgressMovies: List<ContinueWatching> = emptyList(),
    /** In-progress series/episodes (ContinueWatching entries for SERIES type). */
    val inProgressSeries: List<ContinueWatching> = emptyList(),
    /** Long-press multi-select state for "Devam Et" rows. Empty set ⇒ not in selection mode. */
    val selectedCwIds: Set<Pair<String, FavoriteTargetType>> = emptySet(),
) {
    val isCwSelectionMode: Boolean get() = selectedCwIds.isNotEmpty()

    companion object {
        val INITIAL = VodListUiState()
    }
}
