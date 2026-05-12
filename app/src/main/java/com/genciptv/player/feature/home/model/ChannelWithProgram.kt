package com.genciptv.player.feature.home.model

import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Program

data class ChannelWithProgram(
    val channel: Channel,
    val currentProgram: Program?,
    val progressFraction: Float,
)
