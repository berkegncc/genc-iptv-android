package com.genciptv.player.feature.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

// ── State types ───────────────────────────────────────────────────────────────

data class M3uForm(
    val name: String = "",
    val url: String = "",
    val epgUrl: String = "",
)

data class XtreamForm(
    val name: String = "",
    val serverUrl: String = "",
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
)

data class OnboardingUiState(
    val step: Int = 1,
    val displayName: String = "",
    val m3uForm: M3uForm = M3uForm(),
    val xtreamForm: XtreamForm = XtreamForm(),
    val selectedTab: Int = 0,        // 0 = M3U, 1 = Xtream
    val isLoading: Boolean = false,
    val channelCountLoaded: Int = 0,
    val error: String? = null,
    val completed: Boolean = false,
)

// ── Actions ───────────────────────────────────────────────────────────────────

sealed interface OnboardingAction {
    data class SetDisplayName(val name: String) : OnboardingAction
    object GoToStep2 : OnboardingAction
    object BackToStep1 : OnboardingAction
    data class SelectTab(val index: Int) : OnboardingAction

    // M3U form
    data class UpdateM3uName(val value: String) : OnboardingAction
    data class UpdateM3uUrl(val value: String) : OnboardingAction
    data class UpdateM3uEpgUrl(val value: String) : OnboardingAction
    object SubmitM3u : OnboardingAction

    // Xtream form
    data class UpdateXtreamName(val value: String) : OnboardingAction
    data class UpdateXtreamServerUrl(val value: String) : OnboardingAction
    data class UpdateXtreamUsername(val value: String) : OnboardingAction
    data class UpdateXtreamPassword(val value: String) : OnboardingAction
    object ToggleXtreamPasswordVisibility : OnboardingAction
    object SubmitXtream : OnboardingAction

    object DismissError : OnboardingAction
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnboardingAction.SetDisplayName ->
                _state.update { it.copy(displayName = action.name) }

            is OnboardingAction.GoToStep2 -> {
                viewModelScope.launch {
                    try {
                        userPreferencesRepository.setDisplayName(_state.value.displayName.trim())
                        _state.update { it.copy(step = 2) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Ayarlar kaydedilemedi. Lütfen tekrar deneyin.") }
                    }
                }
            }

            is OnboardingAction.BackToStep1 ->
                _state.update { it.copy(step = 1) }

            is OnboardingAction.SelectTab ->
                _state.update { it.copy(selectedTab = action.index) }

            // ── M3U form ──────────────────────────────────────────────────────
            is OnboardingAction.UpdateM3uName ->
                _state.update { it.copy(m3uForm = it.m3uForm.copy(name = action.value)) }

            is OnboardingAction.UpdateM3uUrl ->
                _state.update { it.copy(m3uForm = it.m3uForm.copy(url = action.value)) }

            is OnboardingAction.UpdateM3uEpgUrl ->
                _state.update { it.copy(m3uForm = it.m3uForm.copy(epgUrl = action.value)) }

            is OnboardingAction.SubmitM3u -> submitM3u()

            // ── Xtream form ───────────────────────────────────────────────────
            is OnboardingAction.UpdateXtreamName ->
                _state.update { it.copy(xtreamForm = it.xtreamForm.copy(name = action.value)) }

            is OnboardingAction.UpdateXtreamServerUrl ->
                _state.update { it.copy(xtreamForm = it.xtreamForm.copy(serverUrl = action.value)) }

            is OnboardingAction.UpdateXtreamUsername ->
                _state.update { it.copy(xtreamForm = it.xtreamForm.copy(username = action.value)) }

            is OnboardingAction.UpdateXtreamPassword ->
                _state.update { it.copy(xtreamForm = it.xtreamForm.copy(password = action.value)) }

            is OnboardingAction.ToggleXtreamPasswordVisibility ->
                _state.update {
                    it.copy(xtreamForm = it.xtreamForm.copy(passwordVisible = !it.xtreamForm.passwordVisible))
                }

            is OnboardingAction.SubmitXtream -> submitXtream()

            is OnboardingAction.DismissError ->
                _state.update { it.copy(error = null) }
        }
    }

    // ── M3U submission ────────────────────────────────────────────────────────

    private fun submitM3u() {
        val form = _state.value.m3uForm
        if (form.url.isBlank()) {
            _state.update { it.copy(error = "M3U URL boş olamaz. Lütfen bir URL girin.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, channelCountLoaded = 0, error = null) }
            try {
                val resolvedName = form.name.ifBlank { "Playlist 1" }
                val epgUrl = form.epgUrl.ifBlank { null }

                val playlistId = playlistRepository.addM3u(
                    name = resolvedName,
                    url = form.url.trim(),
                    epgUrl = epgUrl,
                )
                userPreferencesRepository.setActivePlaylistId(playlistId)
                userPreferencesRepository.setOnboardingCompleted(true)
                _state.update { it.copy(isLoading = false, completed = true) }
            } catch (e: Exception) {
                Log.e("GencIPTV/Onboarding", "M3U submission failed", e)
                _state.update { it.copy(isLoading = false, error = mapError(e)) }
            }
        }
    }

    // ── Xtream submission ─────────────────────────────────────────────────────

    private fun submitXtream() {
        val form = _state.value.xtreamForm
        when {
            form.serverUrl.isBlank() ->
                _state.update { it.copy(error = "Sunucu URL boş olamaz.") }
            form.username.isBlank() ->
                _state.update { it.copy(error = "Kullanıcı adı boş olamaz.") }
            form.password.isBlank() ->
                _state.update { it.copy(error = "Şifre boş olamaz.") }
            else -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true, channelCountLoaded = 0, error = null) }
                    try {
                        val resolvedName = form.name.ifBlank { "Xtream" }
                        val playlistId = playlistRepository.addXtream(
                            name = resolvedName,
                            serverUrl = form.serverUrl.trim(),
                            username = form.username.trim(),
                            password = form.password,
                        )
                        userPreferencesRepository.setActivePlaylistId(playlistId)
                        userPreferencesRepository.setOnboardingCompleted(true)
                        _state.update { it.copy(isLoading = false, completed = true) }
                    } catch (e: Exception) {
                        Log.e("GencIPTV/Onboarding", "Xtream submission failed", e)
                        _state.update { it.copy(isLoading = false, error = mapXtreamError(e)) }
                    }
                }
            }
        }
    }

    // ── Error mapping ─────────────────────────────────────────────────────────

    private fun mapError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        val root = (e.cause?.message ?: "").lowercase()
        val all = "$msg | $root | ${e::class.simpleName?.lowercase()}"
        return when {
            "networkonmainthread" in all ->
                "İç hata: ağ isteği ana iş parçacığında çalıştı. Geliştiriciye bildirin."
            "unknownhost" in all || "no address" in all ->
                "Sunucuya ulaşılamadı. URL'yi kontrol edin."
            "connect" in all || "timeout" in all || "unreachable" in all ->
                "Bağlantı hatası. İnternet bağlantınızı kontrol edin."
            "http 4" in all || "http 5" in all ->
                "Sunucu hata kodu döndü (${e.message ?: ""}). URL'yi kontrol edin."
            "empty" in all || "no channel" in all ->
                "Boş playlist. URL'nin geçerli bir M3U içerdiğinden emin olun."
            "parse" in all || "format" in all || "invalid" in all ->
                "Playlist ayrıştırılamadı. Geçerli bir M3U URL'si girin."
            else -> "Playlist yüklenemedi: ${e.message ?: "bilinmeyen hata"}"
        }
    }

    private fun mapXtreamError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        val root = (e.cause?.message ?: "").lowercase()
        val all = "$msg | $root | ${e::class.simpleName?.lowercase()}"
        return when {
            "networkonmainthread" in all ->
                "İç hata: ağ isteği ana iş parçacığında çalıştı. Geliştiriciye bildirin."
            "401" in all || "unauthori" in all || "auth" in all ->
                "Kimlik doğrulama başarısız. Kullanıcı adı ve şifrenizi kontrol edin."
            "unknownhost" in all || "no address" in all ->
                "Sunucuya ulaşılamadı. URL'yi kontrol edin."
            "connect" in all || "timeout" in all || "unreachable" in all ->
                "Bağlantı hatası. Sunucu URL'sini ve internet bağlantınızı kontrol edin."
            "empty" in all || "no channel" in all ->
                "Boş playlist. Xtream hesabınızda kanal bulunamadı."
            else -> "Bağlantı kurulamadı: ${e.message ?: "bilinmeyen hata"}"
        }
    }
}
