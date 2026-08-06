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
    /**
     * The catalogue is out of date and nothing is going to fix it on its own:
     * the user asked to sync over Wi-Fi only and this connection is metered, so
     * both the background job and the gate on app open stood down. Without
     * saying so, the app just looks like it stopped updating.
     */
    val refreshHeldForWifi: Boolean = false,
) {
    companion object {
        val INITIAL = HomeUiState()
    }
}
