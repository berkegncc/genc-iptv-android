package com.genciptv.player.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.AppLogoIcon
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextSecondary
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.ui.readableContentWidth

// ── Stateful screen ───────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) onComplete()
    }

    OnboardingContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

// ── Stateless content composable ──────────────────────────────────────────────

@Composable
fun OnboardingContent(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        when {
            state.isLoading -> LoadingOverlay(channelCount = state.channelCountLoaded)
            state.step == 1 -> Step1Welcome(state = state, onAction = onAction)
            else             -> Step2Playlist(state = state, onAction = onAction)
        }

        // Error dialog
        if (state.error != null) {
            AlertDialog(
                onDismissRequest = { onAction(OnboardingAction.DismissError) },
                title = { Text(stringResource(R.string.onboarding_error_title)) },
                text = { Text(state.error) },
                confirmButton = {
                    TextButton(onClick = { onAction(OnboardingAction.DismissError) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            )
        }
    }
}

// ── Step 1: Welcome ───────────────────────────────────────────────────────────

@Composable
private fun Step1Welcome(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val canProceed = state.displayName.trim().length >= 2

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .readableContentWidth(480.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 28.dp, vertical = 56.dp),
    ) {
        Spacer(Modifier.height(32.dp))

        AppLogoIcon(size = 120.dp)

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.displayMedium.copy(color = TextPrimary),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = { onAction(OnboardingAction.SetDisplayName(it)) },
            label = { Text(stringResource(R.string.onboarding_name_label)) },
            placeholder = { Text(stringResource(R.string.onboarding_name_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                focusedLabelColor = accent,
                cursorColor = accent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onAction(OnboardingAction.GoToStep2) },
            enabled = canProceed,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_continue_button),
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
            )
        }
    }
}

// ── Step 2: Playlist Entry ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Playlist(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val tabTitles = listOf("M3U URL", "Xtream Codes")

    Column(
        modifier = Modifier
            .readableContentWidth(560.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 24.dp),
    ) {
        // Back button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, top = 12.dp),
        ) {
            IconButton(onClick = { onAction(OnboardingAction.BackToStep1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = accent,
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_playlist_title),
                style = MaterialTheme.typography.displaySmall.copy(color = TextPrimary),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_playlist_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            )
            Spacer(Modifier.height(20.dp))
        }

        // Tab row
        SecondaryTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Bg,
            contentColor = accent,
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { onAction(OnboardingAction.SelectTab(index)) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        when (state.selectedTab) {
            0 -> M3uTab(state = state, onAction = onAction)
            1 -> XtreamTab(state = state, onAction = onAction)
        }
    }
}

// ── M3U tab content ───────────────────────────────────────────────────────────

@Composable
private fun M3uTab(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val canSubmit = state.m3uForm.url.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        OnboardingTextField(
            value = state.m3uForm.name,
            onValueChange = { onAction(OnboardingAction.UpdateM3uName(it)) },
            label = stringResource(R.string.onboarding_list_name_label),
            placeholder = stringResource(R.string.onboarding_m3u_name_placeholder),
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))

        OnboardingTextField(
            value = state.m3uForm.url,
            onValueChange = { onAction(OnboardingAction.UpdateM3uUrl(it)) },
            label = "M3U URL",
            placeholder = "http://...",
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))

        OnboardingTextField(
            value = state.m3uForm.epgUrl,
            onValueChange = { onAction(OnboardingAction.UpdateM3uEpgUrl(it)) },
            label = stringResource(R.string.onboarding_epg_url_label),
            placeholder = stringResource(R.string.onboarding_epg_url_placeholder),
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onAction(OnboardingAction.SubmitM3u) },
            enabled = canSubmit,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_m3u_submit_button),
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
            )
        }
    }
}

// ── Xtream tab content ────────────────────────────────────────────────────────

@Composable
private fun XtreamTab(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val form = state.xtreamForm
    val canSubmit = form.serverUrl.isNotBlank() && form.username.isNotBlank() && form.password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        OnboardingTextField(
            value = form.name,
            onValueChange = { onAction(OnboardingAction.UpdateXtreamName(it)) },
            label = stringResource(R.string.onboarding_list_name_label),
            placeholder = "Xtream",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))

        OnboardingTextField(
            value = form.serverUrl,
            onValueChange = { onAction(OnboardingAction.UpdateXtreamServerUrl(it)) },
            label = stringResource(R.string.onboarding_server_url_label),
            placeholder = "http://host:port",
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))

        OnboardingTextField(
            value = form.username,
            onValueChange = { onAction(OnboardingAction.UpdateXtreamUsername(it)) },
            label = stringResource(R.string.onboarding_username_label),
            placeholder = "",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))

        // Password field with visibility toggle
        OutlinedTextField(
            value = form.password,
            onValueChange = { onAction(OnboardingAction.UpdateXtreamPassword(it)) },
            label = { Text(stringResource(R.string.onboarding_password_label)) },
            singleLine = true,
            visualTransformation = if (form.passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            trailingIcon = {
                IconButton(onClick = { onAction(OnboardingAction.ToggleXtreamPasswordVisibility) }) {
                    Icon(
                        imageVector = if (form.passwordVisible) Icons.Default.VisibilityOff
                                      else Icons.Default.Visibility,
                        contentDescription = if (form.passwordVisible) stringResource(R.string.onboarding_password_hide) else stringResource(R.string.onboarding_password_show),
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                focusedLabelColor = accent,
                cursorColor = accent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onAction(OnboardingAction.SubmitXtream) },
            enabled = canSubmit,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_xtream_submit_button),
                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
            )
        }
    }
}

// ── Loading overlay ───────────────────────────────────────────────────────────

@Composable
private fun LoadingOverlay(channelCount: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_loading_playlist),
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
            )
            if (channelCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.onboarding_channels_loaded_count, channelCount),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                )
            }
        }
    }
}

// ── Reusable text field ───────────────────────────────────────────────────────

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    val accent = LocalAccentPalette.current.primary
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            focusedLabelColor = accent,
            cursorColor = accent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun Step1Preview() {
    GencIptvTheme {
        OnboardingContent(
            state = OnboardingUiState(step = 1, displayName = "Mehmet"),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun Step2M3uPreview() {
    GencIptvTheme {
        OnboardingContent(
            state = OnboardingUiState(step = 2, selectedTab = 0),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun Step2XtreamPreview() {
    GencIptvTheme {
        OnboardingContent(
            state = OnboardingUiState(step = 2, selectedTab = 1),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun LoadingPreview() {
    GencIptvTheme {
        OnboardingContent(
            state = OnboardingUiState(isLoading = true, channelCountLoaded = 245),
            onAction = {},
        )
    }
}
