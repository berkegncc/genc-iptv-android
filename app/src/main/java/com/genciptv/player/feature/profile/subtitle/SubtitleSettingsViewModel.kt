package com.genciptv.player.feature.profile.subtitle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.SubtitleStyle
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubtitleSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val subtitleStyle: StateFlow<SubtitleStyle> = userPreferencesRepository.subtitles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubtitleStyle.Default,
        )

    fun update(style: SubtitleStyle) {
        viewModelScope.launch {
            userPreferencesRepository.updateSubtitleStyle(style)
        }
    }

    fun reset() {
        viewModelScope.launch {
            userPreferencesRepository.resetSubtitleStyle()
        }
    }
}
