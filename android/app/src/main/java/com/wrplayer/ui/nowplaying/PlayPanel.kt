package com.wrplayer.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.chipColors

/**
 * Play Mode bottom action panel (PRD §4.1, mock `now-playing.jsx` PlayPanel): the current track's
 * tags as read-only color chips with an Edit affordance, then Queue Editor / Current Queue buttons.
 */
@Composable
fun PlayPanel(
    tags: List<Pair<TagDimension, String>>,
    onEdit: () -> Unit,
    onQueueEditor: () -> Unit,
    onCurrentQueue: () -> Unit,
) {
    val colors = WrTheme.colors
    val dark = colors.isDark
    Column(
        Modifier.fillMaxWidth().background(colors.surface2).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tags.take(4).forEach { (dimension, value) ->
                    val c = chipColors(dimension, ChipState.INCLUDED, dark)
                    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(c.background).padding(horizontal = 11.dp, vertical = 6.dp)) {
                        Text(value, color = c.content, fontSize = 12.5.sp, fontWeight = FontWeight(650), maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.width(7.dp))
            Box(
                Modifier.size(width = 40.dp, height = 34.dp).clip(RoundedCornerShape(10.dp)).background(colors.surface3).border(1.dp, colors.border, RoundedCornerShape(10.dp)).clickable { onEdit() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit tags", tint = colors.text2, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NavButton("Queue Editor", Icons.Filled.Tune, Modifier.weight(1f), onQueueEditor)
            NavButton("Current Queue", Icons.AutoMirrored.Filled.List, Modifier.weight(1f), onCurrentQueue)
        }
    }
}

@Composable
private fun NavButton(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val colors = WrTheme.colors
    Row(
        modifier.height(48.dp).clip(RoundedCornerShape(14.dp)).background(colors.surface).border(1.5.dp, colors.border, RoundedCornerShape(14.dp)).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = colors.text, fontSize = 14.sp, fontWeight = FontWeight(600))
    }
}
