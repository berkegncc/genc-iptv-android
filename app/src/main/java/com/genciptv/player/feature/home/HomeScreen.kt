package com.genciptv.player.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.Copper
import com.genciptv.player.core.designsystem.CopperDim
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.GencLogo
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.Line
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.WindowSize
import com.genciptv.player.core.ui.CanliPill
import com.genciptv.player.core.ui.ChannelLogoMark
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.Poster
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.Series
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind

private val homeChips = listOf("Tümü", "Canlı TV", "Filmler", "Diziler", "Spor")

// ── Stateful screen wrapper ───────────────────────────────────────────────────

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToPlayer: (channelId: String) -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToGuide: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToProfile: () -> Unit,
    onNavigateToVod: ((kind: String) -> Unit)? = null,
    onNavigateToVodDetail: ((id: String) -> Unit)? = null,
    onNavigateToSearch: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    HomeContent(
        state = state,
        isRefreshing = isRefreshing,
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToChannels = onNavigateToChannels,
        onNavigateToProfile = onNavigateToProfile,
        onChipSelected = viewModel::selectChip,
        onRefresh = viewModel::refresh,
        onNavigateToVod = onNavigateToVod,
        onNavigateToVodDetail = onNavigateToVodDetail,
        onNavigateToSearch = onNavigateToSearch,
    )
}

// ── Stateless content composable ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    isRefreshing: Boolean = false,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onNavigateToChannels: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onChipSelected: (Int) -> Unit,
    onRefresh: () -> Unit = {},
    onNavigateToVod: ((kind: String) -> Unit)? = null,
    onNavigateToVodDetail: ((id: String) -> Unit)? = null,
    onNavigateToSearch: () -> Unit = {},
) {
    var selectedChip by rememberSaveable { mutableIntStateOf(0) }
    val posterWidth = if (WindowSize.isTablet) 140.dp else 120.dp
    val posterHeight = if (WindowSize.isTablet) 210.dp else 180.dp

    GencAdaptiveScaffold(
        current = GencNavItem.HOME,
        onItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> Unit
                GencNavItem.CHANNELS -> onNavigateToChannels()
                GencNavItem.MOVIES   -> onNavigateToVod?.invoke("MOVIE")
                GencNavItem.SERIES   -> onNavigateToVod?.invoke("SERIES")
                GencNavItem.PROFILE  -> onNavigateToProfile()
            }
        },
        containerColor = Bg,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                // Header — logo + bell + avatar
                item {
                    HomeHeader(
                        displayName = state.userName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Bg)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp)
                            .padding(top = 12.dp, bottom = 8.dp),
                    )
                }

                // Greeting — Instrument Serif "Hoş geldin, X."
                item {
                    HomeGreeting(
                        displayName = state.userName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 16.dp),
                    )
                }

                // Search — flat hairline button (taps through to search screen)
                item {
                    HomeSearchEntry(
                        onClick = onNavigateToSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 0.dp),
                    )
                }

                // Category chips
                item {
                    HomeChipsRow(
                        chips = homeChips,
                        activeIndex = selectedChip,
                        onSelect = { index ->
                            selectedChip = index
                            onChipSelected(index)
                            when (index) {
                                2 -> onNavigateToVod?.invoke("MOVIE")
                                3 -> onNavigateToVod?.invoke("SERIES")
                                else -> Unit
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                    )
                }

                // Empty state
                val contentEmpty = !state.isLoading &&
                    state.latestMovies.isEmpty() &&
                    state.latestSeries.isEmpty() &&
                    state.recentChannels.isEmpty()
                if (contentEmpty) {
                    item {
                        EmptyState(
                            icon = "📺",
                            title = "Henüz içerik yok",
                            description = "Playlist senkronize edildiğinde filmler, diziler ve son " +
                                "izlenen kanallar buraya gelecek.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                        )
                    }
                }

                // Son Eklenen Filmler
                if (state.latestMovies.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Son Eklenen Filmler",
                            onActionClick = { onNavigateToVod?.invoke("MOVIE") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 8.dp, bottom = 12.dp),
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                        ) {
                            items(state.latestMovies, key = { it.id }) { movie ->
                                MoviePosterColumn(
                                    movie = movie,
                                    width = posterWidth,
                                    height = posterHeight,
                                    onClick = { onNavigateToVodDetail?.invoke(movie.id) },
                                )
                            }
                        }
                    }
                }

                // Son Eklenen Diziler
                if (state.latestSeries.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Son Eklenen Diziler",
                            onActionClick = { onNavigateToVod?.invoke("SERIES") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                        ) {
                            items(state.latestSeries, key = { it.id }) { series ->
                                SeriesPosterColumn(
                                    series = series,
                                    width = posterWidth,
                                    height = posterHeight,
                                    onClick = { onNavigateToVodDetail?.invoke(series.id) },
                                )
                            }
                        }
                    }
                }

                // Son İzlediğim Kanallar — ChannelLogoMark + CanliPill overlay
                if (state.recentChannels.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Son İzlediğim Kanallar",
                            onActionClick = onNavigateToChannels,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                        )
                    }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                        ) {
                            items(state.recentChannels, key = { it.id }) { channel ->
                                RecentChannelTile(
                                    channel = channel,
                                    onClick = { onNavigateToPlayer(channel.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    displayName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        // Logo + wordmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            GencLogo(size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Genç",
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.01).sp,
                        color = TextPrimary,
                    ),
                )
                Text(
                    text = "IPTV",
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 0.12.sp,
                        color = TextSecondary,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Bell with copper notification dot
        IconButton(
            onClick = { /* notifications */ },
            modifier = Modifier.size(36.dp),
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Bildirimler",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Copper)
                        .align(Alignment.TopEnd),
                )
            }
        }

        Spacer(Modifier.width(2.dp))

        // Avatar — copper gradient circle
        val initials = displayName
            .trim()
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { displayName.take(2).uppercase().ifBlank { "G" } }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Copper, CopperDim))),
        ) {
            Text(
                text = initials,
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = Color.White,
                ),
            )
        }
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────

@Composable
private fun HomeGreeting(
    displayName: String,
    modifier: Modifier = Modifier,
) {
    val firstName = displayName.trim().split(" ").firstOrNull()?.takeIf { it.isNotBlank() }
        ?: displayName.ifBlank { "" }

    Row(modifier = modifier) {
        Text(
            text = "Hoş geldin, ",
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.02).sp,
                color = TextPrimary,
            ),
        )
        Text(
            text = if (firstName.isNotBlank()) "$firstName." else "izleyici.",
            style = TextStyle(
                fontFamily = InstrumentSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 30.sp,
                letterSpacing = (-0.02).sp,
                color = Copper,
            ),
        )
    }
}

// ── Search entry — flat hairline tile that opens the search screen ────────────

@Composable
private fun HomeSearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BgElev)
            .border(width = 0.5.dp, color = Line, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Kanal, film veya dizi ara…",
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextTertiary,
            ),
        )
    }
}

// ── Chips ─────────────────────────────────────────────────────────────────────

@Composable
private fun HomeChipsRow(
    chips: List<String>,
    activeIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(chips.size) { idx ->
            val isActive = idx == activeIndex
            val shape = RoundedCornerShape(6.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(shape)
                    .background(if (isActive) accent.primary else BgElev)
                    .border(
                        width = if (isActive) 0.dp else 0.5.dp,
                        color = if (isActive) Color.Transparent else Line,
                        shape = shape,
                    )
                    .clickable { onSelect(idx) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = chips[idx],
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isActive) {
                            if (accent.isDark) Color(0xFF0E1213) else Color.White
                        } else TextSecondary,
                    ),
                )
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
internal fun SectionHeader(
    title: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.005).sp,
                color = TextPrimary,
            ),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Tümü →",
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.02.sp,
                color = TextSecondary,
            ),
            modifier = Modifier.clickable(onClick = onActionClick),
        )
    }
}

// ── Movie poster column ───────────────────────────────────────────────────────

@Composable
private fun MoviePosterColumn(
    movie: VodItem,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.width(width).clickable(onClick = onClick)) {
        Poster(
            title = movie.title,
            posterUrl = movie.posterUrl,
            year = movie.year,
            width = width,
            height = height,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = movie.title,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = TextPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        val sub = buildString {
            movie.year?.let { append(it) }
            movie.durationSecs?.let {
                if (isNotEmpty()) append(" · ")
                append("${it / 60}dk")
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

// ── Series poster column ──────────────────────────────────────────────────────

@Composable
private fun SeriesPosterColumn(
    series: Series,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.width(width).clickable(onClick = onClick)) {
        Poster(
            title = series.title,
            posterUrl = series.posterUrl,
            year = series.year,
            width = width,
            height = height,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = series.title,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = TextPrimary,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        val sub = buildString {
            series.year?.let { append(it) }
            series.rating?.let {
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

// ── Recent channel tile ──────────────────────────────────────────────────────

@Composable
private fun RecentChannelTile(
    channel: Channel,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .width(88.dp)
            .clickable(onClick = onClick),
    ) {
        Box {
            ChannelLogoMark(
                name = channel.name,
                logoUrl = channel.logoUrl,
                size = 88.dp,
            )
            CanliPill(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = channel.name,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = TextPrimary,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC, showSystemUi = true)
@Composable
private fun HomeScreenLightPreview() {
    GencIptvTheme(darkTheme = false) {
        HomeContent(
            state = HomeUiState(
                userName = "Mehmet Kaya",
                latestMovies = listOf(
                    VodItem(id = "m1", playlistId = 1L, title = "Kelebeğin Rüyası", streamUrl = "", kind = VodKind.MOVIE, year = 2013),
                    VodItem(id = "m2", playlistId = 1L, title = "Eşkıya", streamUrl = "", kind = VodKind.MOVIE, year = 1996),
                ),
                latestSeries = listOf(
                    Series(id = "s1", playlistId = 1L, title = "Bir Başkadır", year = 2020, rating = 8.0),
                ),
                recentChannels = listOf(
                    Channel(id = "ch1", playlistId = 1L, name = "TRT 1", streamUrl = "", sortOrder = 0),
                    Channel(id = "ch2", playlistId = 1L, name = "beIN Sports 1", streamUrl = "", sortOrder = 1),
                ),
                isLoading = false,
            ),
            onNavigateToPlayer = {},
            onNavigateToChannels = {},
            onNavigateToProfile = {},
            onChipSelected = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun HomeScreenDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        HomeContent(
            state = HomeUiState(
                userName = "Ahmet",
                latestMovies = listOf(
                    VodItem(id = "m1", playlistId = 1L, title = "Çukur", streamUrl = "", kind = VodKind.MOVIE, year = 2017),
                ),
                latestSeries = emptyList(),
                recentChannels = listOf(
                    Channel(id = "ch1", playlistId = 1L, name = "FOX TV", streamUrl = "", sortOrder = 0),
                ),
                isLoading = false,
            ),
            onNavigateToPlayer = {},
            onNavigateToChannels = {},
            onNavigateToProfile = {},
            onChipSelected = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC, showSystemUi = true)
@Composable
private fun HomeScreenEmptyPreview() {
    GencIptvTheme {
        HomeContent(
            state = HomeUiState(userName = "Ali", isLoading = false),
            onNavigateToPlayer = {},
            onNavigateToChannels = {},
            onNavigateToProfile = {},
            onChipSelected = {},
        )
    }
}
