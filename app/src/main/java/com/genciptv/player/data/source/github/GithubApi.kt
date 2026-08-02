package com.genciptv.player.data.source.github

import com.genciptv.player.data.source.github.dto.GithubReleaseDto
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * Read-only GitHub Releases access. Unauthenticated, so requests are subject
 * to the 60-per-hour-per-IP rate limit — [com.genciptv.player.data.repository.UpdateRepository]
 * treats a 403 like any other failure and stays silent.
 */
interface GithubApi {

    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28",
    )
    // Annotation arguments must be literals, so the repo path is spelled out
    // here rather than composed from the constants below.
    @GET("repos/berkegncc/genc-iptv-android/releases/latest")
    suspend fun getLatestRelease(): GithubReleaseDto

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}
