package com.genciptv.player.core.util

/**
 * Providers spell episode titles out in full — "Sugar - S01E01 - Olivia" —
 * repeating the series name and the season/episode code that the list already
 * shows. This reduces that to the episode's own name.
 *
 * The name only; the screen assembles the label. Turkish puts the number first
 * ("1. Olivia") and English puts a word there ("Episode 1: Olivia"), which is
 * not a difference string concatenation can express — so the number never gets
 * glued on here. Callers use `R.string.term_episode_titled` when there is a
 * name and `R.string.term_episode_number` when there is not.
 */

/** Matches S01E01, s1e1, S01 E01 … */
private val SEASON_EPISODE = Regex("""[sS]\s*\d{1,3}\s*[eE]\s*\d{1,4}""")

/** Separators providers use to join the parts of a title. */
private val LEADING_SEPARATORS = charArrayOf('-', '–', '—', ':', '.', '·', '|', ' ')

/** A number opening the title, with whatever separator follows it: "4.", "04 -" … */
private val LEADING_NUMBER = Regex("""^(\d{1,4})\s*[-–—:.·|]?\s*""")

/**
 * The episode's own name, or null when the raw title carried nothing beyond its
 * number — which is what a provider sending "Kasaba - S01E04 - 4. Bölüm"
 * amounts to once the boilerplate is stripped.
 *
 * [number] is needed to recognise that trailing "4." as a repeat rather than
 * part of a name: episode 3 of a series whose third episode is called
 * "12 Angry Men" keeps its 12.
 */
fun episodeName(number: Int, rawTitle: String): String? {
    val name = strippedName(rawTitle).withoutRepeatedNumber(number)
    // "Bölüm" / "Episode" on its own is the provider restating the number in
    // words. Nothing is lost by dropping it, and keeping it would render as
    // "Episode 4: Bölüm".
    if (name.isBlank() || name.equals("bölüm", ignoreCase = true) ||
        name.equals("episode", ignoreCase = true)
    ) {
        return null
    }
    return name
}

/**
 * Drops a leading number that only repeats [number]. Only an exact match is
 * removed, so a title that genuinely opens with a different number survives.
 */
private fun String.withoutRepeatedNumber(number: Int): String {
    val match = LEADING_NUMBER.find(this) ?: return this
    if (match.groupValues[1].toIntOrNull() != number) return this
    return substring(match.range.last + 1).trim()
}

/**
 * The title with the series prefix and season/episode code stripped. Anything
 * that doesn't match the usual shape is left alone rather than mangled — a
 * title we don't recognise is still better than an empty one.
 */
internal fun strippedName(rawTitle: String): String {
    var text = rawTitle.trim()

    // Everything up to and including the SxxExx marker is boilerplate.
    SEASON_EPISODE.find(text)?.let { match ->
        text = text.substring(match.range.last + 1)
    }

    return text.trim().trimStart(*LEADING_SEPARATORS).trim()
}
