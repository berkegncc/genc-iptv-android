package com.genciptv.player.core.util

/**
 * Providers spell episode titles out in full — "Sugar - S01E01 - Olivia" —
 * repeating the series name and the season/episode code that the list already
 * shows. Reduced to "1. Olivia".
 */

/** Matches S01E01, s1e1, S01 E01 … */
private val SEASON_EPISODE = Regex("""[sS]\s*\d{1,3}\s*[eE]\s*\d{1,4}""")

/** Separators providers use to join the parts of a title. */
private val LEADING_SEPARATORS = charArrayOf('-', '–', '—', ':', '.', '·', '|', ' ')

/** A number opening the title, with whatever separator follows it: "4.", "04 -" … */
private val LEADING_NUMBER = Regex("""^(\d{1,4})\s*[-–—:.·|]?\s*""")

/**
 * "{number}. {name}" for the episode lists.
 *
 * Falls back to "{number}. Bölüm" when nothing survives the trim, which happens
 * with titles that are nothing but the episode code.
 */
fun episodeDisplayTitle(number: Int, rawTitle: String): String {
    val name = episodeName(rawTitle).withoutRepeatedNumber(number)
    return if (name.isBlank()) "$number. Bölüm" else "$number. $name"
}

/**
 * Drops a leading number that only repeats [number].
 *
 * "Kasaba - S01E04 - 4. Bölüm" reduces to "4. Bölüm", and prefixing that gives
 * "4. 4. Bölüm". Only an exact match is removed: episode 3 of a series whose
 * third episode is called "12 Angry Men" still wants its number in front.
 */
private fun String.withoutRepeatedNumber(number: Int): String {
    val match = LEADING_NUMBER.find(this) ?: return this
    if (match.groupValues[1].toIntOrNull() != number) return this
    return substring(match.range.last + 1).trim()
}

/**
 * The episode's own name, with the series prefix and season/episode code
 * stripped. Anything that doesn't match the usual shape is left alone rather
 * than mangled — a title we don't recognise is still better than an empty one.
 */
internal fun episodeName(rawTitle: String): String {
    var text = rawTitle.trim()

    // Everything up to and including the SxxExx marker is boilerplate.
    SEASON_EPISODE.find(text)?.let { match ->
        text = text.substring(match.range.last + 1)
    }

    return text.trim().trimStart(*LEADING_SEPARATORS).trim()
}
