package com.genciptv.player.feature.profile.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.util.AppLanguage
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

/**
 * Language picker.
 *
 * Three options rather than two: "System default" has to stay reachable, or a
 * user who once forced a language could never hand the choice back to the
 * phone. Picking one recreates the activity, so the dialog does not need to
 * tell anyone to restart.
 */
@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current.primary
    // Read once. Applying a language recreates the activity, which takes this
    // dialog with it, so there is no state here to keep in sync afterwards.
    val context = LocalContext.current
    val activity = context as? Activity
    var selected by remember { mutableStateOf(AppLanguage.current(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgElev,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.language_title),
                style = TextStyle(
                    fontFamily = GeistFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                ),
            )
        },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    val label = when (language) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_option_system)
                        AppLanguage.TURKISH -> stringResource(R.string.language_option_turkish)
                        AppLanguage.ENGLISH -> stringResource(R.string.language_option_english)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = language
                                onDismiss()
                                activity?.let { AppLanguage.apply(it, language) }
                            }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontFamily = GeistFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = TextPrimary,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(12.dp))
                        if (language == selected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_close),
                    style = TextStyle(fontFamily = GeistFamily, color = accent),
                )
            }
        },
    )
}
