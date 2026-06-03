package com.wrplayer.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.ui.common.AppMode
import com.wrplayer.ui.common.WrTopBar
import com.wrplayer.ui.theme.DimensionChip
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.dimensionDot

/** TEMPORARY Phase 6 design-system gallery for the visual sign-off (not a shipped screen). */
@Composable
fun DesignGalleryScreen(modifier: Modifier = Modifier) {
    val colors = WrTheme.colors
    var mode by remember { mutableStateOf(AppMode.PLAY) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        WrTopBar(
            mode = mode,
            count = if (mode == AppMode.SORT) 42 else 12,
            scanning = true,
            onModeChange = { mode = it },
            onOpenSettings = {},
        )

        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TagDimension.entries.forEach { dimension ->
                DimensionRow(dimension)
            }
        }
    }
}

@Composable
private fun DimensionRow(dimension: TagDimension) {
    val colors = WrTheme.colors
    val sample = sampleLabel(dimension)
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dimensionDot(dimension, colors.isDark)),
            )
            Text(
                text = dimension.name,
                color = colors.text2,
                fontSize = 11.5.sp,
                fontWeight = FontWeight(700),
                letterSpacing = 0.07.em,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DimensionChip(sample, dimension, ChipState.UNSELECTED, count = 47)
            DimensionChip(sample, dimension, ChipState.INCLUDED, count = 31)
            DimensionChip(sample, dimension, ChipState.EXCLUDED, count = 12)
        }
    }
}

private fun sampleLabel(dimension: TagDimension): String = when (dimension) {
    TagDimension.GENRE -> "Rock"
    TagDimension.MOOD -> "Hype"
    TagDimension.PACE -> "Fast"
    TagDimension.LABELS -> "gym"
    TagDimension.ARTIST -> "Artist A"
    TagDimension.ALBUM -> "Album X"
}
