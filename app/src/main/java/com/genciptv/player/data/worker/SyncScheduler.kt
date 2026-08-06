package com.genciptv.player.data.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily [PlaylistSyncWorker] via WorkManager.
 *
 * Reads the Wi-Fi preference itself rather than taking it as a parameter, so
 * that callers — adding a playlist, app start, flipping the setting — cannot
 * schedule the job under a constraint that disagrees with what the user chose.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    /** Declares the job with whatever network the user currently allows. */
    suspend fun scheduleDailySync() {
        schedule(userPreferencesRepository.user.first().syncOverWifiOnly)
    }

    private fun schedule(wifiOnly: Boolean) {
        val constraints = Constraints.Builder()
            // A refresh re-fetches the whole catalogue — every channel, film and
            // series with their categories — which runs to tens of megabytes on
            // a large playlist. Daily on mobile data that is hundreds of
            // megabytes a month, spent in the background where nobody sees it,
            // so waiting for Wi-Fi is the default. Users who would rather stay
            // current wherever they are can say so in Settings.
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .build()

        val request = PeriodicWorkRequestBuilder<PlaylistSyncWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PlaylistSyncWorker.UNIQUE_NAME,
            // UPDATE, not KEEP. KEEP leaves an already-scheduled job exactly as
            // it was first enqueued, so a change of constraint would only ever
            // reach fresh installs — and flipping the setting would do nothing
            // for the person who flipped it. UPDATE rewrites the job in place
            // without resetting its 24-hour timer.
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
