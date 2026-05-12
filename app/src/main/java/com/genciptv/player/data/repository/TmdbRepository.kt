package com.genciptv.player.data.repository

import android.util.Log
import com.genciptv.player.BuildConfig
import com.genciptv.player.data.model.CastMember
import com.genciptv.player.data.source.tmdb.TmdbApi
import javax.inject.Inject
import javax.inject.Singleton

interface TmdbRepository {
    /**
     * Look up cast for a movie by [title] (and optional [year]).
     * Returns an empty list if the API key is missing, the search yields no
     * match, or the call fails. Never throws.
     */
    suspend fun fetchMovieCast(title: String, year: Int? = null): List<CastMember>

    /**
     * Same contract as [fetchMovieCast] but hits TMDb's TV endpoints
     * (`/search/tv` + `/tv/{id}/credits`). [year] should be the first-air year.
     */
    suspend fun fetchSeriesCast(title: String, year: Int? = null): List<CastMember>

    /**
     * Look up a poster URL for a movie by [title] (and optional [year]).
     * Returns `null` if the API key is missing, no match is found, or the
     * call fails. Never throws. Used as a fallback when the upstream provider
     * (Xtream) didn't supply a `stream_icon` / `cover` for the item.
     */
    suspend fun fetchMoviePosterUrl(title: String, year: Int? = null): String?

    /** TV series equivalent of [fetchMoviePosterUrl]. */
    suspend fun fetchSeriesPosterUrl(title: String, year: Int? = null): String?
}

@Singleton
class TmdbRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : TmdbRepository {

    private val apiKey: String = BuildConfig.TMDB_API_KEY

    override suspend fun fetchMovieCast(title: String, year: Int?): List<CastMember> =
        fetchCastInternal(title, year, isSeries = false)

    override suspend fun fetchSeriesCast(title: String, year: Int?): List<CastMember> =
        fetchCastInternal(title, year, isSeries = true)

    override suspend fun fetchMoviePosterUrl(title: String, year: Int?): String? =
        fetchPosterInternal(title, year, isSeries = false)

    override suspend fun fetchSeriesPosterUrl(title: String, year: Int?): String? =
        fetchPosterInternal(title, year, isSeries = true)

    /**
     * Lightweight search-only lookup — we only need `poster_path`, not the
     * full credits trip. Falls back to the year-less search if the year-bound
     * one returns nothing (Xtream titles are noisy and the year is often off
     * by a year or stripped from the canonical TMDb release date).
     */
    private suspend fun fetchPosterInternal(
        title: String,
        year: Int?,
        isSeries: Boolean,
    ): String? {
        if (apiKey.isBlank()) return null
        if (title.isBlank()) return null

        return runCatching {
            val cleaned = cleanMovieTitle(title)
            val posterPath: String? = if (isSeries) {
                val initial = api.searchTv(apiKey, cleaned, year).results
                val results = initial.ifEmpty {
                    if (year != null) api.searchTv(apiKey, cleaned, null).results else emptyList()
                }
                results.firstOrNull { !it.posterPath.isNullOrBlank() }?.posterPath
            } else {
                val initial = api.searchMovie(apiKey, cleaned, year).results
                val results = initial.ifEmpty {
                    if (year != null) api.searchMovie(apiKey, cleaned, null).results else emptyList()
                }
                results.firstOrNull { !it.posterPath.isNullOrBlank() }?.posterPath
            }
            posterPath?.let { POSTER_FALLBACK_BASE + it }
        }.getOrElse { e ->
            Log.w(TAG, "TMDb poster fetch failed for \"$title\" ($year, series=$isSeries)", e)
            null
        }
    }

    /**
     * Shared implementation for movie + TV cast lookups. The two endpoint
     * families have identical credit shapes — only the search step differs,
     * so the rest of the pipeline (clean title, retry-without-year, sort by
     * billing order, map to domain) is the same.
     */
    private suspend fun fetchCastInternal(
        title: String,
        year: Int?,
        isSeries: Boolean,
    ): List<CastMember> {
        if (apiKey.isBlank()) {
            Log.w(TAG, "TMDB_API_KEY is empty — skipping cast fetch")
            return emptyList()
        }
        if (title.isBlank()) return emptyList()

        return runCatching {
            val cleaned = cleanMovieTitle(title)

            val matchId: Int = if (isSeries) {
                val initial = api.searchTv(apiKey, cleaned, year).results
                val results = initial.ifEmpty {
                    if (year != null) api.searchTv(apiKey, cleaned, null).results else emptyList()
                }
                results.firstOrNull()?.id
            } else {
                val initial = api.searchMovie(apiKey, cleaned, year).results
                val results = initial.ifEmpty {
                    if (year != null) api.searchMovie(apiKey, cleaned, null).results else emptyList()
                }
                results.firstOrNull()?.id
            } ?: return@runCatching emptyList()

            val credits = if (isSeries) api.getTvCredits(matchId, apiKey)
                          else api.getMovieCredits(matchId, apiKey)

            credits.cast
                .sortedBy { it.order }
                .take(MAX_CAST)
                .map { dto ->
                    CastMember(
                        name = dto.name,
                        character = dto.character?.takeIf { it.isNotBlank() },
                        profileUrl = dto.profilePath?.let { POSTER_BASE + it },
                    )
                }
        }.getOrElse { e ->
            Log.w(TAG, "TMDb cast fetch failed for \"$title\" ($year, series=$isSeries)", e)
            emptyList()
        }
    }

    /**
     * Strip noisy tokens Xtream titles often carry — quality, codec, language
     * markers, parenthesised year — so the TMDb search has a better shot at
     * matching the canonical title.
     */
    private fun cleanMovieTitle(title: String): String =
        title
            .replace(Regex("""\(\s*\d{4}\s*\)"""), "")
            .replace(
                Regex(
                    """\b(1080p|720p|480p|2160p|4k|hdr10\+?|hdr|bluray|blu-ray|web-?dl|web-?rip|hdtv|dvdrip|brrip|x264|x265|h\.?264|h\.?265|hevc|aac|ac3|dts|dolby|atmos)\b""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(
                Regex(
                    """\b(tr|en|tur|tür(?:k(?:çe|ce))?|altyazı|altyazili|alt(?:yazı)?l[ıi]|dublaj|dubbed|subbed|sub|multi)\b""",
                    RegexOption.IGNORE_CASE,
                ),
                "",
            )
            .replace(Regex("""[\[\]\{\}]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

    companion object {
        private const val TAG = "GencIPTV/Tmdb"
        private const val POSTER_BASE = "https://image.tmdb.org/t/p/w185"

        /**
         * Larger size for poster fallbacks displayed in VOD/series lists and
         * detail screens (`w185` is fine for cast headshots but looks soft on
         * a 120dp poster card scaled by Coil).
         */
        private const val POSTER_FALLBACK_BASE = "https://image.tmdb.org/t/p/w342"
        private const val MAX_CAST = 15
    }
}
