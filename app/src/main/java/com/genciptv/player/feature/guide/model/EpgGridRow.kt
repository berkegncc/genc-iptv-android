package com.genciptv.player.feature.guide.model

import com.genciptv.player.data.model.Program

/**
 * One row in the EPG time-grid: a channel with its programs for the selected day.
 *
 * @property channelId   the Channel.id (used for navigation to player)
 * @property channelName display name
 * @property logoUrl     nullable logo URL for AsyncImage
 * @property programs    sorted ascending by startMillis
 */
data class EpgGridRow(
    val channelId: String,
    val channelName: String,
    val logoUrl: String?,
    val programs: List<Program>,
)
