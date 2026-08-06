package com.genciptv.player.feature.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.genciptv.player.R
import com.genciptv.player.core.util.ApkInstaller
import com.genciptv.player.data.model.DownloadState
import com.genciptv.player.data.model.UpdateInfo
import com.genciptv.player.data.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Drives the update dialog.
 *
 * Scoped to the Activity by the composables that use it, so one download keeps
 * running (and keeps its state) while the user moves between Ana Sayfa and
 * Profil. The download runs in [viewModelScope] rather than a `LaunchedEffect`
 * for the same reason.
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: UpdateRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    val currentVersion: String get() = repository.currentVersion

    private var downloadJob: Job? = null

    /**
     * @param force user tapped "Güncellemeleri kontrol et". A forced check
     *   reports "you're up to date"; a silent one says nothing.
     */
    fun check(force: Boolean = false) {
        // Never interrupt an in-flight download or a pending install.
        when (_uiState.value) {
            is UpdateUiState.Downloading,
            is UpdateUiState.ReadyToInstall,
            is UpdateUiState.NeedsPermission -> return
            else -> Unit
        }
        viewModelScope.launch {
            val info = repository.checkForUpdate(force)
            _uiState.value = when {
                info != null -> UpdateUiState.Available(info)
                force -> UpdateUiState.UpToDate(repository.currentVersion)
                else -> UpdateUiState.Idle
            }
        }
    }

    fun startDownload(info: UpdateInfo) {
        if (downloadJob?.isActive == true) return
        _uiState.value = UpdateUiState.Downloading(info, percent = 0)
        downloadJob = viewModelScope.launch {
            repository.downloadApk(info)
                .onEach { state ->
                    _uiState.value = when (state) {
                        is DownloadState.Progress -> UpdateUiState.Downloading(
                            info = info,
                            percent = state.percent,
                            bytesRead = state.bytesRead,
                            total = state.total,
                        )
                        is DownloadState.Done -> afterDownload(info, state.file)
                        is DownloadState.Failed -> UpdateUiState.Error(state.message, info)
                    }
                }
                .collect()
        }
    }

    /**
     * Decides what happens once the APK is on disk. Validation runs first so a
     * downgrade or signature mismatch is explained here rather than surfacing
     * as the system's bare "Uygulama yüklenmedi".
     */
    private fun afterDownload(info: UpdateInfo, file: File): UpdateUiState =
        when (val check = ApkInstaller.validate(context, file)) {
            is ApkInstaller.Check.Problem -> UpdateUiState.Error(check.message)
            ApkInstaller.Check.Ok ->
                if (ApkInstaller.canInstall(context)) {
                    UpdateUiState.ReadyToInstall(info, file)
                } else {
                    UpdateUiState.NeedsPermission(info, file)
                }
        }

    /**
     * Called when the hosting screen resumes.
     *
     * The "install unknown apps" screen is a separate Activity with no useful
     * result, so returning from it is the only signal we get. If the user
     * granted the permission while we were waiting, continue straight to the
     * install instead of making them tap again.
     */
    fun onResumed() {
        val state = _uiState.value
        if (state is UpdateUiState.NeedsPermission && ApkInstaller.canInstall(context)) {
            _uiState.value = UpdateUiState.ReadyToInstall(state.info, state.file)
        }
    }

    /** "Daha sonra" — silent checks skip this version from now on. */
    fun dismiss(version: String) {
        viewModelScope.launch { repository.dismiss(version) }
        _uiState.value = UpdateUiState.Idle
    }

    /** Closes the dialog without remembering anything. */
    fun close() {
        _uiState.value = UpdateUiState.Idle
    }

    /**
     * Fires the system installer. Re-checks the permission first: the user may
     * have granted it in Settings while we sat on [UpdateUiState.NeedsPermission].
     */
    fun install() {
        val state = _uiState.value
        val (info, file) = when (state) {
            is UpdateUiState.ReadyToInstall -> state.info to state.file
            is UpdateUiState.NeedsPermission -> state.info to state.file
            else -> return
        }
        if (!ApkInstaller.canInstall(context)) {
            _uiState.value = UpdateUiState.NeedsPermission(info, file)
            return
        }
        // The APK lives in the cache, which the system may clear between the
        // download and the tap that gets us here.
        val check = ApkInstaller.validate(context, file)
        if (check is ApkInstaller.Check.Problem) {
            _uiState.value = UpdateUiState.Error(check.message, info)
            return
        }
        if (ApkInstaller.install(context, file)) {
            // Installer is up; nothing more for the dialog to show.
            _uiState.value = UpdateUiState.Idle
        } else {
            _uiState.value = UpdateUiState.Error(
                context.getString(R.string.errors_update_install_screen_failed)
            )
        }
    }

    /** Opens the "install unknown apps" settings screen for this app. */
    fun openInstallPermissionSettings() {
        ApkInstaller.requestPermission(context)
    }

    /**
     * Retry from [UpdateUiState.Error]. Resumes the failed download when we
     * still know which release it was; otherwise re-runs the check.
     */
    fun retry() {
        val info = (_uiState.value as? UpdateUiState.Error)?.info
        if (info != null) {
            downloadJob = null
            startDownload(info)
        } else {
            _uiState.value = UpdateUiState.Idle
            check(force = true)
        }
    }
}
