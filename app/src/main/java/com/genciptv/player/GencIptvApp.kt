package com.genciptv.player

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.genciptv.player.data.worker.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class GencIptvApp : Application(),
    Configuration.Provider,
    SingletonImageLoader.Factory {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    /**
     * Re-declares the daily sync on every launch.
     *
     * The schedule survives reboots and app updates, so a device that enqueued
     * it under an older build keeps that build's constraints — including the
     * one that let a full catalogue download run over mobile data. Re-declaring
     * here is what carries a changed constraint to installs that already exist;
     * the call is idempotent and does not reset the 24-hour timer.
     */
    override fun onCreate() {
        super.onCreate()
        // Fire-and-forget: nothing waits on this, and a failure to re-declare
        // just leaves the previous schedule in place.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { syncScheduler.scheduleDailySync() }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    /**
     * Coil 3 singleton ImageLoader. Installs the OkHttp network fetcher
     * (picked via `coil3.network.okhttp`), configures modest memory + disk
     * caches, and enables crossfade for smoother channel logo loading.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
}
