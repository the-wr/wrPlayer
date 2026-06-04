package com.wrplayer.ui.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.common.WrTopBar
import com.wrplayer.ui.nowplaying.NowPlayingScreen
import com.wrplayer.ui.nowplaying.NowPlayingUi
import com.wrplayer.ui.nowplaying.SortPanel

/** TEMPORARY Phase 8 host to view the Sort Mode shell. Replaced by SortViewModel wiring. */
@Composable
fun SortModeDemo(modifier: Modifier = Modifier) {
    var plusSelected by remember { mutableStateOf(false) }
    NowPlayingScreen(
        ui = NowPlayingUi(
            title = "Starfighter",
            artist = "Eric Speed",
            album = "Single",
            positionMs = 72_000,
            durationMs = 215_000,
            isPlaying = true,
            showPrevious = false,
        ),
        onPlayPause = {},
        onPrevious = {},
        onNext = {},
        onSeekFraction = {},
        topBar = { WrTopBar(AppMode.SORT, count = 42, scanning = false, onModeChange = {}, onOpenSettings = {}) },
        bottomPanel = {
            SortPanel(
                plusSelected = plusSelected,
                onMinusOne = {},
                onSkip = {},
                onPlusOne = { plusSelected = !plusSelected },
            )
        },
    )
}
