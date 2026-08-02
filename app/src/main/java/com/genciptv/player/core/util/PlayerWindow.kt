package com.genciptv.player.core.util

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Hands the screen back to the user when playback ends.
 *
 * Call this whenever a player screen is left — including via the system back
 * gesture, which never runs the in-app "exit fullscreen" path. Without it the
 * activity stays pinned to the landscape that fullscreen requested and the
 * whole app is stuck sideways.
 *
 * [ActivityInfo.SCREEN_ORIENTATION_USER] rather than `UNSPECIFIED`: `USER`
 * consults the device's auto-rotate setting, so a phone held upright snaps back
 * to portrait while someone who locked rotation keeps their locked orientation.
 * `UNSPECIFIED` only says "no preference", which on some devices resolves to
 * "keep whatever it is" — the landscape that never goes away.
 */
fun Activity.restoreUserOrientation() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
    WindowCompat.setDecorFitsSystemWindows(window, true)
    WindowInsetsControllerCompat(window, window.decorView)
        .show(WindowInsetsCompat.Type.systemBars())
}
