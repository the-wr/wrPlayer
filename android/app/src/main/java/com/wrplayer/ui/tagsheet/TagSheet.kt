@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.wrplayer.ui.tagsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wrplayer.domain.model.Pace
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.ui.theme.JetBrainsMono
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.dimensionDot

/**
 * The Tag Sheet (PRD §5.3 / §7): edit Title/Artist/Album and Genre/Mood/Pace/BPM/Labels, then
 * Confirm to add to the Library. Renders the sheet card content; the caller hosts it in a bottom
 * sheet / overlay.
 */
@Composable
fun TagSheet(
    state: TagSheetState,
    callbacks: TagSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = WrTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(colors.surface2)
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 20.dp),
    ) {
        // Grabber
        Box(
            Modifier
                .padding(bottom = 12.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.border)
                .size(width = 40.dp, height = 4.dp),
        )

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.isEdit) "Edit tags" else "Add to Library",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight(800),
                    letterSpacing = (-0.02).em,
                )
                Text(
                    if (state.isEdit) "Update this track's tags" else "Tag this track to keep it",
                    color = colors.text3,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight(500),
                )
            }
            Box(
                Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.surface3)
                    .clickable { callbacks.onDismiss() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.text2, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Title (with art placeholder)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.surface3),
            )
            WrTextField(state.title, "Title", Modifier.weight(1f), callbacks.onTitle)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            WrTextField(state.artist, "Artist", Modifier.weight(1f), callbacks.onArtist)
            WrTextField(state.album, "Album", Modifier.weight(1f), callbacks.onAlbum)
        }

        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
        Spacer(Modifier.height(16.dp))

        TagSection(TagDimension.GENRE, "Genre", state.genreOptions, state.genres, callbacks.onToggleGenre, callbacks.onAddGenre)
        Spacer(Modifier.height(16.dp))
        TagSection(TagDimension.MOOD, "Mood", state.moodOptions, state.moods, callbacks.onToggleMood, callbacks.onAddMood)
        Spacer(Modifier.height(16.dp))
        PaceSection(state.pace, state.bpm, callbacks.onSetPace, callbacks.onSetBpm)
        Spacer(Modifier.height(16.dp))
        TagSection(TagDimension.LABELS, "Labels", state.labelOptions, state.labels, callbacks.onToggleLabel, callbacks.onAddLabel)

        Spacer(Modifier.height(20.dp))

        // Confirm
        val enabled = state.canConfirm
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(if (enabled) colors.accent else colors.surface3)
                .clickable(enabled = enabled) { callbacks.onConfirm() }
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = null,
                tint = if (enabled) colors.accentFg else colors.text3,
                modifier = Modifier.size(19.dp).padding(end = 0.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (state.isEdit) "Save tags" else "Confirm & add to Library",
                color = if (enabled) colors.accentFg else colors.text3,
                fontSize = 15.5.sp,
                fontWeight = FontWeight(700),
            )
        }
    }
}

@Composable
private fun SectionLabel(dimension: TagDimension, label: String) {
    val colors = WrTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(dimensionDot(dimension, colors.isDark)))
        Text(label.uppercase(), color = colors.text2, fontSize = 11.5.sp, fontWeight = FontWeight(700), letterSpacing = 0.07.em)
    }
}

@Composable
private fun TagSection(
    dimension: TagDimension,
    label: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val all = (options + selected.filter { it !in options }).distinct()

    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        SectionLabel(dimension, label)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            all.forEach { value ->
                SelectableTagChip(value, dimension, value in selected) { onToggle(value) }
            }
            AddChip { adding = true }
        }
        if (adding) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                WrTextField(text, "New $label", Modifier.weight(1f), { text = it }, imeAction = ImeAction.Done) {
                    if (text.isNotBlank()) onAdd(text.trim())
                    text = ""; adding = false
                }
            }
        }
    }
}

@Composable
private fun PaceSection(pace: String?, bpm: Int?, onSetPace: (String?) -> Unit, onSetBpm: (Int?) -> Unit) {
    val colors = WrTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        SectionLabel(TagDimension.PACE, "Pace")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pace.entries.forEach { p ->
                val selected = pace == p.key
                SelectableTagChip(p.key.replaceFirstChar { it.uppercase() }, TagDimension.PACE, selected) {
                    onSetPace(if (selected) null else p.key)
                }
            }
            Spacer(Modifier.width(4.dp))
            // BPM inline editor
            var bpmText by remember(bpm) { mutableStateOf(bpm?.toString() ?: "") }
            Row(
                Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.surface3)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("BPM", color = colors.text3, fontSize = 12.sp, fontWeight = FontWeight(600))
                BasicTextField(
                    value = bpmText,
                    onValueChange = { v -> bpmText = v.filter { it.isDigit() }.take(3); onSetBpm(bpmText.toIntOrNull()) },
                    textStyle = TextStyle(color = colors.text, fontSize = 13.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(600)),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(34.dp),
                )
            }
        }
    }
}

@Composable
private fun WrTextField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    val colors = WrTheme.colors
    Box(
        modifier
            .clip(RoundedCornerShape(11.dp))
            .background(colors.surface3)
            .padding(horizontal = 13.dp, vertical = 11.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = colors.text, fontSize = 14.5.sp, fontWeight = FontWeight(600)),
            singleLine = true,
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(placeholder, color = colors.text3, fontSize = 14.5.sp, fontWeight = FontWeight(500))
                }
                inner()
            },
        )
    }
}
