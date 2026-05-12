package com.genciptv.player.feature.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
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
import com.genciptv.player.core.designsystem.PosterShape
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.core.ui.Backdrop
import com.genciptv.player.core.ui.EmptyState
import com.genciptv.player.core.ui.LoadingState
import com.genciptv.player.core.ui.Poster
import com.genciptv.player.data.model.CastMember
import com.genciptv.player.data.model.Episode
import com.genciptv.player.data.model.VodItem
import com.genciptv.player.data.model.VodKind
import kotlinx.coroutines.launch

// ── Stateful wrapper ──────────────────────────────────────────────────────────

@Composable
fun VodDetailScreen(
    onBack: () -> Unit,
    onNavigateToVodPlayer: ((vodId: String) -> Unit)? = null,
    onNavigateToEpisodePlayer: ((episodeId: String) -> Unit)? = null,
    onNavigateToVodDetail: ((id: String) -> Unit)? = null,
    viewModel: VodDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    VodDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
        onPlay = {
            val vodId = uiState.movie?.id
            if (vodId != null && onNavigateToVodPlayer != null) {
                onNavigateToVodPlayer(vodId)
            } else {
                scope.launch { snackbarHostState.showSnackbar("Film oynatma hazırlanıyor") }
            }
        },
        onPlayEpisode = { episode ->
            if (onNavigateToEpisodePlayer != null) {
                onNavigateToEpisodePlayer(episode.id)
            } else {
                scope.launch { snackbarHostState.showSnackbar("Bölüm oynatma hazırlanıyor") }
            }
        },
        onNavigateToSimilar = { id -> onNavigateToVodDetail?.invoke(id) },
    )
}

// ── Stateless content ─────────────────────────────────────────────────────────

@Composable
fun VodDetailContent(
    uiState: VodDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onNavigateToSimilar: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingState(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            uiState.error != null -> EmptyState(
                icon = "⚠️",
                title = "Yüklenemedi",
                description = uiState.error,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
            else -> VodDetailBody(
                uiState = uiState,
                onBack = onBack,
                onToggleFavorite = onToggleFavorite,
                onPlay = onPlay,
                onPlayEpisode = onPlayEpisode,
                onNavigateToSimilar = onNavigateToSimilar,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }
}

// ── Body ─────────────────────────────────────────────────────────────────────

@Composable
private fun VodDetailBody(
    uiState: VodDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onNavigateToSimilar: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        // ── Backdrop hero ────────────────────────────────────────────────────
        Backdrop(
            title = uiState.title,
            backdropUrl = uiState.backdropUrl ?: uiState.posterUrl,
            height = 280.dp,
        ) {
            // Top bar overlay — back + favourite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackdropIconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                BackdropIconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (uiState.isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                        tint = if (uiState.isFavorite) Copper else Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // ── Meta block ───────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = uiState.title,
                style = TextStyle(
                    fontFamily = InstrumentSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.02).sp,
                    color = TextPrimary,
                ),
            )
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.year != null) {
                    Text(
                        text = uiState.year.toString(),
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary,
                        ),
                    )
                }
                if (uiState.year != null && uiState.rating != null) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(TextTertiary),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (uiState.rating != null) {
                    Text(
                        text = "★ %.1f".format(uiState.rating),
                        style = TextStyle(
                            fontFamily = GeistMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Copper,
                        ),
                    )
                }
            }

            if (uiState.genres.isNotEmpty()) {
                Spacer(Modifier.height(11.dp))
                GenrePills(genres = uiState.genres)
            }

            val plotText = uiState.plot
            if (!plotText.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Özet",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = plotText,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = TextSecondary,
                    ),
                )
            }

            if (!uiState.isSeries) {
                Spacer(Modifier.height(20.dp))
                PlayButton(onClick = onPlay)
            }
        }

        // ── Cast bar — TMDb photos with serif initial fallback ───────────────
        if (!uiState.isSeries) {
            val cast = uiState.displayCast
            if (cast.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle(title = "Oyuncular")
                Spacer(Modifier.height(12.dp))
                CastBar(cast = cast)
            }
        }

        // ── Similar movies / series ──────────────────────────────────────────
        if (!uiState.isSeries && uiState.similarMovies.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle(title = "Benzer Filmler")
            Spacer(Modifier.height(12.dp))
            SimilarRow(
                items = uiState.similarMovies.map { SimilarItem(it.id, it.title, it.posterUrl, it.year, it.rating) },
                onClick = onNavigateToSimilar,
            )
        }
        if (uiState.isSeries && uiState.similarSeries.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            SectionTitle(title = "Benzer Diziler")
            Spacer(Modifier.height(12.dp))
            SimilarRow(
                items = uiState.similarSeries.map { SimilarItem(it.id, it.title, it.posterUrl, it.year, it.rating) },
                onClick = onNavigateToSimilar,
            )
        }

        // ── Series cast (shown after similar bar) + episodes ─────────────────
        if (uiState.isSeries) {
            val cast = uiState.displayCast
            if (cast.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionTitle(title = "Oyuncular")
                Spacer(Modifier.height(12.dp))
                CastBar(cast = cast)
            }
            Spacer(Modifier.height(24.dp))
            EpisodesSection(
                episodes = uiState.episodes,
                isLoading = uiState.isEpisodesLoading,
                onPlayEpisode = onPlayEpisode,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Section title ────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            fontFamily = GeistFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = (-0.005).sp,
            color = TextPrimary,
        ),
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

// ── Backdrop overlay icon button ────────────────────────────────────────────

@Composable
private fun BackdropIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClick),
    ) {
        content()
    }
}

// ── Genre pills ──────────────────────────────────────────────────────────────

@Composable
private fun GenrePills(genres: List<String>) {
    val accent = LocalAccentPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        genres.take(4).forEach { genre ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(accent.soft)
                    .border(width = 0.5.dp, color = accent.mid, shape = RoundedCornerShape(50.dp))
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

// ── Play button ──────────────────────────────────────────────────────────────

@Composable
private fun PlayButton(onClick: () -> Unit) {
    val accent = LocalAccentPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = if (accent.isDark) Color(0xFF0E1213) else Color.White,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Oynat",
            style = TextStyle(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = if (accent.isDark) Color(0xFF0E1213) else Color.White,
            ),
        )
    }
}

// ── Cast bar ─────────────────────────────────────────────────────────────────

@Composable
private fun CastBar(cast: List<CastMember>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(cast, key = { it.name + (it.character ?: "") }) { member ->
            CastChip(member = member)
        }
    }
}

@Composable
private fun CastChip(member: CastMember) {
    val accent = LocalAccentPalette.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BgElev2)
                .border(width = 0.5.dp, color = LineStrong, shape = CircleShape),
        ) {
            if (!member.profileUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(member.profileUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                )
            } else {
                Text(
                    text = member.name.initials(),
                    style = TextStyle(
                        fontFamily = InstrumentSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 22.sp,
                        color = accent.primary,
                    ),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = member.name,
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

private fun String.initials(): String =
    trim().split(Regex("\\s+"))
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { take(2).uppercase() }

// ── Similar bar ──────────────────────────────────────────────────────────────

private data class SimilarItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val year: Int?,
    val rating: Double?,
)

@Composable
private fun SimilarRow(
    items: List<SimilarItem>,
    onClick: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .width(128.dp)
                    .clickable { onClick(item.id) },
            ) {
                Poster(
                    title = item.title,
                    posterUrl = item.posterUrl,
                    year = null,
                    width = 128.dp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = TextPrimary,
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                val sub = buildString {
                    item.year?.let { append(it) }
                    item.rating?.let {
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
    }
}

// ── Episodes ─────────────────────────────────────────────────────────────────

@Composable
private fun EpisodesSection(
    episodes: List<Episode>,
    isLoading: Boolean,
    onPlayEpisode: (Episode) -> Unit,
) {
    val grouped = remember(episodes) { episodes.groupBy { it.season }.toSortedMap() }
    val seasons = remember(grouped) { grouped.keys.toList() }
    var selectedSeason by remember(seasons) { mutableStateOf(seasons.firstOrNull()) }
    LaunchedEffect(seasons) {
        if (selectedSeason == null || selectedSeason !in seasons) {
            selectedSeason = seasons.firstOrNull()
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Bölümler",
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            if (episodes.isNotEmpty()) {
                val current = selectedSeason?.let { grouped[it] }.orEmpty()
                Text(
                    text = "${current.size} BÖLÜM",
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
        Spacer(Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                ) {
                    CircularProgressIndicator(
                        color = Copper,
                        strokeWidth = 1.5.dp,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            episodes.isEmpty() -> {
                Text(
                    text = "Bölüm bilgisi yüklenemiyor.",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontSize = 13.sp,
                        color = TextSecondary,
                    ),
                )
            }
            else -> {
                SeasonPicker(
                    seasons = seasons,
                    selectedSeason = selectedSeason,
                    onSeasonSelected = { selectedSeason = it },
                )
                Spacer(Modifier.height(14.dp))
                val seasonEpisodes = selectedSeason
                    ?.let { grouped[it].orEmpty() }
                    ?.sortedBy { it.episode }
                    .orEmpty()
                seasonEpisodes.forEach { ep ->
                    EpisodeItem(episode = ep, onPlay = { onPlayEpisode(ep) })
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun SeasonPicker(
    seasons: List<Int>,
    selectedSeason: Int?,
    onSeasonSelected: (Int) -> Unit,
) {
    val accent = LocalAccentPalette.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        items(seasons) { season ->
            val active = season == selectedSeason
            val shape = RoundedCornerShape(50.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(shape)
                    .background(if (active) accent.primary else BgElev)
                    .border(
                        width = if (active) 0.dp else 0.5.dp,
                        color = if (active) Color.Transparent else LineStrong,
                        shape = shape,
                    )
                    .clickable { onSeasonSelected(season) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "Sezon $season",
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (active) {
                            if (accent.isDark) Color(0xFF0E1213) else Color.White
                        } else TextSecondary,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    onPlay: () -> Unit,
) {
    val accent = LocalAccentPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BgElev)
            .border(width = 0.5.dp, color = Line, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onPlay)
            .padding(11.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.soft),
        ) {
            Text(
                text = episode.episode.toString().padStart(2, '0'),
                style = TextStyle(
                    fontFamily = GeistMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = accent.primary,
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
            val plot = episode.plot
            if (!plot.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = plot,
                    style = TextStyle(
                        fontFamily = GeistFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Oynat",
            tint = accent.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0E1213, showSystemUi = true)
@Composable
private fun VodDetailMovieDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        VodDetailContent(
            uiState = VodDetailUiState(
                movie = VodItem(
                    id = "1:movie:123",
                    playlistId = 1L,
                    title = "Kelebeğin Rüyası",
                    year = 2013,
                    rating = 7.8,
                    plot = "Zonguldak'ta yaşayan iki şair, aynı kıza tutkuyla bağlanırken hayatın acılarına meydan okurlar. Yılmaz Erdoğan'ın yönetmenliğini yaptığı bu film, gerçek bir hikâyeden uyarlanmıştır.",
                    genres = listOf("Dram", "Romantik"),
                    cast = listOf("Mert Fırat", "Belçim Bilgin", "Kıvanç Tatlıtuğ"),
                    streamUrl = "",
                    kind = VodKind.MOVIE,
                ),
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onToggleFavorite = {}, onPlay = {}, onPlayEpisode = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC, showSystemUi = true)
@Composable
private fun VodDetailMovieLightPreview() {
    GencIptvTheme(darkTheme = false) {
        VodDetailContent(
            uiState = VodDetailUiState(
                movie = VodItem(
                    id = "1:movie:456",
                    playlistId = 1L,
                    title = "Eşkıya",
                    year = 1996,
                    rating = 8.4,
                    plot = "35 yıllık hapis cezasını çekip dışarı çıkan Baran, sevdiği kadın için intikam yoluna düşer.",
                    genres = listOf("Suç", "Dram"),
                    cast = listOf("Şener Şen", "Uğur Yücel"),
                    streamUrl = "",
                    kind = VodKind.MOVIE,
                ),
                isLoading = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {}, onToggleFavorite = {}, onPlay = {}, onPlayEpisode = {},
        )
    }
}
