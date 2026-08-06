package com.genciptv.player.core.util

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService

/**
 * Whether the connection the app would use right now costs the user money.
 *
 * This exists for the one decision WorkManager cannot make for us. Background
 * sync states its network requirement up front as a constraint
 * (`NetworkType.UNMETERED`) and the system holds the job until it is satisfied.
 * The auto-sync gate on app open has no such luxury: it runs in the foreground,
 * the moment the user opens the app, so it has to ask.
 *
 * "Metered" is the user's own answer, not a guess from the transport type — a
 * hotspot or a capped home line can be marked metered in Android's settings and
 * is then reported as such here, while an unlimited mobile plan the user has
 * flagged unmetered is not. Reading the flag rather than testing for Wi-Fi is
 * what makes "Yalnızca Wi-Fi" mean what the user expects.
 *
 * Fails safe: if connectivity cannot be read at all, we report metered so the
 * cautious path wins and no large download starts behind the user's back.
 */
fun Context.isConnectionMetered(): Boolean {
    val manager = getSystemService<ConnectivityManager>() ?: return true
    return manager.isActiveNetworkMetered
}
