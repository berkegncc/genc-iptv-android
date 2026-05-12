package com.genciptv.player.feature.channels

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.genciptv.player.core.designsystem.BgElev2
import com.genciptv.player.core.designsystem.Copper
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.Line
import com.genciptv.player.core.designsystem.LineStrong
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.designsystem.categoryGradientFor
import com.genciptv.player.core.ui.CanliPill
import com.genciptv.player.core.ui.ChannelLogoMark
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.ErrorState
import com.genciptv.player.core.ui.GencAdaptiveScaffold
import com.genciptv.player.core.ui.GencNavItem
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.QualityPill
import com.genciptv.player.data.model.CategoryChannelCount
import com.genciptv.player.data.model.Channel
import com.genciptv.player.feature.home.model.ChannelWithProgram

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun ChannelsScreen(
    viewModel: ChannelsViewModel = hiltViewModel(),
    onNavigateToPlayer: (channelId: String) -> Unit,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToVod: ((kind: String) -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    BackHandler(enabled = state.inCategoryView) {
        viewModel.backToCategories()
    }

    ChannelsContent(
        state = state,
        isSyncing = isSyncing,
        onNavigateToPlayer = onNavigateToPlayer,
        onBack = onBack,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToHome = onNavigateToHome,
        onNavigateToVod = onNavigateToVod,
        onQueryChange = viewModel::setQuery,
        onEnterCategory = viewModel::enterCategory,
        onBackToCategories = viewModel::backToCategories,
        onToggleFavorite = viewModel::toggleFavorite,
        onResync = viewModel::resyncActivePlaylist,
        onRefresh = viewModel::resyncActivePlaylist,
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsContent(
    state: ChannelsUiState,
    isSyncing: Boolean = false,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVod: ((kind: String) -> Unit)? = null,
    onQueryChange: (String) -> Unit,
    onEnterCategory: (String?) -> Unit,
    onBackToCategories: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onResync: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    GencAdaptiveScaffold(
        current = GencNavItem.CHANNELS,
        onItemClick = { item ->
            when (item) {
                GencNavItem.HOME     -> onNavigateToHome()
                GencNavItem.CHANNELS -> Unit
                GencNavItem.MOVIES   -> onNavigateToVod?.invoke("MOVIE")
                GencNavItem.SERIES   -> onNavigateToVod?.invoke("SERIES")
                GencNavItem.PROFILE  -> onNavigateToProfile()
            }
        },
        containerColor = Bg,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingState(
                    message = "Kanallar yükleniyor…",
                    modifier = Modifier.fillMaxSize(),
                )

                state.error != null -> ErrorState(
                    title = "Kanallar Yüklenemedi",
                    description = state.error,
                    retryLabel = "Geri Dön",
                    onRetry = onBack,
                    modifier = Modifier.fillMaxSize(),
                )

                !state.inCategoryView -> CategoryPickerView(
                    categories = state.categories,
                    totalChannelCount = state.totalChannelCount,
                    isSyncing = isSyncing,
                    onBack = onBack,
                    onResync = onResync,
                    onEnterCategory = onEnterCategory,
                )

                else -> ChannelListView(
                    state = state,
                    isSyncing = isSyncing,
                    onBackToCategories = onBackToCategories,
                    onResync = onResync,
                    onQueryChange = onQueryChange,
                    onNavigateToPlayer = onNavigateToPlayer,
                    onToggleFavorite = onToggleFavorite,
                )
            }
        }
    }
}

// ── Category picker — Glyph rows ─────────────────────────────────────────────

@Composable
private fun CategoryPickerView(
    categories: List<CategoryChannelCount>,
    totalChannelCount: Int,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onResync: () -> Unit,
    onEnterCategory: (String?) -> Unit,
) {
    val accent = LocalAccentPalette.current

    Column(modifier = Modifier.fillMaxSize()) {
        ChannelsHeader(
            title = "Kanallar",
            onLeading = onBack,
            isSyncing = isSyncing,
            onTrailing = onResync,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Editorial intro — Instrument Serif headline + monospace meta line
            item(key = "__intro__") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Text(
                        text = "Kategoriler",
                        style = TextStyle(
                            fontFamily = InstrumentSerifFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                            letterSpacing = (-0.6).sp,
                            color = TextPrimary,
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${categories.size + 1} koleksiyon",
                            style = TextStyle(
                                fontFamily = GeistMonoFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.66.sp,
                                color = TextSecondary,
                            ),
                        )
                        Text(
                            text = "  ·  ",
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                        )
                        Text(
                            text = "$totalChannelCount kanal",
                            style = TextStyle(
                                fontFamily = GeistMonoFamily,
                                fontSize = 11.sp,
                                letterSpacing = 0.66.sp,
                                color = TextSecondary,
                            ),
                        )
                    }
                }
            }

            // "Tümü" — synthesised first row using the user's accent gradient.
            item(key = "__tumu__") {
                CategoryGlyphRow(
                    name = "Tümü",
                    count = totalChannelCount,
                    gradientFrom = accent.primary,
                    gradientTo = accent.primary.copy(alpha = 0.75f),
                    onClick = { onEnterCategory(null) },
                )
            }

            // Real Xtream categories. Each chip shows a 2-3 letter monospace
            // abbreviation derived from the name itself — works for arbitrary
            // provider categories (e.g. "UHD RAW CHANNELS" → "UHD") without
            // needing a hand-curated glyph mapping.
            itemsIndexed(
                items = categories,
                key = { _, c -> c.name },
            ) { index, cat ->
                val gradient = categoryGradientFor(index)
                CategoryGlyphRow(
                    name = cat.name,
                    count = cat.count,
                    gradientFrom = gradient.from,
                    gradientTo = gradient.to,
                    onClick = { onEnterCategory(cat.name) },
                )
            }

            if (categories.isEmpty()) {
                item(key = "__empty__") {
                    EmptyState(
                        icon = "📂",
                        title = "Kategori Bulunamadı",
                        description = "Playlist senkronizasyonu sonrasında kategoriler burada listelenecek. " +
                            "Sağ üstteki yenile butonuna basabilirsin.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                    )
                }
            }
        }
    }
}

/**
 * Single category row: 46dp gradient-ringed chip with monospace abbreviation
 * → name → count + chevron. Tap target ≈ 74dp tall (46 chip + 14×2 padding).
 *
 * The ring is drawn by stacking a 46dp gradient circle behind a 43.6dp
 * Bg-coloured inner circle, giving a 1.2dp-thick ring without a Stroke.
 *
 * Inside the chip we render the first ~3 alphanumeric characters of [name]
 * uppercased in Geist Mono. Stable for any provider category name and reads
 * as an editorial type mark rather than a guessed icon.
 */
@Composable
private fun CategoryGlyphRow(
    name: String,
    count: Int,
    gradientFrom: Color,
    gradientTo: Color,
    onClick: () -> Unit,
) {
    val abbreviation = androidx.compose.runtime.remember(name) { categoryAbbreviation(name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(colors = listOf(gradientFrom, gradientTo))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(43.6.dp)
                    .clip(CircleShape)
                    .background(Bg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = abbreviation,
                    style = TextStyle(
                        fontFamily = GeistMonoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp,
                        color = gradientTo,
                    ),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = name,
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = (-0.16).sp,
                color = TextPrimary,
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = count.toString(),
            style = TextStyle(
                fontFamily = GeistMonoFamily,
                fontSize = 11.sp,
                color = TextSecondary,
            ),
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * Take the first 3 letter/digit characters of [name] uppercased — used as
 * the chip mark on each category row. Skips spaces, separators, and
 * punctuation so noisy provider names like `"TR | Spor"` collapse to `"TRS"`
 * and `"UHD RAW CHANNELS"` collapses to `"UHD"`.
 */
private fun categoryAbbreviation(name: String): String =
    name.filter { it.isLetterOrDigit() }
        .take(3)
        .uppercase()

// ── Channel list view ────────────────────────────────────────────────────────

@Composable
private fun ChannelListView(
    state: ChannelsUiState,
    isSyncing: Boolean,
    onBackToCategories: () -> Unit,
    onResync: () -> Unit,
    onQueryChange: (String) -> Unit,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            ChannelsHeader(
                title = state.selectedCategory ?: "Tümü",
                onLeading = onBackToCategories,
                isSyncing = isSyncing,
                onTrailing = onResync,
            )
        }
        item {
            ChannelSearchEntry(
                query = state.query,
                onQueryChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        if (state.channels.isEmpty()) {
            item {
                EmptyState(
                    icon = "📺",
                    title = "Kanal Bulunamadı",
                    description = "Arama kriterlerinizi değiştirmeyi deneyin.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
            }
        } else {
            itemsIndexed(state.channels, key = { _, item -> item.channel.id }) { _, item ->
                ChannelRow(
                    item = item,
                    isFavorite = item.channel.id in state.favoriteIds,
                    onClick = { onNavigateToPlayer(item.channel.id) },
                    onToggleFavorite = { onToggleFavorite(item.channel.id) },
                )
            }
        }
    }
}

// ── Channel row — v2 layout ──────────────────────────────────────────────────

@Composable
private fun ChannelRow(
    item: ChannelWithProgram,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        ChannelLogoMark(
            name = item.channel.name,
            logoUrl = item.channel.logoUrl,
            size = 44.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.channel.name,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (item.currentProgram != null) {
                    CanliPill()
                }
                if (item.channel.isHd) {
                    QualityPill(label = "HD")
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.currentProgram?.title ?: "Yayın bilgisi yok",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = TextSecondary,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                tint = if (isFavorite) Copper else TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    // Hairline divider between rows
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 72.dp)  // align under text, not the logo
            .height(0.5.dp)
            .background(Line),
    )
}

// ── Header (shared between picker and list) ──────────────────────────────────

@Composable
private fun ChannelsHeader(
    title: String,
    onLeading: () -> Unit,
    isSyncing: Boolean,
    onTrailing: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Bg),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            // Leading — back
            IconButton(
                onClick = onLeading,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.005).sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            // Trailing — resync
            IconButton(
                onClick = onTrailing,
                enabled = !isSyncing,
                modifier = Modifier.size(40.dp),
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = Copper,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Yeniden yükle",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        // Hairline below header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Line),
        )
    }
}

// ── Search entry on the channel list ─────────────────────────────────────────

@Composable
private fun ChannelSearchEntry(
    query: String,
    onQueryChange: (String) -> Unit,
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
        // Use a basic in-place text input — no full TextField chrome.
        androidx.compose.foundation.text.BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Copper),
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
                            text = "Kanal ara…",
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

// ── Previews ─────────────────────────────────────────────────────────────────

private val previewChannels = listOf(
    ChannelWithProgram(
        channel = Channel(id = "1:1", playlistId = 1L, name = "TRT 1", streamUrl = "", isHd = true, sortOrder = 0),
        currentProgram = null, progressFraction = 0f,
    ),
    ChannelWithProgram(
        channel = Channel(id = "1:2", playlistId = 1L, name = "beIN Sports 1", streamUrl = "", isHd = true, groupTitle = "Spor"),
        currentProgram = null, progressFraction = 0f,
    ),
)
private val previewCats = listOf(
    CategoryChannelCount("Ulusal", 84),
    CategoryChannelCount("Spor", 42),
    CategoryChannelCount("Haber", 28),
    CategoryChannelCount("Belgesel", 36),
    CategoryChannelCount("Çocuk", 22),
)

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun ChannelsPickerDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        ChannelsContent(
            state = ChannelsUiState(
                categories = previewCats,
                isLoading = false,
                inCategoryView = false,
            ),
            onNavigateToPlayer = {}, onBack = {},
            onNavigateToProfile = {}, onNavigateToHome = {},
            onQueryChange = {}, onEnterCategory = {}, onBackToCategories = {},
            onToggleFavorite = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC, showSystemUi = true)
@Composable
private fun ChannelsPickerLightPreview() {
    GencIptvTheme(darkTheme = false) {
        ChannelsContent(
            state = ChannelsUiState(
                categories = previewCats,
                isLoading = false,
                inCategoryView = false,
            ),
            onNavigateToPlayer = {}, onBack = {},
            onNavigateToProfile = {}, onNavigateToHome = {},
            onQueryChange = {}, onEnterCategory = {}, onBackToCategories = {},
            onToggleFavorite = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun ChannelsListDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        ChannelsContent(
            state = ChannelsUiState(
                channels = previewChannels,
                categories = previewCats,
                selectedCategory = "Spor",
                inCategoryView = true,
                isLoading = false,
            ),
            onNavigateToPlayer = {}, onBack = {},
            onNavigateToProfile = {}, onNavigateToHome = {},
            onQueryChange = {}, onEnterCategory = {}, onBackToCategories = {},
            onToggleFavorite = {},
        )
    }
}
