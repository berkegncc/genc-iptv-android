package com.genciptv.player.feature.guide.model

/**
 * One selectable day in the EPG's 7-day strip.
 *
 * Carries the facts, not the words. The label used to be built here as Turkish
 * text, which stops working once the app has a second language: a ViewModel
 * survives the activity recreation that a language change triggers, so a string
 * decided at load time would still read "Bugün" after a switch to English. The
 * screen resolves [dayOffset] and [dayOfWeek] against string resources instead,
 * and that re-runs on every composition.
 *
 * @property dateMillis  midnight (00:00:00) of this day in the device local timezone, as epoch ms
 * @property dayOffset   days from today: -1 yesterday, 0 today, 1 tomorrow, and so on
 * @property dayOfWeek   [java.util.Calendar.DAY_OF_WEEK] value for this day
 * @property dayNumber   day-of-month string, e.g. "15"
 */
data class DayOption(
    val dateMillis: Long,
    val dayOffset: Int,
    val dayOfWeek: Int,
    val dayNumber: String,
)
