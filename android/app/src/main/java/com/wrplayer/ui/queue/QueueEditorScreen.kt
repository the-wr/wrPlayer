package com.wrplayer.ui.queue

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wrplayer.domain.facet.FacetValue
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.data.repo.SavedPreset
import com.wrplayer.ui.theme.ChipColors
import com.wrplayer.ui.theme.DimensionChip
import com.wrplayer.ui.theme.JetBrainsMono
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.chipColors
import com.wrplayer.ui.theme.dimensionDot

/**
 * Queue Editor — design variant H (PRD §6.2): a full-screen modal dismissed by back-nav (no close
 * button), full-width preset row, active filters, tag search, per-dimension three-state chip
 * sections with prospective counts, and the Shuffle & Play / Enqueue / Play Next CTA bar with a
 * press-and-hold Preview. [onPlaybackStarted] fires after Shuffle & Play so the host can show Now Playing.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun QueueEditorScreen(
    onDismiss: () -> Unit,
    onPlaybackStarted: () -> Unit,
    viewModel: QueueEditorViewModel = hiltViewModel(),
) {
    val colors = WrTheme.colors
    val state by viewModel.state.collectAsState()
    val preview by viewModel.preview.collectAsState()
    val context = LocalContext.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var presetMenu by remember { mutableStateOf<SavedPreset?>(null) }

    BackHandler { onDismiss() }

    LaunchedEffect(state.staleMessage) {
        state.staleMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.dismissStaleMessage()
        }
    }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.fillMaxSize()) {
            // Header — title + Reset (no close button; back-nav dismisses).
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Queue Editor", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight(700), modifier = Modifier.weight(1f))
                Row(
                    Modifier.clip(RoundedCornerShape(8.dp)).clickable { viewModel.reset() }.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = colors.text2, modifier = Modifier.size(15.dp))
                    Text("Reset", color = colors.text2, fontSize = 13.sp, fontWeight = FontWeight(600))
                }
            }

            // Preset row — full width.
            PresetRow(
                presets = state.presets,
                canSave = state.hasActiveFilters,
                onLoad = viewModel::loadPreset,
                onLongPress = { presetMenu = it },
                onNew = { showSaveDialog = true },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )

            // Scroll body.
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                if (state.activeFilters.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.activeFilters.forEach { chip ->
                            ActiveFilterChip(chip) { viewModel.clear(chip.dimension, chip.value) }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
                SearchField(state.search, viewModel::onSearch)
                Spacer(Modifier.height(18.dp))
                state.sections.forEach { section ->
                    DimensionSectionView(section, onToggle = { viewModel.toggle(section.dimension, it) })
                    Spacer(Modifier.height(18.dp))
                }
                Spacer(Modifier.height(10.dp))
            }

            CtaBar(
                count = state.matchCount,
                enabled = state.matchCount > 0,
                onShuffle = { viewModel.shuffleAndPlay(onPlaybackStarted) },
                onEnqueue = { viewModel.enqueue(onDismiss) },
                onPlayNext = { viewModel.playNext(onDismiss) },
                onPreviewDown = viewModel::openPreview,
                onPreviewUp = viewModel::closePreview,
                previewOpen = preview != null,
            )
        }

        preview?.let { PreviewOverlay(it, state.matchCount) }
    }

    if (showSaveDialog) {
        SavePresetDialog(onConfirm = { viewModel.savePreset(it); showSaveDialog = false }, onDismiss = { showSaveDialog = false })
    }
    presetMenu?.let { p ->
        PresetMenuDialog(
            preset = p,
            onDelete = { viewModel.deletePreset(p.name); presetMenu = null },
            onRename = { viewModel.renamePreset(p.name, it); presetMenu = null },
            onDismiss = { presetMenu = null },
        )
    }
}

@Composable
private fun PresetRow(
    presets: List<SavedPreset>,
    canSave: Boolean,
    onLoad: (SavedPreset) -> Unit,
    onLongPress: (SavedPreset) -> Unit,
    onNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WrTheme.colors
    Row(modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { p ->
            Box(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.surface3)
                    .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                    .pointerInput(p.name) {
                        detectTapGestures(onTap = { onLoad(p) }, onLongPress = { onLongPress(p) })
                    }
                    .padding(horizontal = 13.dp, vertical = 7.dp),
            ) {
                Text(p.name, color = colors.text2, fontSize = 13.sp, fontWeight = FontWeight(600), maxLines = 1)
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                .then(if (canSave) Modifier.clickable { onNew() } else Modifier)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = if (canSave) colors.text2 else colors.text3, modifier = Modifier.size(14.dp))
            Text("New", color = if (canSave) colors.text2 else colors.text3, fontSize = 13.sp, fontWeight = FontWeight(600))
        }
    }
}

@Composable
private fun ActiveFilterChip(chip: ActiveChip, onRemove: () -> Unit) {
    val dark = WrTheme.colors.isDark
    val c: ChipColors = chipColors(chip.dimension, if (chip.excluded) ChipState.EXCLUDED else ChipState.INCLUDED, dark)
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(c.background).clickable { onRemove() }.padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            chip.value,
            color = c.content,
            fontSize = 13.5.sp,
            fontWeight = FontWeight(if (chip.excluded) 550 else 650),
            textDecoration = if (chip.excluded) TextDecoration.LineThrough else TextDecoration.None,
        )
        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = c.content.copy(alpha = 0.7f), modifier = Modifier.size(13.dp))
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun DimensionSectionView(section: DimensionSection, onToggle: (String) -> Unit) {
    val colors = WrTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(bottom = 11.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dimensionDot(section.dimension, colors.isDark)))
        Text(
            section.dimension.name,
            color = colors.text2,
            fontSize = 11.5.sp,
            fontWeight = FontWeight(700),
            letterSpacing = 0.07.em,
        )
        Box(Modifier.weight(1f).height(1.dp).background(colors.border))
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        section.chips.forEach { chip: FacetValue ->
            DimensionChip(
                label = chip.value,
                dimension = section.dimension,
                state = chip.state,
                count = chip.count,
                onClick = { onToggle(chip.value) },
            )
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    val colors = WrTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surface3).border(1.dp, colors.border, RoundedCornerShape(12.dp)).padding(horizontal = 13.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.text3, modifier = Modifier.size(17.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("Filter tags…", color = colors.text3, fontSize = 13.5.sp)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = colors.text, fontSize = 13.5.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
            )
        }
    }
}

@Composable
private fun CtaBar(
    count: Int,
    enabled: Boolean,
    onShuffle: () -> Unit,
    onEnqueue: () -> Unit,
    onPlayNext: () -> Unit,
    onPreviewDown: () -> Unit,
    onPreviewUp: () -> Unit,
    previewOpen: Boolean,
) {
    val colors = WrTheme.colors
    Column(
        Modifier.fillMaxWidth().background(colors.surface2).padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Primary: Shuffle & Play with count pill.
        Row(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (enabled) colors.accent else colors.surface3)
                .clickable(enabled = enabled) { onShuffle() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.Shuffle, contentDescription = null, tint = if (enabled) colors.accentFg else colors.text3, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(9.dp))
            Text("Shuffle & Play", color = if (enabled) colors.accentFg else colors.text3, fontSize = 15.5.sp, fontWeight = FontWeight(700))
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(999.dp)).background((if (enabled) colors.accentFg else colors.text3).copy(alpha = 0.22f)).padding(horizontal = 9.dp, vertical = 2.dp),
            ) {
                Text("$count", color = if (enabled) colors.accentFg else colors.text3, fontSize = 13.5.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(700))
            }
        }
        // Secondary: Enqueue, Play Next, Preview (hold).
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GhostButton("Enqueue", Icons.AutoMirrored.Filled.PlaylistAdd, Modifier.weight(1f), enabled = enabled, onClick = onEnqueue)
            GhostButton("Play Next", Icons.AutoMirrored.Filled.PlaylistPlay, Modifier.weight(1f), enabled = enabled, onClick = onPlayNext)
            PreviewButton(
                modifier = Modifier.weight(1f),
                open = previewOpen,
                onDown = onPreviewDown,
                onUp = onPreviewUp,
            )
        }
    }
}

@Composable
private fun GhostButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    val colors = WrTheme.colors
    val fg = if (enabled) colors.text else colors.text3
    Row(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(colors.surface)
            .border(1.5.dp, colors.border, RoundedCornerShape(13.dp))
            .clickable(enabled = enabled) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, color = fg, fontSize = 13.5.sp, fontWeight = FontWeight(600), maxLines = 1)
    }
}

@Composable
private fun PreviewButton(modifier: Modifier, open: Boolean, onDown: () -> Unit, onUp: () -> Unit) {
    val colors = WrTheme.colors
    Row(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(if (open) colors.accent else colors.surface)
            .border(1.5.dp, if (open) colors.accent else colors.border, RoundedCornerShape(13.dp))
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    onDown()
                    tryAwaitRelease()
                    onUp()
                })
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = if (open) colors.accentFg else colors.text, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text("Preview", color = if (open) colors.accentFg else colors.text, fontSize = 13.5.sp, fontWeight = FontWeight(600))
    }
}

@Composable
private fun PreviewOverlay(tracks: List<PreviewTrack>, count: Int) {
    val colors = WrTheme.colors
    Box(Modifier.fillMaxSize().background(Color(0x38000000)), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(colors.surface2).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$count", color = colors.accent, fontSize = 20.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(700))
                Spacer(Modifier.width(7.dp))
                Text("matching tracks", color = colors.text, fontSize = 13.5.sp, fontWeight = FontWeight(600), modifier = Modifier.weight(1f))
                Text("Release to close", color = colors.text3, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.heightInPreview().verticalScroll(rememberScrollState())) {
                tracks.take(80).forEach { t ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(colors.surface3))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight(600), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(t.artist, color = colors.text2, fontSize = 12.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.heightInPreview() = this.height(360.dp)

@Composable
private fun SavePresetDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = WrTheme.colors
    var name by remember { mutableStateOf(TextFieldValue("")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save preset", color = colors.text) },
        text = {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surface3).border(1.dp, colors.border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (name.text.isEmpty()) Text("Preset name", color = colors.text3, fontSize = 14.sp)
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.text, fontSize = 14.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name.text) }, enabled = name.text.isNotBlank()) { Text("Save", color = colors.accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.text2) } },
        containerColor = colors.surface2,
    )
}

@Composable
private fun PresetMenuDialog(preset: SavedPreset, onDelete: () -> Unit, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = WrTheme.colors
    var renaming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(TextFieldValue(preset.name)) }
    if (renaming) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Rename preset", color = colors.text) },
            text = {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surface3).border(1.dp, colors.border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp)) {
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = colors.text, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { onRename(name.text) }, enabled = name.text.isNotBlank()) { Text("Rename", color = colors.accent) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = colors.text2) } },
            containerColor = colors.surface2,
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(preset.name, color = colors.text) },
            text = { Text("Manage this preset.", color = colors.text2) },
            confirmButton = { TextButton(onClick = { renaming = true }) { Text("Rename", color = colors.accent) } },
            dismissButton = { TextButton(onClick = onDelete) { Text("Delete", color = colors.text2) } },
            containerColor = colors.surface2,
        )
    }
}
