package com.genciptv.player.feature.update

import com.genciptv.player.data.model.UpdateInfo
import java.io.File

/**
 * States the update dialog can be in. [Idle] and [UpToDate] render nothing and
 * a short confirmation respectively; everything else shows the dialog.
 */
sealed interface UpdateUiState {

    /** Nothing to show — also the state after a silent check finds nothing. */
    data object Idle : UpdateUiState

    /** A newer release exists and the user has not been asked yet. */
    data class Available(val info: UpdateInfo) : UpdateUiState

    data class Downloading(
        val info: UpdateInfo,
        val percent: Int,
        val bytesRead: Long = 0L,
        val total: Long = 0L,
    ) : UpdateUiState

    /** APK is on disk; the dialog fires the installer as soon as it sees this. */
    data class ReadyToInstall(val info: UpdateInfo, val file: File) : UpdateUiState

    /** Downloaded, but "install unknown apps" is not granted yet. */
    data class NeedsPermission(val info: UpdateInfo, val file: File) : UpdateUiState

    /** Only reached from a manual check — a silent one stays [Idle]. */
    data class UpToDate(val version: String) : UpdateUiState

    /**
     * [info] is carried so "Tekrar dene" can resume the same download instead
     * of starting the check over.
     */
    data class Error(val message: String, val info: UpdateInfo? = null) : UpdateUiState
}
