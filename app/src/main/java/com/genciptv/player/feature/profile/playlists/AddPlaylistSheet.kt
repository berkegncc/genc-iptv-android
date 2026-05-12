package com.genciptv.player.feature.profile.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.LocalAccentPalette

// ── Add Playlist Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistSheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAddM3u: (name: String, url: String, epgUrl: String?) -> Unit,
    onAddXtream: (name: String, serverUrl: String, username: String, password: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = LocalAccentPalette.current.primary

    var selectedTab by remember { mutableIntStateOf(0) }
    // M3U form state
    var m3uName by remember { mutableStateOf("") }
    var m3uUrl by remember { mutableStateOf("") }
    var m3uEpgUrl by remember { mutableStateOf("") }
    // Xtream form state
    var xtName by remember { mutableStateOf("") }
    var xtServer by remember { mutableStateOf("") }
    var xtUser by remember { mutableStateOf("") }
    var xtPass by remember { mutableStateOf("") }
    var xtPassVisible by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Yeni Playlist Ekle",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Bg,
                contentColor = accent,
            ) {
                listOf("M3U URL", "Xtream Codes").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    color = accent,
                )
            } else {
                when (selectedTab) {
                    0 -> M3uForm(
                        name = m3uName,
                        url = m3uUrl,
                        epgUrl = m3uEpgUrl,
                        onNameChange = { m3uName = it },
                        onUrlChange = { m3uUrl = it },
                        onEpgUrlChange = { m3uEpgUrl = it },
                        onSubmit = { onAddM3u(m3uName, m3uUrl, m3uEpgUrl.ifBlank { null }) },
                    )
                    1 -> XtreamForm(
                        name = xtName,
                        serverUrl = xtServer,
                        username = xtUser,
                        password = xtPass,
                        passwordVisible = xtPassVisible,
                        onNameChange = { xtName = it },
                        onServerUrlChange = { xtServer = it },
                        onUsernameChange = { xtUser = it },
                        onPasswordChange = { xtPass = it },
                        onPasswordVisibilityToggle = { xtPassVisible = !xtPassVisible },
                        onSubmit = { onAddXtream(xtName, xtServer, xtUser, xtPass) },
                    )
                }
            }
        }
    }
}

@Composable
private fun M3uForm(
    name: String,
    url: String,
    epgUrl: String,
    onNameChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onEpgUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val canSubmit = url.isNotBlank()

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SheetTextField(
            value = name, onValueChange = onNameChange,
            label = "Liste Adı", placeholder = "Playlist 1",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))
        SheetTextField(
            value = url, onValueChange = onUrlChange,
            label = "M3U URL", placeholder = "http://...",
            keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))
        SheetTextField(
            value = epgUrl, onValueChange = onEpgUrlChange,
            label = "EPG URL (opsiyonel)", placeholder = "XMLTV link",
            keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Yükle", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
        }
    }
}

@Composable
private fun XtreamForm(
    name: String,
    serverUrl: String,
    username: String,
    password: String,
    passwordVisible: Boolean,
    onNameChange: (String) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
) {
    val accent = LocalAccentPalette.current.primary
    val canSubmit = serverUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SheetTextField(
            value = name, onValueChange = onNameChange,
            label = "Liste Adı", placeholder = "Xtream",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))
        SheetTextField(
            value = serverUrl, onValueChange = onServerUrlChange,
            label = "Sunucu URL", placeholder = "http://host:port",
            keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))
        SheetTextField(
            value = username, onValueChange = onUsernameChange,
            label = "Kullanıcı Adı", placeholder = "",
            imeAction = ImeAction.Next,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Şifre") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                                      else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Gizle" else "Göster",
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
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Bağlan", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
        }
    }
}

@Composable
private fun SheetTextField(
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
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            focusedLabelColor = accent,
            cursorColor = accent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
