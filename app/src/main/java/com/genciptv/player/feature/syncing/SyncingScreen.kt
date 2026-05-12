package com.genciptv.player.feature.syncing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextSecondary

/**
 * Full-screen "auto-sync gate" displayed on app open when the active
 * playlist is stale. Triggers the sync via [SyncingViewModel] and
 * fires [onComplete] when done — fail-open, so errors also complete.
 */
@Composable
fun SyncingScreen(
    onComplete: () -> Unit,
    viewModel: SyncingViewModel = hiltViewModel(),
) {
    val phase by viewModel.phase.collectAsStateWithLifecycle()

    LaunchedEffect(phase) {
        if (phase == SyncingViewModel.Phase.Done) {
            onComplete()
        }
    }

    SyncingScreenContent()
}

@Composable
private fun SyncingScreenContent() {
    val accent = LocalAccentPalette.current.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Genç IPTV",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            CircularProgressIndicator(
                color = accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Sağlayıcı içerikleri güncelleniyor…",
                style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Bu işlem birkaç saniye sürebilir.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F2EC)
@Composable
private fun SyncingScreenLightPreview() {
    GencIptvTheme(darkTheme = false) {
        SyncingScreenContent()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun SyncingScreenDarkPreview() {
    GencIptvTheme(darkTheme = true) {
        SyncingScreenContent()
    }
}
