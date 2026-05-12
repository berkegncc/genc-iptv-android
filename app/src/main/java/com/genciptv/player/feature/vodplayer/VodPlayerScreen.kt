package com.genciptv.player.feature.vodplayer

import android.content.pm.ActivityInfo
import android.provider.Settings
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.genciptv.player.core.player.buildIptvDataSourceFactory
import com.genciptv.player.core.player.buildIptvMediaSource
import com.genciptv.player.core.ui.ErrorState
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.Poster
import com.genciptv.player.core.ui.applySubtitleStyle
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

private enum class PlayerSheet { SPEED, AUDIO, SUBTITLE, SCALE, SEASON }

private enum class VideoScale(val label: String) {
    ORIGINAL("Orijinal"),
    FIT_SCREEN("Ekrana Sığdır"),
    STRETCH("Gerdir"),
    RATIO_16_9("16:9"),
    RATIO_21_9("21:9"),
}

private val SPEED_PRESETS = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f)

private fun speedLabel(speed: Float): String = when (speed) {
    0.25f -> "0.25x (Çok Yavaş)"
    0.5f  -> "0.5x (Yavaş)"
    0.75f -> "0.75x"
    1f    -> "1x (Normal)"
    1.25f -> "1.25x"
    1.5f  -> "1.5x (Hızlı)"
    2f    -> "2x (Çok Hızlı)"
    3f    -> "3x"
    4f    -> "4x"
    else  -> "${speed}x"
}

private fun languageDisplay(code: String?): String {
    if (code.isNullOrBlank() || code == "und") return "Bilinmeyen"
    return runCatching {
        val loc = Locale.forLanguageTag(code)
        loc.getDisplayLanguage(Locale.forLanguageTag("tr")).replaceFirstChar { it.uppercase() }
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
    val dataSourceFactory = remember { buildIptvDataSourceFactory() }

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

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
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
            val activity = context as? ComponentActivity ?: return@onDispose
            val autoRotateOn = Settings.System.getInt(
                activity.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                1,
            ) == 1
            activity.requestedOrientation = if (autoRotateOn) {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
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
            message = "İçerik yükleniyor…",
            modifier = Modifier.fillMaxSize().background(GencColors.Dark.bg),
        )
        errorMessage != null -> ErrorState(
            title = "Oynatma Hatası",
            description = errorMessage,
            retryLabel = "Geri Dön",
            onRetry = onBack,
            modifier = Modifier.fillMaxSize(),
        )
        else -> VodPlayerContent(
            state = state,
            exoPlayer = exoPlayer,
            playbackSpeed = playbackSpeed,
            playbackError = playbackError,
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
    subtitleStyle: SubtitleStyle = SubtitleStyle.Default,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onSelectSeason: (Int) -> Unit,
    onSelectEpisode: (String) -> Unit,
) {
    val accent = LocalAccentPalette.current
    val context = LocalContext.current

    val isInPipMode by PipController.isInPipMode.collectAsState()

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

    LaunchedEffect(isFullscreen) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            WindowCompat.setDecorFitsSystemWindows(window, true)
            controller.show(WindowInsetsCompat.Type.systemBars())
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

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Video area — 40 % via weight 2/3 (fullscreen takes all) ─────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.weight(2f))
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

            // Buffering spinner
            if (exoPlayer.playbackState == ExoPlayer.STATE_BUFFERING && playbackError == null) {
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

        // ── Bottom panel — context-aware (movie or episode) ─────────────────
        if (!isFullscreen && !isInPipMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f)
                    .clip(SheetTopShape)
                    .background(BgElev),
            ) {
                Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
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
                PlayerSheet.AUDIO -> AudioSheet(exoPlayer = exoPlayer, onDismiss = { openSheet = null })
                PlayerSheet.SUBTITLE -> SubtitleSheet(exoPlayer = exoPlayer, onDismiss = { openSheet = null })
                PlayerSheet.SCALE -> ScaleSheet(
                    current = videoScale,
                    onSelect = { scale -> videoScale = scale; openSheet = null },
                )
                PlayerSheet.SEASON -> SeasonSheet(
                    seasons = state.availableSeasons,
                    selected = state.selectedSeason,
                    onSelect = { season -> onSelectSeason(season); openSheet = null },
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
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))) {
        // Top bar — back + serif title + favourite
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            GlassIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
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
            GlassIconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (state.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                    tint = if (state.isFavorite) Copper else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Centre controls — 10 s rewind / play-pause / 10 s forward
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center),
        ) {
            CenterCtrl(onClick = onRewind) {
                Icon(Icons.Filled.Replay10, "-10 saniye", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable(onClick = onPlayPause),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            CenterCtrl(onClick = onForward) {
                Icon(Icons.Filled.Forward10, "+10 saniye", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            // Skip-next is series-only — for movies and series finales the
            // callback is null, so we drop the icon entirely instead of
            // showing a disabled stub.
            if (onSkipNext != null) {
                CenterCtrl(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Sonraki bölüm",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
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
            Spacer(Modifier.height(2.dp))
            // Action row — speed (mono pill) | audio | subtitle | scale | spacer | fullscreen
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 44.dp, height = 32.dp)
                        .clickable(onClick = { onOpenSheet(PlayerSheet.SPEED) }),
                ) {
                    Text(
                        text = "${playbackSpeed}×",
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (playbackSpeed != 1f) Copper else Color.White,
                        ),
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).clickable(onClick = { onOpenSheet(PlayerSheet.AUDIO) }),
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, "Ses", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).clickable(onClick = { onOpenSheet(PlayerSheet.SUBTITLE) }),
                ) {
                    Icon(Icons.Filled.Subtitles, "Altyazı", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).clickable(onClick = { onOpenSheet(PlayerSheet.SCALE) }),
                ) {
                    Icon(Icons.Filled.AspectRatio, "Ölçek", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp).clickable(onClick = onToggleFullscreen),
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                        contentDescription = "Tam ekran",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
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

@Composable
private fun CenterCtrl(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
    ) {
        content()
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
            GlassIconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
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
            GlassIconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
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
            PanelSectionTitle("Özet")
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
            PanelSectionTitle("Oyuncular")
            Spacer(Modifier.height(10.dp))
            CastRow(cast = cast)
        }

        // Similar movies
        if (state.similarMovies.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            PanelSectionTitle("Benzer Filmler")
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
    val seasonLabel = state.selectedSeason?.let { "Sezon $it" } ?: "—"
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
                    text = "$seasonLabel  ·  $totalEpisodes BÖLÜM",
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
                    contentDescription = "Sezon seç",
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
                    text = "Bölümler yükleniyor…",
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
        // Episode number badge — accent mono
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrent) accent.primary else accent.soft),
        ) {
            Text(
                text = episode.episode.toString().padStart(2, '0'),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isCurrent) {
                        if (accent.isDark) Color(0xFF0E1213) else Color.White
                    } else accent.primary,
                ),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val sub = buildString {
                episode.durationSecs?.let { append("${it / 60} dk") }
                if (isCurrent) {
                    if (isNotEmpty()) append("  ·  ")
                    append("ŞU AN OYNUYOR")
                }
            }
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 0.06.sp,
                        color = if (isCurrent) accent.primary else TextTertiary,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Oynat",
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
            text = "Sezon",
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
                    text = "Sezon $season",
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
private fun ScaleSheet(current: VideoScale, onSelect: (VideoScale) -> Unit) {
    val accent = LocalAccentPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Görüntü Ölçeği",
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
                    text = scale.label,
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
            text = "Oynatma Hızı",
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
private fun AudioSheet(exoPlayer: ExoPlayer, onDismiss: () -> Unit) {
    val accent = LocalAccentPalette.current
    val tracks = remember(exoPlayer.currentTracks) {
        exoPlayer.currentTracks.groups
            .mapIndexedNotNull { groupIndex, group ->
                if (group.type != C.TRACK_TYPE_AUDIO) return@mapIndexedNotNull null
                (0 until group.length).map { trackIndex ->
                    val format = group.getTrackFormat(trackIndex)
                    AudioTrackInfo(
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        displayName = languageDisplay(format.language),
                    )
                }
            }
            .flatten()
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Ses",
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
                text = "Ses kanalı bulunamadı",
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
private fun SubtitleSheet(exoPlayer: ExoPlayer, onDismiss: () -> Unit) {
    val accent = LocalAccentPalette.current
    val tracks = remember(exoPlayer.currentTracks) {
        exoPlayer.currentTracks.groups
            .mapIndexedNotNull { groupIndex, group ->
                if (group.type != C.TRACK_TYPE_TEXT) return@mapIndexedNotNull null
                (0 until group.length).map { trackIndex ->
                    val format = group.getTrackFormat(trackIndex)
                    SubtitleTrackInfo(
                        groupIndex = groupIndex,
                        trackIndex = trackIndex,
                        displayName = languageDisplay(format.language),
                    )
                }
            }
            .flatten()
    }
    val subtitleDisabled = remember(exoPlayer.trackSelectionParameters) {
        exoPlayer.trackSelectionParameters.disabledTrackTypes.contains(C.TRACK_TYPE_TEXT)
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Altyazı",
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
                text = "Kapalı",
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
                text = "Altyazı bulunamadı",
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
