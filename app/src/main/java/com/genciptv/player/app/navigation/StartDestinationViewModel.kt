package com.genciptv.player.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Thin ViewModel that decides Onboarding / Syncing / Home as the app's start
 * destination.
 *
 * Emits `null` while still loading (splash visible), then one of:
 *  - [AppRoute.Home.route]       — onboarded, valid active playlist, last sync
 *    is fresh (within [AUTO_SYNC_FRESHNESS_MS]).
 *  - [AppRoute.Syncing.route]    — onboarded, valid active playlist, but last
 *    sync is older than the threshold. Shows the auto-sync gate which then
 *    navigates to Home.
 *  - [AppRoute.Onboarding.route] — fresh install, missing prefs, OR a stale
 *    `activePlaylistId` pointing at a playlist row that no longer exists
 *    (e.g. after a destructive DB migration). In the last case we also clear
 *    the stale user-pref flags so we don't loop.
 *
 * The route is **latched on first non-null emission**. After sync completes
 * the underlying flows would re-emit (e.g. Syncing → Home), but we don't want
 * the NavHost's `startDestination` to flip mid-session — the [SyncingScreen]
 * itself navigates to Home when done.
 */
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val startRoute: StateFlow<String?> = combine(
        userPreferencesRepository.user,
        playlistRepository.observeActive(),
    ) { prefs, activePlaylist ->
        when {
            prefs.onboardingCompleted &&
                prefs.activePlaylistId > 0L &&
                activePlaylist != null -> {
                val now = System.currentTimeMillis()
                val age = now - activePlaylist.lastSyncedAt
                if (age > AUTO_SYNC_FRESHNESS_MS) {
                    AppRoute.Syncing.route
                } else {
                    AppRoute.Home.route
                }
            }

            // DB was wiped (destructive migration) but prefs still think we
            // are set up — reset prefs and force user back through onboarding.
            prefs.onboardingCompleted &&
                prefs.activePlaylistId > 0L &&
                activePlaylist == null -> {
                viewModelScope.launch {
                    userPreferencesRepository.setOnboardingCompleted(false)
                    userPreferencesRepository.setActivePlaylistId(-1L)
                }
                AppRoute.Onboarding.route
            }

            else -> AppRoute.Onboarding.route
        }
    }
        // Latch on first emission. The combined flow re-emits whenever prefs
        // or the active playlist change (e.g. lastSyncedAt updates after the
        // auto-sync finishes), but the start route must stay stable for the
        // life of this VM — navigation to Home is driven by SyncingScreen.
        .runningReduce { firstResolved, _ -> firstResolved }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val isLoading: Boolean get() = startRoute.value == null

    private companion object {
        /** Auto-sync gate kicks in when last sync is older than this. */
        const val AUTO_SYNC_FRESHNESS_MS: Long = 6L * 60L * 60L * 1000L
    }
}
