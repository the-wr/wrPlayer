package com.wrplayer.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrplayer.ui.theme.WrTheme

/**
 * The persistent top bar (PRD §4.1): a Sort|Play segmented toggle, the active-list count, a scan
 * spinner while a reconciliation walk runs, and the Settings gear.
 */
@Composable
fun WrTopBar(
    mode: AppMode,
    count: Int,
    scanning: Boolean,
    onModeChange: (AppMode) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = WrTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SegmentedToggle(mode, onModeChange)

        Text(
            text = when (mode) {
                AppMode.SORT -> "Sorting: $count tracks"
                AppMode.PLAY -> "Queue: $count tracks"
            },
            color = colors.text2,
            fontSize = 13.5.sp,
            fontWeight = FontWeight(600),
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f),
        )

        if (scanning) {
            val transition = rememberInfiniteTransition(label = "scan")
            val angle by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
                label = "scanAngle",
            )
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Scanning",
                tint = colors.text3,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .rotate(angle),
            )
        }

        Icon(
            Icons.Filled.Settings,
            contentDescription = "Settings",
            tint = colors.text2,
            modifier = Modifier.clickable { onOpenSettings() },
        )
    }
}

@Composable
private fun SegmentedToggle(mode: AppMode, onModeChange: (AppMode) -> Unit) {
    val colors = WrTheme.colors
    Row(
        modifier = Modifier
            .width(122.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(colors.surface3)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Segment("Sort", mode == AppMode.SORT, Modifier.weight(1f)) { onModeChange(AppMode.SORT) }
        Segment("Play", mode == AppMode.PLAY, Modifier.weight(1f)) { onModeChange(AppMode.PLAY) }
    }
}

@Composable
private fun Segment(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = WrTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) colors.accent else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (active) colors.accentFg else colors.text2,
            fontSize = 13.sp,
            fontWeight = FontWeight(650),
        )
    }
}
