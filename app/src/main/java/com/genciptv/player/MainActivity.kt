package com.genciptv.player

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.app.AppThemeViewModel
import com.genciptv.player.app.PipController
import com.genciptv.player.app.navigation.AppNavHost
import com.genciptv.player.app.navigation.StartDestinationViewModel
import com.genciptv.player.core.designsystem.AccentPalette
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalWindowSize
import com.genciptv.player.data.model.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Resolve start destination before setContent so splash screen can be kept
    private val startDestinationViewModel: StartDestinationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate per androidx.core:core-splashscreen docs
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Keep splash screen visible until startRoute has resolved (non-null)
        splashScreen.setKeepOnScreenCondition {
            startDestinationViewModel.startRoute.value == null
        }

        enableEdgeToEdge()

        setContent {
            val themeViewModel: AppThemeViewModel by viewModels()
            val appearance by themeViewModel.appearance.collectAsStateWithLifecycle()

            val palette = AccentPalette.entries.firstOrNull { it.name == appearance.accentKey }
                ?: AccentPalette.PURPLE

            val systemDark = isSystemInDarkTheme()
            val isDark = when (appearance.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDark
            }

            @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
            val windowSize = calculateWindowSizeClass(this)

            GencIptvTheme(darkTheme = isDark, accentPalette = palette) {
                CompositionLocalProvider(LocalWindowSize provides windowSize) {
                    AppNavHost(viewModel = startDestinationViewModel)
                }
            }
        }
    }

    // ── Picture-in-Picture ────────────────────────────────────────────────────

    /**
     * Called when the user presses the Home button while the app is in the
     * foreground.  Enter PiP if a player is actively streaming.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (PipController.shouldEnterPip) {
            enterPipMode()
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration,
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        PipController.onPipModeChanged(isInPictureInPictureMode)
    }
}
