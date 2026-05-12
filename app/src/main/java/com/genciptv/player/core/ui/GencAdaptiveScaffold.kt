package com.genciptv.player.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.genciptv.player.core.designsystem.Bg
import com.genciptv.player.core.designsystem.WindowSize

/**
 * Drop-in Scaffold replacement that swaps between a bottom nav (phone) and
 * a side rail (tablet / landscape) based on [WindowSize.isTablet].
 *
 * Call sites don't need to know which mode is active — they just provide the
 * current nav item and content lambda, identical to [Scaffold]'s API.
 */
@Composable
fun GencAdaptiveScaffold(
    current: GencNavItem?,
    onItemClick: (GencNavItem) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Bg,
    content: @Composable (PaddingValues) -> Unit,
) {
    val isTablet = WindowSize.isTablet

    if (isTablet) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(containerColor),
        ) {
            GencNavRail(
                current = current,
                onItemClick = onItemClick,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                content(PaddingValues(0.dp))
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            containerColor = containerColor,
            bottomBar = {
                GencBottomNav(
                    current = current,
                    onItemClick = onItemClick,
                )
            },
            content = content,
        )
    }
}
