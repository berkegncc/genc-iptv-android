package com.genciptv.player.data.model

import java.io.File

/**
 * A GitHub release that is newer than the running build, reduced to what the
 * update dialog needs.
 */
data class UpdateInfo(
    /** Release tag as published, e.g. `v1.2.0`. */
    val tagName: String,
    /** Tag without the `v` prefix — what we show and store as "dismissed". */
    val versionName: String,
    /** Release title; falls back to the tag when GitHub returns none. */
    val title: String,
    /** Release body (markdown), lightly cleaned before display. */
    val changelog: String,
    /** Browser download URL of the first `.apk` asset. */
    val downloadUrl: String,
    /** Asset size in bytes; 0 when the API omits it. */
    val sizeBytes: Long,
) {
    val sizeMb: Double get() = sizeBytes / 1_048_576.0
}

/** Progress of an APK download. */
sealed interface DownloadState {
    data class Progress(
        val percent: Int,
        val bytesRead: Long,
        val total: Long,
    ) : DownloadState

    data class Done(val file: File) : DownloadState

    data class Failed(val message: String) : DownloadState
}
