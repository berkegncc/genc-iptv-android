package com.genciptv.player.core.designsystem

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * Device-width CompositionLocal. Set once in [com.genciptv.player.MainActivity]
 * and read by any composable that needs to adapt layout to the current screen.
 *
 * Breakpoints (Material3):
 *   - Compact   < 600dp  — phones in portrait
 *   - Medium    600–840dp — small tablets, phones in landscape, foldables
 *   - Expanded  ≥ 840dp — tablets, large foldables, Chromebooks
 */
val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("LocalWindowSize not provided — wrap content in CompositionLocalProvider")
}

object WindowSize {
    /** True when the UI should switch from bottom-nav to a side rail + wider layouts. */
    val isTablet: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalWindowSize.current.widthSizeClass != WindowWidthSizeClass.Compact
}
