package com.genciptv.player.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.WindowSize
import com.genciptv.player.core.ui.ChannelListItem
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.GencSearchBar
import com.genciptv.player.core.ui.readableContentWidth
import com.genciptv.player.feature.vod.VodPosterCard

private val sectionGradient = Brush.linearGradient(
    listOf(Color(0xFF667eea), Color(0xFF764ba2))
)

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: (channelId: String) -> Unit,
    onNavigateToVodDetail: (id: String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val posterWidth = if (WindowSize.isTablet) 160.dp else 120.dp

    // TextField input is held in local state and pushed one-way to the VM.
    // Reading the field's value back through `uiState.query` (combine + flowOn
    // Default + stateIn) used to round-trip every keystroke through a worker
    // thread; the lagging value re-rendered the TextField with the cursor
    // bumped one position back. Local state keeps the field crisp and lets
    // the VM debounce on its own schedule for the actual search.
    var queryInput by rememberSaveable { mutableStateOf("") }

    // Auto-focus the search input when the screen opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg),
    ) {
        // ── Top bar: back + search field ─────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary,
                )
            }
            GencSearchBar(
                query = queryInput,
                onQueryChange = { newValue ->
                    queryInput = newValue
                    viewModel.onQueryChange(newValue)
                },
                showMic = false,
                focusRequester = focusRequester,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )
        }

        // ── Results ──────────────────────────────────────────────────────────
        when {
            !state.hasQuery -> EmptyState(
                icon = "🔎",
                title = "Ne arıyorsun?",
                description = "Kanal, film veya dizi adından en az 2 karakter yaz.",
                modifier = Modifier.fillMaxSize(),
            )
            state.isEmpty -> EmptyState(
                icon = "🙁",
                title = "Sonuç bulunamadı",
                description = "\"${state.query}\" için eşleşme yok. Farklı bir şey dene.",
                modifier = Modifier.fillMaxSize(),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .readableContentWidth(760.dp)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (state.channels.isNotEmpty()) {
                    item { SectionHeader("Kanallar", state.channels.size) }
                    items(items = state.channels, key = { "ch-${it.id}" }) { channel ->
                        ChannelListItem(
                            name = channel.name,
                            program = channel.groupTitle ?: "",
                            isFavorite = false,
                            thumbBrush = sectionGradient,
                            logoUrl = channel.logoUrl,
                            isLive = true,
                            isHd = channel.isHd,
                            onClick = { onNavigateToPlayer(channel.id) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }

                if (state.movies.isNotEmpty()) {
                    item { SectionHeader("Filmler", state.movies.size) }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.padding(bottom = 12.dp),
                        ) {
                            items(items = state.movies, key = { "mv-${it.id}" }) { movie ->
                                VodPosterCard(
                                    title = movie.title,
                                    posterUrl = movie.posterUrl,
                                    year = movie.year,
                                    rating = movie.rating,
                                    onClick = { onNavigateToVodDetail(movie.id) },
                                    modifier = Modifier.width(posterWidth),
                                )
                            }
                        }
                    }
                }

                if (state.series.isNotEmpty()) {
                    item { SectionHeader("Diziler", state.series.size) }
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.padding(bottom = 12.dp),
                        ) {
                            items(items = state.series, key = { "sr-${it.id}" }) { series ->
                                VodPosterCard(
                                    title = series.title,
                                    posterUrl = series.posterUrl,
                                    year = series.year,
                                    rating = series.rating,
                                    onClick = { onNavigateToVodDetail(series.id) },
                                    modifier = Modifier.width(posterWidth),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
        )
    }
}
