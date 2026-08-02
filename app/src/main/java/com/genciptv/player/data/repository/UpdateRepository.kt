package com.genciptv.player.data.repository

import android.content.Context
import android.util.Log
import com.genciptv.player.BuildConfig
import com.genciptv.player.core.util.VersionComparator
import com.genciptv.player.data.di.GithubRetrofit
import com.genciptv.player.data.model.DownloadState
import com.genciptv.player.data.model.UpdateInfo
import com.genciptv.player.data.source.github.GithubApi
import com.genciptv.player.data.source.local.prefs.UpdatePreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface UpdateRepository {
    /**
     * Returns the newer release, or null when there is nothing to offer.
     *
     * Never throws and never surfaces an error: checking for updates is a
     * background courtesy, so rate limits, DNS failures and offline devices all
     * resolve to "no update".
     *
     * @param force user-initiated. Skips the once-a-day throttle and ignores a
     *   previously dismissed version, so "Güncellemeleri kontrol et" always
     *   answers honestly.
     */
    suspend fun checkForUpdate(force: Boolean): UpdateInfo?

    /** Streams the APK into the cache directory. */
    fun downloadApk(info: UpdateInfo): Flow<DownloadState>

    /** Current version as shown in settings. */
    val currentVersion: String

    suspend fun dismiss(version: String)
}

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val githubApi: GithubApi,
    private val prefs: UpdatePreferencesDataSource,
    @GithubRetrofit private val okHttpClient: OkHttpClient,
) : UpdateRepository {

    override val currentVersion: String get() = BuildConfig.VERSION_NAME

    override suspend fun checkForUpdate(force: Boolean): UpdateInfo? {
        return try {
            val now = System.currentTimeMillis()
            if (!force) {
                val last = prefs.lastCheckTimestampOnce()
                if (now - last < CHECK_INTERVAL_MS) return null
            }

            val release = githubApi.getLatestRelease()

            // Only a completed check counts, so an offline launch retries on the
            // next one instead of going quiet for a day.
            prefs.setLastCheckTimestamp(now)

            if (release.prerelease) return null

            val remoteVersion = VersionComparator.normalize(release.tagName)
            if (remoteVersion.isBlank()) return null
            if (!VersionComparator.isNewer(BuildConfig.VERSION_NAME, release.tagName)) return null

            // An automatic check respects "Daha sonra"; a manual one does not.
            if (!force && prefs.dismissedVersionOnce() == remoteVersion) return null

            val asset = release.assets.firstOrNull {
                it.name.endsWith(".apk", ignoreCase = true)
            } ?: return null

            UpdateInfo(
                tagName = release.tagName,
                versionName = remoteVersion,
                title = release.name?.takeIf { it.isNotBlank() } ?: release.tagName,
                changelog = release.body.orEmpty(),
                downloadUrl = asset.browserDownloadUrl,
                sizeBytes = asset.size,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Rate limit (403), offline, DNS, malformed payload — all the same.
            Log.i(TAG, "Update check skipped: ${e.javaClass.simpleName}")
            null
        }
    }

    override fun downloadApk(info: UpdateInfo): Flow<DownloadState> = flow {
        val dir = File(context.cacheDir, UPDATES_DIR)
        // Cache dir, so the system may evict it; still, clear stale APKs before
        // pulling a new one instead of letting them pile up.
        clearOldApks(dir)
        dir.mkdirs()

        val target = File(dir, "genciptv-${info.versionName}.apk")
        target.delete()

        emit(DownloadState.Progress(0, 0L, info.sizeBytes))

        val request = Request.Builder().url(info.downloadUrl).build()
        // The asset URL redirects to objects.githubusercontent.com; the GitHub
        // client follows redirects, which is what makes this a single call.
        val call = okHttpClient.newBuilder()
            .callTimeout(DOWNLOAD_TIMEOUT_MIN, TimeUnit.MINUTES)
            .build()
            .newCall(request)

        call.execute().use { response ->
            if (!response.isSuccessful) {
                emit(DownloadState.Failed("Sunucu ${response.code} yanıtı döndü"))
                return@flow
            }
            val body = response.body
                ?: run {
                    emit(DownloadState.Failed("İndirme yanıtı boş geldi"))
                    return@flow
                }

            val total = body.contentLength().takeIf { it > 0 } ?: info.sizeBytes
            var readTotal = 0L
            var lastPercent = -1

            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        // Stop promptly if the caller went away mid-download.
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        readTotal += read

                        val percent = if (total > 0) {
                            ((readTotal * 100) / total).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        // Emit per percentage point, not per chunk.
                        if (percent != lastPercent) {
                            lastPercent = percent
                            emit(DownloadState.Progress(percent, readTotal, total))
                        }
                    }
                    output.flush()
                }
            }

            if (target.length() <= 0L) {
                emit(DownloadState.Failed("İndirilen dosya boş"))
                return@flow
            }
            emit(DownloadState.Done(target))
        }
    }
        .flowOn(Dispatchers.IO)
        .catch { e ->
            if (e is CancellationException) throw e
            Log.w(TAG, "APK download failed", e)
            emit(DownloadState.Failed("İndirme başarısız oldu. Bağlantınızı kontrol edip tekrar deneyin."))
        }

    override suspend fun dismiss(version: String) {
        runCatching { prefs.setDismissedVersion(version) }
    }

    private fun clearOldApks(dir: File) {
        runCatching {
            dir.listFiles()?.forEach { f ->
                if (f.isFile && f.name.endsWith(".apk", ignoreCase = true)) f.delete()
            }
        }
    }

    companion object {
        private const val TAG = "GencIPTV/Update"
        private const val UPDATES_DIR = "updates"
        private val CHECK_INTERVAL_MS = TimeUnit.HOURS.toMillis(24)
        private const val DOWNLOAD_TIMEOUT_MIN = 10L
    }
}
