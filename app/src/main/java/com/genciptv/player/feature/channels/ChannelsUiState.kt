package com.genciptv.player.feature.channels

import com.genciptv.player.data.model.CategoryChannelCount
import com.genciptv.player.feature.home.model.ChannelWithProgram

data class ChannelsUiState(
    val channels: List<ChannelWithProgram> = emptyList(),
    /** Categories with channel counts — used by the vertical category picker. */
    val categories: List<CategoryChannelCount> = emptyList(),
    val selectedCategory: String? = null,      // null while in category picker, or "Tümü" view
    val query: String = "",
    val featuredNow: ChannelWithProgram? = null,
    val favoriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /**
     * false = vertical category picker view (initial / after back).
     * true  = channel list view (entered from a category card).
     *         When [selectedCategory] is null in this mode, all channels show.
     */
    val inCategoryView: Boolean = false,
) {
    /** Total channels across all categories — shown on the "Tümü" card. */
    val totalChannelCount: Int get() = categories.sumOf { it.count }

    companion object {
        val INITIAL = ChannelsUiState()
    }
}
