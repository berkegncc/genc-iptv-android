package com.genciptv.player.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genciptv.player.R
import com.genciptv.player.core.designsystem.BgElev
import com.genciptv.player.core.designsystem.GeistFamily
import com.genciptv.player.core.designsystem.GencIptvTheme
import com.genciptv.player.core.designsystem.Line
import com.genciptv.player.core.designsystem.LocalAccentPalette
import com.genciptv.player.core.designsystem.TextPrimary
import com.genciptv.player.core.designsystem.TextTertiary

/**
 * Says out loud that the catalogue is stale because the user asked to sync over
 * Wi-Fi only and this connection is metered.
 *
 * Without it the app is indistinguishable from one that has quietly stopped
 * updating: the background job waits for an unmetered network and the gate on
 * app open stands down, both correctly, and neither leaves a trace the user can
 * see. [onRefresh] is the deliberate override — the user asking counts as
 * consent, which is why it syncs regardless of the connection.
 */
@Composable
fun RefreshHeldNotice(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalAccentPalette.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgElev)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_refresh_held_title),
                fontFamily = GeistFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_refresh_held_body),
                fontFamily = GeistFamily,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = TextTertiary,
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = stringResource(R.string.home_refresh_held_action),
            fontFamily = GeistFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = accent.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRefresh)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E1213)
@Composable
private fun RefreshHeldNoticePreview() {
    GencIptvTheme(darkTheme = true) {
        RefreshHeldNotice(onRefresh = {}, modifier = Modifier.padding(16.dp))
    }
}
