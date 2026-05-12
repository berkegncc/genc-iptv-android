package com.genciptv.player.feature.guide

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.Program
import com.genciptv.player.data.repository.ChannelRepository
import com.genciptv.player.data.repository.EpgRepository
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import com.genciptv.player.feature.guide.model.DayOption
import com.genciptv.player.feature.guide.model.EpgGridRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GuideViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val channelRepository: ChannelRepository,
    private val epgRepository: EpgRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    // ── Day selector state ────────────────────────────────────────────────────

    private val _selectedDayIndex = MutableStateFlow(1) // 0=yesterday, 1=today, 2=tomorrow, …

    /** 7 day options: yesterday, today, tomorrow, +3 more. */
    val days: List<DayOption> = buildDayOptions()

    // ── Active playlist id ────────────────────────────────────────────────────

    private val activePlaylistFlow = userPreferencesRepository.user
        .map { it.activePlaylistId }
        .distinctUntilChanged()

    // ── Channels (top 100) ────────────────────────────────────────────────────

    private val channelsFlow = activePlaylistFlow.flatMapLatest { playlistId ->
        if (playlistId <= 0L) flowOf(emptyList())
        else channelRepository.observeByPlaylist(playlistId).map { channels ->
            channels.take(100)
        }
    }

    // ── EPG grid for selected day ─────────────────────────────────────────────

    private val epgGridFlow = combine(
        activePlaylistFlow,
        channelsFlow,
        _selectedDayIndex,
    ) { playlistId, channels, dayIndex -> Triple(playlistId, channels, dayIndex) }
        .flatMapLatest { (playlistId, channels, dayIndex) ->
            val epgIds = channels.mapNotNull { it.epgChannelId }.filter { it.isNotBlank() }
            if (playlistId <= 0L || channels.isEmpty() || epgIds.isEmpty()) {
                Log.i(
                    "GencIPTV/Guide",
                    "Skipping EPG query — playlistId=$playlistId, channels=${channels.size}, " +
                        "epgIds=${epgIds.size} (no usable epgChannelId on any channel?)",
                )
                flowOf(emptyMap())
            } else {
                val dayMillis = days.getOrNull(dayIndex)?.dateMillis
                    ?: midnightMillis(System.currentTimeMillis(), 0)
                val dayEndMillis = dayMillis + 24 * 60 * 60 * 1000L
                Log.i(
                    "GencIPTV/Guide",
                    "Querying EPG grid: ${channels.size} channels, ${epgIds.size} with epgIds, " +
                        "day=$dayIndex (sample: ${epgIds.take(5)})",
                )
                epgRepository.observeDayGrid(playlistId, epgIds, dayMillis, dayEndMillis)
                    .map { grid ->
                        Log.i(
                            "GencIPTV/Guide",
                            "EPG grid result: ${grid.size} channels matched programmes " +
                                "out of ${epgIds.size} queried",
                        )
                        grid
                    }
            }
        }

    // ── Combined UI state ─────────────────────────────────────────────────────

    val uiState: StateFlow<GuideUiState> = combine(
        channelsFlow,
        epgGridFlow,
        _selectedDayIndex,
    ) { channels, programMap, dayIndex ->
        val now = System.currentTimeMillis()
        val selectedDay = days.getOrElse(dayIndex) { days[1] }

        // Build grid rows — only channels that have programs for the day
        val rows: List<EpgGridRow> = channels.mapNotNull { channel ->
            val epgId = channel.epgChannelId ?: return@mapNotNull null
            val programs = programMap[epgId] ?: return@mapNotNull null
            if (programs.isEmpty()) return@mapNotNull null
            EpgGridRow(
                channelId = channel.id,
                channelName = channel.name,
                logoUrl = channel.logoUrl,
                programs = programs,
            )
        }

        // Featured: first row with a program currently on air
        val featuredNow = rows.firstOrNull { row ->
            row.programs.any { prog -> prog.startMillis <= now && prog.stopMillis > now }
        }

        GuideUiState(
            days = days,
            selectedDayIndex = dayIndex,
            selectedDateMillis = selectedDay.dateMillis,
            rows = rows,
            featuredNow = featuredNow,
            hasAnyEpgData = rows.isNotEmpty(),
            isLoading = false,
            error = null,
        )
    }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            Log.e("GencIPTV/Guide", "EPG grid error", e)
            emit(GuideUiState.INITIAL.copy(isLoading = false, error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GuideUiState.INITIAL,
        )

    // ── Actions ───────────────────────────────────────────────────────────────

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index.coerceIn(0, days.lastIndex)
    }

    fun resyncEpg() {
        viewModelScope.launch {
            try {
                val active = playlistRepository.getActive() ?: return@launch
                playlistRepository.sync(active.id)
            } catch (e: Exception) {
                Log.e("GencIPTV/Guide", "Resync failed", e)
            }
        }
    }

    // ── Day building ──────────────────────────────────────────────────────────

    private fun buildDayOptions(): List<DayOption> {
        val tz = TimeZone.getDefault()
        val todayMidnight = midnightMillis(System.currentTimeMillis(), 0)
        // 7 entries: yesterday (today-1), today, tomorrow, +4 more
        return (-1..5).map { offset ->
            val dayMidnight = todayMidnight + offset * 24 * 60 * 60 * 1000L
            val cal = Calendar.getInstance(tz)
            cal.timeInMillis = dayMidnight
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val dayLabel = when (offset) {
                -1 -> "Dün"
                0  -> "Bugün"
                1  -> "Yarın"
                else -> turkishWeekdayShort(cal.get(Calendar.DAY_OF_WEEK))
            }
            DayOption(
                dateMillis = dayMidnight,
                dayLabel = dayLabel,
                dayNumber = dayOfMonth.toString(),
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    companion object {
        /** Returns midnight (00:00:00.000) of the day that contains [epochMs], offset by [daysOffset] days. */
        fun midnightMillis(epochMs: Long, daysOffset: Int): Long {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = epochMs
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_MONTH, daysOffset)
            return cal.timeInMillis
        }

        fun turkishWeekdayShort(calDayOfWeek: Int): String = when (calDayOfWeek) {
            Calendar.MONDAY    -> "Pzt"
            Calendar.TUESDAY   -> "Sal"
            Calendar.WEDNESDAY -> "Çar"
            Calendar.THURSDAY  -> "Per"
            Calendar.FRIDAY    -> "Cum"
            Calendar.SATURDAY  -> "Cmt"
            Calendar.SUNDAY    -> "Paz"
            else               -> ""
        }

        private val TURKISH_MONTHS = listOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )

        /** e.g. "15 Nisan 2026" */
        fun formatDatePill(epochMs: Long): String {
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = epochMs
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = TURKISH_MONTHS[cal.get(Calendar.MONTH)]
            val year = cal.get(Calendar.YEAR)
            return "$day $month $year"
        }

        /** Program progress fraction [0f, 1f] for a given program relative to now. */
        fun programProgress(program: Program, nowMillis: Long): Float {
            val duration = (program.stopMillis - program.startMillis).coerceAtLeast(1L)
            val elapsed = (nowMillis - program.startMillis).coerceAtLeast(0L)
            return (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        }
    }
}
