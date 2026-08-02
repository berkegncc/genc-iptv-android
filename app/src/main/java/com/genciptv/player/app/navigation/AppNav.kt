package com.genciptv.player.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.genciptv.player.core.ui.GencBottomNav
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.feature.channels.ChannelsScreen
import com.genciptv.player.feature.favorites.FavoritesScreen
import com.genciptv.player.feature.guide.GuideScreen
import com.genciptv.player.feature.home.HomeScreen
import com.genciptv.player.feature.onboarding.OnboardingScreen
import com.genciptv.player.feature.player.PlayerScreen
import com.genciptv.player.feature.profile.ProfileScreen
import com.genciptv.player.feature.profile.playlists.PlaylistManagerScreen
import com.genciptv.player.feature.profile.player.PlayerSettingsScreen
import com.genciptv.player.feature.profile.subtitle.SubtitleSettingsScreen
import com.genciptv.player.feature.profile.theme.ThemeSettingsScreen
import com.genciptv.player.feature.search.SearchScreen
import com.genciptv.player.feature.syncing.SyncingScreen
import com.genciptv.player.feature.vod.VOD_KIND_ARG
import com.genciptv.player.feature.vod.VodDetailScreen
import com.genciptv.player.feature.vod.VodListScreen
import com.genciptv.player.feature.vodplayer.VodPlayerScreen

// ── Route definitions ─────────────────────────────────────────────────────────

sealed class AppRoute(val route: String) {
    object Onboarding : AppRoute("onboarding")
    object Syncing : AppRoute("syncing")
    object Home : AppRoute("home")
    object Channels : AppRoute("channels")
    object Guide : AppRoute("guide")
    object Favorites : AppRoute("favorites")
    object Profile : AppRoute("profile")
    object Search : AppRoute("search")

    object ProfilePlaylists : AppRoute("profile/playlists")
    object ProfilePlayer : AppRoute("profile/player")
    object ProfileSubtitle : AppRoute("profile/subtitle")
    object ProfileTheme : AppRoute("profile/theme")

    /**
     * Filmler and Diziler are deliberately two *separate* destinations.
     *
     * They used to share one `vod?kind={kind}` destination. Tab switching goes
     * through [navigateBottomNav], which relies on `popUpTo(saveState)` +
     * `restoreState` — and Navigation keys saved state by destination id, not
     * by arguments. So tapping "Diziler" restored the saved "Filmler" entry,
     * arguments and all, and the user had to tap a second time to actually get
     * there. Distinct routes give each tab its own saved state and ViewModel.
     */
    object Movies : AppRoute("vod/movies")
    object Series : AppRoute("vod/series")

    data class Player(val channelId: String) : AppRoute("player/$channelId") {
        companion object { const val PATTERN = "player/{channelId}" }
    }
    data class VodDetail(val id: String) : AppRoute("vod/detail/$id") {
        companion object { const val PATTERN = "vod/detail/{id}" }
    }

    data class VodPlayerMovie(val vodId: String) : AppRoute("vodplayer/movie/$vodId") {
        companion object { const val PATTERN = "vodplayer/movie/{vodId}" }
    }

    data class VodPlayerEpisode(val episodeId: String) : AppRoute("vodplayer/episode/$episodeId") {
        companion object { const val PATTERN = "vodplayer/episode/{episodeId}" }
    }
}

// ── Bottom nav route mapping ──────────────────────────────────────────────────

private val navItemRouteMap = mapOf(
    GencNavItem.HOME     to AppRoute.Home.route,
    GencNavItem.CHANNELS to AppRoute.Channels.route,
    GencNavItem.MOVIES   to AppRoute.Movies.route,
    GencNavItem.SERIES   to AppRoute.Series.route,
    GencNavItem.PROFILE  to AppRoute.Profile.route,
)

/** The two VOD tabs, for transition decisions. */
private val VodRoutes = setOf(AppRoute.Movies.route, AppRoute.Series.route)

/** Screens hand us a [VodKind] name; map it to the matching tab route. */
private fun vodRouteFor(kind: String): String =
    if (kind == VodKind.SERIES.name) AppRoute.Series.route else AppRoute.Movies.route

private fun routeToNavItem(route: String?): GencNavItem? = navItemRouteMap
    .entries
    .firstOrNull { it.value == route }
    ?.key

/**
 * Canonical bottom-nav destination switching.
 *
 * We hard-code [AppRoute.Home] as the popUpTo target rather than using
 * [findStartDestination]. The graph's start destination can be `Syncing` or
 * `Onboarding` (depending on app state at launch); both routes are
 * inclusive-popped after they finish, so they're no longer in the back stack
 * when bottom nav fires. `popUpTo` against a route that isn't on the stack
 * silently does nothing — which would defeat saveState/restoreState for tab
 * switching and let the back stack accumulate. Home is always present once
 * the user reaches the main app (every onboarding/syncing transition lands
 * there), so it's the reliable tab root.
 */
private fun NavController.navigateBottomNav(route: String) {
    navigate(route) {
        popUpTo(AppRoute.Home.route) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

// ── Root app composable — resolves start destination from UserPreferences ─────

/**
 * Top-level composable that determines whether to start at Onboarding or Home.
 */
@Composable
fun AppNavHost(
    viewModel: StartDestinationViewModel = hiltViewModel(),
) {
    val startRoute by viewModel.startRoute.collectAsStateWithLifecycle()

    // [BrandedSplashGate] mounts the [AnimatedSplashOverlay] above whatever
    // content we render below — it auto-dismisses ~1.7 s after first frame
    // and replays only on cold start (not on configuration changes).
    BrandedSplashGate {
        val resolved = startRoute
        if (resolved != null) {
            GencAppNavHost(startDestination = resolved)
        }
        // While `resolved` is null the system splash is still up
        // (MainActivity.setKeepOnScreenCondition keeps it), so we don't need
        // a Compose fallback here.
    }
}

// ── NavHost with resolved start destination ───────────────────────────────────

// ── Transitions ───────────────────────────────────────────────────────────────
//
// Default: gentle 220ms cross-fade between every destination — replaces the
// snap that made pops "feel cheap" (the previous PlayerScreen surface used
// to flash for ~one frame after popBackStack).
//
// Player routes override with a modal slide: enter slides up from the bottom
// (presents itself), popExit slides down to the bottom (dismisses). Reads as
// intentional rather than a screen merely disappearing.

private val DefaultEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
    { fadeIn(animationSpec = tween(220)) }

private val DefaultExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
    { fadeOut(animationSpec = tween(220)) }

private val PlayerEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInVertically(
        animationSpec = tween(300),
        initialOffsetY = { fullHeight -> fullHeight },
    ) + fadeIn(animationSpec = tween(300))
}

private val PlayerPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutVertically(
        animationSpec = tween(260),
        targetOffsetY = { fullHeight -> fullHeight },
    ) + fadeOut(animationSpec = tween(260))
}

@Composable
private fun GencAppNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = DefaultEnterTransition,
        exitTransition = DefaultExitTransition,
        popEnterTransition = DefaultEnterTransition,
        popExitTransition = DefaultExitTransition,
    ) {
        // ── Onboarding ────────────────────────────────────────────────────────
        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Syncing (auto-sync gate on app open if active playlist is stale) ──
        composable(AppRoute.Syncing.route) {
            SyncingScreen(
                onComplete = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Syncing.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(AppRoute.Home.route) {
            HomeScreen(
                onNavigateToPlayer = { channelId ->
                    navController.navigate("player/$channelId")
                },
                onNavigateToChannels = {
                    navController.navigateBottomNav(AppRoute.Channels.route)
                },
                onNavigateToProfile = {
                    navController.navigateBottomNav(AppRoute.Profile.route)
                },
                onNavigateToVod = { kind ->
                    navController.navigateBottomNav(vodRouteFor(kind))
                },
                onNavigateToVodDetail = { id ->
                    navController.navigate("vod/detail/$id")
                },
                onNavigateToSearch = {
                    navController.navigate(AppRoute.Search.route)
                },
            )
        }

        // ── Search ────────────────────────────────────────────────────────────
        composable(AppRoute.Search.route) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { channelId ->
                    navController.navigate("player/$channelId")
                },
                onNavigateToVodDetail = { id ->
                    navController.navigate("vod/detail/$id")
                },
            )
        }

        // ── Channels ──────────────────────────────────────────────────────────
        composable(AppRoute.Channels.route) {
            ChannelsScreen(
                onNavigateToPlayer = { channelId ->
                    navController.navigate("player/$channelId")
                },
                onBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigateBottomNav(AppRoute.Profile.route)
                },
                onNavigateToHome = {
                    navController.navigateBottomNav(AppRoute.Home.route)
                },
                onNavigateToVod = { kind ->
                    navController.navigateBottomNav(vodRouteFor(kind))
                },
            )
        }

        // ── Guide ─────────────────────────────────────────────────────────────
        composable(AppRoute.Guide.route) {
            GuideScreen(
                onNavigateToPlayer = { channelId ->
                    navController.navigate("player/$channelId")
                },
                onBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigateBottomNav(AppRoute.Home.route)
                },
                onNavigateToChannels = {
                    navController.navigateBottomNav(AppRoute.Channels.route)
                },
                // Favoriler is a drill-in destination, not a tab — preserve
                // Guide on the back stack so back returns here.
                onNavigateToFavorites = {
                    navController.navigate(AppRoute.Favorites.route)
                },
                onNavigateToProfile = {
                    navController.navigateBottomNav(AppRoute.Profile.route)
                },
                onNavigateToVod = { kind ->
                    navController.navigateBottomNav(vodRouteFor(kind))
                },
            )
        }

        // ── Favorites ─────────────────────────────────────────────────────────
        composable(AppRoute.Favorites.route) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlayer = { channelId ->
                    navController.navigate("player/$channelId")
                },
                onNavigateToVodDetail = { id ->
                    navController.navigate("vod/detail/$id")
                },
                onNavigateToHome = {
                    navController.navigateBottomNav(AppRoute.Home.route)
                },
                onNavigateToChannels = {
                    navController.navigateBottomNav(AppRoute.Channels.route)
                },
                // Guide is a drill-in destination, not a tab — preserve
                // Favoriler on the back stack so back returns here.
                onNavigateToGuide = {
                    navController.navigate(AppRoute.Guide.route)
                },
                onNavigateToProfile = {
                    navController.navigateBottomNav(AppRoute.Profile.route)
                },
                onNavigateToVod = { kind ->
                    navController.navigateBottomNav(vodRouteFor(kind))
                },
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(AppRoute.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPlaylistManager = {
                    navController.navigate(AppRoute.ProfilePlaylists.route)
                },
                onNavigateToPlayerSettings = {
                    navController.navigate(AppRoute.ProfilePlayer.route)
                },
                onNavigateToSubtitleSettings = {
                    navController.navigate(AppRoute.ProfileSubtitle.route)
                },
                onNavigateToThemeSettings = {
                    navController.navigate(AppRoute.ProfileTheme.route)
                },
                onNavigateToHome = {
                    navController.navigateBottomNav(AppRoute.Home.route)
                },
                onNavigateToChannels = {
                    navController.navigateBottomNav(AppRoute.Channels.route)
                },
                // Guide and Favorites are drill-in screens (not bottom-nav
                // tabs), so use plain navigate. Otherwise navigateBottomNav's
                // popUpTo(Home, saveState) would strip Profile from the back
                // stack and pressing back from Guide/Favoriler would land on
                // Home instead of returning to Profile.
                onNavigateToGuide = {
                    navController.navigate(AppRoute.Guide.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(AppRoute.Favorites.route)
                },
                onNavigateToVod = { kind ->
                    navController.navigateBottomNav(vodRouteFor(kind))
                },
            )
        }

        // ── Profile: Playlist Manager ─────────────────────────────────────────
        composable(AppRoute.ProfilePlaylists.route) {
            PlaylistManagerScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Profile: Player Settings ──────────────────────────────────────────
        composable(AppRoute.ProfilePlayer.route) {
            PlayerSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Profile: Subtitle Settings ────────────────────────────────────────
        composable(AppRoute.ProfileSubtitle.route) {
            SubtitleSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── Profile: Theme Settings ───────────────────────────────────────────
        composable(AppRoute.ProfileTheme.route) {
            ThemeSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        // ── VOD List — one destination per tab ────────────────────────────────
        vodListDestination(AppRoute.Movies.route, VodKind.MOVIE, navController)
        vodListDestination(AppRoute.Series.route, VodKind.SERIES, navController)

        // ── Player ────────────────────────────────────────────────────────────
        composable(
            route = AppRoute.Player.PATTERN,
            enterTransition = PlayerEnterTransition,
            popExitTransition = PlayerPopExitTransition,
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: return@composable
            PlayerScreen(
                channelId = channelId,
                onBack = { navController.popBackStack() },
            )
        }

        // ── VOD Detail ────────────────────────────────────────────────────────
        composable(AppRoute.VodDetail.PATTERN) {
            VodDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToVodPlayer = { vodId ->
                    navController.navigate("vodplayer/movie/$vodId")
                },
                onNavigateToEpisodePlayer = { episodeId ->
                    navController.navigate("vodplayer/episode/$episodeId")
                },
                onNavigateToVodDetail = { id ->
                    navController.navigate("vod/detail/$id")
                },
            )
        }

        // ── VOD Player (Movie) ────────────────────────────────────────────────
        composable(
            route = AppRoute.VodPlayerMovie.PATTERN,
            arguments = listOf(
                navArgument("vodId") { type = NavType.StringType }
            ),
            enterTransition = PlayerEnterTransition,
            popExitTransition = PlayerPopExitTransition,
        ) {
            VodPlayerScreen(onBack = { navController.popBackStack() })
        }

        // ── VOD Player (Episode) ──────────────────────────────────────────────
        composable(
            route = AppRoute.VodPlayerEpisode.PATTERN,
            arguments = listOf(
                navArgument("episodeId") { type = NavType.StringType }
            ),
            enterTransition = PlayerEnterTransition,
            popExitTransition = PlayerPopExitTransition,
        ) {
            VodPlayerScreen(onBack = { navController.popBackStack() })
        }
    }
}

// ── VOD tab destination ───────────────────────────────────────────────────────

/**
 * Registers one of the two VOD tabs. Both render the same screen; the route is
 * what decides which kind it shows, and the tab row navigates between them so
 * route and content can't drift apart.
 */
private fun NavGraphBuilder.vodListDestination(
    route: String,
    kind: VodKind,
    navController: NavHostController,
) {
    composable(
        route = route,
        // `kind` isn't in the path — it rides along as a default argument so
        // VodListViewModel can read it from its SavedStateHandle and be correct
        // on its very first emission.
        arguments = listOf(
            navArgument(VOD_KIND_ARG) {
                type = NavType.StringType
                defaultValue = kind.name
            }
        ),
        // Filmler ⇄ Diziler reads as one screen with a tab row, so switching
        // between them shouldn't cross-fade; only animate arrivals from
        // elsewhere.
        enterTransition = {
            if (initialState.destination.route in VodRoutes) EnterTransition.None
            else DefaultEnterTransition(this)
        },
        exitTransition = {
            if (targetState.destination.route in VodRoutes) ExitTransition.None
            else DefaultExitTransition(this)
        },
        popEnterTransition = {
            if (initialState.destination.route in VodRoutes) EnterTransition.None
            else DefaultEnterTransition(this)
        },
        popExitTransition = {
            if (targetState.destination.route in VodRoutes) ExitTransition.None
            else DefaultExitTransition(this)
        },
    ) {
        VodListScreen(
            onBack = { navController.popBackStack() },
            onNavigateToDetail = { id ->
                navController.navigate("vod/detail/$id")
            },
            onNavigateToVodPlayer = { vodId ->
                navController.navigate("vodplayer/movie/$vodId")
            },
            onNavigateToEpisodePlayer = { episodeId ->
                navController.navigate("vodplayer/episode/$episodeId")
            },
            onNavigateToHome = {
                navController.navigateBottomNav(AppRoute.Home.route)
            },
            onNavigateToChannels = {
                navController.navigateBottomNav(AppRoute.Channels.route)
            },
            onNavigateToProfile = {
                navController.navigateBottomNav(AppRoute.Profile.route)
            },
            onSelectKind = { selected ->
                navController.navigateBottomNav(vodRouteFor(selected.name))
            },
        )
    }
}

// ── Placeholder screen template ───────────────────────────────────────────────

@Composable
private fun PlaceholderScreen(
    label: String,
    navController: NavHostController
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentNavItem = routeToNavItem(currentRoute)

    Scaffold(
        bottomBar = {
            GencBottomNav(
                current = currentNavItem ?: GencNavItem.HOME,
                onItemClick = { item ->
                    val route = navItemRouteMap[item] ?: return@GencBottomNav
                    if (currentRoute != route) {
                        navController.navigateBottomNav(route)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
