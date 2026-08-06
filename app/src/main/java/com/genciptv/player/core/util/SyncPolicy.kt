package com.genciptv.player.core.util

/**
 * When the provider catalogue counts as out of date.
 *
 * Two places need the same answer and must not drift apart: the gate on app
 * open decides whether to sync before showing content, and Home decides whether
 * to tell the user that a refresh is being held back. If these disagreed, the
 * app could sit on a stale catalogue while insisting it was current, or nag
 * about a refresh it had in fact just done.
 */
object SyncPolicy {

    /** A catalogue older than this is stale. */
    const val STALE_AFTER_MS: Long = 6L * 60L * 60L * 1000L
}
