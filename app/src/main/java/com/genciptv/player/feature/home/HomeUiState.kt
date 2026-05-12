package com.genciptv.player.feature.home

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem

data class HomeUiState(
    val userName: String = "",
    /** Latest 10 movies ordered by ID desc (newest added). */
    val latestMovies: List<VodItem> = emptyList(),
    /** Latest 10 series ordered by ID desc (newest added). */
    val latestSeries: List<Series> = emptyList(),
    /** Recently-watched live channels, in LRU order (most recent first). */
    val recentChannels: List<Channel> = emptyList(),
    val isLoading: Boolean = true,
    val selectedChipIndex: Int = 0,
) {
    companion object {
        val INITIAL = HomeUiState()
    }
}
