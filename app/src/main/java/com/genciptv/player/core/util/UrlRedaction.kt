package com.genciptv.player.core.util

/**
 * Strips credentials out of a URL before it reaches a log.
 *
 * Xtream carries the subscription username and password as query parameters —
 * `xmltv.php?username=...&password=...` — so logging such a URL verbatim writes
 * a working account into logcat, where a bug report or an `adb logcat` picks it
 * straight up.
 *
 * Also handles the `user:pass@host` form. Anything that fails to parse is
 * reduced to a bare marker rather than passed through, on the principle that a
 * log line is never worth leaking a credential for.
 */
fun String.redactCredentials(): String = runCatching {
    var out = this

    // user:pass@host
    out = Regex("//[^/@\\s]+:[^/@\\s]+@").replace(out, "//<redacted>@")

    // Query parameters whose name suggests a secret.
    out = Regex(
        "([?&](?:username|user|password|pass|pwd|token|api_key|apikey|key)=)[^&\\s]*",
        RegexOption.IGNORE_CASE,
    ).replace(out) { "${it.groupValues[1]}<redacted>" }

    out
}.getOrDefault("<url redacted>")
