package com.genciptv.player.data.source.github.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Subset of `GET /repos/{owner}/{repo}/releases/latest`.
 * The shared [kotlinx.serialization.json.Json] provider has
 * `ignoreUnknownKeys = true`, so the many fields we skip are harmless.
 */
@Serializable
data class GithubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean = false,
    val assets: List<GithubAssetDto> = emptyList(),
)

@Serializable
data class GithubAssetDto(
    val name: String,
    val size: Long = 0L,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
)
