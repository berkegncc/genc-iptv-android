package com.genciptv.player.core.util

/**
 * Ranks catalogue entries by how many genres they share with the current one.
 *
 * Replaces a category filter that only looked like similarity: Xtream groups by
 * *source* — "NETFLIX DİZİLERİ", "EXXEN", "YERLİ DİZİLER" — not by content, so
 * same-category meant little more than "also on Netflix", and taking the first
 * 15 (alphabetical inside a category) made the row effectively arbitrary. Genre
 * is the only similarity signal the payload actually carries.
 *
 * Ties break on rating, so equally-related titles lead with the better ones.
 *
 * Returns empty when the item has no genres or nothing overlaps — callers hide
 * the row on an empty list, so it disappears rather than showing filler.
 *
 * Runs over the whole catalogue, so call it off the main thread.
 */
fun <T> rankBySharedGenres(
    genres: List<String>,
    candidates: List<T>,
    genresOf: (T) -> List<String>,
    ratingOf: (T) -> Double?,
    limit: Int = 15,
): List<T> {
    val target = genres.mapNotNull { it.trim().lowercase().takeIf(String::isNotEmpty) }.toSet()
    if (target.isEmpty()) return emptyList()

    return candidates
        .map { item -> item to genresOf(item).count { it.trim().lowercase() in target } }
        .filter { (_, shared) -> shared > 0 }
        .sortedWith(
            compareByDescending<Pair<T, Int>> { it.second }
                .thenByDescending { ratingOf(it.first) ?: 0.0 }
        )
        .take(limit)
        .map { it.first }
}
