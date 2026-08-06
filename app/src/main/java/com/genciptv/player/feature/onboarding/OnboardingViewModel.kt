package com.genciptv.player.feature.onboarding

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.R
import com.genciptv.player.data.repository.PlaylistRepository
import com.genciptv.player.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
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
                        _state.update { it.copy(error = context.getString(R.string.errors_onboarding_settings_save_failed)) }
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
            _state.update { it.copy(error = context.getString(R.string.errors_onboarding_m3u_url_empty)) }
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
                _state.update { it.copy(error = context.getString(R.string.errors_onboarding_xtream_server_url_empty)) }
            form.username.isBlank() ->
                _state.update { it.copy(error = context.getString(R.string.errors_onboarding_xtream_username_empty)) }
            form.password.isBlank() ->
                _state.update { it.copy(error = context.getString(R.string.errors_onboarding_xtream_password_empty)) }
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
                context.getString(R.string.errors_onboarding_internal_network_main_thread)
            "unknownhost" in all || "no address" in all ->
                context.getString(R.string.errors_onboarding_host_unreachable)
            "connect" in all || "timeout" in all || "unreachable" in all ->
                context.getString(R.string.errors_onboarding_m3u_connection_failed)
            "http 4" in all || "http 5" in all ->
                context.getString(R.string.errors_onboarding_m3u_server_error_code, e.message ?: "")
            "empty" in all || "no channel" in all ->
                context.getString(R.string.errors_onboarding_m3u_empty_playlist)
            "parse" in all || "format" in all || "invalid" in all ->
                context.getString(R.string.errors_onboarding_m3u_parse_failed)
            else -> context.getString(
                R.string.errors_onboarding_m3u_load_failed,
                e.message ?: context.getString(R.string.errors_unknown),
            )
        }
    }

    private fun mapXtreamError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        val root = (e.cause?.message ?: "").lowercase()
        val all = "$msg | $root | ${e::class.simpleName?.lowercase()}"
        return when {
            "networkonmainthread" in all ->
                context.getString(R.string.errors_onboarding_internal_network_main_thread)
            "401" in all || "unauthori" in all || "auth" in all ->
                context.getString(R.string.errors_onboarding_xtream_auth_failed)
            "unknownhost" in all || "no address" in all ->
                context.getString(R.string.errors_onboarding_host_unreachable)
            "connect" in all || "timeout" in all || "unreachable" in all ->
                context.getString(R.string.errors_onboarding_xtream_connection_failed)
            "empty" in all || "no channel" in all ->
                context.getString(R.string.errors_onboarding_xtream_empty_playlist)
            else -> context.getString(
                R.string.errors_onboarding_xtream_connect_failed,
                e.message ?: context.getString(R.string.errors_unknown),
            )
        }
    }
}
