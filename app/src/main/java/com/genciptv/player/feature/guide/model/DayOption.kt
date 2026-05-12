package com.genciptv.player.feature.guide.model

/**
 * Represents a single selectable day in the EPG 7-day strip.
 *
 * @property dateMillis  midnight (00:00:00) of this day in the device local timezone, as epoch ms
 * @property dayLabel    Turkish label: "Dün", "Bugün", "Yarın", or short weekday "Pzt", "Sal", …
 * @property dayNumber   two-digit day-of-month string, e.g. "15"
 */
data class DayOption(
    val dateMillis: Long,
    val dayLabel: String,
    val dayNumber: String,
)
