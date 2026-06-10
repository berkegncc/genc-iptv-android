package com.genciptv.player.core.designsystem

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Device-width CompositionLocal. Set once in [com.genciptv.player.MainActivity]
 * and read by any composable that needs to adapt layout to the current screen.
 *
 * Breakpoints (Material3):
 *   - Compact   < 600dp  — phones in portrait
 *   - Medium    600–840dp — small tablets, phones in landscape, foldables
 *   - Expanded  ≥ 840dp — tablets, large foldables, Chromebooks
 *
 * Defaults to a Compact (phone) size so @Preview composables and any caller that
 * forgets the provider degrade gracefully to the phone layout instead of
 * crashing. [com.genciptv.player.MainActivity] always overrides this with the
 * real window size at runtime.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp))
}

object WindowSize {
    /** True when the UI should switch from bottom-nav to a side rail + wider layouts. */
    val isTablet: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSize.current.widthSizeClass != WindowWidthSizeClass.Compact

    /**
     * True on Medium widths (600–840dp) — small tablets, large foldables, phones
     * in landscape. Side rail + adaptive grids, but still a single content pane.
     */
    val isMedium: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSize.current.widthSizeClass == WindowWidthSizeClass.Medium

    /**
     * True on Expanded widths (≥840dp) — tablets in landscape, large tablets,
     * Chromebooks. The breakpoint at which master-detail (two-pane) layouts turn
     * on across the app.
     */
    val isExpanded: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSize.current.widthSizeClass == WindowWidthSizeClass.Expanded
}
