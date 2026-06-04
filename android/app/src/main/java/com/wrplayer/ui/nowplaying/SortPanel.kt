package com.wrplayer.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.oklch

private const val RED = 24.0
private const val GREEN = 152.0

/**
 * Sort Mode bottom action panel (PRD §4.1 / §5.2): −1 (final), Skip, +1 (changeable; solid when
 * selected). No Previous — the sort queue is forward-only.
 */
@Composable
fun SortPanel(
    plusSelected: Boolean,
    onMinusOne: () -> Unit,
    onSkip: () -> Unit,
    onPlusOne: () -> Unit,
) {
    val colors = WrTheme.colors
    val dark = colors.isDark
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface2)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        SortButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Remove,
            background = if (dark) oklch(0.30, 0.06, RED) else oklch(0.96, 0.035, RED),
            content = if (dark) oklch(0.82, 0.13, RED) else oklch(0.52, 0.18, RED),
            onClick = onMinusOne,
        )
        SortButton(
            modifier = Modifier.weight(0.7f),
            icon = Icons.Filled.SkipNext,
            background = colors.surface,
            content = colors.text2,
            onClick = onSkip,
        )
        SortButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Add,
            background = when {
                plusSelected -> oklch(0.60, 0.14, GREEN)
                dark -> oklch(0.30, 0.055, GREEN)
                else -> oklch(0.95, 0.04, GREEN)
            },
            content = when {
                plusSelected -> if (dark) oklch(0.16, 0.02, GREEN) else Color.White
                dark -> oklch(0.82, 0.12, GREEN)
                else -> oklch(0.46, 0.13, GREEN)
            },
            onClick = onPlusOne,
        )
    }
}

@Composable
private fun SortButton(
    modifier: Modifier,
    icon: ImageVector,
    background: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(30.dp))
    }
}
