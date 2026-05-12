package com.genciptv.player.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.ui.ChannelListItem
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind
import com.genciptv.player.feature.vod.VodPosterCard

// ── Stateful wrapper ──────────────────────────────────────────────────────────

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onNavigateToVodDetail: (id: String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVod: (kind: String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesContent(
        uiState = uiState,
        onBack = onBack,
        onTabSelected = viewModel::selectTab,
        onChannelClick = onNavigateToPlayer,
        onMovieClick = onNavigateToVodDetail,
        onSeriesClick = onNavigateToVodDetail,
        onUnfavoriteChannel = { id -> viewModel.unfavorite(id, FavoriteTargetType.CHANNEL) },
        onUnfavoriteMovie = { id -> viewModel.unfavorite(id, FavoriteTargetType.MOVIE) },
        onUnfavoriteSeries = { id -> viewModel.unfavorite(id, FavoriteTargetType.SERIES) },
        onNavigateToHome = onNavigateToHome,
        onNavigateToChannels = onNavigateToChannels,
        onNavigateToGuide = onNavigateToGuide,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToVod = onNavigateToVod,
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@Composable
fun FavoritesContent(
    uiState: FavoritesUiState,
    onBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onChannelClick: (String) -> Unit,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    onUnfavoriteChannel: (String) -> Unit,
    onUnfavoriteMovie: (String) -> Unit,
    onUnfavoriteSeries: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToVod: (kind: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary
    val tabs = listOf("Kanallar", "Filmler", "Diziler")

    // Pager drives both the content area and the tab indicator. Two-way sync:
    //  - Tab tap (`onTabSelected` → VM → `uiState.selectedTab`) animates the
    //    pager to the chosen page.
    //  - Swipe (pagerState.currentPage changes) pushes the new page back into
    //    the VM via `onTabSelected`.
    // The `if` guards on each side break the feedback loop.
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedTab.coerceIn(0, tabs.lastIndex),
        pageCount = { tabs.size },
    )

    LaunchedEffect(uiState.selectedTab) {
        if (pagerState.currentPage != uiState.selectedTab) {
            pagerState.animateScrollToPage(uiState.selectedTab)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != uiState.selectedTab) {
                onTabSelected(page)
            }
        }
    }

    GencAdaptiveScaffold(
        current = null, // Favoriler is under Profile; no tab is "active"
        onItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> onNavigateToHome()
                GencNavItem.CHANNELS -> onNavigateToChannels()
                GencNavItem.MOVIES   -> onNavigateToVod("MOVIE")
                GencNavItem.SERIES   -> onNavigateToVod("SERIES")
                GencNavItem.PROFILE  -> onNavigateToProfile()
            }
        },
        containerColor = Bg,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            FavoritesHeader(onBack = onBack)

            // Tab row — the indicator follows the pager so it slides smoothly
            // mid-swipe rather than snapping at the end of the gesture.
            @OptIn(ExperimentalMaterial3Api::class)
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Surface,
                contentColor = accent,
            ) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (pagerState.currentPage == index) accent else TextSecondary,
                            )
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )

            // Swipeable content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> FavoriteChannelsTab(
                        channels = uiState.favoriteChannels,
                        isLoading = uiState.isLoading,
                        onChannelClick = onChannelClick,
                        onUnfavorite = onUnfavoriteChannel,
                    )
                    1 -> FavoriteMoviesTab(
                        movies = uiState.favoriteMovies,
                        isLoading = uiState.isLoading,
                        onMovieClick = onMovieClick,
                        onUnfavorite = onUnfavoriteMovie,
                    )
                    2 -> FavoriteSeriesTab(
                        seriesList = uiState.favoriteSeries,
                        isLoading = uiState.isLoading,
                        onSeriesClick = onSeriesClick,
                        onUnfavorite = onUnfavoriteSeries,
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun FavoritesHeader(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = TextPrimary,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Favoriler",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            ),
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Border)
    )
}

// ── Channels tab ──────────────────────────────────────────────────────────────

@Composable
private fun FavoriteChannelsTab(
    channels: List<Channel>,
    isLoading: Boolean,
    onChannelClick: (String) -> Unit,
    onUnfavorite: (String) -> Unit,
) {
    when {
        isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
        channels.isEmpty() -> EmptyState(
            icon = "⭐",
            title = "Henüz favori kanal yok",
            description = "Kanallar ekranında ⭐ simgesine dokunarak favori ekleyebilirsin.",
            modifier = Modifier.fillMaxSize(),
        )
        else -> {
            val brushes = channelBrushes()
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(channels, key = { it.id }) { channel ->
                    ChannelListItem(
                        name = channel.name,
                        program = "",
                        isFavorite = true,
                        logoUrl = channel.logoUrl,
                        thumbBrush = brushes[channels.indexOf(channel) % brushes.size],
                        isHd = channel.isHd,
                        onClick = { onChannelClick(channel.id) },
                        onFavoriteClick = { onUnfavorite(channel.id) },
                    )
                }
            }
        }
    }
}

// ── Movies tab ────────────────────────────────────────────────────────────────

@Composable
private fun FavoriteMoviesTab(
    movies: List<VodItem>,
    isLoading: Boolean,
    onMovieClick: (String) -> Unit,
    onUnfavorite: (String) -> Unit,
) {
    when {
        isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
        movies.isEmpty() -> EmptyState(
            icon = "🎬",
            title = "Henüz favori film yok",
            description = "Film listesinde ⭐ simgesine dokunarak favori ekleyebilirsin.",
            modifier = Modifier.fillMaxSize(),
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(movies, key = { it.id }) { movie ->
                VodPosterCard(
                    title = movie.title,
                    posterUrl = movie.posterUrl,
                    year = movie.year,
                    rating = movie.rating,
                    isFavorite = true,
                    onClick = { onMovieClick(movie.id) },
                    onFavoriteClick = { onUnfavorite(movie.id) },
                )
            }
        }
    }
}

// ── Series tab ────────────────────────────────────────────────────────────────

@Composable
private fun FavoriteSeriesTab(
    seriesList: List<Series>,
    isLoading: Boolean,
    onSeriesClick: (String) -> Unit,
    onUnfavorite: (String) -> Unit,
) {
    when {
        isLoading -> LoadingState(modifier = Modifier.fillMaxSize())
        seriesList.isEmpty() -> EmptyState(
            icon = "📺",
            title = "Henüz favori dizi yok",
            description = "Dizi listesinde ⭐ simgesine dokunarak favori ekleyebilirsin.",
            modifier = Modifier.fillMaxSize(),
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(seriesList, key = { it.id }) { series ->
                VodPosterCard(
                    title = series.title,
                    posterUrl = series.posterUrl,
                    year = series.year,
                    rating = series.rating,
                    isFavorite = true,
                    onClick = { onSeriesClick(series.id) },
                    onFavoriteClick = { onUnfavorite(series.id) },
                )
            }
        }
    }
}

// ── Brush helpers ─────────────────────────────────────────────────────────────

@Composable
private fun channelBrushes(): List<Brush> = listOf(
    Brush.linearGradient(listOf(Color(0xFF4facfe), Color(0xFF00f2fe))),
    Brush.linearGradient(listOf(Color(0xFF43e97b), Color(0xFF38f9d7))),
    Brush.linearGradient(listOf(Color(0xFFfa709a), Color(0xFFfee140))),
    Brush.linearGradient(listOf(Color(0xFF6a11cb), Color(0xFF2575fc))),
    Brush.linearGradient(listOf(Color(0xFFf77062), Color(0xFFfe5196))),
)

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun FavoritesEmptyPreview() {
    GencIptvTheme {
        FavoritesContent(
            uiState = FavoritesUiState(isLoading = false),
            onBack = {},
            onTabSelected = {},
            onChannelClick = {},
            onMovieClick = {},
            onSeriesClick = {},
            onUnfavoriteChannel = {},
            onUnfavoriteMovie = {},
            onUnfavoriteSeries = {},
            onNavigateToHome = {},
            onNavigateToChannels = {},
            onNavigateToGuide = {},
            onNavigateToProfile = {},
            onNavigateToVod = {},
        )
    }
}

