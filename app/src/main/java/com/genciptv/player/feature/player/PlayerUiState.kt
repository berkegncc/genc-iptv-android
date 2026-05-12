package com.genciptv.player.feature.player

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Program
import com.genciptv.player.feature.home.model.ChannelWithProgram

data class PlayerUiState(
    val channel: Channel? = null,
    val currentProgram: Program? = null,
    val upcomingOtherChannels: List<ChannelWithProgram> = emptyList(),
    val isFavorite: Boolean = false,
    val isPlaying: Boolean = false,
    val volume: Float = 1f,
    val error: String? = null,
) {
    companion object {
        val INITIAL = PlayerUiState()
    }
}
