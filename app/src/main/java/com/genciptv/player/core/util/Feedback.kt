package com.genciptv.player.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.genciptv.player.BuildConfig

/**
 * Opens the feedback form — bug reports, suggestions, complaints.
 *
 * A Google Form rather than a `mailto:` link. The APK is downloadable by
 * anyone, so an address baked into it is an address that gets scraped; the form
 * keeps the maintainer's inbox out of the binary and collects answers in a
 * spreadsheet instead of a pile of mail. Nothing here is a secret — the form URL
 * is public by design and safe to commit.
 *
 * The form asks for the topic, the message and an optional reply address. Only
 * [ENTRY_DEVICE] is filled in by the app; everything else is the reporter's to
 * write.
 */
object Feedback {

    private const val FORM_URL =
        "https://docs.google.com/forms/d/e/" +
            "1FAIpQLSeavDMaePREwYiF9N5ZzaoT6qEkKVQmoz2pLkiTFIb8lvghRQ/viewform"

    /**
     * The form's "Cihaz" field. Prefilled so a report arrives with enough
     * context to act on — "açılmıyor" with no build or device behind it is not
     * something anyone can chase.
     *
     * The number is the form's own field id, taken from its pre-filled link. If
     * the form's questions are ever reordered or replaced, this has to be read
     * off a fresh pre-filled link: a stale id silently drops the value rather
     * than failing loudly.
     */
    private const val ENTRY_DEVICE = "entry.826510221"

    /**
     * Build, device and OS, and deliberately nothing else.
     *
     * No playlist URL, no Xtream username, no password. A user reporting a
     * crash is not consenting to publish their subscription, and the form's
     * answers live in a spreadsheet — anything added here would land there in
     * plain text, from someone who thought they were describing a bug.
     */
    fun deviceSummary(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        // Most models already carry the brand ("Samsung SM-X820" would read
        // "Samsung Samsung SM-X820"); only prepend it when it is missing.
        val device = if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
        return "Genç IPTV ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) · " +
            "$device · Android ${Build.VERSION.RELEASE}"
    }

    /** The form's URL with the device field filled in. */
    fun formUri(): Uri = Uri.parse(FORM_URL)
        .buildUpon()
        .appendQueryParameter("usp", "pp_url")
        .appendQueryParameter(ENTRY_DEVICE, deviceSummary())
        .build()

    /**
     * Opens the form in the browser. Returns false when the device has nothing
     * that can open a link, so the caller can say so rather than appearing to
     * have done nothing.
     */
    fun open(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, formUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
