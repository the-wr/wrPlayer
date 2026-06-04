package com.wrplayer.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wrplayer.ui.theme.JetBrainsMono
import com.wrplayer.ui.theme.WrTheme

/** Shared Now Playing state (PRD §4.1). */
data class NowPlayingUi(
    val title: String,
    val artist: String,
    val album: String,
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean,
    val showPrevious: Boolean,
)

/**
 * The shared Now Playing layout used by both Sort and Play modes (PRD §4.1): cover art, metadata,
 * scrubbable progress, transport. Only the [bottomPanel] differs between modes.
 */
@Composable
fun NowPlayingScreen(
    ui: NowPlayingUi,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    topBar: @Composable () -> Unit,
    bottomPanel: @Composable () -> Unit,
) {
    val colors = WrTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        topBar()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CoverArt(Modifier.fillMaxWidth(0.78f).aspectRatio(1f))
            Spacer(Modifier.height(28.dp))
            Text(
                ui.title,
                color = colors.text,
                fontSize = 24.sp,
                fontWeight = FontWeight(800),
                letterSpacing = (-0.02).em,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(ui.artist, color = colors.text2, fontSize = 15.5.sp, fontWeight = FontWeight(600), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(ui.album, color = colors.text3, fontSize = 13.5.sp, fontWeight = FontWeight(500), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(28.dp))
            ProgressBar(ui.positionMs, ui.durationMs, onSeekFraction)
            Spacer(Modifier.height(24.dp))
            TransportRow(ui.isPlaying, ui.showPrevious, onPlayPause, onPrevious, onNext)
        }
        bottomPanel()
    }
}

@Composable
private fun CoverArt(modifier: Modifier) {
    val colors = WrTheme.colors
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.surface3, colors.surface2),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = colors.text3, modifier = Modifier.size(64.dp))
    }
}

@Composable
private fun ProgressBar(positionMs: Long, durationMs: Long, onSeekFraction: (Float) -> Unit) {
    val colors = WrTheme.colors
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .pointerInput(durationMs) {
                    detectTapGestures { offset -> onSeekFraction((offset.x / size.width).coerceIn(0f, 1f)) }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures { change, _ ->
                        onSeekFraction((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(colors.surface3))
            Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp)).background(colors.accent))
        }
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(positionMs), color = colors.text3, fontSize = 11.5.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(500))
            Text(formatTime(durationMs), color = colors.text3, fontSize = 11.5.sp, fontFamily = JetBrainsMono, fontWeight = FontWeight(500))
        }
    }
}

@Composable
private fun TransportRow(
    isPlaying: Boolean,
    showPrevious: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = WrTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        if (showPrevious) {
            CircleButton(52.dp, colors.surface3) {
                Icon(Icons.Filled.SkipPrevious, "Previous", tint = colors.text, modifier = Modifier.size(26.dp).clickable { onPrevious() })
            }
        }
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(colors.accent).clickable { onPlayPause() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                tint = colors.accentFg,
                modifier = Modifier.size(34.dp),
            )
        }
        CircleButton(52.dp, colors.surface3) {
            Icon(Icons.Filled.SkipNext, "Next", tint = colors.text, modifier = Modifier.size(26.dp).clickable { onNext() })
        }
    }
}

@Composable
private fun CircleButton(size: androidx.compose.ui.unit.Dp, bg: androidx.compose.ui.graphics.Color, content: @Composable () -> Unit) {
    Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { content() }
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
