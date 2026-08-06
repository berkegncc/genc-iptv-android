package com.genciptv.player.core.util

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

/**
 * The app's language.
 *
 * Two mechanisms, one per platform era, because there is no single API that
 * covers both:
 *
 *  - Android 13 and up have a per-app language the system owns. It is also what
 *    Settings → Apps → Genç IPTV → Language writes to, so it has to be the
 *    source of truth there; keeping our own copy beside it would give two
 *    answers that drift apart. Setting it makes the framework recreate the
 *    activity on its own.
 *  - Below that there is nothing to defer to, so the choice is stored here and
 *    applied by wrapping the activity's base context in [wrap].
 *
 * AppCompatDelegate.setApplicationLocales would paper over the split, but only
 * for apps built on AppCompat components — it works through the delegates that
 * AppCompatActivity registers, and this app's activity is a plain
 * ComponentActivity with a platform theme. Calling it here did nothing at all,
 * silently.
 */
enum class AppLanguage(val tag: String?) {

    /** Follow the phone. */
    SYSTEM(null),

    TURKISH("tr"),
    ENGLISH("en"),
    ;

    companion object {

        private const val PREFS = "app_language"
        private const val KEY_TAG = "tag"

        private fun prefs(context: Context) =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        /** The language tag in effect, or null when following the system. */
        private fun storedTag(context: Context): String? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)
                    ?.applicationLocales
                    ?.takeIf { !it.isEmpty }
                    ?.get(0)
                    ?.language
            } else {
                prefs(context).getString(KEY_TAG, null)
            }

        /** What is in effect right now. */
        fun current(context: Context): AppLanguage {
            val tag = storedTag(context)
            return entries.firstOrNull { it.tag == tag } ?: SYSTEM
        }

        /**
         * Applies [language] and restarts the UI so every string is re-read.
         *
         * On Android 13+ the framework does the restart itself once the locale
         * is set; below that we ask for it, since nothing else will.
         */
        fun apply(activity: Activity, language: AppLanguage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val locales = language.tag
                    ?.let { LocaleList.forLanguageTags(it) }
                    ?: LocaleList.getEmptyLocaleList()
                activity.getSystemService(LocaleManager::class.java)?.applicationLocales = locales
            } else {
                prefs(activity).edit { putString(KEY_TAG, language.tag) }
                activity.recreate()
            }
        }

        /**
         * Base context carrying the chosen language, for `attachBaseContext`.
         *
         * A no-op on Android 13+, where the system has already applied its own
         * per-app locale to the context we are handed.
         */
        fun wrap(base: Context): Context {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
            val tag = prefs(base).getString(KEY_TAG, null) ?: return base
            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            return base.createConfigurationContext(config)
        }
    }
}
