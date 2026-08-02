package com.genciptv.player.core.util

/**
 * Semver comparison for release tags versus [com.genciptv.player.BuildConfig.VERSION_NAME].
 *
 * Tolerates the two shapes this project has shipped: a bare `v` prefix and a
 * differing number of parts. `1.1` and `v1.1.0` are the same release — builds
 * before v1.2.0 carry a two-part versionName, so missing parts count as zero.
 *
 * Pre-release suffixes (`-beta`, `+build`) are stripped and ignored, so
 * `2.0.0-beta` ranks as `2.0.0`. That is deliberate: the repository already
 * skips releases flagged `prerelease`, and a tag that reaches here with a
 * suffix should still be treated as its numeric version.
 */
object VersionComparator {

    /** True when [remote] is a strictly newer version than [current]. */
    fun isNewer(current: String, remote: String): Boolean =
        compare(normalize(remote), normalize(current)) > 0

    /** Strips a leading `v`, then any `-`/`+` suffix, leaving `1.2.3`. */
    fun normalize(raw: String): String =
        raw.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore('-')
            .substringBefore('+')
            .trim()

    /** Part-by-part numeric compare; missing parts read as 0. Non-numeric parts read as 0. */
    private fun compare(a: String, b: String): Int {
        val left = a.split('.')
        val right = b.split('.')
        for (i in 0 until maxOf(left.size, right.size)) {
            val l = left.getOrNull(i)?.trim()?.toIntOrNull() ?: 0
            val r = right.getOrNull(i)?.trim()?.toIntOrNull() ?: 0
            if (l != r) return l.compareTo(r)
        }
        return 0
    }
}
