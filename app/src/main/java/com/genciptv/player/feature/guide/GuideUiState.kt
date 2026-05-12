package com.genciptv.player.feature.guide

import com.genciptv.player.feature.guide.model.DayOption
import com.genciptv.player.feature.guide.model.EpgGridRow

data class GuideUiState(
    /** 7 selectable days starting from yesterday. */
    val days: List<DayOption> = emptyList(),
    /** Index into [days] for the currently viewed day. Default = 1 (today). */
    val selectedDayIndex: Int = 1,
    /** Midnight epoch millis (local) of the currently selected day. */
    val selectedDateMillis: Long = 0L,
    /** EPG rows — one per channel that has EPG data. Capped at 100 channels. */
    val rows: List<EpgGridRow> = emptyList(),
    /** The channel currently on air — used in the top "now playing" card. */
    val featuredNow: EpgGridRow? = null,
    /** True when at least one row carries program data. */
    val hasAnyEpgData: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    companion object {
        val INITIAL = GuideUiState(isLoading = true)
    }
}
