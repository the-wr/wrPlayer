package com.wrplayer.ui.play

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wrplayer.data.playback.QueueTrack
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.common.WrTopBar
import com.wrplayer.ui.theme.JetBrainsMono
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.oklch
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Current Queue screen (PRD §6.3, mock `current-queue.jsx`): the ordered play queue with tap-to-jump,
 * grip drag-to-reorder, swipe-to-remove, and the now-playing row highlighted. Dismissed by back-nav.
 */
@Composable
fun CurrentQueueScreen(
    queue: List<QueueTrack>,
    currentIndex: Int,
    isPlaying: Boolean,
    scanning: Boolean,
    onModeChange: (AppMode) -> Unit,
    onOpenSettings: () -> Unit,
    onJump: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = WrTheme.colors
    BackHandler { onDismiss() }
    val rowHeightPx = with(LocalDensity.current) { 64.dp.toPx() }
    var dragging by remember { mutableStateOf(-1) }

    Column(Modifier.fillMaxSize().background(colors.surface)) {
        WrTopBar(AppMode.PLAY, queue.size, scanning = scanning, onModeChange = onModeChange, onOpenSettings = onOpenSettings)

        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = "Close",
                tint = colors.text,
                modifier = Modifier.size(22.dp).clickable { onDismiss() },
            )
            Spacer(Modifier.width(9.dp))
            Text("Current Queue", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight(700), letterSpacing = (-0.01).em, modifier = Modifier.weight(1f))
            Text("${queue.size}", color = colors.text3, fontSize = 12.5.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(600))
        }
        Text(
            "Drag to reorder · swipe to remove",
            color = colors.text3,
            fontSize = 11.sp,
            fontWeight = FontWeight(700),
            letterSpacing = 0.06.em,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp),
        )

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp)) {
            itemsIndexed(queue, key = { _, t -> t.mediaId }) { index, track ->
                QueueRow(
                    track = track,
                    isCurrent = index == currentIndex,
                    isPlaying = isPlaying,
                    isDragging = index == dragging,
                    onTap = { onJump(index) },
                    onRemove = { onRemove(index) },
                    onGripDrag = { dyTotal ->
                        // Live single-step reorder: move one slot each time accumulated drag passes a row.
                        var from = if (dragging == -1) index else dragging
                        if (dragging == -1) dragging = index
                        val steps = (dyTotal / rowHeightPx).roundToInt()
                        val target = (from + steps).coerceIn(0, queue.lastIndex)
                        if (target != from) {
                            onMove(from, target)
                            dragging = target
                        }
                    },
                    onGripDragEnd = { dragging = -1 },
                )
            }
        }

        // Mini now-playing footer.
        queue.getOrNull(currentIndex)?.let { current ->
            Row(
                Modifier.fillMaxWidth().background(colors.surface2).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Thumb(44.dp)
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(current.title, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight(700), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(current.artist, color = colors.text3, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(colors.accent).clickable { onPlayPause() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = "Play/Pause", tint = colors.accentFg, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    track: QueueTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isDragging: Boolean,
    onTap: () -> Unit,
    onRemove: () -> Unit,
    onGripDrag: (Float) -> Unit,
    onGripDragEnd: () -> Unit,
) {
    val colors = WrTheme.colors
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val removeThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }

    Box(Modifier.fillMaxWidth().height(64.dp)) {
        // Red remove background revealed on left-swipe (mock `oklch(0.60 0.16 24)`).
        Box(
            Modifier.fillMaxSize().padding(vertical = 4.dp).clip(RoundedCornerShape(14.dp)).background(oklch(0.60, 0.16, 24.0)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(end = 22.dp).size(20.dp))
        }

        Row(
            Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(RoundedCornerShape(14.dp))
                .background(if (isCurrent || isDragging) colors.surface2 else colors.surface)
                .then(if (isCurrent) Modifier.border(1.dp, colors.border, RoundedCornerShape(14.dp)) else Modifier)
                .pointerInput(track.mediaId) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX.value < -removeThresholdPx) onRemove()
                            else scope.launch { offsetX.animateTo(0f) }
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            scope.launch { offsetX.snapTo((offsetX.value + amount).coerceAtMost(0f)) }
                        },
                    )
                }
                .clickable { onTap() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Grip — drag vertically to reorder.
            Icon(
                Icons.Filled.DragHandle,
                contentDescription = "Reorder",
                tint = colors.text3,
                modifier = Modifier
                    .size(22.dp)
                    .pointerInput(track.mediaId) {
                        var total = 0f
                        detectDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = { onGripDragEnd() },
                            onDragCancel = { onGripDragEnd() },
                            onDrag = { change, amount ->
                                change.consume()
                                total += amount.y
                                onGripDrag(total)
                            },
                        )
                    },
            )
            Spacer(Modifier.width(13.dp))
            Thumb(46.dp, overlayPlaying = isCurrent && isPlaying)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = if (isCurrent) colors.accent else colors.text,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight(if (isCurrent) 800 else 650),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(track.artist, color = colors.text3, fontSize = 12.5.sp, fontWeight = FontWeight(500), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun Thumb(size: androidx.compose.ui.unit.Dp, overlayPlaying: Boolean = false) {
    val colors = WrTheme.colors
    Box(
        Modifier.size(size).clip(RoundedCornerShape(9.dp)).background(Brush.linearGradient(listOf(colors.surface3, colors.surface2))),
        contentAlignment = Alignment.Center,
    ) {
        if (overlayPlaying) {
            Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0x61000000)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Pause, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp))
            }
        } else {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = colors.text3, modifier = Modifier.size(size * 0.42f))
        }
    }
}
