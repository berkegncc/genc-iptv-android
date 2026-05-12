package com.genciptv.player.feature.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.Surface2
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.data.model.Program
import com.genciptv.player.feature.guide.model.DayOption
import com.genciptv.player.feature.guide.model.EpgGridRow
import kotlinx.coroutines.launch

// ── Grid constants ────────────────────────────────────────────────────────────

private val HOUR_WIDTH_DP = 70.dp
private val TOTAL_GRID_WIDTH_DP = HOUR_WIDTH_DP * 24  // 1680dp
private val LOGO_COL_WIDTH_DP = 56.dp
private val ROW_HEIGHT_DP = 60.dp
private val MIN_BLOCK_WIDTH_DP = 40.dp

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun GuideScreen(
    onNavigateToPlayer: (channelId: String) -> Unit,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVod: (kind: String) -> Unit,
    viewModel: GuideViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    GuideContent(
        state = state,
        onDaySelected = viewModel::selectDay,
        onNavigateToPlayer = onNavigateToPlayer,
        onResyncEpg = viewModel::resyncEpg,
        onNavItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> onNavigateToHome()
                GencNavItem.CHANNELS -> onNavigateToChannels()
                GencNavItem.MOVIES   -> onNavigateToVod("MOVIE")
                GencNavItem.SERIES   -> onNavigateToVod("SERIES")
                GencNavItem.PROFILE  -> onNavigateToProfile()
            }
        },
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideContent(
    state: GuideUiState,
    onDaySelected: (Int) -> Unit,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onResyncEpg: () -> Unit,
    onNavItemClick: (GencNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary
    val accentSoft = LocalAccentPalette.current.soft
    val accentMid = LocalAccentPalette.current.mid

    // Shared horizontal scroll state — ruler + all program rows scroll together
    val sharedHScroll = rememberScrollState()

    // Program detail bottom sheet
    var selectedProgram by remember { mutableStateOf<Pair<Program, String>?>(null) } // program + channelId
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    GencAdaptiveScaffold(
        current = null, // Guide is under Profile; no tab is "active"
        onItemClick = onNavItemClick,
        containerColor = Bg,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ── A) Header ─────────────────────────────────────────────────────
            GuideHeader(
                selectedDateMillis = state.selectedDateMillis,
                accent = accent,
                accentSoft = accentSoft,
            )

            // ── B) 7-Day Selector ─────────────────────────────────────────────
            DaySelector(
                days = state.days,
                selectedIndex = state.selectedDayIndex,
                onDaySelected = onDaySelected,
                accent = accent,
                accentMid = accentMid,
            )

            // ── Main body ─────────────────────────────────────────────────────
            when {
                state.isLoading -> {
                    LoadingState(
                        message = "Program rehberi yükleniyor\u2026",
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                !state.hasAnyEpgData -> {
                    EmptyState(
                        icon = "\uD83D\uDCC5",
                        title = "Program rehberi boş",
                        description = "Bu playlist için EPG verisi bulunmuyor. " +
                            "Provider'ın EPG sağlamıyor olabilir, ya da playlist ilk kez " +
                            "senkronize edildiğinde EPG henüz inmedi. " +
                            "İleriki sürümlerde otomatik EPG senkronizasyonu eklenecek.",
                        actionLabel = "Playlist'i Yenile",
                        onAction = onResyncEpg,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    // ── C) Time Ruler (sticky, outside LazyColumn) ────────────
                    val now = System.currentTimeMillis()
                    val dayStart = state.selectedDateMillis
                    val currentHour = ((now - dayStart) / 3_600_000L).toInt().coerceIn(0, 23)
                    val isToday = state.selectedDayIndex == 1

                    TimeRuler(
                        currentHour = if (isToday) currentHour else -1,
                        sharedScroll = sharedHScroll,
                        accent = accent,
                    )

                    // ── D) Current Playing Card ───────────────────────────────
                    val featured = state.featuredNow
                    if (featured != null) {
                        val currentProgram = featured.programs.firstOrNull { p ->
                            p.startMillis <= now && p.stopMillis > now
                        }
                        if (currentProgram != null) {
                            CurrentPlayingCard(
                                row = featured,
                                program = currentProgram,
                                nowMillis = now,
                                accent = accent,
                                accentSoft = accentSoft,
                                accentMid = accentMid,
                                onPlayClick = { onNavigateToPlayer(featured.channelId) },
                            )
                        }
                    }

                    // ── E) EPG Rows ───────────────────────────────────────────
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        items(
                            items = state.rows,
                            key = { it.channelId },
                        ) { row ->
                            EpgChannelRow(
                                row = row,
                                dayStartMillis = dayStart,
                                nowMillis = now,
                                sharedScroll = sharedHScroll,
                                accent = accent,
                                accentSoft = accentSoft,
                                accentMid = accentMid,
                                onProgramClick = { program ->
                                    selectedProgram = program to row.channelId
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // ── F) Program Detail Bottom Sheet ────────────────────────────────────────
    selectedProgram?.let { (program, channelId) ->
        ModalBottomSheet(
            onDismissRequest = { selectedProgram = null },
            sheetState = sheetState,
            containerColor = Surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            ProgramDetailSheet(
                program = program,
                accent = accent,
                onWatchClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedProgram = null
                        onNavigateToPlayer(channelId)
                    }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        selectedProgram = null
                    }
                },
            )
        }
    }
}

// ── A) Header ─────────────────────────────────────────────────────────────────

@Composable
private fun GuideHeader(
    selectedDateMillis: Long,
    accent: Color,
    accentSoft: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "\uD83D\uDCC5 Program Rehberi",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                    ),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                // Date pill
                val dateText = if (selectedDateMillis > 0L) {
                    GuideViewModel.formatDatePill(selectedDateMillis)
                } else {
                    GuideViewModel.formatDatePill(System.currentTimeMillis())
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accentSoft)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                    )
                }
            }
            // 1dp bottom border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )
        }
    }
}

// ── B) 7-Day Selector ─────────────────────────────────────────────────────────

@Composable
private fun DaySelector(
    days: List<DayOption>,
    selectedIndex: Int,
    onDaySelected: (Int) -> Unit,
    accent: Color,
    accentMid: Color,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(days.size) { index ->
            val day = days[index]
            val isSelected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) accent else Surface)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) accent else Border,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onDaySelected(index) }
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = day.dayLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                        ),
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextTertiary,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = day.dayNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                        ),
                        color = if (isSelected) Color.White else TextPrimary,
                    )
                }
            }
        }
    }
}

// ── C) Time Ruler ─────────────────────────────────────────────────────────────

@Composable
private fun TimeRuler(
    currentHour: Int, // -1 if not today
    sharedScroll: androidx.compose.foundation.ScrollState,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(sharedScroll)
                .padding(vertical = 8.dp)
        ) {
            for (hour in 0..23) {
                val isCurrentHour = hour == currentHour
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.width(HOUR_WIDTH_DP),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isCurrentHour) {
                            Text(
                                text = "\u25BC",
                                fontSize = 8.sp,
                                color = accent,
                            )
                        } else {
                            Spacer(Modifier.height(11.dp))
                        }
                        Text(
                            text = "%02d:00".format(hour),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isCurrentHour) accent else TextSecondary,
                        )
                    }
                }
            }
        }
        // 1dp bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
    }
}

// ── D) Current Playing Card ───────────────────────────────────────────────────

@Composable
private fun CurrentPlayingCard(
    row: EpgGridRow,
    program: Program,
    nowMillis: Long,
    accent: Color,
    accentSoft: Color,
    accentMid: Color,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = GuideViewModel.programProgress(program, nowMillis)
    val durationMin = ((program.stopMillis - program.startMillis) / 60_000L).toInt()

    Surface(
        color = Surface,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(width = 1.5.dp, color = Border, shape = RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Logo
            ChannelLogoBox(
                logoUrl = row.logoUrl,
                size = 42.dp,
            )

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.channelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = program.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = Border,
                )
            }

            Spacer(Modifier.width(12.dp))

            // Play button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent)
                    .clickable(onClick = onPlayClick)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "İzle",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ── E) EPG Channel Row ────────────────────────────────────────────────────────

@Composable
private fun EpgChannelRow(
    row: EpgGridRow,
    dayStartMillis: Long,
    nowMillis: Long,
    sharedScroll: androidx.compose.foundation.ScrollState,
    accent: Color,
    accentSoft: Color,
    accentMid: Color,
    onProgramClick: (Program) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ROW_HEIGHT_DP)
        ) {
            // Logo column (fixed 56dp, left-anchored)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(LOGO_COL_WIDTH_DP)
                    .height(ROW_HEIGHT_DP)
                    .background(Surface)
                    .border(width = 1.dp, color = Border)
            ) {
                ChannelLogoBox(
                    logoUrl = row.logoUrl,
                    size = 40.dp,
                )
            }

            // Program blocks (horizontally scrolling, shared scroll state)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(ROW_HEIGHT_DP)
                    .horizontalScroll(sharedScroll)
            ) {
                // 1680dp wide canvas for absolute positioning
                Box(
                    modifier = Modifier
                        .width(TOTAL_GRID_WIDTH_DP)
                        .height(ROW_HEIGHT_DP)
                        .background(Bg)
                ) {
                    row.programs.forEach { program ->
                        val startFraction = (program.startMillis - dayStartMillis).toFloat() / 3_600_000f
                        val durationFraction = (program.stopMillis - program.startMillis).toFloat() / 3_600_000f
                        val xOffset = (startFraction * HOUR_WIDTH_DP.value).dp
                        val rawWidth = (durationFraction * HOUR_WIDTH_DP.value).dp
                        val blockWidth = rawWidth.coerceAtLeast(MIN_BLOCK_WIDTH_DP)

                        val blockState = when {
                            nowMillis >= program.stopMillis -> ProgramBlockState.PAST
                            nowMillis >= program.startMillis && nowMillis < program.stopMillis -> ProgramBlockState.NOW
                            else -> ProgramBlockState.FUTURE
                        }

                        ProgramBlock(
                            program = program,
                            state = blockState,
                            width = blockWidth,
                            accent = accent,
                            accentSoft = accentSoft,
                            accentMid = accentMid,
                            onClick = { onProgramClick(program) },
                            modifier = Modifier.offset(x = xOffset),
                        )
                    }
                }
            }
        }
        // 1dp row separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )
    }
}

// ── Program block state ───────────────────────────────────────────────────────

private enum class ProgramBlockState { PAST, NOW, FUTURE }

@Composable
private fun ProgramBlock(
    program: Program,
    state: ProgramBlockState,
    width: androidx.compose.ui.unit.Dp,
    accent: Color,
    accentSoft: Color,
    accentMid: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (state) {
        ProgramBlockState.PAST   -> Surface2
        ProgramBlockState.NOW    -> accentSoft
        ProgramBlockState.FUTURE -> Surface
    }
    val borderColor = when (state) {
        ProgramBlockState.PAST   -> Border
        ProgramBlockState.NOW    -> accentMid
        ProgramBlockState.FUTURE -> Border
    }
    val textColor = when (state) {
        ProgramBlockState.PAST   -> TextTertiary
        ProgramBlockState.NOW    -> accent
        ProgramBlockState.FUTURE -> TextPrimary
    }
    val contentAlpha = if (state == ProgramBlockState.PAST) 0.6f else 1f
    val startHour = (program.startMillis / 3_600_000L % 24).toInt()
    val startMin = (program.startMillis / 60_000L % 60).toInt()
    val timeLabel = "%02d:%02d".format(startHour, startMin)

    Column(
        modifier = modifier
            .width(width)
            .height(ROW_HEIGHT_DP)
            .clip(RoundedCornerShape(0.dp))
            .background(bgColor.copy(alpha = contentAlpha))
            .border(width = 1.5.dp, color = borderColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        // "Now" top accent indicator bar
        if (state == ProgramBlockState.NOW) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(accentMid)
            )
            Spacer(Modifier.height(2.dp))
        }
        Text(
            text = program.title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
            ),
            color = textColor.copy(alpha = contentAlpha),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (state == ProgramBlockState.NOW) "$timeLabel · şimdi" else timeLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextTertiary.copy(alpha = contentAlpha),
            maxLines = 1,
        )
    }
}

// ── Channel Logo Box ──────────────────────────────────────────────────────────

private val LOGO_GRADIENT = Brush.linearGradient(
    listOf(Color(0xFF5B5FEF), Color(0xFF8B6FEF))
)

@Composable
private fun ChannelLogoBox(
    logoUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    if (!logoUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(Surface2),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(LOGO_GRADIENT),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TV",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                ),
                fontSize = 10.sp,
            )
        }
    }
}

// ── F) Program Detail Bottom Sheet ───────────────────────────────────────────

@Composable
private fun ProgramDetailSheet(
    program: Program,
    accent: Color,
    onWatchClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startHour = (program.startMillis / 3_600_000L % 24).toInt()
    val startMin = (program.startMillis / 60_000L % 60).toInt()
    val stopHour = (program.stopMillis / 3_600_000L % 24).toInt()
    val stopMin = (program.stopMillis / 60_000L % 60).toInt()
    val durationMin = ((program.stopMillis - program.startMillis) / 60_000L).toInt()
    val timeRange = "%02d:%02d - %02d:%02d · %d dk".format(startHour, startMin, stopHour, stopMin, durationMin)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Title
        Text(
            text = program.title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
        )
        Spacer(Modifier.height(6.dp))

        // Time range
        Text(
            text = timeRange,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )

        // Category pill
        if (!program.category.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Border)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = program.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
        }

        // Description
        if (!program.description.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = program.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }

        Spacer(Modifier.height(20.dp))

        // Watch button
        Button(
            onClick = onWatchClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "\u0130zle",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA, name = "Guide — With EPG data")
@Composable
private fun GuideContentPreview() {
    GencIptvTheme {
        val now = System.currentTimeMillis()
        val dayStart = GuideViewModel.midnightMillis(now, 0)

        val fakePrograms1 = listOf(
            Program(1L, "ch1", 1L, "Sabah Haberleri", null, dayStart + 6 * 3600_000L, dayStart + 7 * 3600_000L),
            Program(2L, "ch1", 1L, "Ana Haber Bülteni", "Günün önemli haberleri", dayStart + 19 * 3600_000L, dayStart + 21 * 3600_000L),
            Program(3L, "ch1", 1L, "Belgesel: Doğa", null, dayStart + 21 * 3600_000L, dayStart + 22 * 3600_000L),
        )
        val fakePrograms2 = listOf(
            Program(4L, "ch2", 1L, "Dizimiz", null, dayStart + 20 * 3600_000L, dayStart + 22 * 3600_000L),
            Program(5L, "ch2", 1L, "Spor Haberleri", null, dayStart + 22 * 3600_000L, dayStart + 23 * 3600_000L),
        )

        val fakeRows = listOf(
            EpgGridRow("1:101", "TRT 1", null, fakePrograms1),
            EpgGridRow("1:102", "Star TV", null, fakePrograms2),
        )

        val fakeDays = (-1..5).map { offset ->
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = dayStart + offset * 24 * 3600_000L
            val dom = cal.get(java.util.Calendar.DAY_OF_MONTH)
            val label = when (offset) { -1 -> "Dün"; 0 -> "Bugün"; 1 -> "Yarın"; else -> "+" }
            DayOption(dayStart + offset * 24 * 3600_000L, label, dom.toString())
        }

        GuideContent(
            state = GuideUiState(
                days = fakeDays,
                selectedDayIndex = 1,
                selectedDateMillis = dayStart,
                rows = fakeRows,
                featuredNow = fakeRows.first(),
                hasAnyEpgData = true,
                isLoading = false,
            ),
            onDaySelected = {},
            onNavigateToPlayer = {},
            onResyncEpg = {},
            onNavItemClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA, name = "Guide — Empty EPG")
@Composable
private fun GuideEmptyPreview() {
    GencIptvTheme {
        GuideContent(
            state = GuideUiState(
                days = emptyList(),
                selectedDayIndex = 1,
                selectedDateMillis = System.currentTimeMillis(),
                rows = emptyList(),
                featuredNow = null,
                hasAnyEpgData = false,
                isLoading = false,
            ),
            onDaySelected = {},
            onNavigateToPlayer = {},
            onResyncEpg = {},
            onNavItemClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA, name = "Guide — Loading")
@Composable
private fun GuideLoadingPreview() {
    GencIptvTheme {
        GuideContent(
            state = GuideUiState.INITIAL,
            onDaySelected = {},
            onNavigateToPlayer = {},
            onResyncEpg = {},
            onNavItemClick = {},
        )
    }
}
