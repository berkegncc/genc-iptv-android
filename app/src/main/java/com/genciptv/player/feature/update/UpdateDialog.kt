package com.genciptv.player.feature.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GeistMonoFamily
import com.genciptv.player.core.designsystem.InstrumentSerifFamily
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextTertiary
import com.genciptv.player.data.model.UpdateInfo

/**
 * Single dialog covering every [UpdateUiState]. Renders nothing for
 * [UpdateUiState.Idle], so both hosts can mount it unconditionally.
 */
@Composable
fun UpdateDialog(
    state: UpdateUiState,
    onDownload: (UpdateInfo) -> Unit,
    onDismissVersion: (String) -> Unit,
    onClose: () -> Unit,
    onInstall: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    if (state is UpdateUiState.Idle) return

    val accent = LocalAccentPalette.current

    // Downloading must not be cancelled by a stray tap or back press.
    val locked = state is UpdateUiState.Downloading
    val properties = DialogProperties(
        dismissOnBackPress = !locked,
        dismissOnClickOutside = !locked,
    )

    // The installer hand-off is automatic once the APK is on disk.
    LaunchedEffect(state) {
        if (state is UpdateUiState.ReadyToInstall) onInstall()
    }

    AlertDialog(
        onDismissRequest = { if (!locked) onClose() },
        properties = properties,
        title = { DialogTitle(state) },
        text = { DialogBody(state) },
        confirmButton = {
            when (state) {
                is UpdateUiState.Available -> DialogButton(
                    text = stringResource(R.string.update_button_update),
                    color = accent.primary,
                    onClick = { onDownload(state.info) },
                )
                is UpdateUiState.NeedsPermission -> DialogButton(
                    text = stringResource(R.string.update_button_grant_permission),
                    color = accent.primary,
                    onClick = onOpenPermissionSettings,
                )
                is UpdateUiState.Error -> DialogButton(
                    text = stringResource(R.string.action_retry),
                    color = accent.primary,
                    onClick = onRetry,
                )
                is UpdateUiState.UpToDate -> DialogButton(
                    text = stringResource(R.string.action_ok),
                    color = accent.primary,
                    onClick = onClose,
                )
                // Downloading shows no buttons; ReadyToInstall is transient.
                else -> Unit
            }
        },
        dismissButton = {
            when (state) {
                is UpdateUiState.Available -> DialogButton(
                    text = stringResource(R.string.update_button_later),
                    color = TextTertiary,
                    onClick = { onDismissVersion(state.info.versionName) },
                )
                is UpdateUiState.NeedsPermission,
                is UpdateUiState.Error -> DialogButton(
                    text = stringResource(R.string.action_close),
                    color = TextTertiary,
                    onClick = onClose,
                )
                else -> Unit
            }
        },
    )
}

// ── Pieces ───────────────────────────────────────────────────────────────────

@Composable
private fun DialogTitle(state: UpdateUiState) {
    val text = when (state) {
        is UpdateUiState.Available -> stringResource(R.string.update_title_available)
        is UpdateUiState.Downloading -> stringResource(R.string.update_title_downloading)
        is UpdateUiState.ReadyToInstall -> stringResource(R.string.update_title_installing)
        is UpdateUiState.NeedsPermission -> stringResource(R.string.update_title_needs_permission)
        is UpdateUiState.UpToDate -> stringResource(R.string.update_title_up_to_date)
        is UpdateUiState.Error -> stringResource(R.string.state_error_title)
        UpdateUiState.Idle -> return
    }
    Text(
        text = text,
        style = TextStyle(
            fontFamily = InstrumentSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            color = TextPrimary,
        ),
    )
}

@Composable
private fun DialogBody(state: UpdateUiState) {
    val accent = LocalAccentPalette.current
    when (state) {
        is UpdateUiState.Available -> Column(modifier = Modifier.fillMaxWidth()) {
            VersionLine(
                version = state.info.versionName,
                trailing = if (state.info.sizeBytes > 0) {
                    "· %.1f MB".format(state.info.sizeMb)
                } else {
                    null
                },
            )
            // Release name, unless it just repeats the tag we already show.
            val releaseTitle = state.info.title.trim()
            if (releaseTitle.isNotBlank() &&
                releaseTitle.trimStart('v', 'V') != state.info.versionName
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = releaseTitle,
                    style = bodyStyle().copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            val changelog = cleanChangelog(state.info.changelog)
            if (changelog.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = changelog,
                    style = bodyStyle(),
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
        }

        is UpdateUiState.Downloading -> Column(modifier = Modifier.fillMaxWidth()) {
            VersionLine(
                version = state.info.versionName,
                trailing = stringResource(R.string.update_download_progress_suffix, state.percent),
            )
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { state.percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accent.primary,
                trackColor = accent.soft,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.update_body_downloading),
                style = bodyStyle(),
            )
        }

        is UpdateUiState.ReadyToInstall -> Text(
            text = stringResource(R.string.update_body_ready_to_install),
            style = bodyStyle(),
        )

        is UpdateUiState.NeedsPermission -> Text(
            text = stringResource(R.string.update_body_needs_permission),
            style = bodyStyle(),
        )

        is UpdateUiState.UpToDate -> VersionLine(
            version = state.version,
            leading = stringResource(R.string.update_up_to_date_leading),
        )

        is UpdateUiState.Error -> Text(text = state.message, style = bodyStyle())

        UpdateUiState.Idle -> Unit
    }
}

/** Version numbers are technical text — Geist Mono, per the design language. */
@Composable
private fun VersionLine(
    version: String,
    leading: String? = null,
    trailing: String? = null,
) {
    val accent = LocalAccentPalette.current
    Text(
        text = buildString {
            leading?.let { append(it).append(' ') }
            append('v').append(version)
            trailing?.let { append(' ').append(it) }
        },
        style = TextStyle(
            fontFamily = GeistMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            letterSpacing = 0.02.sp,
            color = accent.primary,
        ),
    )
}

@Composable
private fun bodyStyle(): TextStyle = TextStyle(
    fontFamily = GeistFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    color = TextSecondary,
)

@Composable
private fun DialogButton(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                color = color,
            ),
        )
    }
}

// ── Changelog ────────────────────────────────────────────────────────────────

/**
 * Just enough markdown flattening for a release body: bullets become "•" and
 * heading hashes are dropped. Deliberately not a parser — release notes we
 * write ourselves, and a half-rendered table is worse than plain text.
 */
internal fun cleanChangelog(raw: String): String =
    raw.lineSequence()
        .map { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("- ") -> "• " + trimmed.removePrefix("- ").trim()
                trimmed.startsWith("* ") -> "• " + trimmed.removePrefix("* ").trim()
                trimmed.startsWith("#") -> trimmed.trimStart('#').trim()
                else -> trimmed
            }
        }
        .joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
