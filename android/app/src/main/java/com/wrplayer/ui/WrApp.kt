package com.wrplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.play.PlayScreen
import com.wrplayer.ui.sort.SortScreen
import com.wrplayer.ui.theme.WrTheme

/** Top-level app shell hosting the persistent mode toggle and the active mode's screen (PRD §3). */
@Composable
fun WrApp() {
    var mode by remember { mutableStateOf(AppMode.SORT) }
    val colors = WrTheme.colors

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        when (mode) {
            AppMode.SORT -> SortScreen(onModeChange = { mode = it }, onOpenSettings = {})
            AppMode.PLAY -> PlayScreen(onModeChange = { mode = it }, onOpenSettings = {})
        }
    }
}
