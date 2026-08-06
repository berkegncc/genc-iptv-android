package com.genciptv.player.feature.vodplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.genciptv.player.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.genciptv.player.app.PipController
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.BgElev2
import com.genciptv.player.core.designsystem.Copper
import com.genciptv.player.core.designsystem.Danger
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencColors
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.Line
import com.genciptv.player.core.designsystem.LineStrong
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.SheetTopShape
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.WindowSize
import com.genciptv.player.core.player.buildIptvDataSourceFactory
import com.genciptv.player.core.player.buildIptvMediaSource
import com.genciptv.player.core.ui.ErrorState
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.Poster
import com.genciptv.player.core.ui.applySubtitleStyle
import com.genciptv.player.core.util.restoreUserOrientation
import com.genciptv.player.core.util.episodeName
import com.genciptv.player.data.model.CastMember
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.model.VodItem
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Track / sheet helpers ─────────────────────────────────────────────────────

private data class AudioTrackInfo(val groupIndex: Int, val trackIndex: Int, val displayName: String)
private data class SubtitleTrackInfo(val groupIndex: Int, val trackIndex: Int, val displayName: String)

/** TRACKS merges audio and subtitles into one sheet, matching the single
 *  "Seslendirme ve Alt Yazı" control in the overlay. */
private enum class PlayerSheet { SPEED, TRACKS, SCALE, SEASON, EPISODES }

private enum class VideoScale {
    ORIGINAL,
    FIT_SCREEN,
    STRETCH,
    RATIO_16_9,
    RATIO_21_9,
}

@Composable
private fun videoScaleLabel(scale: VideoScale): String = when (scale) {
    VideoScale.ORIGINAL -> stringResource(R.string.player_scale_original)
    VideoScale.FIT_SCREEN -> stringResource(R.string.player_scale_fit_screen)
    VideoScale.STRETCH -> stringResource(R.string.player_scale_stretch)
    VideoScale.RATIO_16_9 -> stringResource(R.string.player_scale_ratio_16_9)
    VideoScale.RATIO_21_9 -> stringResource(R.string.player_scale_ratio_21_9)
}

private val SPEED_PRESETS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f)

/**
 * Width one control in the player's bottom row may occupy. The row is capped at
 * this times however many controls it is showing, so a film's three and a
 * series' five each get the same room rather than sharing one fixed width.
 * Enough for the longest label ("Ses ve Altyazı") plus its icon and padding.
 */
private val MaxActionSlotWidth = 240.dp

@Composable
private fun speedLabel(speed: Float): String {
    // formatSpeed's numeric part ("1x", "1.5x"…) is a number format, not
    // translatable text — only the parenthetical descriptor needs a resource.
    val numeric = formatSpeed(speed)
    val descriptorRes = when (speed) {
        0.25f -> R.string.player_speed_very_slow
        0.5f  -> R.string.player_speed_slow
        1f    -> R.string.player_speed_normal
        1.5f  -> R.string.player_speed_fast
        2f    -> R.string.player_speed_very_fast
        else  -> null
    }
    return if (descriptorRes != null) {
        stringResource(R.string.player_speed_with_descriptor, numeric, stringResource(descriptorRes))
    } else {
        numeric
    }
}

/**
 * A track's language, named in the language the app is currently running in.
 *
 * [displayLocale] used to be pinned to Turkish, which meant an English UI still
 * listed its audio tracks as "İngilizce" and "Türkçe". The caller passes the
 * composition's locale so the names follow the app's language setting.
 *
 * [unknownLabel] is resolved via `stringResource` at the call site rather than
 * here, because this is invoked from inside `remember { }` blocks, which
 * disallow composable calls.
 */
private fun languageDisplay(
    code: String?,
    unknownLabel: String,
    displayLocale: Locale,
): String {
    if (code.isNullOrBlank() || code == "und") return unknownLabel
    return runCatching {
        Locale.forLanguageTag(code)
            .getDisplayLanguage(displayLocale)
            .replaceFirstChar { it.uppercase(displayLocale) }
    }.getOrDefault(code)
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
    return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
    else String.format(Locale.getDefault(), "%d:%02d", m, s)
}

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun VodPlayerScreen(
    onBack: () -> Unit,
    viewModel: VodPlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val subtitleStyle by viewModel.subtitleStyle.collectAsStateWithLifecycle()
    val preferredAudioLang by viewModel.preferredAudioLang.collectAsStateWithLifecycle()
    val userAgent by viewModel.userAgent.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Keep the device awake while a stream is on screen. Without this Android's
    // user-inactivity timer kicks in and the display sleeps mid-watch — the
    // user has to power the screen back on every couple of minutes.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val dataSourceFactory = remember(userAgent) { buildIptvDataSourceFactory(userAgent) }

    // Push the user's preferred audio language into the track selector. `null`
    // = no preference, so multi-audio streams play in their native default
    // (ExoPlayer's fallback respects system locale). When the user has
    // explicitly chosen a language in Player Settings (e.g. "tr"), the
    // selector locks to tracks tagged with that language.
    LaunchedEffect(exoPlayer, preferredAudioLang) {
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage(preferredAudioLang)
            .build()
    }

    var playbackError by remember { mutableStateOf<String?>(null) }
    var fallbackTriedFor by remember { mutableStateOf<String?>(null) }
    /** Mirrors the player's buffering state; kept in sync by the listener below. */
    var isBuffering by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            // ExoPlayer is not observable by Compose, so reading
            // `exoPlayer.playbackState` inline leaves the spinner stuck: it is
            // drawn when some unrelated recomposition happens to catch
            // STATE_BUFFERING, and nothing re-evaluates it once buffering ends.
            // Mirroring the state here is what actually drives the UI.
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: PlaybackException) {
                val url = state.streamUrl.takeIf { it.isNotBlank() }
                val isParseError = error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ||
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED
                if (isParseError && url != null && fallbackTriedFor != url) {
                    fallbackTriedFor = url
                    val source = buildIptvMediaSource(url, dataSourceFactory, forceProgressive = true)
                    exoPlayer.setMediaSource(source)
                    if (state.initialPositionMs > 0L) exoPlayer.seekTo(state.initialPositionMs)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    return
                }
                playbackError = error.errorCodeName + " — " + (error.message ?: "Akış oynatılamadı")
            }
        }
        // Seed it: buffering may already be under way by the time we attach.
        isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    DisposableEffect(Unit) {
        PipController.shouldEnterPip = true
        onDispose {
            PipController.shouldEnterPip = false
            viewModel.savePosition(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
            exoPlayer.release()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Was UNSPECIFIED-or-PORTRAIT depending on the auto-rotate setting,
            // read by hand. SCREEN_ORIENTATION_USER expresses the same intent
            // and the platform applies it reliably — UNSPECIFIED could leave a
            // phone stuck in the landscape fullscreen had requested.
            (context as? ComponentActivity)?.restoreUserOrientation()
        }
    }

    LaunchedEffect(state.streamUrl) {
        val url = state.streamUrl
        if (url.isBlank()) return@LaunchedEffect
        playbackError = null
        fallbackTriedFor = null
        exoPlayer.setMediaSource(buildIptvMediaSource(url, dataSourceFactory))
        if (state.initialPositionMs > 0L) exoPlayer.seekTo(state.initialPositionMs)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
        exoPlayer.play()
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    LaunchedEffect(state.streamUrl) {
        while (true) {
            delay(15_000)
            viewModel.savePosition(
                exoPlayer.currentPosition,
                exoPlayer.duration.coerceAtLeast(0L),
            )
        }
    }

    val errorMessage = state.error
    when {
        state.isLoading -> LoadingState(
            message = stringResource(R.string.player_loading_content),
            modifier = Modifier.fillMaxSize().background(GencColors.Dark.bg),
        )
        errorMessage != null -> ErrorState(
            title = stringResource(R.string.player_playback_error_title),
            description = errorMessage,
            retryLabel = stringResource(R.string.player_go_back),
            onRetry = onBack,
            modifier = Modifier.fillMaxSize(),
        )
        else -> VodPlayerContent(
            state = state,
            exoPlayer = exoPlayer,
            playbackSpeed = playbackSpeed,
            playbackError = playbackError,
            isBuffering = isBuffering,
            subtitleStyle = subtitleStyle,
            onBack = onBack,
            onToggleFavorite = viewModel::toggleFavorite,
            onSpeedSelected = viewModel::setPlaybackSpeed,
            onSelectSeason = viewModel::selectSeason,
            onSelectEpisode = { newId ->
                viewModel.switchToEpisode(
                    newEpisodeId = newId,
                    currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
                    currentDurationMs = exoPlayer.duration.coerceAtLeast(0L),
                )
            },
        )
    }
}

// ── Stateless content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VodPlayerContent(
    state: VodPlayerUiState,
    exoPlayer: ExoPlayer,
    playbackSpeed: Float,
    playbackError: String? = null,
    /** Player is buffering; mirrored from the listener in [VodPlayerScreen]. */
    isBuffering: Boolean = false,
    subtitleStyle: SubtitleStyle = SubtitleStyle.Default,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onSelectSeason: (Int) -> Unit,
    onSelectEpisode: (String) -> Unit,
) {
    val accent = LocalAccentPalette.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val isInPipMode by PipController.isInPipMode.collectAsState()

    // Two different questions, deliberately answered by two different values:
    //
    //  - isTabletDevice asks about the *hardware*, and drives whether rotating
    //    the device should force fullscreen. That has to stay device-based:
    //    a tablet shouldn't start auto-fullscreening just because the user
    //    dragged it into a split-screen pane.
    //  - hasRoomForSidePanel asks about the *window*, and drives the layout.
    //    The side-by-side branch hands a fixed 380dp to the info panel, so on a
    //    400dp split-screen pane it would leave the video about 19dp wide.
    val isTabletDevice = configuration.smallestScreenWidthDp >= 600
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hasRoomForSidePanel = isTabletDevice && WindowSize.isExpanded

    var isFullscreen by remember { mutableStateOf(false) }
    var overlayVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    // Default: ORIGINAL (FIT) — natural aspect, no stretch. With the 40/60
    // layout the video has a bounded area so it never tries to fill the whole
    // screen with cropping.
    var videoScale by remember { mutableStateOf(VideoScale.ORIGINAL) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var seekFraction by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    var containerWidth by remember { mutableIntStateOf(0) }

    var seekFeedbackText by remember { mutableStateOf("") }
    var seekFeedbackVisible by remember { mutableStateOf(false) }

    var openSheet by remember { mutableStateOf<PlayerSheet?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isSeeking) {
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                durationMs = exoPlayer.duration.coerceAtLeast(0L)
                seekFraction = if (durationMs > 0L) positionMs.toFloat() / durationMs else 0f
                isPlaying = exoPlayer.isPlaying
            }
            delay(1_000)
        }
    }

    LaunchedEffect(overlayVisible) {
        if (overlayVisible) {
            delay(4_000)
            overlayVisible = false
        }
    }

    LaunchedEffect(seekFeedbackVisible) {
        if (seekFeedbackVisible) {
            delay(700)
            seekFeedbackVisible = false
        }
    }

    // Turning the phone is the natural way to ask for fullscreen, so follow it.
    // Phones only: on tablets landscape means the side-by-side layout. With
    // auto-rotate switched off the system never reports a landscape
    // configuration, so this stays dormant and the button remains the way in.
    LaunchedEffect(isLandscape) {
        if (isTabletDevice) return@LaunchedEffect
        isFullscreen = isLandscape
    }

    LaunchedEffect(isFullscreen) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isFullscreen) {
            // Pin the orientation only when the request came from portrait on
            // a phone — that means the button, and the user wants landscape
            // however they are holding it. When we are already landscape the
            // device put us here; pinning would stop a turn back to portrait
            // from ever exiting. Tablets are excluded outright: a 10" screen
            // held upright still gives the video plenty of width, so spinning
            // the whole UI under a propped-up or docked tablet is a worse
            // answer than simply going fullscreen where it is.
            if (!isLandscape && !isTabletDevice) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Hand the orientation back to the device rather than pinning
            // portrait — pinning is what stopped rotation reaching us again.
            activity.restoreUserOrientation()
        }
    }

    fun performSeek(deltaMs: Long) {
        val newPos = (exoPlayer.currentPosition + deltaMs)
            .coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L))
        exoPlayer.seekTo(newPos)
        seekFeedbackText = if (deltaMs > 0) "+10s" else "-10s"
        seekFeedbackVisible = true
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Video surface + overlays, reused by all three layouts. [boxModifier]
    // carries the sizing (weight / fixed / fill) from the call site.
    @Composable
    fun VideoArea(boxModifier: Modifier) {
        Box(
            modifier = boxModifier
                .background(Color.Black)
                .onSizeChanged { containerWidth = it.width }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { overlayVisible = !overlayVisible },
                        onDoubleTap = { offset ->
                            val seekMs = if (offset.x > containerWidth / 2) 10_000L else -10_000L
                            performSeek(seekMs)
                        },
                    )
                },
        ) {
            // Video surface — scale-mode-aware
            val videoModifier = when (videoScale) {
                VideoScale.RATIO_16_9 -> Modifier.fillMaxWidth().aspectRatio(16f / 9f).align(Alignment.Center)
                VideoScale.RATIO_21_9 -> Modifier.fillMaxWidth().aspectRatio(21f / 9f).align(Alignment.Center)
                else -> Modifier.fillMaxSize()
            }
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                update = { view ->
                    view.resizeMode = when (videoScale) {
                        VideoScale.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        VideoScale.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        VideoScale.STRETCH -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        VideoScale.RATIO_16_9, VideoScale.RATIO_21_9 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                    view.applySubtitleStyle(subtitleStyle)
                },
                modifier = videoModifier,
            )

            // Seek feedback (no fade — Compose AnimatedVisibility scope-resolution
            // gets confused inside Column>Box; conditional render is clearer here)
            if (seekFeedbackVisible) {
                Text(
                    text = seekFeedbackText,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.White,
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                )
            }

            // Overlay controls — hidden in PiP and when there's a fatal error
            if (playbackError == null && overlayVisible && !isInPipMode) {
                VodPlayerOverlay(
                        state = state,
                        isFullscreen = isFullscreen,
                        isPlaying = isPlaying,
                        positionMs = positionMs,
                        durationMs = durationMs,
                        seekFraction = seekFraction,
                        playbackSpeed = playbackSpeed,
                        onBack = onBack,
                        onToggleFavorite = onToggleFavorite,
                        onPlayPause = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            isPlaying = exoPlayer.isPlaying
                        },
                        onRewind = { performSeek(-10_000L) },
                        onForward = { performSeek(10_000L) },
                        // Reuses the existing episode-switch callback already
                        // wired by the parent — saves the current position and
                        // loads the new episode's stream.
                        onSkipNext = state.nextEpisode?.let { next ->
                            { onSelectEpisode(next.id) }
                        },
                        onSeek = { fraction ->
                            isSeeking = true
                            seekFraction = fraction
                        },
                        onSeekFinished = {
                            val targetMs = (seekFraction * durationMs).toLong()
                            exoPlayer.seekTo(targetMs)
                            positionMs = targetMs
                            isSeeking = false
                        },
                        onToggleFullscreen = { isFullscreen = !isFullscreen },
                        onOpenSheet = { openSheet = it },
                    )
            }

            // Buffering spinner — driven by the listener above, not by reading
            // the player directly.
            if (isBuffering && playbackError == null) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 1.5.dp,
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                )
            }

            // Error overlay
            if (playbackError != null) {
                VodPlayerErrorOverlay(
                    errorCode = playbackError,
                    title = state.title,
                    onBack = onBack,
                    onToggleFullscreen = { isFullscreen = !isFullscreen },
                    isFullscreen = isFullscreen,
                )
            }
        }
    }

    // Context-aware info panel (movie meta / episode list). [panelModifier]
    // carries the sizing; [statusBarPadding] is needed when the panel reaches
    // the top of the screen (side-by-side landscape).
    @Composable
    fun BottomPanel(panelModifier: Modifier, statusBarPadding: Boolean) {
        Box(
            modifier = panelModifier
                .clip(SheetTopShape)
                .background(BgElev),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (statusBarPadding) Modifier.statusBarsPadding() else Modifier)
                    .navigationBarsPadding(),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(LineStrong),
                )

                if (state.isMovie) {
                    MoviePanel(state = state, onItemClick = { /* navigate to similar — needs callback from parent */ })
                } else {
                    EpisodePanel(
                        state = state,
                        onSeasonClick = { openSheet = PlayerSheet.SEASON },
                        onEpisodeClick = onSelectEpisode,
                    )
                }
            }
        }
    }

    when {
        // Fullscreen / PiP — video fills everything.
        isFullscreen || isInPipMode -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoArea(Modifier.fillMaxSize())
            }
        }

        // Tablet landscape — video on the left, info/episodes on the right.
        hasRoomForSidePanel && isLandscape -> {
            Row(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoArea(Modifier.weight(1f).fillMaxHeight())
                VerticalDivider(thickness = 1.dp, color = Line)
                BottomPanel(Modifier.width(380.dp).fillMaxHeight(), statusBarPadding = true)
            }
        }

        // Phone / tablet portrait — stacked video over the info panel.
        else -> {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoArea(Modifier.fillMaxWidth().weight(2f))
                BottomPanel(Modifier.fillMaxWidth().weight(3f), statusBarPadding = false)
            }
        }
    }

    // ── Bottom sheets ────────────────────────────────────────────────────────

    if (openSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { openSheet = null },
            sheetState = sheetState,
            containerColor = BgElev,
        ) {
            when (openSheet) {
                PlayerSheet.SPEED -> SpeedSheet(
                    currentSpeed = playbackSpeed,
                    onSelect = { speed -> onSpeedSelected(speed); openSheet = null },
                )
                PlayerSheet.TRACKS -> TracksSheet(exoPlayer = exoPlayer, onDismiss = { openSheet = null })
                PlayerSheet.SCALE -> ScaleSheet(
                    current = videoScale,
                    onSelect = { scale -> videoScale = scale; openSheet = null },
                )
                PlayerSheet.SEASON -> SeasonSheet(
                    seasons = state.availableSeasons,
                    selected = state.selectedSeason,
                    onSelect = { season -> onSelectSeason(season); openSheet = null },
                )
                PlayerSheet.EPISODES -> EpisodesSheet(
                    state = state,
                    onSelectSeason = onSelectSeason,
                    onSelectEpisode = { id -> onSelectEpisode(id); openSheet = null },
                )
                null -> {}
            }
        }
    }
}

// ── Video overlay (controls when visible) ─────────────────────────────────────

@Composable
private fun VodPlayerOverlay(
    state: VodPlayerUiState,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    seekFraction: Float,
    playbackSpeed: Float,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    /** Non-null when there's a next episode in the series — null hides the button. */
    onSkipNext: (() -> Unit)?,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenSheet: (PlayerSheet) -> Unit,
) {
    val accent = LocalAccentPalette.current
    Box(modifier = Modifier.fillMaxSize()) {
        // Scrims instead of a flat wash. A single 45%-black sheet over the
        // whole frame dulled every scene; darkening only the strips the
        // controls actually occupy keeps the picture open in the middle and is
        // what makes the overlay read as premium rather than cheap.
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)))
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent),
                    )
                )
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                    )
                )
        )

        // Top bar — back + serif title + favourite + fullscreen
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            FlatIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
                size = 24.dp,
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.01).sp,
                        color = Color.White,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!state.subtitle.isNullOrBlank()) {
                    Text(
                        text = state.subtitle.uppercase(),
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 0.06.sp,
                            color = Color.White.copy(alpha = 0.65f),
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            FlatIconButton(
                icon = if (state.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (state.isFavorite) {
                    stringResource(R.string.player_remove_favorite)
                } else {
                    stringResource(R.string.player_add_favorite)
                },
                onClick = onToggleFavorite,
                tint = if (state.isFavorite) Copper else Color.White,
            )
            FlatIconButton(
                icon = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                contentDescription = stringResource(R.string.player_fullscreen),
                onClick = onToggleFullscreen,
            )
        }

        // Centre controls — 10 s rewind / play-pause / 10 s forward.
        // Skip-next moved to the labelled row below, where "Sonraki Bölüm"
        // says what it does instead of leaving the user to decode an icon.
        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center),
        ) {
            SeekButton(
                icon = Icons.Filled.FastRewind,
                contentDescription = stringResource(R.string.player_seek_back_10),
                onClick = onRewind,
            )
            FlatIconButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) {
                    stringResource(R.string.player_pause)
                } else {
                    stringResource(R.string.action_play)
                },
                onClick = onPlayPause,
                size = 52.dp,
                touchTarget = 68.dp,
            )
            SeekButton(
                icon = Icons.Filled.FastForward,
                contentDescription = stringResource(R.string.player_seek_forward_10),
                onClick = onForward,
            )
        }

        // Bottom strip — scrub bar + action row
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Scrub bar with mono time codes
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatMs(positionMs),
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Color.White,
                    ),
                )
                Slider(
                    value = seekFraction,
                    onValueChange = onSeek,
                    onValueChangeFinished = onSeekFinished,
                    colors = SliderDefaults.colors(
                        thumbColor = accent.primary,
                        activeTrackColor = accent.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.30f),
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )
                Text(
                    text = formatMs(durationMs),
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.70f),
                    ),
                )
            }
            Spacer(Modifier.height(4.dp))
            // Labelled action row. Spread across the full width where there is
            // room — in fullscreen landscape the controls otherwise bunch up on
            // the left with a third of the bar empty. Narrow layouts (the
            // portrait inline player) scroll instead, since five labels cannot
            // fit however they are arranged.
            // How many controls this title actually shows. The row's width cap
            // is derived from it below, so the two have to be counted the same
            // way — hence the conditions mirroring the ones inside [actionRow].
            val actionCount = 3 +
                (if (!state.isMovie && state.seriesEpisodes.isNotEmpty()) 1 else 0) +
                (if (onSkipNext != null) 1 else 0)

            // [itemModifier] is how the two branches below differ: the wide one
            // hands every control an equal slice of the row, the scrolling one
            // lets each size to its own label.
            val actionRow: @Composable RowScope.(itemModifier: Modifier) -> Unit = { itemModifier ->
                PlayerActionButton(
                    icon = Icons.Filled.AspectRatio,
                    label = stringResource(R.string.player_scale),
                    onClick = { onOpenSheet(PlayerSheet.SCALE) },
                    modifier = itemModifier,
                )
                PlayerActionButton(
                    icon = Icons.Filled.Speed,
                    label = stringResource(R.string.player_speed, formatSpeed(playbackSpeed)),
                    onClick = { onOpenSheet(PlayerSheet.SPEED) },
                    modifier = itemModifier,
                    tint = if (playbackSpeed != 1f) Copper else Color.White,
                )
                if (!state.isMovie && state.seriesEpisodes.isNotEmpty()) {
                    PlayerActionButton(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = stringResource(R.string.term_episodes),
                        onClick = { onOpenSheet(PlayerSheet.EPISODES) },
                        modifier = itemModifier,
                    )
                }
                // "Seslendirme ve Alt Yazı" was too long to survive an equal
                // slice once a series adds its two extra controls, and it is
                // the audio *track* being chosen here, not dubbing specifically.
                PlayerActionButton(
                    icon = Icons.Filled.Subtitles,
                    label = stringResource(R.string.player_audio_subtitles),
                    onClick = { onOpenSheet(PlayerSheet.TRACKS) },
                    modifier = itemModifier,
                )
                // Series-only, and gone on the finale — the parent passes null
                // rather than us showing a dead control.
                if (onSkipNext != null) {
                    PlayerActionButton(
                        icon = Icons.Filled.SkipNext,
                        label = stringResource(R.string.player_next_episode),
                        onClick = onSkipNext,
                        modifier = itemModifier,
                    )
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth >= 600.dp) {
                    // Equal slices, each centred in its own slice. SpaceBetween
                    // was the obvious choice and it looks wrong here: it evens
                    // out the *gaps*, but the labels differ a lot in length, so
                    // equal gaps leave the controls themselves unevenly spaced.
                    // Giving each the same width puts their centres on a
                    // regular rhythm, which is what actually reads as aligned.
                    //
                    // The cap scales with the number of controls rather than
                    // being one fixed width. A flat cap has to serve both a
                    // film's three controls and a series' five: sized for three
                    // it starves the five, sized for five it strands the three
                    // in opposite corners of a tablet. Per-control instead, so
                    // both end up with the same room each.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .widthIn(max = MaxActionSlotWidth * actionCount)
                            .fillMaxWidth(),
                    ) {
                        actionRow(Modifier.weight(1f))
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        // No weights here — the row is wider than the screen and
                        // scrolls, so each control keeps its natural width.
                        actionRow(Modifier)
                    }
                }
            }
        }
    }
}

/** Trims "1.0" to "1x" while keeping "1.5x" readable. */
private fun formatSpeed(speed: Float): String =
    if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"

/**
 * Bare icon control. No pill, no circle, and deliberately no `Modifier.shadow`
 * either — that draws a shadow *in the given shape*, so a CircleShape shadow
 * put a dark disc behind every glyph, re-creating the very backgrounds this
 * was meant to remove. Contrast comes from the scrims instead.
 *
 * [touchTarget] stays at least 44dp even when the glyph is small, so dropping
 * the backgrounds costs nothing in tap accuracy.
 */
@Composable
private fun FlatIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color = Color.White,
    size: Dp = 22.dp,
    touchTarget: Dp = 44.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(touchTarget)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        GlyphIcon(icon = icon, contentDescription = contentDescription, tint = tint, size = size)
    }
}

/**
 * Icon with a drop shadow that follows the glyph instead of a shape behind it.
 *
 * `Modifier.shadow` and `graphicsLayer.shadowElevation` both shade an *outline*
 * — pass CircleShape and you get a disc behind the icon, which is exactly the
 * background we are trying to be rid of. Drawing the vector twice, once in
 * black and nudged down, gives a shadow in the shape of the glyph itself and
 * works on every API level (Modifier.blur is API 31+).
 */
@Composable
private fun GlyphIcon(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    size: Dp,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.Black.copy(alpha = 0.55f),
        modifier = Modifier.size(size).offset(y = 1.5.dp),
    )
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

/**
 * 10-second seek. Uses the plain double-triangle rather than Material's
 * `Replay10`/`Forward10`, whose glyphs are drawn as a ring with the number
 * inside — a circle we do not want. The interval moves to a small caption so
 * the meaning survives the change.
 */
@Composable
private fun SeekButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // Box, not Column: a Column would size itself around glyph + caption and
    // centring *that* in the row leaves the glyph sitting higher than the play
    // icon next to it. Matching the play button's box and floating the caption
    // with an offset keeps every glyph on one centre line.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        GlyphIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            size = 30.dp,
        )
        Text(
            text = "10",
            style = TextStyle(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    offset = Offset(0f, 1f),
                    blurRadius = 4f,
                ),
            ),
            modifier = Modifier.align(Alignment.Center).offset(y = 20.dp),
        )
    }
}

/** Icon + label control for the bottom row, styled after the reference layout. */
@Composable
private fun PlayerActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            GlyphIcon(icon = icon, contentDescription = null, tint = tint, size = 18.dp)
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = tint,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    offset = Offset(0f, 1f),
                    blurRadius = 5f,
                ),
            ),
            maxLines = 1,
        )
    }
}

// ── Error overlay (in video area) ─────────────────────────────────────────────

@Composable
private fun VodPlayerErrorOverlay(
    errorCode: String,
    title: String,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit,
    isFullscreen: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize().background(GencColors.Dark.bg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            FlatIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
                size = 24.dp,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    color = Color.White,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            FlatIconButton(
                icon = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                contentDescription = stringResource(R.string.player_fullscreen),
                onClick = onToggleFullscreen,
                size = 20.dp,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Danger.copy(alpha = 0.12f))
                    .border(0.5.dp, Danger.copy(alpha = 0.30f), CircleShape),
            ) {
                Icon(Icons.Default.Warning, null, tint = Danger, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.player_stream_playback_failed),
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    color = Color.White,
                    letterSpacing = (-0.01).sp,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = errorCode,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        letterSpacing = 0.04.sp,
                        color = Color.White.copy(alpha = 0.55f),
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Movie panel ──────────────────────────────────────────────────────────────

@Composable
private fun MoviePanel(
    state: VodPlayerUiState,
    onItemClick: (String) -> Unit,
) {
    val accent = LocalAccentPalette.current
    val movie = state.movie
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Title row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        lineHeight = 26.sp,
                        letterSpacing = (-0.015).sp,
                        color = TextPrimary,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                val meta = buildString {
                    movie?.year?.let { append(it) }
                    movie?.rating?.let {
                        if (isNotEmpty()) append("  ·  ")
                        append("★ %.1f".format(it))
                    }
                    movie?.durationSecs?.let {
                        if (isNotEmpty()) append("  ·  ")
                        append("${it / 60}dk")
                    }
                }
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta,
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = TextSecondary,
                        ),
                    )
                }
            }
        }

        // Genre pills
        if (movie?.genres?.isNotEmpty() == true) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            ) {
                items(movie.genres.take(6)) { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(accent.soft)
                            .border(0.5.dp, accent.mid, RoundedCornerShape(50.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = genre,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = accent.primary,
                            ),
                        )
                    }
                }
            }
        }

        // Plot
        val plot = movie?.plot
        if (!plot.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            PanelSectionTitle(stringResource(R.string.player_section_summary))
            Spacer(Modifier.height(6.dp))
            Text(
                text = plot,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextSecondary,
                ),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // Cast
        val cast = state.castWithPhotos.ifEmpty {
            movie?.cast?.map { CastMember(name = it) } ?: emptyList()
        }
        if (cast.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            PanelSectionTitle(stringResource(R.string.player_section_cast))
            Spacer(Modifier.height(10.dp))
            CastRow(cast = cast)
        }

        // Similar movies
        if (state.similarMovies.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            PanelSectionTitle(stringResource(R.string.player_section_similar_movies))
            Spacer(Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                items(state.similarMovies, key = { it.id }) { item ->
                    Column(
                        modifier = Modifier
                            .width(110.dp)
                            .clickable { onItemClick(item.id) },
                    ) {
                        Poster(
                            title = item.title,
                            posterUrl = item.posterUrl,
                            year = null,
                            width = 110.dp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = TextPrimary,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Episode panel ────────────────────────────────────────────────────────────

@Composable
private fun EpisodePanel(
    state: VodPlayerUiState,
    onSeasonClick: () -> Unit,
    onEpisodeClick: (String) -> Unit,
) {
    val accent = LocalAccentPalette.current
    val episodes = state.episodesInSelectedSeason
    val seasonLabel = state.selectedSeason?.let { stringResource(R.string.term_season_number, it) } ?: "—"
    val totalEpisodes = episodes.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Header row — series title + season picker button on the right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.series?.title ?: state.title,
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.015).sp,
                        color = TextPrimary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.player_season_episode_count, seasonLabel, totalEpisodes),
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        letterSpacing = 0.06.sp,
                        color = TextTertiary,
                    ),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            // Season picker — opens bottom sheet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(accent.soft)
                    .border(0.5.dp, accent.mid, RoundedCornerShape(50.dp))
                    .clickable(
                        enabled = state.availableSeasons.isNotEmpty(),
                        onClick = onSeasonClick,
                    )
                    .padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
            ) {
                Text(
                    text = seasonLabel,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = accent.primary,
                    ),
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.player_select_season),
                    tint = accent.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Episode list
        if (state.seriesEpisodes.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(32.dp),
            ) {
                Text(
                    text = stringResource(R.string.player_episodes_loading),
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = TextSecondary,
                    ),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(episodes, key = { it.id }) { ep ->
                    EpisodeRow(
                        episode = ep,
                        isCurrent = ep.id == state.targetId,
                        onClick = { onEpisodeClick(ep.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    episode: Episode,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    val borderColor = if (isCurrent) accent.primary else Line
    val bgColor = if (isCurrent) accent.soft else BgElev2
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(width = if (isCurrent) 1.dp else 0.5.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 10.dp),
    ) {
        // No number badge: the title already begins with the episode number,
        // and the accent background marks the one that is playing.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episodeName(episode.episode, episode.title)
                        ?.let { stringResource(R.string.term_episode_titled, episode.episode, it) }
                        ?: stringResource(R.string.term_episode_number, episode.episode),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // Runtime under the title, matching the detail screen's card.
            val sub = buildString {
                episode.durationSecs?.let { append(stringResource(R.string.unit_minutes_short, it / 60)) }
                if (isCurrent) {
                    if (isNotEmpty()) append("  ·  ")
                    append(stringResource(R.string.player_now_playing))
                }
            }
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 0.04.sp,
                        color = if (isCurrent) accent.primary else TextTertiary,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            // Shown in full here too, so the two episode lists read the same.
            // Costs vertical room in a short sheet, but the list scrolls.
            val plot = episode.plot
            if (!plot.isNullOrBlank()) {
                Text(
                    text = plot,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = TextSecondary,
                    ),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = stringResource(R.string.action_play),
            tint = if (isCurrent) accent.primary else TextTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Cast row ─────────────────────────────────────────────────────────────────

@Composable
private fun CastRow(cast: List<CastMember>) {
    val accent = LocalAccentPalette.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        items(cast.take(15), key = { it.name + (it.character ?: "") }) { member ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(72.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BgElev2)
                        .border(0.5.dp, LineStrong, CircleShape),
                ) {
                    if (!member.profileUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(member.profileUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = member.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                        )
                    } else {
                        Text(
                            text = member.name.take(2).uppercase(),
                            style = TextStyle(
                                fontFamily = InstrumentSerifFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                color = accent.primary,
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = member.name,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        color = TextPrimary,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!member.character.isNullOrBlank()) {
                    Text(
                        text = member.character,
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            color = TextTertiary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ── Common helpers ───────────────────────────────────────────────────────────

@Composable
private fun PanelSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = TextStyle(
            fontFamily = GeistMonoFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 0.10.sp,
            color = TextTertiary,
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

// ── Bottom sheets ────────────────────────────────────────────────────────────

@Composable
private fun SeasonSheet(
    seasons: List<Int>,
    selected: Int?,
    onSelect: (Int) -> Unit,
) {
    val accent = LocalAccentPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.player_season_sheet_title),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        seasons.forEach { season ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(season) }
                    .padding(vertical = 6.dp),
            ) {
                RadioButton(
                    selected = season == selected,
                    onClick = { onSelect(season) },
                    colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                )
                Text(
                    text = stringResource(R.string.term_season_number, season),
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

/**
 * "Seslendirme ve Alt Yazı" — one control in the overlay, so one sheet here.
 *
 * Tabbed rather than stacked: each list is a fixed-height LazyColumn, and two
 * of them plus headers overflow a bottom sheet on a phone held landscape,
 * which is exactly when this sheet gets used.
 */
@Composable
private fun TracksSheet(exoPlayer: ExoPlayer, onDismiss: () -> Unit) {
    var showSubtitles by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.player_audio_subtitle_sheet_title),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            SheetTab(label = stringResource(R.string.player_tab_audio), selected = !showSubtitles) { showSubtitles = false }
            SheetTab(label = stringResource(R.string.player_tab_subtitles), selected = showSubtitles) { showSubtitles = true }
        }
        if (showSubtitles) {
            SubtitleSheet(exoPlayer = exoPlayer, onDismiss = onDismiss, showHeader = false)
        } else {
            AudioSheet(exoPlayer = exoPlayer, onDismiss = onDismiss, showHeader = false)
        }
    }
}

@Composable
private fun SheetTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val accent = LocalAccentPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accent.soft else BgElev2)
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) accent.primary else Line,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (selected) accent.primary else TextSecondary,
            ),
        )
    }
}

/** Episode picker reachable from the overlay, so switching episodes no longer
 *  means leaving the player. Reuses [EpisodeRow] from the info panel. */
@Composable
private fun EpisodesSheet(
    state: VodPlayerUiState,
    onSelectSeason: (Int) -> Unit,
    onSelectEpisode: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.term_episodes),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            textAlign = TextAlign.Center,
        )
        if (state.availableSeasons.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 10.dp),
            ) {
                state.availableSeasons.forEach { season ->
                    SheetTab(
                        label = stringResource(R.string.term_season_number, season),
                        selected = season == state.selectedSeason,
                        onClick = { onSelectSeason(season) },
                    )
                }
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().heightForSheet(),
        ) {
            items(state.episodesInSelectedSeason, key = { it.id }) { ep ->
                EpisodeRow(
                    episode = ep,
                    isCurrent = ep.id == state.episode?.id,
                    onClick = { onSelectEpisode(ep.id) },
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ScaleSheet(current: VideoScale, onSelect: (VideoScale) -> Unit) {
    val accent = LocalAccentPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.player_scale_sheet_title),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        VideoScale.entries.forEach { scale ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(scale) }.padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = scale == current,
                    onClick = { onSelect(scale) },
                    colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                )
                Text(
                    text = videoScaleLabel(scale),
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun SpeedSheet(currentSpeed: Float, onSelect: (Float) -> Unit) {
    val accent = LocalAccentPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.player_speed_sheet_title),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        SPEED_PRESETS.forEach { speed ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(speed) }.padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = speed == currentSpeed,
                    onClick = { onSelect(speed) },
                    colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                )
                Text(
                    text = speedLabel(speed),
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun AudioSheet(
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit,
    /** False when hosted inside [TracksSheet], which supplies its own title. */
    showHeader: Boolean = true,
) {
    val accent = LocalAccentPalette.current
    val unknownLanguageLabel = stringResource(R.string.player_language_unknown)
    // Keyed on the locale too: a language change has to rebuild these names,
    // not leave the previous language's spellings cached.
    val displayLocale = LocalConfiguration.current.locales[0]
    val tracks = remember(exoPlayer.currentTracks, unknownLanguageLabel, displayLocale) {
        exoPlayer.currentTracks.groups
            .mapIndexedNotNull { groupIndex, group ->
                if (group.type != C.TRACK_TYPE_AUDIO) return@mapIndexedNotNull null
                (0 until group.length).map { trackIndex ->
                    val format = group.getTrackFormat(trackIndex)
                    AudioTrackInfo(
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        displayName = languageDisplay(format.language, unknownLanguageLabel, displayLocale),
                    )
                }
            }
            .flatten()
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (showHeader) Text(
            text = stringResource(R.string.player_tab_audio),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        if (tracks.isEmpty()) {
            Text(
                text = stringResource(R.string.player_no_audio_tracks),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary,
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightForSheet()) {
                items(tracks) { track ->
                    val group = exoPlayer.currentTracks.groups[track.groupIndex]
                    val isSelected = group.isTrackSelected(track.trackIndex)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                val mediaGroup = group.mediaTrackGroup
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setOverrideForType(TrackSelectionOverride(mediaGroup, listOf(track.trackIndex)))
                                    .build()
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                        )
                        Text(
                            text = track.displayName,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextPrimary,
                            ),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun SubtitleSheet(
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit,
    /** False when hosted inside [TracksSheet], which supplies its own title. */
    showHeader: Boolean = true,
) {
    val accent = LocalAccentPalette.current
    val unknownLanguageLabel = stringResource(R.string.player_language_unknown)
    // Keyed on the locale too: a language change has to rebuild these names,
    // not leave the previous language's spellings cached.
    val displayLocale = LocalConfiguration.current.locales[0]
    val tracks = remember(exoPlayer.currentTracks, unknownLanguageLabel, displayLocale) {
        exoPlayer.currentTracks.groups
            .mapIndexedNotNull { groupIndex, group ->
                if (group.type != C.TRACK_TYPE_TEXT) return@mapIndexedNotNull null
                (0 until group.length).map { trackIndex ->
                    val format = group.getTrackFormat(trackIndex)
                    SubtitleTrackInfo(
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        displayName = languageDisplay(format.language, unknownLanguageLabel, displayLocale),
                    )
                }
            }
            .flatten()
    }
    val subtitleDisabled = remember(exoPlayer.trackSelectionParameters) {
        exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (showHeader) Text(
            text = stringResource(R.string.player_subtitle_label),
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                color = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            textAlign = TextAlign.Center,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        ) {
            RadioButton(
                selected = subtitleDisabled,
                onClick = {
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                        .build()
                    onDismiss()
                },
                colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
            )
            Text(
                text = stringResource(R.string.player_subtitles_off),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (tracks.isEmpty()) {
            Text(
                text = stringResource(R.string.player_no_subtitle_tracks),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary,
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightForSheet()) {
                items(tracks) { track ->
                    val group = exoPlayer.currentTracks.groups[track.groupIndex]
                    val isSelected = !subtitleDisabled && group.isTrackSelected(track.trackIndex)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                val mediaGroup = group.mediaTrackGroup
                                exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                    .buildUpon()
                                    .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                    .setOverrideForType(TrackSelectionOverride(mediaGroup, listOf(track.trackIndex)))
                                    .build()
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = accent.primary),
                        )
                        Text(
                            text = track.displayName,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextPrimary,
                            ),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

private fun Modifier.heightForSheet(): Modifier = this.then(
    Modifier.height(280.dp)
)
