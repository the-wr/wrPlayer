package com.wrplayer.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/** TEMPORARY debug UI for Phase 5 (replaced by Now Playing in Phase 8). */
@Composable
fun DebugPlaybackScreen(
    modifier: Modifier = Modifier,
    viewModel: DebugPlaybackViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("wrPlayer — playback debug")
        Text("connected=${state.isConnected}  playing=${state.isPlaying}  queue=${state.queueSize}")
        Text("title=${state.title ?: "-"}")
        Text("pos=${state.positionMs / 1000}s / ${state.durationMs / 1000}s")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::previous) { Text("Prev") }
            Button(onClick = viewModel::playPause) { Text("Play/Pause") }
            Button(onClick = viewModel::next) { Text("Next") }
        }
        Button(onClick = viewModel::playSamples) { Text("Load samples") }
    }
}
