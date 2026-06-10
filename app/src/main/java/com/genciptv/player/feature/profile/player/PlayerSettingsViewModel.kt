package com.genciptv.player.feature.profile.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.model.DecoderPref
import com.genciptv.player.data.model.PlayerPreferences
import com.genciptv.player.data.model.QualityPref
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val player: StateFlow<PlayerPreferences> = userPreferencesRepository.player
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerPreferences(),
        )

    fun setQuality(quality: QualityPref) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(defaultQuality = quality) }
        }
    }

    fun setDecoder(decoder: DecoderPref) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(decoderPref = decoder) }
        }
    }

    fun setAudioLang(lang: String?) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(preferredAudioLang = lang?.ifBlank { null }) }
        }
    }

    fun toggleLoudness(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(loudnessNormalization = enabled) }
        }
    }

    fun setUserAgent(ua: String?) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(userAgent = ua?.ifBlank { null }) }
        }
    }

    fun togglePip(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updatePlayer { it.copy(pictureInPicture = enabled) }
        }
    }
}
