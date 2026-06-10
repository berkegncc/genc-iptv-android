package com.genciptv.player.feature.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.Channel
import com.genciptv.player.data.model.FavoriteTargetType
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.EpgRepository
import com.genciptv.player.data.repository.FavoriteRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.feature.home.model.ChannelWithProgram
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val channelRepository: ChannelRepository,
    private val epgRepository: EpgRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // ── Channel id — can be updated when user taps "next" list ────────────────

    private val _channelId = MutableStateFlow(
        savedStateHandle.get<String>("channelId") ?: ""
    )

    // ── Volume (kept in VM, applied to ExoPlayer from Composable) ─────────────

    private val _volume = MutableStateFlow(1f)

    // ── Active playlist id ────────────────────────────────────────────────────

    private val activePlaylistFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }

    // ── Current channel data ──────────────────────────────────────────────────

    private val channelFlow = _channelId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf<Channel?>(null)
            else channelRepository.observeById(id)
        }

    // ── Current EPG program ───────────────────────────────────────────────────

    private val currentProgramFlow = combine(
        activePlaylistFlow,
        channelFlow,
    ) { playlistId, channel -> Pair(playlistId, channel) }
        .flatMapLatest { (playlistId, channel) ->
            val epgId = channel?.epgChannelId
            if (playlistId <= 0L || epgId.isNullOrBlank()) {
                flowOf<List<Program>>(emptyList())
            } else {
                epgRepository.observeNow(playlistId, listOf(epgId))
            }
        }
        .map { programs -> programs.firstOrNull() }

    // ── Favorite status ───────────────────────────────────────────────────────

    private val isFavoriteFlow = _channelId
        .flatMapLatest { id ->
            if (id.isBlank()) flowOf(false)
            else favoriteRepository.observeIsFavorite(id, FavoriteTargetType.CHANNEL)
        }

    // ── Other channels for "now on other channels" list ───────────────────────

    private val otherChannelsBaseFlow = combine(
        activePlaylistFlow,
        _channelId,
    ) { playlistId: Long, currentId: String -> Pair(playlistId, currentId) }
        .flatMapLatest { (playlistId, currentId) ->
            if (playlistId <= 0L) flowOf<List<Channel>>(emptyList())
            else {
                channelRepository.observePopular(playlistId, limit = 11)
                    .map { channels: List<Channel> ->
                        channels.filter { it.id != currentId }.take(10)
                    }
            }
        }

    private val otherChannelsWithProgramFlow = combine(
        activePlaylistFlow,
        otherChannelsBaseFlow,
    ) { playlistId: Long, channels: List<Channel> -> Pair(playlistId, channels) }
        .flatMapLatest { (playlistId, channels) ->
            if (channels.isEmpty()) {
                flowOf<List<ChannelWithProgram>>(emptyList())
            } else {
                val epgIds = channels.mapNotNull { it.epgChannelId }.filter { it.isNotBlank() }
                if (epgIds.isEmpty()) {
                    flowOf(channels.map { ChannelWithProgram(it, null, 0f) })
                } else {
                    epgRepository.observeNow(playlistId, epgIds)
                        .map { programs -> joinChannelsWithPrograms(channels, programs) }
                }
            }
        }

    // ── Combined UI state ─────────────────────────────────────────────────────

    val uiState: StateFlow<PlayerUiState> = combine(
        channelFlow,
        currentProgramFlow,
        otherChannelsWithProgramFlow,
        isFavoriteFlow,
        _volume,
    ) { values ->
        val channel  = values[0] as Channel?
        val program  = values[1] as Program?
        @Suppress("UNCHECKED_CAST")
        val others   = values[2] as List<ChannelWithProgram>
        val isFav    = values[3] as Boolean
        val volume   = values[4] as Float

        PlayerUiState(
            channel = channel,
            currentProgram = program,
            upcomingOtherChannels = others,
            isFavorite = isFav,
            isPlaying = true,
            volume = volume,
            error = if (channel == null && _channelId.value.isNotBlank()) "Kanal bulunamadı" else null,
        )
    }
        .flowOn(Dispatchers.Default)
        .catch { e -> emit(PlayerUiState.INITIAL.copy(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerUiState.INITIAL,
        )

    // ── Public state for composable to read volume ────────────────────────────

    val volume: StateFlow<Float> get() = _volume

    /** User's saved subtitle style; consumed by [PlayerScreen] and forwarded to PlayerView. */
    val subtitleStyle: StateFlow<SubtitleStyle> = userPreferencesRepository.subtitles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubtitleStyle.Default,
        )

    /**
     * User's preferred audio track language (ISO 639-1, e.g. "tr", "en"), or
     * null/blank if the user hasn't set one. When non-null, [PlayerScreen]
     * pushes this into ExoPlayer's `trackSelectionParameters` so multi-audio
     * streams lock onto the chosen language; otherwise ExoPlayer falls back
     * to system locale.
     */
    val preferredAudioLang: StateFlow<String?> = userPreferencesRepository.player
        .map { it.preferredAudioLang?.takeIf { lang -> lang.isNotBlank() } }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val userAgent: StateFlow<String?> = userPreferencesRepository.player
        .map { it.userAgent?.takeIf { ua -> ua.isNotBlank() } }
        .distinctUntilChanged()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    init {
        // Record recently-watched channel in local-only DataStore (LRU list).
        // This is NOT ContinueWatching — no position/duration, just an ID list.
        // The provider cannot see this; it never leaves the device.
        channelFlow
            .filterNotNull()
            .distinctUntilChanged { a, b -> a.id == b.id }
            .onEach { channel ->
                runCatching {
                    userPreferencesRepository.addRecentChannel(channel.id)
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Switch to a new channel (tapping from the "other channels" list).
     */
    fun switchTo(channelId: String) {
        _channelId.value = channelId
    }

    fun toggleFavorite() {
        val id = _channelId.value.ifBlank { return }
        viewModelScope.launch {
            favoriteRepository.toggle(id, FavoriteTargetType.CHANNEL)
        }
    }

    fun setVolume(v: Float) {
        _volume.value = v.coerceIn(0f, 1f)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun joinChannelsWithPrograms(
        channels: List<Channel>,
        programs: List<Program>,
    ): List<ChannelWithProgram> {
        val now = System.currentTimeMillis()
        val programByEpgId = programs.associateBy { it.channelEpgId }
        return channels.map { channel ->
            val program = channel.epgChannelId?.let { programByEpgId[it] }
            val progress = if (program != null) {
                val duration = (program.stopMillis - program.startMillis).coerceAtLeast(1L)
                val elapsed = (now - program.startMillis).coerceAtLeast(0L)
                (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            } else 0f
            ChannelWithProgram(channel, program, progress)
        }
    }
}
