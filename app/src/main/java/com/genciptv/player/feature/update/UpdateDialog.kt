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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
                    text = "Güncelle",
                    color = accent.primary,
                    onClick = { onDownload(state.info) },
                )
                is UpdateUiState.NeedsPermission -> DialogButton(
                    text = "İzin ver",
                    color = accent.primary,
                    onClick = onOpenPermissionSettings,
                )
                is UpdateUiState.Error -> DialogButton(
                    text = "Tekrar dene",
                    color = accent.primary,
                    onClick = onRetry,
                )
                is UpdateUiState.UpToDate -> DialogButton(
                    text = "Tamam",
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
                    text = "Daha sonra",
                    color = TextTertiary,
                    onClick = { onDismissVersion(state.info.versionName) },
                )
                is UpdateUiState.NeedsPermission,
                is UpdateUiState.Error -> DialogButton(
                    text = "Kapat",
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
        is UpdateUiState.Available -> "Yeni sürüm hazır"
        is UpdateUiState.Downloading -> "İndiriliyor"
        is UpdateUiState.ReadyToInstall -> "Kurulum başlatılıyor"
        is UpdateUiState.NeedsPermission -> "Kurulum için izin gerekli"
        is UpdateUiState.UpToDate -> "Güncelsiniz"
        is UpdateUiState.Error -> "Bir sorun oldu"
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
            VersionLine(version = state.info.versionName, trailing = "· %${state.percent}")
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
                text = "Güncelleme indiriliyor, uygulamayı kapatmayın.",
                style = bodyStyle(),
            )
        }

        is UpdateUiState.ReadyToInstall -> Text(
            text = "İndirme tamamlandı. Kurulum ekranı açılıyor.",
            style = bodyStyle(),
        )

        is UpdateUiState.NeedsPermission -> Text(
            text = "Güncellemeyi kurabilmek için bu uygulamaya \"bilinmeyen " +
                "kaynaklardan uygulama yükleme\" izni vermeniz gerekiyor. " +
                "İzni verdikten sonra buraya dönüp tekrar deneyin.",
            style = bodyStyle(),
        )

        is UpdateUiState.UpToDate -> VersionLine(
            version = state.version,
            leading = "Güncel sürümdesiniz",
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
