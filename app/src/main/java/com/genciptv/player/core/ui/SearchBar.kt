package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Border
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.Surface
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextTertiary

private val SearchBarRadius = RoundedCornerShape(50.dp)
private val SearchBarHeight = 42.dp

/**
 * Search bar matching the HTML `.search-bar` spec:
 *   - Rounded pill (radius 50), Surface background, 1.5dp border
 *   - Leading search icon (opacity 0.38), placeholder text
 *   - Optional mic button with accent-colour circular background
 */
@Composable
fun GencSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Kanal, film veya dizi ara…",
    showMic: Boolean = true,
    onMicClick: () -> Unit = {},
    /** When non-null, the bar becomes a tap target (no keyboard) that fires this callback. */
    onClick: (() -> Unit)? = null,
    /** Attaches to the inner text field so callers can request focus programmatically. */
    focusRequester: FocusRequester? = null,
) {
    val accent = LocalAccentPalette.current.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(SearchBarHeight)
            .shadow(
                elevation = 2.dp,
                shape = SearchBarRadius,
                ambientColor = Color(0xFF12131A).copy(alpha = 0.06f),
                spotColor = Color(0xFF12131A).copy(alpha = 0.04f)
            )
            .clip(SearchBarRadius)
            .background(Surface)
            .border(width = 1.5.dp, color = Border, shape = SearchBarRadius)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 9.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextPrimary.copy(alpha = 0.38f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        if (onClick != null) {
            Text(
                text = query.ifEmpty { placeholder },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (query.isEmpty()) TextTertiary else TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
        } else {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(accent),
                modifier = Modifier
                    .weight(1f)
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        if (showMic) {
            Spacer(Modifier.width(6.dp))
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(accent)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Sesli arama",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SearchBarEmptyPreview() {
    GencIptvTheme {
        GencSearchBar(
            query = "",
            onQueryChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SearchBarWithQueryPreview() {
    GencIptvTheme {
        GencSearchBar(
            query = "TRT 1",
            onQueryChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F6FA)
@Composable
private fun SearchBarNoMicPreview() {
    GencIptvTheme {
        GencSearchBar(
            query = "",
            onQueryChange = {},
            showMic = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
