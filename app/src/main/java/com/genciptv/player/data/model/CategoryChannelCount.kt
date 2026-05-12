package com.genciptv.player.data.model

/**
 * A live-channel category along with how many channels are currently mapped
 * to it. Used by the Channels screen's vertical category picker so each
 * card can display "Spor — 42 kanal".
 */
data class CategoryChannelCount(
    val name: String,
    val count: Int,
)
