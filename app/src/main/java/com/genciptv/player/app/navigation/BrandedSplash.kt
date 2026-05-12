package com.genciptv.player.app.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.GencLogo
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Easings (matches Splash.html cubic-bezier curves) ────────────────────────

/**
 * Primary "snap-out" easing used by mark-in, rise (wordmark/tag/loading/bar),
 * and the halo expand. CSS: cubic-bezier(.2, 0, 0, 1).
 */
private val PrimaryEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Symmetric pulse easing. CSS: cubic-bezier(.4, 0, .4, 1). */
private val PulseEasing = CubicBezierEasing(0.4f, 0f, 0.4f, 1f)

/** Material-style FastOutSlowIn for the bar sweep. CSS: cubic-bezier(.4, 0, .2, 1). */
private val SweepEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

// ── Timing constants (ms — pulled from Splash.html `animation` declarations) ─

private const val MARK_DELAY = 200
private const val MARK_DUR = 720
private const val MARK_PULSE_DELAY = 900
private const val MARK_PULSE_DUR = 1_800   // full back-and-forth (RepeatMode.Reverse → 900 each way)
private const val HALO_RING1_DELAY = 200
private const val HALO_RING2_DELAY = HALO_RING1_DELAY + 1_200
private const val HALO_DUR = 2_400
private const val WORD_DELAY = 760
private const val WORD_DUR = 600
private const val TAG_DELAY = 920
private const val TAG_DUR = 540
private const val LOADING_DELAY = 1_100
private const val LOADING_DUR = 540
private const val LOADING_BLINK_DUR = 1_400
private const val BAR_DELAY = 1_280
private const val BAR_DUR = 480
private const val BAR_SWEEP_DUR = 1_600

/** Total visible time before the overlay fades out. Mirrors the CSS sequence
 *  end (~1760 ms) plus a tiny buffer so the bar's first sweep has settled. */
private const val OVERLAY_HOLD = 1_700
private const val OVERLAY_FADE = 400

// ── Animated overlay ─────────────────────────────────────────────────────────

/**
 * Splash overlay that re-creates `Splash.html`'s entry sequence in Compose:
 *
 *   t=200ms   mark fades + scales in (with overshoot at 60%)
 *   t=200ms   halo ring 1 starts looping outward
 *   t=760ms   "Genç" wordmark rises
 *   t=900ms   mark begins infinite breathing pulse
 *   t=920ms   "IPTV PLAYER" tag rises
 *   t=1100ms  "YÜKLENİYOR…" appears + blink loop
 *   t=1280ms  progress bar appears + teal sweep loop
 *   t=1400ms  halo ring 2 starts (offset for layered effect)
 *   t=1700ms  overlay starts fading; [onComplete] called when fully invisible.
 *
 * Composed above the main content so the system splash → animated overlay
 * → main app handoff is visually continuous (same icon on the same dark
 * background; only the surrounding chrome animates in).
 *
 * All durations and easings are pulled directly from the `Splash.html`
 * keyframes — see the timing constants at the top of this file.
 */
@Composable
fun AnimatedSplashOverlay(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Mark entry — opacity, vertical translate, scale (with overshoot)
    val markOpacity = remember { Animatable(0f) }
    val markTranslateY = remember { Animatable(8f) }
    val markScale = remember { Animatable(0.86f) }

    // Mark breathing pulse (infinite while overlay is up)
    val markPulse = remember { Animatable(1f) }

    // Halo rings — each animates alpha and scale on its own timeline.
    val halo1Alpha = remember { Animatable(0f) }
    val halo1Scale = remember { Animatable(0.85f) }
    val halo2Alpha = remember { Animatable(0f) }
    val halo2Scale = remember { Animatable(0.85f) }

    // Wordmark + tag + loading + bar reveals
    val wordOpacity = remember { Animatable(0f) }
    val wordTranslateY = remember { Animatable(6f) }
    val tagOpacity = remember { Animatable(0f) }
    val tagTranslateY = remember { Animatable(4f) }
    val loadingOpacity = remember { Animatable(0f) }
    val loadingBlink = remember { Animatable(1f) }
    val barOpacity = remember { Animatable(0f) }
    val barSweep = remember { Animatable(-1f) } // 0..1 → -100% .. 350% later

    // Whole-overlay alpha for the dismiss fade
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            // ── Mark entry ─────────────────────────────────────────────────────
            launch {
                delay(MARK_DELAY.toLong())
                launch {
                    markOpacity.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = (MARK_DUR * 0.6f).toInt(),
                            easing = PrimaryEasing,
                        ),
                    )
                }
                launch {
                    markTranslateY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = (MARK_DUR * 0.6f).toInt(),
                            easing = PrimaryEasing,
                        ),
                    )
                }
                launch {
                    // Overshoot to 1.04 at 60%, then settle at 1.0 at 100%
                    markScale.animateTo(
                        targetValue = 1f,
                        animationSpec = keyframes {
                            durationMillis = MARK_DUR
                            0.86f at 0 using PrimaryEasing
                            1.04f at (MARK_DUR * 0.6f).toInt() using PrimaryEasing
                            1.0f at MARK_DUR using PrimaryEasing
                        },
                    )
                }
            }

            // ── Mark pulse (infinite, kicks in after entry settles) ────────────
            launch {
                delay(MARK_PULSE_DELAY.toLong())
                markPulse.animateTo(
                    targetValue = 0.94f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = MARK_PULSE_DUR / 2,
                            easing = PulseEasing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }

            // ── Halo ring 1 (infinite) ─────────────────────────────────────────
            launch {
                delay(HALO_RING1_DELAY.toLong())
                launch {
                    halo1Alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = HALO_DUR
                                0f at 0 using PrimaryEasing
                                0.7f at (HALO_DUR * 0.2f).toInt() using PrimaryEasing
                                0f at HALO_DUR using PrimaryEasing
                            },
                        ),
                    )
                }
                launch {
                    halo1Scale.animateTo(
                        targetValue = 0.85f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = HALO_DUR
                                0.85f at 0 using PrimaryEasing
                                1.0f at (HALO_DUR * 0.2f).toInt() using PrimaryEasing
                                2.4f at HALO_DUR using PrimaryEasing
                            },
                        ),
                    )
                }
            }

            // ── Halo ring 2 (offset by 1200 ms) ────────────────────────────────
            launch {
                delay(HALO_RING2_DELAY.toLong())
                launch {
                    halo2Alpha.animateTo(
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = HALO_DUR
                                0f at 0 using PrimaryEasing
                                0.7f at (HALO_DUR * 0.2f).toInt() using PrimaryEasing
                                0f at HALO_DUR using PrimaryEasing
                            },
                        ),
                    )
                }
                launch {
                    halo2Scale.animateTo(
                        targetValue = 0.85f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = HALO_DUR
                                0.85f at 0 using PrimaryEasing
                                1.0f at (HALO_DUR * 0.2f).toInt() using PrimaryEasing
                                2.4f at HALO_DUR using PrimaryEasing
                            },
                        ),
                    )
                }
            }

            // ── Wordmark "Genç" rise ───────────────────────────────────────────
            launch {
                delay(WORD_DELAY.toLong())
                launch {
                    wordOpacity.animateTo(1f, tween(WORD_DUR, easing = PrimaryEasing))
                }
                launch {
                    wordTranslateY.animateTo(0f, tween(WORD_DUR, easing = PrimaryEasing))
                }
            }

            // ── Tag "IPTV PLAYER" rise ─────────────────────────────────────────
            launch {
                delay(TAG_DELAY.toLong())
                launch {
                    tagOpacity.animateTo(1f, tween(TAG_DUR, easing = PrimaryEasing))
                }
                launch {
                    tagTranslateY.animateTo(0f, tween(TAG_DUR, easing = PrimaryEasing))
                }
            }

            // ── "YÜKLENİYOR…" rise + infinite blink ────────────────────────────
            launch {
                delay(LOADING_DELAY.toLong())
                launch {
                    loadingOpacity.animateTo(1f, tween(LOADING_DUR, easing = PrimaryEasing))
                }
                launch {
                    // Hold a beat, then start blinking 0.55 ↔ 1.0
                    delay(LOADING_DUR.toLong())
                    loadingBlink.animateTo(
                        targetValue = 0.55f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = LOADING_BLINK_DUR / 2,
                                easing = EaseInOut,
                            ),
                            repeatMode = RepeatMode.Reverse,
                        ),
                    )
                }
            }

            // ── Bar reveal + infinite sweep ────────────────────────────────────
            launch {
                delay(BAR_DELAY.toLong())
                launch {
                    barOpacity.animateTo(1f, tween(BAR_DUR, easing = PrimaryEasing))
                }
                launch {
                    barSweep.animateTo(
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = BAR_SWEEP_DUR,
                                easing = SweepEasing,
                            ),
                        ),
                    )
                }
            }

            // ── Hold + dismiss ────────────────────────────────────────────────
            launch {
                delay(OVERLAY_HOLD.toLong())
                overlayAlpha.animateTo(0f, tween(OVERLAY_FADE, easing = PrimaryEasing))
                onComplete()
            }
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value },
    ) {
        val density = LocalDensity.current.density
        val widthPx = maxWidth.value * density
        val heightPx = maxHeight.value * density

        // Stage background — radial gradient matching CSS `.stage.dark`.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFF1F2A2C),
                            0.7f to Color(0xFF0A0D0E),
                            1f to Color(0xFF0A0D0E),
                        ),
                        center = Offset(widthPx / 2f, heightPx * 0.35f),
                        radius = (widthPx.coerceAtLeast(heightPx)) * 1.2f,
                    ),
                ),
        )

        // Soft teal sheen behind the icon (CSS .stage.dark::before).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x243FD0BD), // ~14% teal
                            Color(0x003FD0BD),
                        ),
                        center = Offset(widthPx / 2f, heightPx * 0.38f),
                        radius = widthPx.coerceAtLeast(heightPx) * 0.58f,
                    ),
                ),
        )

        // Mark size — 36vmin clamped at 200dp, matches HTML `.mark { width:36vmin; max:200px }`.
        val minDimDp: Dp = if (maxWidth < maxHeight) maxWidth else maxHeight
        val markSizeDp = (minDimDp * 0.36f).coerceAtMost(200.dp)

        // ── Halo rings ─────────────────────────────────────────────────────────
        HaloRing(
            sizeDp = markSizeDp,
            alpha = halo1Alpha.value,
            scale = halo1Scale.value,
        )
        HaloRing(
            sizeDp = markSizeDp,
            alpha = halo2Alpha.value,
            scale = halo2Scale.value,
        )

        // ── Mark + Wordmark + Tag column ──────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mark with entry transform + breathing pulse
            Box(
                modifier = Modifier
                    .size(markSizeDp)
                    .graphicsLayer {
                        alpha = markOpacity.value
                        translationY = markTranslateY.value * this.density
                        scaleX = markScale.value * markPulse.value
                        scaleY = markScale.value * markPulse.value
                    },
            ) {
                GencLogo(size = markSizeDp)
            }

            Spacer(Modifier.height(32.dp))

            // "Genç" — Instrument Serif italic, big serif headline
            Text(
                text = "Genç",
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 48.sp,
                    letterSpacing = (-0.025f).em,
                    color = Color(0xFFE8EDEC),
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = wordOpacity.value
                    translationY = wordTranslateY.value * this.density
                },
            )

            Spacer(Modifier.height(10.dp))

            // "IPTV PLAYER" — Geist Mono caps, wide tracking
            Text(
                text = "IPTV PLAYER",
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 2.6.sp,
                    color = Color(0xFF6A7472),
                ),
                modifier = Modifier.graphicsLayer {
                    alpha = tagOpacity.value
                    translationY = tagTranslateY.value * this.density
                },
            )
        }

        // ── "YÜKLENİYOR…" line near the bottom ─────────────────────────────────
        Text(
            text = "YÜKLENİYOR…",
            style = TextStyle(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 2.4.sp,
                color = Color(0xFF3FD0BD),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (maxHeight.value * 0.09f).dp)
                .graphicsLayer { alpha = loadingOpacity.value * loadingBlink.value },
        )

        // ── Progress sweep bar ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (maxHeight.value * 0.055f).dp)
                .width(120.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .graphicsLayer { alpha = barOpacity.value }
                .drawBehind {
                    // Sweep highlight: a 40%-wide gradient slug travelling
                    // from -100% to +350% of the track per cycle.
                    val sweepFrac = barSweep.value
                    val trackWidth = size.width
                    val slugWidth = trackWidth * 0.4f
                    val travel = trackWidth * 4.5f // -100% (= -trackWidth) → 350% (+ 3.5*trackWidth)
                    val slugStart = -trackWidth + sweepFrac * travel
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF3FD0BD),
                                Color.Transparent,
                            ),
                            startX = slugStart,
                            endX = slugStart + slugWidth,
                        ),
                        topLeft = Offset(slugStart, 0f),
                        size = androidx.compose.ui.geometry.Size(slugWidth, size.height),
                    )
                },
        )
    }
}

/** Single halo ring drawn as a 1dp teal stroke circle. */
@Composable
private fun HaloRing(
    sizeDp: androidx.compose.ui.unit.Dp,
    alpha: Float,
    scale: Float,
) {
    if (alpha <= 0f) return
    Box(
        modifier = Modifier
            .size(sizeDp)
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .clip(CircleShape)
            .drawBehind {
                drawCircle(
                    color = Color(0xFF3FD0BD).copy(alpha = 0.55f),
                    radius = size.minDimension / 2f - 0.5.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
    )
}

/**
 * Wrapper that mounts [AnimatedSplashOverlay] exactly once per cold start.
 * `rememberSaveable` survives configuration changes, so the splash doesn't
 * replay on rotation or theme switch.
 */
@Composable
fun BrandedSplashGate(
    content: @Composable () -> Unit,
) {
    var splashVisible by rememberSaveable { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        if (splashVisible) {
            AnimatedSplashOverlay(onComplete = { splashVisible = false })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun AnimatedSplashOverlayPreview() {
    GencIptvTheme(darkTheme = true) {
        AnimatedSplashOverlay(onComplete = {})
    }
}
