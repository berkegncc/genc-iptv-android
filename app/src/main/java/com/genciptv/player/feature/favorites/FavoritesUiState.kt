package com.genciptv.player.feature.favorites

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem

data class FavoritesUiState(
    val selectedTab: Int = 0,
    val favoriteChannels: List<Channel> = emptyList(),
    val favoriteMovies: List<VodItem> = emptyList(),
    val favoriteSeries: List<Series> = emptyList(),
    val isLoading: Boolean = true,
) {
    companion object {
        val INITIAL = FavoritesUiState()
    }
}
