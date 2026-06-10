package com.genciptv.player.feature.vod

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
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
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.PosterShape
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.WindowSize
import com.genciptv.player.core.ui.DetailPlaceholder
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.Poster
import com.genciptv.player.core.ui.TwoPaneRow
import com.genciptv.player.core.ui.TwoPaneSide
import com.genciptv.player.data.model.ContinueWatching
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodCategory
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind

// ── Stateful wrapper ──────────────────────────────────────────────────────────

@Composable
fun VodListScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (id: String) -> Unit,
    onNavigateToVodPlayer: ((vodId: String) -> Unit)? = null,
    onNavigateToEpisodePlayer: ((episodeId: String) -> Unit)? = null,
    onNavigateToHome: () -> Unit = {},
    onNavigateToChannels: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    initialKind: VodKind = VodKind.MOVIE,
    viewModel: VodListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    if (uiState.kind != initialKind && uiState.isLoading) {
        viewModel.setKind(initialKind)
    }

    VodListContent(
        uiState = uiState,
        isRefreshing = isRefreshing,
        onBack = onBack,
        onTabSelected = { tabIndex ->
            viewModel.setKind(if (tabIndex == 0) VodKind.MOVIE else VodKind.SERIES)
        },
        onCategorySelected = viewModel::selectCategory,
        onQueryChanged = viewModel::setQuery,
        onRefresh = viewModel::refresh,
        onItemClick = onNavigateToDetail,
        onContinueMovie = { cwId -> onNavigateToVodPlayer?.invoke(cwId) ?: onNavigateToDetail(cwId) },
        onContinueEpisode = { cwId -> onNavigateToEpisodePlayer?.invoke(cwId) ?: Unit },
        onToggleCwSelection = viewModel::toggleCwSelection,
        onClearCwSelection = viewModel::clearCwSelection,
        onRemoveSelectedCw = viewModel::removeSelectedCw,
        onNavigateToHome = onNavigateToHome,
        onNavigateToChannels = onNavigateToChannels,
        onNavigateToProfile = onNavigateToProfile,
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VodListContent(
    uiState: VodListUiState,
    isRefreshing: Boolean = false,
    onBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onRefresh: () -> Unit = {},
    onContinueMovie: (String) -> Unit = {},
    onContinueEpisode: (String) -> Unit = {},
    onToggleCwSelection: (ContinueWatching) -> Unit = {},
    onClearCwSelection: () -> Unit = {},
    onRemoveSelectedCw: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToChannels: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current
    val activeNavItem = if (uiState.kind == VodKind.MOVIE) GencNavItem.MOVIES else GencNavItem.SERIES

    var showRemoveDialog by remember { mutableStateOf(false) }
    if (showRemoveDialog) {
        val count = uiState.selectedCwIds.size
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = {
                Text(
                    text = "Kaldırılsın mı?",
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        color = TextPrimary,
                    ),
                )
            },
            text = {
                Text(
                    text = "Seçili $count içerik \"Devam Et\" listesinden kaldırılacak.",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = TextSecondary,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    onRemoveSelectedCw()
                }) {
                    Text(
                        text = "Kaldır",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Danger,
                        ),
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(
                        text = "İptal",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextSecondary,
                        ),
                    )
                }
            },
        )
    }

    GencAdaptiveScaffold(
        current = activeNavItem,
        onItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> onNavigateToHome()
                GencNavItem.CHANNELS -> onNavigateToChannels()
                GencNavItem.MOVIES   -> onTabSelected(0)
                GencNavItem.SERIES   -> onTabSelected(1)
                GencNavItem.PROFILE  -> onNavigateToProfile()
            }
        },
        containerColor = Bg,
        modifier = modifier,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val isExpanded = WindowSize.isExpanded

            if (isExpanded) {
                // Tablet master-detail: poster grid on the left, the selected
                // item's detail in a fixed pane on the right.
                var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
                // Switching Movies <-> Series invalidates the previous selection.
                LaunchedEffect(uiState.kind) { selectedId = null }

                TwoPaneRow(
                    fixedSide = TwoPaneSide.End,
                    fixedWidth = 400.dp,
                    startPane = {
                        VodListBody(
                            uiState = uiState,
                            onBack = onBack,
                            onTabSelected = onTabSelected,
                            onCategorySelected = onCategorySelected,
                            onQueryChanged = onQueryChanged,
                            onItemClick = { id -> selectedId = id },
                            onContinueMovie = onContinueMovie,
                            onContinueEpisode = onContinueEpisode,
                            onToggleCwSelection = onToggleCwSelection,
                            onClearCwSelection = onClearCwSelection,
                            onRequestRemove = { showRemoveDialog = true },
                        )
                    },
                    endPane = {
                        val id = selectedId
                        if (id == null) {
                            DetailPlaceholder(text = "Soldan bir film veya dizi seçin")
                        } else {
                            EmbeddedVodDetail(
                                id = id,
                                onClose = { selectedId = null },
                                onPlayMovie = onContinueMovie,
                                onPlayEpisode = onContinueEpisode,
                                onSelectSimilar = { selectedId = it },
                            )
                        }
                    },
                )
            } else {
                VodListBody(
                    uiState = uiState,
                    onBack = onBack,
                    onTabSelected = onTabSelected,
                    onCategorySelected = onCategorySelected,
                    onQueryChanged = onQueryChanged,
                    onItemClick = onItemClick,
                    onContinueMovie = onContinueMovie,
                    onContinueEpisode = onContinueEpisode,
                    onToggleCwSelection = onToggleCwSelection,
                    onClearCwSelection = onClearCwSelection,
                    onRequestRemove = { showRemoveDialog = true },
                )
            }
        }
    }
}

// ── List body (shared by single-pane and the master pane of two-pane) ────────

@Composable
private fun VodListBody(
    uiState: VodListUiState,
    onBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onQueryChanged: (String) -> Unit,
    onItemClick: (String) -> Unit,
    onContinueMovie: (String) -> Unit,
    onContinueEpisode: (String) -> Unit,
    onToggleCwSelection: (ContinueWatching) -> Unit,
    onClearCwSelection: () -> Unit,
    onRequestRemove: () -> Unit,
) {
    val inProgress = if (uiState.kind == VodKind.MOVIE) uiState.inProgressMovies
                     else uiState.inProgressSeries

    Column(modifier = Modifier.fillMaxSize()) {
        // Header — toggles between normal and selection-mode bar
        if (uiState.isCwSelectionMode) {
            SelectionTopBar(
                selectedCount = uiState.selectedCwIds.size,
                onClear = onClearCwSelection,
                onRemove = onRequestRemove,
            )
        } else {
            VodListHeader(onBack = onBack)
        }

        // Tabs (Filmler / Diziler)
        if (!uiState.isCwSelectionMode) {
            VodTabRow(
                activeIndex = if (uiState.kind == VodKind.MOVIE) 0 else 1,
                onSelect = onTabSelected,
            )
        }

        // Search
        if (!uiState.isCwSelectionMode) {
            VodSearchEntry(
                query = uiState.query,
                onQueryChange = onQueryChanged,
                placeholder = if (uiState.kind == VodKind.MOVIE) "Film ara…" else "Dizi ara…",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )

            if (uiState.categories.isNotEmpty()) {
                VodCategoryChips(
                    categories = uiState.categories,
                    selectedId = uiState.selectedCategoryId,
                    onSelected = onCategorySelected,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // Body
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.fillMaxSize())

            uiState.kind == VodKind.MOVIE && uiState.movies.isEmpty() && inProgress.isEmpty() ->
                EmptyState(
                    icon = "🎬",
                    title = "Film bulunamadı",
                    description = "Senkronizasyon tamamlandıktan sonra filmler burada görünecek.",
                    modifier = Modifier.fillMaxSize(),
                )

            uiState.kind == VodKind.SERIES && uiState.series.isEmpty() && inProgress.isEmpty() ->
                EmptyState(
                    icon = "📺",
                    title = "Dizi bulunamadı",
                    description = "Senkronizasyon tamamlandıktan sonra diziler burada görünecek.",
                    modifier = Modifier.fillMaxSize(),
                )

            uiState.kind == VodKind.MOVIE -> VodMovieList(
                inProgress = inProgress,
                movies = uiState.movies,
                selectedCwIds = uiState.selectedCwIds,
                onContinue = onContinueMovie,
                onToggleCwSelection = onToggleCwSelection,
                onMovieClick = onItemClick,
            )
            else -> VodSeriesList(
                inProgress = inProgress,
                seriesList = uiState.series,
                selectedCwIds = uiState.selectedCwIds,
                onContinue = onContinueEpisode,
                onToggleCwSelection = onToggleCwSelection,
                onSeriesClick = onItemClick,
            )
        }
    }
}

// ── Embedded detail pane (tablet two-pane) ───────────────────────────────────

/**
 * Renders [VodDetailContent] for [id] inside the right pane of the tablet VOD
 * list. Backed by its own [VodDetailViewModel] scoped to the VOD list nav entry;
 * [VodDetailViewModel.open] repoints it whenever the selection changes.
 */
@Composable
private fun EmbeddedVodDetail(
    id: String,
    onClose: () -> Unit,
    onPlayMovie: (String) -> Unit,
    onPlayEpisode: (String) -> Unit,
    onSelectSimilar: (String) -> Unit,
) {
    val viewModel: VodDetailViewModel = hiltViewModel()
    LaunchedEffect(id) { viewModel.open(id) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    VodDetailContent(
        uiState = state,
        snackbarHostState = snackbarHostState,
        onBack = onClose,
        onToggleFavorite = viewModel::toggleFavorite,
        onPlay = { onPlayMovie(id) },
        onPlayEpisode = { episode -> onPlayEpisode(episode.id) },
        onNavigateToSimilar = onSelectSimilar,
    )
}

// ── Header (normal) ──────────────────────────────────────────────────────────

@Composable
private fun VodListHeader(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Bg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Film & Dizi",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.005).sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Line))
    }
}

// ── Header (selection mode) ──────────────────────────────────────────────────

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClear: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(BgElev)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onClear, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Seçimi iptal et",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$selectedCount seçili",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            // "Kaldır" button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Danger.copy(alpha = 0.10f))
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Kaldır",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Danger,
                    ),
                )
            }
            Spacer(Modifier.width(4.dp))
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Line))
    }
}

// ── Tab row ──────────────────────────────────────────────────────────────────

@Composable
private fun VodTabRow(
    activeIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val accent = LocalAccentPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Bg),
    ) {
        listOf("Filmler", "Diziler").forEachIndexed { i, label ->
            val active = i == activeIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(i) }
                    .padding(vertical = 11.dp),
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (active) accent.primary else TextSecondary,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (active) 0.40f else 1f)
                        .height(if (active) 2.dp else 0.5.dp)
                        .background(if (active) accent.primary else Line),
                )
            }
        }
    }
}

// ── Category chips ───────────────────────────────────────────────────────────

@Composable
private fun VodCategoryChips(
    categories: List<VodCategory>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
) {
    val accent = LocalAccentPalette.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            VodCategoryChip(
                label = "Tümü",
                isSelected = selectedId == null,
                onClick = { onSelected(null) },
            )
        }
        items(categories, key = { it.id }) { cat ->
            VodCategoryChip(
                label = cat.name,
                isSelected = cat.id == selectedId,
                onClick = { onSelected(if (cat.id == selectedId) null else cat.id) },
            )
        }
    }
}

@Composable
private fun VodCategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    val shape = RoundedCornerShape(6.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) accent.primary else BgElev)
            .border(
                width = if (isSelected) 0.dp else 0.5.dp,
                color = if (isSelected) Color.Transparent else Line,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp,
                color = if (isSelected) {
                    if (accent.isDark) Color(0xFF0E1213) else Color.White
                } else TextSecondary,
            ),
        )
    }
}

// ── Search ───────────────────────────────────────────────────────────────────

@Composable
private fun VodSearchEntry(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(BgElev)
            .border(width = 0.5.dp, color = Line, shape = shape)
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(Copper),
            textStyle = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextPrimary,
            ),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = TextTertiary,
                            ),
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Devam Et section ─────────────────────────────────────────────────────────

@Composable
private fun DevamEtSection(
    items: List<ContinueWatching>,
    selectedCwIds: Set<Pair<String, FavoriteTargetType>>,
    onItemClick: (String) -> Unit,
    onToggleSelection: (ContinueWatching) -> Unit,
) {
    if (items.isEmpty()) return
    val isSelectionMode = selectedCwIds.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        ) {
            Text(
                text = "Devam Et",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = items.size.toString().padStart(2, '0'),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 0.06.sp,
                    color = TextTertiary,
                ),
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            items(items, key = { "${it.targetId}_${it.targetType}" }) { cw ->
                val key = cw.targetId to cw.targetType
                val isSelected = key in selectedCwIds
                DevamEtCard(
                    cw = cw,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onTap = {
                        if (isSelectionMode) {
                            onToggleSelection(cw)
                        } else {
                            // For series rows the targetId is now the series id
                            // (so the row collapses to one entry per series);
                            // route the tap to the actual episode player using
                            // `resumeEpisodeId`. Movies keep `targetId`.
                            onItemClick(cw.resumeEpisodeId ?: cw.targetId)
                        }
                    },
                    onLongPress = { onToggleSelection(cw) },
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Line))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DevamEtCard(
    cw: ContinueWatching,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    val progress = if (cw.durationMs > 0L) (cw.positionMs.toFloat() / cw.durationMs).coerceIn(0f, 1f) else 0f

    Column(modifier = Modifier.width(140.dp)) {
        // 16:9 poster with DEVAM label
        Box(
            modifier = Modifier
                .combinedClickable(onClick = onTap, onLongClick = onLongPress)
                .then(
                    if (isSelected) Modifier.border(width = 2.dp, color = accent.primary, shape = PosterShape)
                    else Modifier,
                ),
        ) {
            Poster(
                title = cw.title,
                posterUrl = cw.thumbnailUrl,
                year = null,
                label = "DEVAM",
                width = 140.dp,
                height = 90.dp,
                // Title is rendered underneath this card as a marquee row;
                // suppress the on-art overlay to avoid duplicate, cramped text.
                showTitleOverlay = false,
            )
            // Progress bar at the very bottom of the poster
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .background(Copper),
                )
            }
            if (isSelectionMode) {
                SelectionBubble(
                    isSelected = isSelected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Title + subtitle frequently overflow the 140dp card width
        // (e.g. "Inspector Koo" + "S2 · B7 — Saklı Bir Mektup"). Wrap both
        // lines in a single `basicMarquee`-modified Column so they scroll as
        // one unit — applying marquee to each Text separately makes them
        // drift out of sync because each Text has its own animation timer
        // and a different cycle length.
        //
        // `repeatDelayMillis = 5000` pauses 5 s between iterations instead of
        // looping continuously, so the text is readable when it stops at the
        // start.
        Column(
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                repeatDelayMillis = 5_000,
                // Wait 1.5 s before the very first scroll so the card has time
                // to settle (Coil decodes the poster, layout stabilises, the
                // animation pipeline warms up) — otherwise the first iteration
                // visibly stutters while subsequent ones are smooth. After the
                // first cycle every restart uses `repeatDelayMillis` instead.
                initialDelayMillis = 1_500,
            ),
        ) {
            Text(
                text = cw.title,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextPrimary,
                ),
                maxLines = 1,
            )
            if (!cw.subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = cw.subtitle,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = TextTertiary,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Selection bubble ─────────────────────────────────────────────────────────

@Composable
private fun SelectionBubble(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isSelected) accent.primary else Color.Black.copy(alpha = 0.40f))
            .border(
                width = 1.5.dp,
                color = if (isSelected) accent.primary else Color.White.copy(alpha = 0.70f),
                shape = CircleShape,
            ),
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (accent.isDark) Color(0xFF0E1213) else Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

// ── Movie / Series grid lists ────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VodMovieList(
    inProgress: List<ContinueWatching>,
    movies: List<VodItem>,
    selectedCwIds: Set<Pair<String, FavoriteTargetType>>,
    onContinue: (String) -> Unit,
    onToggleCwSelection: (ContinueWatching) -> Unit,
    onMovieClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 116.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (inProgress.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                DevamEtSection(
                    items = inProgress,
                    selectedCwIds = selectedCwIds,
                    onItemClick = onContinue,
                    onToggleSelection = onToggleCwSelection,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Tümü",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${movies.size} FİLM",
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 0.06.sp,
                            color = TextTertiary,
                        ),
                    )
                }
            }
        }
        gridItems(movies, key = { it.id }) { movie ->
            GridPosterTile(
                title = movie.title,
                posterUrl = movie.posterUrl,
                year = movie.year,
                rating = movie.rating,
                onClick = { onMovieClick(movie.id) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VodSeriesList(
    inProgress: List<ContinueWatching>,
    seriesList: List<Series>,
    selectedCwIds: Set<Pair<String, FavoriteTargetType>>,
    onContinue: (String) -> Unit,
    onToggleCwSelection: (ContinueWatching) -> Unit,
    onSeriesClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 116.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (inProgress.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                DevamEtSection(
                    items = inProgress,
                    selectedCwIds = selectedCwIds,
                    onItemClick = onContinue,
                    onToggleSelection = onToggleCwSelection,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Tümü",
                        style = TextStyle(
                            fontFamily = GeistFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${seriesList.size} DİZİ",
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 0.06.sp,
                            color = TextTertiary,
                        ),
                    )
                }
            }
        }
        gridItems(seriesList, key = { it.id }) { series ->
            GridPosterTile(
                title = series.title,
                posterUrl = series.posterUrl,
                year = series.year,
                rating = series.rating,
                onClick = { onSeriesClick(series.id) },
            )
        }
    }
}

// ── Grid poster tile — full-width 2:3 poster + title + meta ──────────────────

@Composable
private fun GridPosterTile(
    title: String,
    posterUrl: String?,
    year: Int?,
    rating: Double?,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Poster(
            title = title,
            posterUrl = posterUrl,
            year = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = TextPrimary,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        val sub = buildString {
            year?.let { append(it) }
            rating?.let {
                if (isNotEmpty()) append(" · ")
                append("★ %.1f".format(it))
            }
        }
        if (sub.isNotEmpty()) {
            Text(
                text = sub,
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    color = TextTertiary,
                ),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun VodListDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        VodListContent(
            uiState = VodListUiState(
                kind = VodKind.MOVIE,
                movies = listOf(
                    VodItem(id = "m1", playlistId = 1L, title = "Kelebeğin Rüyası", streamUrl = "", kind = VodKind.MOVIE, year = 2013, rating = 7.8),
                    VodItem(id = "m2", playlistId = 1L, title = "Eşkıya", streamUrl = "", kind = VodKind.MOVIE, year = 1996, rating = 8.4),
                ),
                isLoading = false,
            ),
            onBack = {}, onTabSelected = {}, onCategorySelected = {},
            onQueryChanged = {}, onItemClick = {},
        )
    }
}
