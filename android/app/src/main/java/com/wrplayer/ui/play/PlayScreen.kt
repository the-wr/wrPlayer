package com.wrplayer.ui.play

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.common.WrTopBar
import com.wrplayer.ui.nowplaying.NowPlayingScreen
import com.wrplayer.ui.nowplaying.PlayPanel
import com.wrplayer.ui.queue.QueueEditorScreen
import com.wrplayer.ui.tagsheet.TagSheet
import com.wrplayer.ui.tagsheet.TagSheetCallbacks
import com.wrplayer.ui.theme.WrTheme

private enum class PlayOverlay { NONE, QUEUE_EDITOR, CURRENT_QUEUE }

/** Play Mode screen (PRD §4.1): Now Playing + Play panel, with the Queue Editor / Current Queue overlays. */
@Composable
fun PlayScreen(
    onModeChange: (AppMode) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: PlayViewModel = hiltViewModel(),
) {
    val colors = WrTheme.colors
    val state by viewModel.state.collectAsState()
    var overlay by remember { mutableStateOf(PlayOverlay.NONE) }

    val topBar: @Composable () -> Unit = {
        WrTopBar(AppMode.PLAY, state.queueCount, scanning = false, onModeChange = onModeChange, onOpenSettings = onOpenSettings)
    }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        val np = state.nowPlaying
        if (np != null) {
            NowPlayingScreen(
                ui = np,
                onPlayPause = viewModel::onPlayPause,
                onPrevious = viewModel::onPrevious,
                onNext = viewModel::onNext,
                onSeekFraction = viewModel::onSeekFraction,
                topBar = topBar,
                bottomPanel = {
                    PlayPanel(
                        tags = state.tags,
                        onEdit = viewModel::openEdit,
                        onQueueEditor = { overlay = PlayOverlay.QUEUE_EDITOR },
                        onCurrentQueue = { overlay = PlayOverlay.CURRENT_QUEUE },
                    )
                },
            )
        } else {
            EmptyQueueState(topBar, onOpenQueueEditor = { overlay = PlayOverlay.QUEUE_EDITOR })
        }

        when (overlay) {
            PlayOverlay.QUEUE_EDITOR ->
                QueueEditorScreen(onDismiss = { overlay = PlayOverlay.NONE }, onPlaybackStarted = { overlay = PlayOverlay.NONE })
            PlayOverlay.CURRENT_QUEUE ->
                CurrentQueuePlaceholder(onDismiss = { overlay = PlayOverlay.NONE })
            PlayOverlay.NONE -> {}
        }

        state.tagSheet?.let { sheet ->
            Box(Modifier.fillMaxSize().background(Color(0x6B000000)), contentAlignment = Alignment.BottomCenter) {
                TagSheet(sheet, tagSheetCallbacks(viewModel))
            }
        }
    }
}

@Composable
private fun EmptyQueueState(topBar: @Composable () -> Unit, onOpenQueueEditor: () -> Unit) {
    val colors = WrTheme.colors
    Column(Modifier.fillMaxSize()) {
        topBar()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(colors.surface3), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = colors.accent, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.size(22.dp))
            Text("Queue is empty", color = colors.text, fontSize = 21.sp, fontWeight = FontWeight(800))
            Spacer(Modifier.size(8.dp))
            Text("Build a queue by tag in the Queue Editor.", color = colors.text2, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.size(28.dp))
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.accent).clickable { onOpenQueueEditor() }.padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) { Text("Open Queue Editor", color = colors.accentFg, fontSize = 15.sp, fontWeight = FontWeight(700)) }
        }
    }
}

/** Placeholder for the Current Queue screen (built in Phase 10); dismissed by back-nav. */
@Composable
private fun CurrentQueuePlaceholder(onDismiss: () -> Unit) {
    val colors = WrTheme.colors
    BackHandler { onDismiss() }
    Column(Modifier.fillMaxSize().background(colors.surface).padding(24.dp)) {
        Text("Current Queue", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight(700))
        Spacer(Modifier.size(12.dp))
        Text("Coming in Phase 10.", color = colors.text3, fontSize = 14.sp)
    }
}

private fun Set<String>.toggle(v: String) = if (v in this) this - v else this + v

private fun tagSheetCallbacks(vm: PlayViewModel) = TagSheetCallbacks(
    onTitle = { v -> vm.updateTagSheet { it.copy(title = v) } },
    onArtist = { v -> vm.updateTagSheet { it.copy(artist = v) } },
    onAlbum = { v -> vm.updateTagSheet { it.copy(album = v) } },
    onToggleGenre = { g -> vm.updateTagSheet { it.copy(genres = it.genres.toggle(g)) } },
    onAddGenre = { g -> vm.updateTagSheet { it.copy(genres = it.genres + g, genreOptions = it.genreOptions + g) } },
    onToggleMood = { m -> vm.updateTagSheet { it.copy(moods = it.moods.toggle(m)) } },
    onAddMood = { m -> vm.updateTagSheet { it.copy(moods = it.moods + m, moodOptions = it.moodOptions + m) } },
    onSetPace = { p -> vm.updateTagSheet { it.copy(pace = p) } },
    onSetBpm = { b -> vm.updateTagSheet { it.copy(bpm = b) } },
    onToggleLabel = { l -> vm.updateTagSheet { it.copy(labels = it.labels.toggle(l)) } },
    onAddLabel = { l -> vm.updateTagSheet { it.copy(labels = it.labels + l, labelOptions = it.labelOptions + l) } },
    onConfirm = vm::confirmTagSheet,
    onDismiss = vm::dismissTagSheet,
)
