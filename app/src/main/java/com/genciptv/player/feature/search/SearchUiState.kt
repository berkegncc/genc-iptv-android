package com.genciptv.player.feature.search

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem

data class SearchUiState(
    val query: String = "",
    val channels: List<Channel> = emptyList(),
    val movies: List<VodItem> = emptyList(),
    val series: List<Series> = emptyList(),
    val isSearching: Boolean = false,
) {
    val hasQuery: Boolean get() = query.length >= MIN_QUERY_LENGTH
    val isEmpty: Boolean get() = channels.isEmpty() && movies.isEmpty() && series.isEmpty()

    companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_RESULTS_PER_SECTION = 20
        val INITIAL = SearchUiState()
    }
}
