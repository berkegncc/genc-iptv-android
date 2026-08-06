package com.genciptv.player.app.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.genciptv.player.R

// ── Easings ──────────────────────────────────────────────────────────────────

/**
 * The stroke curve: a slow lead-in, a long confident middle, and a soft arrival.
 * Everything that draws the mark uses this, which is what makes the light read
 * as one continuous movement rather than several separate animations.
 */
private val Glide = CubicBezierEasing(0.40f, 0.02f, 0.18f, 1f)

/** Standard decelerate, used for opacity and colour arrivals. */
private val OutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

/** Long tail, used for the triangle's settle so it never looks like it snaps. */
private val OutQuint = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Symmetric, used for the ignition decaying back to its resting level. */
private val InOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

// ── Timing constants (ms) ────────────────────────────────────────────────────

// The silver arc is drawn first, the copper picks up where it ends, and only
// then does the triangle ignite — one continuous stroke rather than three
// elements fading in together.
private const val SILVER_AT = 60
private const val SILVER_DUR = 880
private const val SILVER_FADE_DUR = 200

private const val COPPER_AT = 860        // overlaps the silver's tail by 80 ms
private const val COPPER_DUR = 440
private const val COPPER_GRAD_DUR = 520
private const val COPPER_FADE_DUR = 170

private const val IGNITE_AT = 1_280
private const val IGNITE_DUR = 230
private const val IGNITE_PEAK = 0.92f
private const val DECAY_AT = IGNITE_AT + IGNITE_DUR
private const val DECAY_DUR = 490
private const val EMISSION_REST = 0.30f  // the level it holds once settled

private const val TRI_DUR = 300
private const val TRI_SCALE_DUR = 440

/** One start time per glyph of "Genç" — the word lights left to right. */
private val WORD_AT = intArrayOf(1_400, 1_442, 1_480, 1_516)
private const val WORD_DUR = 380

private const val TAG_AT = 1_580
private const val TAG_DUR = 330
private const val LOADING_AT = 1_660
private const val LOADING_DUR = 320

/** Length of the drawn sequence: the loading line is the last thing to arrive. */
private const val SEQUENCE_END = LOADING_AT + LOADING_DUR

/** A beat to read the finished lockup, then the hand-off to the app. */
private const val OVERLAY_HOLD = SEQUENCE_END + 40
private const val OVERLAY_FADE = 340

// ── Animated overlay ─────────────────────────────────────────────────────────

/**
 * Splash overlay: light travels the mark, ignites it, then spreads to the type.
 *
 *   t=60ms    silver arc draws from 12 o'clock down through 9 to 6
 *   t=860ms   copper arc picks up at 6 and carries on to 3
 *   t=1280ms  the triangle ignites — teal spikes, then settles
 *   t=1400ms  "Genç" lights glyph by glyph
 *   t=1580ms  "IPTV PLAYER" resolves
 *   t=1660ms  the loading line arrives
 *   t=2020ms  overlay fades; [onComplete] fires once it is invisible.
 *
 * Nothing fades in from nothing: the arcs and triangle are on screen from the
 * first frame as unlit rails, and only the light animates. The system splash is
 * configured with an invisible icon (res/drawable/ic_splash_invisible.xml) over
 * a flat #0E1213, so it hands over into that same darkness with nothing to cut.
 */
@Composable
fun AnimatedSplashOverlay(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // One linear clock in milliseconds drives every track. Each element reads
    // its own slice out of it, which keeps the sequence in the timing table at
    // the top of the file rather than spread across a dozen coroutines.
    val clock = remember { Animatable(0f) }

    // Whole-overlay alpha for the dismiss fade
    val overlayAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Drive the clock straight through the sequence, hold on the finished
        // lockup for a beat, then fade out and hand the screen to the app.
        clock.animateTo(
            targetValue = SEQUENCE_END.toFloat(),
            animationSpec = tween(SEQUENCE_END, easing = LinearEasing),
        )
        delay((OVERLAY_HOLD - SEQUENCE_END).toLong())
        overlayAlpha.animateTo(0f, tween(OVERLAY_FADE, easing = OutCubic))
        onComplete()
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = overlayAlpha.value },
    ) {
        // Flat, and the same value as windowSplashScreenBackground: the system
        // splash paints #0E1213 with an invisible icon, so mounting this overlay
        // changes nothing on screen. A vignette here would read as a shift of
        // shade at the hand-off, which is why the approved lockup has none.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E1213)),
        )

        // Mark size — the approved lockup proportion (~34% of the narrow edge),
        // clamped so it doesn't dominate a tablet.
        val minDimDp: Dp = if (maxWidth < maxHeight) maxWidth else maxHeight
        val markSizeDp = (minDimDp * 0.34f).coerceAtMost(190.dp)

        val t = clock.value

        // ── Mark tracks ───────────────────────────────────────────────────────
        val silverDraw = track(t, SILVER_AT, SILVER_DUR, Glide)
        val copperDraw = track(t, COPPER_AT, COPPER_DUR, Glide)
        val silverAlpha = track(t, SILVER_AT, SILVER_FADE_DUR, OutCubic)
        val copperAlpha = track(t, COPPER_AT, COPPER_FADE_DUR, OutCubic)
        val silverShift = 20f + (-4f - 20f) * silverDraw
        val copperShift = 24f + (3f - 24f) * track(t, COPPER_AT, COPPER_GRAD_DUR, Glide)

        // The teal spikes as the triangle lands, then settles to a steady glow.
        val emission = IGNITE_PEAK * track(t, IGNITE_AT, IGNITE_DUR, OutCubic) -
            (IGNITE_PEAK - EMISSION_REST) * track(t, DECAY_AT, DECAY_DUR, InOutCubic)

        // No full-screen teal wash here: the mark carries its own emission (see
        // [SplashMark]), and a second glow across the whole stage turned the
        // background visibly green — the lockup deliberately keeps the light
        // contained to the mark.

        // ── Mark + wordmark + tag ─────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            SplashMark(
                sizeDp = markSizeDp,
                silverAlpha = silverAlpha,
                silverSweep = SILVER_SWEEP * silverDraw,
                silverShift = silverShift,
                copperAlpha = copperAlpha,
                copperSweep = COPPER_SWEEP * copperDraw,
                copperShift = copperShift,
                emission = emission,
                triangleAlpha = 0.28f + 0.72f * track(t, IGNITE_AT, TRI_DUR, OutCubic),
                triangleScale = 0.972f + 0.028f * track(t, IGNITE_AT, TRI_SCALE_DUR, OutQuint),
            )

            Spacer(Modifier.height(26.dp))

            // "Genç" — the glyphs light left to right, so the word arrives as
            // light rather than as a fade.
            Text(
                text = buildAnnotatedString {
                    WORD.forEachIndexed { i, ch ->
                        val lit = track(t, WORD_AT[i], WORD_DUR, OutCubic)
                        withStyle(SpanStyle(color = lerp(WordDim, WordLit, lit))) {
                            append(ch)
                        }
                    }
                },
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 38.sp,
                    letterSpacing = WORD_TRACK,
                ),
            )

            Spacer(Modifier.height(13.dp))

            Text(
                text = "IPTV PLAYER",
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = TAG_TRACK,
                    color = lerp(TagDim, TagLit, track(t, TAG_AT, TAG_DUR, OutCubic)),
                ),
            )
        }

        Text(
            text = stringResource(R.string.splash_loading),
            style = TextStyle(
                fontFamily = GeistMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = LOAD_TRACK,
                color = lerp(LoadDim, LoadLit, track(t, LOADING_AT, LOADING_DUR, OutCubic)),
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (maxHeight.value * 0.09f).dp),
        )
    }
}

// ── Mark palette ─────────────────────────────────────────────────────────────
//
// Geometry is expressed in the source icon's 32-unit viewBox and scaled at draw
// time, so the mark stays identical to res/drawable/ic_genc_logo.xml at any size.
//
// Angles follow Compose's convention: 0° is 3 o'clock, positive is clockwise.
// Both arcs run counter-clockwise (negative sweep) to match the icon's path
// direction — silver from 12 o'clock down through 9 to 6, copper on from 6 to 3.

private const val VIEWBOX = 32f
private const val ARC_RADIUS = 10.5f
private const val ARC_STROKE = 3f
private const val SILVER_START = 270f
private const val SILVER_SWEEP = -180f
private const val COPPER_START = 90f
private const val COPPER_SWEEP = -90f

/** The rails the light travels along — present, but unlit, from the first frame. */
private val SilverRail = Color(0xFF5A6163).copy(alpha = 0.15f)
private val CopperRail = Color(0xFF6A4B36).copy(alpha = 0.17f)

/** Split per glyph so each can light on its own beat — see [WORD_AT]. */
private const val WORD = "Genç"

// Tracking from the approved lockup. These lines are centred and need no
// nudging to sit true: unlike CSS — which appends letter-spacing after the last
// glyph and so drags centred text left by half a unit — Android splits the
// spacing evenly on both sides of every glyph, leaving the box already
// balanced. Padding the start to "fix" that pushes the line right by T/2, which
// measured as a visible 2.5px drift on the tag. Don't add it back.
private val WORD_TRACK = 0.015.em
private val TAG_TRACK = 0.34.em
private val LOAD_TRACK = 0.24.em

// Lit tones, straight from the approved lockup. The wordmark's white is warm
// (#F0E9E3, not a cool grey-white) — it is what keeps the serif from reading as
// clinical next to the copper. The loading line is deliberately a muted grey
// rather than the brand teal, so nothing competes with the mark's own light.
private val WordLit = Color(0xFFF0E9E3)
private val TagLit = Color(0xFF7C8385)
private val LoadLit = Color(0xFF4C5355)

// Unlit tones, also from the lockup. These are tuned to sit level with the
// stage behind them — #141A1B against a #151B1C centre — so the type is present
// but unreadable until it lights. Lighten them and the reveal is given away a
// second early; they are not arbitrary dark greys.
private val WordDim = Color(0xFF141A1B)
private val TagDim = Color(0xFF121718)
private val LoadDim = Color(0xFF101516)

/** Eased progress of one track at time [t]; 0 before it starts, 1 once done. */
private fun track(t: Float, at: Int, dur: Int, easing: Easing): Float =
    easing.transform(((t - at) / dur.toFloat()).coerceIn(0f, 1f))

private val SilverStops = arrayOf(
    0.00f to Color(0xFF4F5557),
    0.33f to Color(0xFF787F81),
    0.47f to Color(0xFFE2E6E7),   // the specular band
    0.59f to Color(0xFF9FA5A7),
    1.00f to Color(0xFF4F5557),
)

private val CopperStops = arrayOf(
    0.00f to Color(0xFF6B3F22),
    0.40f to Color(0xFFA9713F),
    0.52f to Color(0xFFE0A878),
    0.66f to Color(0xFF96602F),
    1.00f to Color(0xFF7A4A2A),
)

/**
 * Brushed-metal gradient running along the 45° diagonal, offset by [shift]
 * viewBox units. Sliding the axis is what makes the highlight travel: the
 * geometry never moves, only the band of light crossing it.
 */
private fun diagonalBrush(u: Float, shift: Float, stops: Array<Pair<Float, Color>>): Brush =
    Brush.linearGradient(
        colorStops = stops,
        start = Offset((-4f + shift) * u, (-4f + shift) * u),
        end = Offset((36f + shift) * u, (36f + shift) * u),
    )

/**
 * The mark, drawn rather than blitted, so light can travel along it.
 *
 * The sweeps trim the arcs (0 draws nothing) while the shifts slide the
 * specular band along the diagonal, so the highlight runs ahead of the stroke
 * instead of the whole arc brightening at once.
 */
@Composable
private fun SplashMark(
    sizeDp: Dp,
    silverAlpha: Float,
    silverSweep: Float,
    silverShift: Float,
    copperAlpha: Float,
    copperSweep: Float,
    copperShift: Float,
    emission: Float,
    triangleAlpha: Float,
    triangleScale: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(sizeDp)) {
        val u = size.minDimension / VIEWBOX
        val stroke = Stroke(width = ARC_STROKE * u, cap = StrokeCap.Round)
        val radius = ARC_RADIUS * u
        val centre = Offset(16f * u, 16f * u)
        val topLeft = Offset(centre.x - radius, centre.y - radius)
        val box = Size(radius * 2f, radius * 2f)

        // Teal emission, behind everything — the mark's own light source.
        if (emission > 0.001f) {
            val glowCentre = Offset(15.9f * u, 16f * u)
            val glowRadius = 6.2f * u
            drawCircle(
                brush = Brush.radialGradient(
                    0.00f to Color(0xFF7FF0E0).copy(alpha = 0.62f),
                    0.30f to Color(0xFF3FD0BD).copy(alpha = 0.20f),
                    0.62f to Color(0xFF3FD0BD).copy(alpha = 0.04f),
                    1.00f to Color(0x003FD0BD),
                    center = glowCentre,
                    radius = glowRadius,
                ),
                radius = glowRadius,
                center = glowCentre,
                alpha = emission,
            )
        }

        // Unlit rails: the silhouette is whole from the first frame, which is
        // what lets the system splash hand over without a visible cut.
        drawArc(SilverRail, SILVER_START, SILVER_SWEEP, false, topLeft, box, style = stroke)
        drawArc(CopperRail, COPPER_START, COPPER_SWEEP, false, topLeft, box, style = stroke)

        drawMarkLight(
            u, topLeft, box, stroke, centre,
            silverAlpha, silverSweep, silverShift,
            copperAlpha, copperSweep, copperShift,
            triangleAlpha, triangleScale,
        )
    }
}

/** The lit portion of the mark, split out to keep [SplashMark] readable. */
private fun DrawScope.drawMarkLight(
    u: Float,
    topLeft: Offset,
    box: Size,
    stroke: Stroke,
    centre: Offset,
    silverAlpha: Float,
    silverSweep: Float,
    silverShift: Float,
    copperAlpha: Float,
    copperSweep: Float,
    copperShift: Float,
    triangleAlpha: Float,
    triangleScale: Float,
) {
    // A zero sweep with a round cap still paints a dot, so skip it outright.
    if (silverSweep != 0f) {
        drawArc(
            brush = diagonalBrush(u, silverShift, SilverStops),
            startAngle = SILVER_START,
            sweepAngle = silverSweep,
            useCenter = false,
            topLeft = topLeft,
            size = box,
            alpha = silverAlpha,
            style = stroke,
        )
    }
    if (copperSweep != 0f) {
        drawArc(
            brush = diagonalBrush(u, copperShift, CopperStops),
            startAngle = COPPER_START,
            sweepAngle = copperSweep,
            useCenter = false,
            topLeft = topLeft,
            size = box,
            alpha = copperAlpha,
            style = stroke,
        )
    }

    scale(triangleScale, pivot = centre) {
        drawPath(
            path = Path().apply {
                moveTo(13f * u, 11f * u)
                lineTo(21f * u, 16f * u)
                lineTo(13f * u, 21f * u)
                close()
            },
            brush = Brush.linearGradient(
                listOf(Color(0xFF5DEAD8), Color(0xFF0E8A7C)),
                start = Offset(13f * u, 11f * u),
                end = Offset(21f * u, 21f * u),
            ),
            alpha = triangleAlpha,
        )
    }
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
