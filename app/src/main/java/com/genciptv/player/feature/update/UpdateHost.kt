package com.genciptv.player.feature.update

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * [UpdateViewModel] scoped to the hosting Activity rather than the nav entry,
 * so a download started on Ana Sayfa survives a trip to Profil (and vice
 * versa) and both screens observe the same dialog state.
 *
 * Falls back to the default (nav-entry) scope when there is no Activity in the
 * context chain, which only happens in previews.
 */
@Composable
fun rememberUpdateViewModel(): UpdateViewModel {
    val activity = LocalContext.current.findComponentActivity()
    return if (activity != null) hiltViewModel(activity) else hiltViewModel()
}

/**
 * Mounts [UpdateDialog] and wires it to [viewModel]. Renders nothing while the
 * state is [UpdateUiState.Idle], so hosts can call this unconditionally.
 */
@Composable
fun UpdateDialogHost(viewModel: UpdateViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // Every time the app comes to the front, not just the first
                // time this screen is built. Tying the check to composition
                // meant an app left in the background for days never looked
                // again — the activity is not recreated on resume, so the
                // effect never re-ran. The repository's own interval keeps
                // this from turning into a request per app switch.
                Lifecycle.Event.ON_START -> viewModel.check(force = false)

                // Granting "install unknown apps" happens in a separate
                // Settings Activity that returns no result, so resuming is the
                // only signal that the user is back. If the permission is now
                // granted, the install continues by itself.
                Lifecycle.Event.ON_RESUME -> viewModel.onResumed()

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    UpdateDialog(
        state = state,
        onDownload = viewModel::startDownload,
        onDismissVersion = viewModel::dismiss,
        onClose = viewModel::close,
        onInstall = viewModel::install,
        onOpenPermissionSettings = viewModel::openInstallPermissionSettings,
        onRetry = viewModel::retry,
    )
}

private fun Context.findComponentActivity(): ComponentActivity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
