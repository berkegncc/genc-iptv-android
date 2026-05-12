package com.genciptv.player.data.repository

import android.util.Log
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.source.local.dao.SeriesDao
import com.genciptv.player.data.source.local.dao.VodDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backfills missing posters for VOD/series items via TMDb when the upstream
 * provider (Xtream) didn't ship a `stream_icon` / `cover`.
 *
 * Design:
 *  - Lazy: invoked from VMs as soon as a list is observed. Items with a
 *    poster already are skipped instantly.
 *  - Deduped: an in-memory set tracks every item id we've already attempted
 *    this session, so re-emitting the same list (e.g. after a Flow update)
 *    won't re-fire the same TMDb queries.
 *  - DB-backed: the resolved URL is written to the row via the DAO, which in
 *    turn re-emits the Flow downstream — the UI updates without any direct
 *    coupling between this class and Compose.
 *  - Throttled: a small semaphore caps in-flight TMDb requests so a 1000-item
 *    list doesn't fan out to 1000 simultaneous network calls.
 *  - Long-lived scope: uses an internal supervisor scope so a VM clearing
 *    mid-fetch doesn't cancel an in-flight lookup that's about to land.
 *    Lookups are cheap and idempotent, so finishing them is fine.
 */
@Singleton
class PosterEnricher @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val vodDao: VodDao,
    private val seriesDao: SeriesDao,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val attempted = ConcurrentHashMap.newKeySet<String>()
    private val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)

    /** Backfill posters for movies in [items] that don't already have one. */
    fun enrichMovies(items: List<VodItem>) {
        items.asSequence()
            .filter { it.posterUrl.isNullOrBlank() }
            .filter { attempted.add(it.id) }
            .forEach { item ->
                scope.launch {
                    semaphore.withPermit {
                        val url = tmdbRepository.fetchMoviePosterUrl(item.title, item.year)
                        if (url != null) {
                            runCatching { vodDao.updatePosterUrl(item.id, url) }
                                .onFailure { Log.w(TAG, "Failed to persist movie poster for ${item.id}", it) }
                        }
                    }
                }
            }
    }

    /** Backfill posters for series in [items] that don't already have one. */
    fun enrichSeries(items: List<Series>) {
        items.asSequence()
            .filter { it.posterUrl.isNullOrBlank() }
            .filter { attempted.add(it.id) }
            .forEach { item ->
                scope.launch {
                    semaphore.withPermit {
                        val url = tmdbRepository.fetchSeriesPosterUrl(item.title, item.year)
                        if (url != null) {
                            runCatching { seriesDao.updatePosterUrl(item.id, url) }
                                .onFailure { Log.w(TAG, "Failed to persist series poster for ${item.id}", it) }
                        }
                    }
                }
            }
    }

    private companion object {
        const val TAG = "GencIPTV/PosterEnricher"
        // Free-tier TMDb is 50 req/sec; well under that for our list sizes.
        const val MAX_CONCURRENT_REQUESTS = 6
    }
}
