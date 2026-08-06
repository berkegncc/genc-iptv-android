package com.genciptv.player.app.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.core.util.SyncPolicy.STALE_AFTER_MS
import com.genciptv.player.core.util.isConnectionMetered
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
 *    is fresh (within [STALE_AFTER_MS]).
 *  - [AppRoute.Syncing.route]    — onboarded, valid active playlist, last sync
 *    is older than [STALE_AFTER_MS], **and** the connection is one the user has
 *    agreed to sync over. Shows the auto-sync gate, which then goes to Home.
 *    On a metered connection with "Yalnızca Wi-Fi" set, this is skipped and the
 *    catalogue simply stays as it was until Wi-Fi or a manual sync.
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
    @ApplicationContext private val appContext: Context,
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
                val stale = age > STALE_AFTER_MS

                // "Yalnızca Wi-Fi" has to hold here too, not just for the
                // background job. This gate downloads the whole catalogue, and
                // it fires the moment the app opens — so on a metered
                // connection we skip it and go straight to Home rather than
                // spending the user's data against a setting that promised we
                // would not. Profil → "Şimdi senkronize et" still syncs on
                // demand, because that is the user asking for it.
                val blockedByMetering =
                    prefs.syncOverWifiOnly && appContext.isConnectionMetered()

                if (stale && !blockedByMetering) {
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
}
