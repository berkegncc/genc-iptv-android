package com.genciptv.player.feature.player

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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.genciptv.player.app.PipController
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.BgElev2
import com.genciptv.player.core.designsystem.Copper
import com.genciptv.player.core.designsystem.Danger
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.Line
import com.genciptv.player.core.designsystem.LineStrong
import com.genciptv.player.core.designsystem.Live
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.SheetTopShape
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.WindowSize
import com.genciptv.player.core.player.buildIptvDataSourceFactory
import com.genciptv.player.core.player.buildIptvMediaSource
import com.genciptv.player.core.player.swapToTsExtension
import com.genciptv.player.core.ui.CanliPill
import com.genciptv.player.core.ui.ChannelLogoMark
import com.genciptv.player.core.ui.ErrorState
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.QualityPill
import com.genciptv.player.core.ui.applySubtitleStyle
import com.genciptv.player.core.util.restoreUserOrientation
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.feature.home.model.ChannelWithProgram
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("ConstantLocale")
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

// ── Stateful screen ──────────────────────────────────────────────────────────

@Composable
fun PlayerScreen(
    channelId: String,
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    // Push the user's preferred audio language into the track selector. `null`
    // = no preference, so the stream's default language plays (ExoPlayer
    // falls back to system locale). When the user has explicitly chosen a
    // language in Player Settings, the selector locks to tracks tagged with it.
    LaunchedEffect(exoPlayer, preferredAudioLang) {
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage(preferredAudioLang)
            .build()
    }

    var playbackError by remember { mutableStateOf<String?>(null) }
    // 3-stage fallback: track the URL we're stepping through and which stage
    // we last attempted. Stages:
    //   null                       → HLS (initial), no fallback yet attempted
    //   "progressive_same"         → tried HLS, failed; tried progressive same URL
    //   "progressive_ts_variant"   → tried both above; tried progressive on .ts URL
    //   "exhausted"                → no further fallbacks possible
    var fallbackForUrl by remember { mutableStateOf<String?>(null) }
    /**
     * Mirrors the player's buffering state. ExoPlayer is not observable by
     * Compose, so reading `exoPlayer.playbackState` inline would only be
     * re-evaluated when some unrelated recomposition happened to occur.
     */
    var isBuffering by remember { mutableStateOf(false) }
    var fallbackStage by remember { mutableStateOf<String?>(null) }
    val dataSourceFactory = remember(userAgent) { buildIptvDataSourceFactory(userAgent) }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }

            override fun onPlayerError(error: PlaybackException) {
                val url = state.channel?.streamUrl
                val isParseError = error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ||
                    error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED

                if (!isParseError || url == null) {
                    playbackError = error.errorCodeName + " — " + (error.message ?: "Akış oynatılamadı")
                    return
                }

                // Reset fallback chain if the URL changed
                if (fallbackForUrl != url) {
                    fallbackForUrl = url
                    fallbackStage = null
                }

                when (fallbackStage) {
                    null -> {
                        // Stage 1: progressive with the same URL
                        fallbackStage = "progressive_same"
                        val source = buildIptvMediaSource(url, dataSourceFactory, forceProgressive = true)
                        exoPlayer.setMediaSource(source)
                        exoPlayer.prepare()
                        exoPlayer.play()
                    }
                    "progressive_same" -> {
                        // Stage 2: rewrite .m3u8 → .ts and retry as progressive.
                        // This is the IPTV-specific trick that fixes streams where
                        // the provider serves raw TS but the .m3u8 extension misroutes
                        // ExoPlayer's extractor selection.
                        val tsUrl = swapToTsExtension(url)
                        if (tsUrl != null) {
                            fallbackStage = "progressive_ts_variant"
                            val source = buildIptvMediaSource(tsUrl, dataSourceFactory, forceProgressive = true)
                            exoPlayer.setMediaSource(source)
                            exoPlayer.prepare()
                            exoPlayer.play()
                        } else {
                            fallbackStage = "exhausted"
                            playbackError = error.errorCodeName + " — " + (error.message ?: "Akış oynatılamadı")
                        }
                    }
                    else -> {
                        // Already tried everything — surface the error
                        fallbackStage = "exhausted"
                        playbackError = error.errorCodeName + " — " + (error.message ?: "Akış oynatılamadı")
                    }
                }
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
            exoPlayer.release()
        }
    }

    LaunchedEffect(state.channel?.streamUrl) {
        val url = state.channel?.streamUrl ?: return@LaunchedEffect
        playbackError = null
        fallbackForUrl = url
        fallbackStage = null
        exoPlayer.setMediaSource(buildIptvMediaSource(url, dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    LaunchedEffect(state.volume) { exoPlayer.volume = state.volume }

    when {
        state.channel == null && state.error != null -> ErrorState(
            title = "Kanal Bulunamadı",
            description = state.error,
            retryLabel = "Geri Dön",
            onRetry = onBack,
            modifier = Modifier.fillMaxSize(),
        )
        state.channel == null -> LoadingState(
            message = "Kanal yükleniyor…",
            modifier = Modifier.fillMaxSize(),
        )
        else -> PlayerContent(
            state = state,
            exoPlayer = exoPlayer,
            playbackError = playbackError,
            isBuffering = isBuffering,
            subtitleStyle = subtitleStyle,
            onBack = onBack,
            onToggleFavorite = viewModel::toggleFavorite,
            onVolumeChange = viewModel::setVolume,
            onSwitchChannel = { newId -> viewModel.switchTo(newId) },
        )
    }
}

// ── Stateless content ─────────────────────────────────────────────────────────

@Composable
fun PlayerContent(
    state: PlayerUiState,
    exoPlayer: ExoPlayer,
    playbackError: String? = null,
    /** Player is buffering; mirrored from the listener in [PlayerScreen]. */
    isBuffering: Boolean = false,
    subtitleStyle: SubtitleStyle = SubtitleStyle.Default,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSwitchChannel: (String) -> Unit,
) {
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
    //    The master-detail branch hands a fixed width to the side panel, which
    //    leaves the video almost nothing in a narrow split-screen pane.
    val isTabletDevice = configuration.smallestScreenWidthDp >= 600
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val hasRoomForSidePanel = isTabletDevice && WindowSize.isExpanded

    var isFullscreen by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var inactivityTick by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var volumeLocal by remember { mutableFloatStateOf(state.volume) }

    LaunchedEffect(state.volume) { volumeLocal = state.volume }
    LaunchedEffect(inactivityTick) {
        if (controlsVisible) {
            delay(3_000L)
            controlsVisible = false
        }
    }

    fun enterFullscreen() {
        isFullscreen = true
        val activity = context as? ComponentActivity ?: return
        // Pin the orientation only when the request came from portrait on a
        // phone — that means the fullscreen button, and the user wants
        // landscape however they are holding it. When we are already landscape
        // the device put us here, so leave the orientation free; pinning it
        // would make turning back to portrait unable to exit. Tablets are
        // excluded outright: a 10" screen held upright still gives the video
        // plenty of width, so spinning the whole UI under a propped-up or
        // docked tablet is a worse answer than simply going fullscreen where
        // it is.
        if (!isLandscape && !isTabletDevice) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun exitFullscreen() {
        isFullscreen = false
        // Hand the orientation back to the device rather than pinning portrait.
        // Pinning is what stopped rotation from ever reaching us again, so the
        // screen could only be turned once.
        (context as? ComponentActivity)?.restoreUserOrientation()
    }

    // Turning the phone is the natural way to ask for fullscreen, so follow it.
    // Phones only: on tablets landscape means the side-by-side layout, not
    // fullscreen. With auto-rotate switched off the system never reports a
    // landscape configuration, so this stays dormant — exactly the behaviour
    // wanted there, with the button still available.
    LaunchedEffect(isLandscape) {
        if (isTabletDevice) return@LaunchedEffect
        if (isLandscape) enterFullscreen() else exitFullscreen()
    }

    // Leaving the player by any route — in-app back, system back gesture,
    // navigating elsewhere — has to undo what enterFullscreen() did. Doing it
    // here rather than in the back handler is what covers the system gesture,
    // which never runs our own back code.
    DisposableEffect(Unit) {
        onDispose {
            (context as? ComponentActivity)?.restoreUserOrientation()
        }
    }

    // Back always leaves the screen. Fullscreen is a display mode, not a step
    // in the navigation history — treating it as one made the user press back
    // twice to get out of the player.
    val handleBack: () -> Unit = onBack
    val handleToggleFullscreen: () -> Unit = { if (isFullscreen) exitFullscreen() else enterFullscreen() }
    val handleTap: () -> Unit = {
        controlsVisible = !controlsVisible
        inactivityTick++
    }
    val handlePlayPause: () -> Unit = {
        isPlaying = !isPlaying
        if (isPlaying) exoPlayer.play() else exoPlayer.pause()
        inactivityTick++
    }
    val handleVolume: (Float) -> Unit = { v ->
        volumeLocal = v
        onVolumeChange(v)
    }

    when {
        // Fullscreen / PiP — video fills everything, no side panel.
        isFullscreen || isInPipMode -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                PlayerVideoArea(
                    state = state,
                    exoPlayer = exoPlayer,
                    subtitleStyle = subtitleStyle,
                    playbackError = playbackError,
                    isBuffering = isBuffering,
                    isFullscreen = isFullscreen,
                    controlsVisible = controlsVisible,
                    isInPipMode = isInPipMode,
                    isPlaying = isPlaying,
                    onTap = handleTap,
                    onBack = handleBack,
                    onToggleFullscreen = handleToggleFullscreen,
                    onPlayPause = handlePlayPause,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Tablet landscape — video on the left, channel/info panel on the right
        // (live-TV master-detail).
        hasRoomForSidePanel && isLandscape -> {
            Row(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                PlayerVideoArea(
                    state = state,
                    exoPlayer = exoPlayer,
                    subtitleStyle = subtitleStyle,
                    playbackError = playbackError,
                    isBuffering = isBuffering,
                    isFullscreen = isFullscreen,
                    controlsVisible = controlsVisible,
                    isInPipMode = isInPipMode,
                    isPlaying = isPlaying,
                    onTap = handleTap,
                    onBack = handleBack,
                    onToggleFullscreen = handleToggleFullscreen,
                    onPlayPause = handlePlayPause,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                VerticalDivider(thickness = 1.dp, color = Line)
                PlayerInfoPanel(
                    state = state,
                    exoPlayer = exoPlayer,
                    volume = volumeLocal,
                    onVolumeChange = handleVolume,
                    onToggleFavorite = onToggleFavorite,
                    onSwitchChannel = onSwitchChannel,
                    statusBarPadding = true,
                    modifier = Modifier.width(380.dp).fillMaxHeight(),
                )
            }
        }

        // Phone / tablet portrait — stacked video over the info panel.
        else -> {
            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                PlayerVideoArea(
                    state = state,
                    exoPlayer = exoPlayer,
                    subtitleStyle = subtitleStyle,
                    playbackError = playbackError,
                    isBuffering = isBuffering,
                    isFullscreen = isFullscreen,
                    controlsVisible = controlsVisible,
                    isInPipMode = isInPipMode,
                    isPlaying = isPlaying,
                    onTap = handleTap,
                    onBack = handleBack,
                    onToggleFullscreen = handleToggleFullscreen,
                    onPlayPause = handlePlayPause,
                    modifier = Modifier.fillMaxWidth().weight(2f),
                )
                PlayerInfoPanel(
                    state = state,
                    exoPlayer = exoPlayer,
                    volume = volumeLocal,
                    onVolumeChange = handleVolume,
                    onToggleFavorite = onToggleFavorite,
                    onSwitchChannel = onSwitchChannel,
                    statusBarPadding = false,
                    modifier = Modifier.fillMaxWidth().weight(3f),
                )
            }
        }
    }
}

// ── Video area + panel (extracted so both stacked & side-by-side reuse them) ─

@Composable
private fun PlayerVideoArea(
    state: PlayerUiState,
    exoPlayer: ExoPlayer,
    subtitleStyle: SubtitleStyle,
    playbackError: String?,
    isBuffering: Boolean,
    isFullscreen: Boolean,
    controlsVisible: Boolean,
    isInPipMode: Boolean,
    isPlaying: Boolean,
    onTap: () -> Unit,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VideoAreaBox(
        isFullscreen = isFullscreen,
        onTap = onTap,
        modifier = modifier,
    ) {
        // ExoPlayer surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            update = { view -> view.applySubtitleStyle(subtitleStyle) },
            modifier = Modifier.fillMaxSize(),
        )

        // Live streams stall often — without this the picture just freezes
        // with no sign the app is still working.
        if (isBuffering && playbackError == null && !isInPipMode) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 1.5.dp,
                modifier = Modifier.size(32.dp).align(Alignment.Center),
            )
        }

        if (playbackError != null) {
            ErrorOverlay(
                errorCode = playbackError,
                channelName = state.channel?.name ?: "",
                onBack = onBack,
                onToggleFullscreen = onToggleFullscreen,
                isFullscreen = isFullscreen,
            )
        } else {
            PlayerOverlay(
                visible = controlsVisible && !isInPipMode,
                state = state,
                isFullscreen = isFullscreen,
                isPlaying = isPlaying,
                onBack = onBack,
                onToggleFullscreen = onToggleFullscreen,
                onPlayPause = onPlayPause,
            )
        }
    }
}

@Composable
private fun PlayerInfoPanel(
    state: PlayerUiState,
    exoPlayer: ExoPlayer,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onSwitchChannel: (String) -> Unit,
    statusBarPadding: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current
    Box(
        modifier = modifier
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

            // Channel header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                ChannelLogoMark(
                    name = state.channel?.name ?: "",
                    logoUrl = state.channel?.logoUrl,
                    size = 44.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.channel?.name ?: "",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    val cat = buildCatTimeString(state.channel?.groupTitle, state.currentProgram)
                    Text(
                        text = cat.ifBlank { "Yayın bilgisi yok" }.uppercase(),
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 0.06.sp,
                            color = TextTertiary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (state.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                        tint = if (state.isFavorite) Copper else TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = {}, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Daha fazla",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Quality / Audio / Subtitle pill row
            TrackPillsRow(
                exoPlayer = exoPlayer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Volume slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = accent.primary,
                        activeTrackColor = accent.primary,
                        inactiveTrackColor = BgElev2,
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }

            if (state.upcomingOtherChannels.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                ) {
                    Text(
                        text = "Şu Anda Diğer Kanallarda",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            letterSpacing = (-0.005).sp,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "SURF",
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            letterSpacing = 0.08.sp,
                            color = TextTertiary,
                        ),
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    items(
                        items = state.upcomingOtherChannels,
                        key = { it.channel.id },
                    ) { item ->
                        OtherChannelRow(
                            item = item,
                            onClick = { onSwitchChannel(item.channel.id) },
                        )
                    }
                }
            }
        }
    }
}

// ── Video area wrapper ───────────────────────────────────────────────────────

@Composable
private fun VideoAreaBox(
    isFullscreen: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            },
    ) {
        content()
    }
}

// ── Player overlay (normal state) ────────────────────────────────────────────

@Composable
private fun PlayerOverlay(
    visible: Boolean,
    state: PlayerUiState,
    isFullscreen: Boolean,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onPlayPause: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.62f), Color.Transparent),
                        ),
                    )
            )

            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .align(Alignment.TopStart),
            ) {
                GlassIconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.channel?.name ?: "",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.currentProgram?.let { prog ->
                        Text(
                            text = prog.title,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.70f),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                CanliPill()
                Spacer(Modifier.width(8.dp))
                GlassIconButton(onClick = onToggleFullscreen) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = if (isFullscreen) "Küçük ekran" else "Tam ekran",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Live streams keep only play/pause; seeking belongs to the VOD player.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onPlayPause),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }

            // Bottom: thin live progress bar (current EPG program)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
            ) {
                state.currentProgram?.let { prog ->
                    val now = System.currentTimeMillis()
                    val duration = (prog.stopMillis - prog.startMillis).coerceAtLeast(1L)
                    val elapsed = (now - prog.startMillis).coerceAtLeast(0L)
                    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(Live),
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.40f))
            .clickable(onClick = onClick),
    ) {
        content()
    }
}

// ── Error overlay ────────────────────────────────────────────────────────────

@Composable
private fun ErrorOverlay(
    errorCode: String,
    channelName: String,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit,
    isFullscreen: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0E1213))) {
        // Top bar same as normal player
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            GlassIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = channelName,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            GlassIconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Center: warning icon + serif title + mono code pill + helper text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Danger.copy(alpha = 0.12f))
                    .border(width = 0.5.dp, color = Danger.copy(alpha = 0.30f), shape = CircleShape),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Akış oynatılamadı",
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
                    .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp))
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
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Aşağıdaki listeden başka bir kanal seçebilir veya bağlantınızı kontrol edebilirsiniz.",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color.White.copy(alpha = 0.60f),
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Track pills row (Otomatik / Varsayılan / Altyazı) ────────────────────────

@Composable
private fun TrackPillsRow(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current
    val currentTracks = exoPlayer.currentTracks
    val videoGroups = currentTracks.groups.filter {
        it.type == androidx.media3.common.C.TRACK_TYPE_VIDEO
    }
    val audioGroups = currentTracks.groups.filter {
        it.type == androidx.media3.common.C.TRACK_TYPE_AUDIO
    }
    val subtitleGroups = currentTracks.groups.filter {
        it.type == androidx.media3.common.C.TRACK_TYPE_TEXT
    }

    var activeQualityIndex by remember { mutableIntStateOf(0) }
    var activeAudioIndex by remember { mutableIntStateOf(0) }
    var showSubtitleSheet by remember { mutableStateOf(false) }

    val qualityLabels: List<String> = buildList {
        add("Otomatik")
        videoGroups.forEach { group ->
            for (tIdx in 0 until group.length) {
                val format = group.getTrackFormat(tIdx)
                val label = if (format.height > 0) "${format.height}p" else "Video"
                if (!contains(label)) add(label)
            }
        }
    }
    val audioLabels: List<String> = if (audioGroups.isEmpty()) {
        listOf("Varsayılan")
    } else {
        audioGroups.mapIndexed { gIdx, group ->
            val lang = group.getTrackFormat(0).language
            if (!lang.isNullOrBlank()) {
                runCatching {
                    Locale.forLanguageTag(lang).displayLanguage
                        .replaceFirstChar { it.uppercase() }
                        .ifBlank { "Ses ${gIdx + 1}" }
                }.getOrDefault("Ses ${gIdx + 1}")
            } else "Ses ${gIdx + 1}"
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        // Quality pills
        qualityLabels.forEachIndexed { idx, label ->
            FlatTrackPill(
                label = label,
                isActive = idx == activeQualityIndex,
                onClick = {
                    activeQualityIndex = idx
                    if (idx == 0) {
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon().clearVideoSizeConstraints().build()
                    } else if (videoGroups.isNotEmpty()) {
                        val gIdx = (idx - 1).coerceIn(0, videoGroups.lastIndex.coerceAtLeast(0))
                        val override = androidx.media3.common.TrackSelectionOverride(
                            videoGroups[gIdx].mediaTrackGroup, 0
                        )
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon().setOverrideForType(override).build()
                    }
                },
            )
        }
        // Audio pills
        audioLabels.forEachIndexed { idx, label ->
            FlatTrackPill(
                label = label,
                leadingIcon = Icons.AutoMirrored.Filled.VolumeUp,
                isActive = idx == activeAudioIndex,
                onClick = {
                    activeAudioIndex = idx
                    if (audioGroups.isNotEmpty()) {
                        val group = audioGroups.getOrNull(idx) ?: audioGroups.first()
                        val override = androidx.media3.common.TrackSelectionOverride(group.mediaTrackGroup, 0)
                        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                            .buildUpon().setOverrideForType(override).build()
                    }
                },
            )
        }
        // Subtitle pill
        FlatTrackPill(
            label = "Altyazı",
            leadingIcon = Icons.Default.Subtitles,
            isActive = showSubtitleSheet,
            onClick = { showSubtitleSheet = !showSubtitleSheet },
        )
    }
}

@Composable
private fun FlatTrackPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val accent = LocalAccentPalette.current
    val shape = RoundedCornerShape(6.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(if (isActive) accent.soft else BgElev2)
            .border(
                width = 0.5.dp,
                color = if (isActive) accent.mid else Line,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isActive) accent.primary else TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp,
                color = if (isActive) accent.primary else TextSecondary,
            ),
        )
    }
}

// ── Other channel row ────────────────────────────────────────────────────────

@Composable
private fun OtherChannelRow(
    item: ChannelWithProgram,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        ChannelLogoMark(
            name = item.channel.name,
            logoUrl = item.channel.logoUrl,
            size = 36.dp,
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.channel.name,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextPrimary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Live),
                )
            }
            val program = item.currentProgram?.title ?: "Yayın bilgisi yok"
            Text(
                text = program,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = TextSecondary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun buildCatTimeString(groupTitle: String?, program: Program?): String {
    val parts = mutableListOf<String>()
    groupTitle?.let { if (it.isNotBlank()) parts.add(it) }
    program?.let {
        val start = timeFormat.format(Date(it.startMillis))
        val end = timeFormat.format(Date(it.stopMillis))
        parts.add("$start–$end")
    }
    return parts.joinToString(" · ")
}

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewChannel = Channel(
    id = "1:sports1",
    playlistId = 1L,
    name = "beIN Sports 1",
    streamUrl = "https://example.com/stream.m3u8",
    groupTitle = "Spor",
    isHd = true,
)

private val previewProgram = Program(
    channelEpgId = "beinsports1",
    playlistId = 1L,
    title = "Galatasaray — Fenerbahçe",
    startMillis = System.currentTimeMillis() - 3_600_000L,
    stopMillis = System.currentTimeMillis() + 3_600_000L,
)

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF0E1213)
@Composable
private fun PlayerContentDarkPreview() {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    GencIptvTheme(darkTheme = true) {
        PlayerContent(
            state = PlayerUiState(
                channel = previewChannel,
                currentProgram = previewProgram,
                isFavorite = true,
                isPlaying = true,
                volume = 0.62f,
                upcomingOtherChannels = listOf(
                    ChannelWithProgram(
                        channel = Channel(id = "1:2", playlistId = 1L, name = "TRT 1", streamUrl = ""),
                        currentProgram = null, progressFraction = 0f,
                    ),
                    ChannelWithProgram(
                        channel = Channel(id = "1:3", playlistId = 1L, name = "FOX TV", streamUrl = ""),
                        currentProgram = null, progressFraction = 0f,
                    ),
                ),
            ),
            exoPlayer = exoPlayer,
            onBack = {}, onToggleFavorite = {},
            onVolumeChange = {}, onSwitchChannel = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun PlayerContentLightPreview() {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    GencIptvTheme(darkTheme = false) {
        PlayerContent(
            state = PlayerUiState(
                channel = previewChannel,
                currentProgram = previewProgram,
                upcomingOtherChannels = emptyList(),
            ),
            exoPlayer = exoPlayer,
            onBack = {}, onToggleFavorite = {},
            onVolumeChange = {}, onSwitchChannel = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFF0E1213)
@Composable
private fun PlayerContentErrorPreview() {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }
    GencIptvTheme(darkTheme = true) {
        PlayerContent(
            state = PlayerUiState(
                channel = previewChannel,
                currentProgram = previewProgram,
                upcomingOtherChannels = emptyList(),
            ),
            exoPlayer = exoPlayer,
            playbackError = "ERROR_CODE_PARSING_MANIFEST_MALFORMED",
            onBack = {}, onToggleFavorite = {},
            onVolumeChange = {}, onSwitchChannel = {},
        )
    }
}
