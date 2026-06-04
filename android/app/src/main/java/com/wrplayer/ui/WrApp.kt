package com.wrplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.common.WrTopBar
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
            AppMode.PLAY -> PlayPlaceholder(onModeChange = { mode = it })
        }
    }
}

/** Temporary Play Mode placeholder until Phase 9 wires the Queue Editor + Play panel. */
@Composable
private fun PlayPlaceholder(onModeChange: (AppMode) -> Unit) {
    val colors = WrTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        WrTopBar(AppMode.PLAY, count = 0, scanning = false, onModeChange = onModeChange, onOpenSettings = {})
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Play Mode — coming in Phase 9", color = colors.text3, fontSize = 15.sp)
        }
    }
}
