package com.genciptv.player.data.model

/**
 * A single actor entry shown in the VOD detail "Oyuncular" bar.
 *
 * [profileUrl] is null when we only have a name (Xtream string list, or TMDb
 * returned no profile photo). The UI falls back to an initials avatar in that
 * case. [character] is also nullable — Xtream never provides it; TMDb usually
 * does.
 */
data class CastMember(
    val name: String,
    val character: String? = null,
    val profileUrl: String? = null,
)
