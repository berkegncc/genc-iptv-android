package com.genciptv.player.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.genciptv.player.data.repository.EpgRepository
import com.genciptv.player.data.repository.PlaylistRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that refreshes the active playlist (channels + EPG) on a daily schedule.
 * Triggered by [scheduleDailySync] after a playlist is added.
 */
@HiltWorker
class PlaylistSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val playlistRepository: PlaylistRepository,
    private val epgRepository: EpgRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val active = playlistRepository.getActive() ?: return Result.success()
            playlistRepository.sync(active.id)
            active.epgUrl?.takeIf { it.isNotBlank() }?.let { xmltv ->
                runCatching { epgRepository.syncFromXmltv(active.id, xmltv) }
            }
            // Keep only last 48h of past programs + current week.
            val cutoff = System.currentTimeMillis() - 48 * 3_600_000L
            epgRepository.clearOld(active.id, cutoff)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "playlist-daily-sync"
    }
}
