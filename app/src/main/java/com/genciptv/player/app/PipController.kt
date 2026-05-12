package com.genciptv.player.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Lightweight singleton that bridges the Compose player screens and the
 * Activity lifecycle callbacks for Picture-in-Picture (PiP) mode.
 *
 * Usage:
 *  - PlayerScreen / VodPlayerScreen set [shouldEnterPip] = true while playing.
 *  - MainActivity reads [shouldEnterPip] in [onUserLeaveHint] to decide
 *    whether to enter PiP.
 *  - MainActivity updates [isInPipMode] from [onPictureInPictureModeChanged];
 *    player composables observe this flow to hide overlays in PiP mode.
 */
object PipController {

    /** True while a player screen is actively showing and playback is running. */
    var shouldEnterPip: Boolean = false

    /** True while the activity is in the PiP floating window. */
    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode

    fun onPipModeChanged(isInPip: Boolean) {
        _isInPipMode.value = isInPip
    }
}
