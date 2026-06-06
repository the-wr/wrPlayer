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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.play.PlayScreen
import com.wrplayer.ui.settings.SettingsScreen
import com.wrplayer.ui.sort.SortScreen
import com.wrplayer.ui.theme.WrTheme

/** Top-level app shell hosting the persistent mode toggle, Settings, and launch routing (PRD §3). */
@Composable
fun WrApp(shell: AppShellViewModel = hiltViewModel()) {
    val colors = WrTheme.colors
    val shellState by shell.state.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        if (shellState.resolved) {
            AppContent(startInSettings = shellState.startInSettings)
        }
    }
}

@Composable
private fun AppContent(startInSettings: Boolean) {
    var mode by remember { mutableStateOf(AppMode.PLAY) }
    var showSettings by remember { mutableStateOf(startInSettings) }
    val openSettings = { showSettings = true }

    when (mode) {
        AppMode.SORT -> SortScreen(onModeChange = { mode = it }, onOpenSettings = openSettings)
        AppMode.PLAY -> PlayScreen(onModeChange = { mode = it }, onOpenSettings = openSettings)
    }

    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
    }
}
