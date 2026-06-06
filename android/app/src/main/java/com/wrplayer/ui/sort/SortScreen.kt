package com.wrplayer.ui.sort

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.wrplayer.ui.nowplaying.SortPanel
import com.wrplayer.ui.tagsheet.TagSheet
import com.wrplayer.ui.tagsheet.TagSheetCallbacks
import com.wrplayer.ui.theme.WrTheme

/** Sort Mode screen: hosts the picker, the Now Playing shell + Sort panel, the Tag Sheet, and the end state. */
@Composable
fun SortScreen(
    onModeChange: (AppMode) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SortViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.enter() }
    val colors = WrTheme.colors

    val topBar: @Composable () -> Unit = {
        WrTopBar(AppMode.SORT, state.remaining, scanning = state.scanning, onModeChange = onModeChange, onOpenSettings = onOpenSettings)
    }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        val np = state.nowPlaying
        when {
            state.phase == SortPhase.END ->
                EndState(state.emptyInbox, topBar, onReenter = viewModel::enter, onSwitchToPlay = { onModeChange(AppMode.PLAY) }, onSeed = viewModel::seedDemoInbox)

            np != null ->
                NowPlayingScreen(
                    ui = np,
                    onPlayPause = viewModel::onPlayPause,
                    onPrevious = {},
                    onNext = viewModel::onNext,
                    onSeekFraction = viewModel::onSeekFraction,
                    topBar = topBar,
                    bottomPanel = {
                        SortPanel(
                            plusSelected = state.plusSelected,
                            onMinusOne = viewModel::onMinusOne,
                            onSkip = viewModel::onSkip,
                            onPlusOne = viewModel::onPlusOne,
                        )
                    },
                )

            else -> Column { topBar() }
        }

        if (state.phase == SortPhase.PICKER) {
            SortOrderPicker(
                initial = state.pickerOrder,
                trackCount = state.inboxCount,
                onConfirm = viewModel::confirmOrder,
                onDismiss = { onModeChange(AppMode.PLAY) },
            )
        }

        state.tagSheet?.let { sheet ->
            Box(Modifier.fillMaxSize().background(Color(0x6B000000)), contentAlignment = Alignment.BottomCenter) {
                TagSheet(sheet, tagSheetCallbacks(viewModel))
            }
        }
    }
}

private fun Set<String>.toggle(v: String) = if (v in this) this - v else this + v

private fun tagSheetCallbacks(vm: SortViewModel) = TagSheetCallbacks(
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

@Composable
private fun EndState(
    emptyInbox: Boolean,
    topBar: @Composable () -> Unit,
    onReenter: () -> Unit,
    onSwitchToPlay: () -> Unit,
    onSeed: () -> Unit,
) {
    val colors = WrTheme.colors
    Column(Modifier.fillMaxSize()) {
        topBar()
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(84.dp).clip(CircleShape).background(colors.surface3), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Favorite, contentDescription = null, tint = colors.accent, modifier = Modifier.size(38.dp))
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(22.dp))
            Text(
                if (emptyInbox) "Inbox is empty" else "Queue cleared",
                color = colors.text, fontSize = 21.sp, fontWeight = FontWeight(800),
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text(
                if (emptyInbox) "Add files to a watched folder, or seed demo tracks." else "You worked through this sort queue.",
                color = colors.text2, fontSize = 14.sp, textAlign = TextAlign.Center,
            )
            androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
            PrimaryButton(if (emptyInbox) "Seed demo tracks" else "Re-enter Sort Mode") {
                if (emptyInbox) onSeed() else onReenter()
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(10.dp))
            SecondaryButton("Switch to Play Mode", onSwitchToPlay)
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    val colors = WrTheme.colors
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.accent).clickable { onClick() }.padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = colors.accentFg, fontSize = 15.sp, fontWeight = FontWeight(700)) }
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit) {
    val colors = WrTheme.colors
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface3).clickable { onClick() }.padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = colors.text, fontSize = 15.sp, fontWeight = FontWeight(700)) }
}
