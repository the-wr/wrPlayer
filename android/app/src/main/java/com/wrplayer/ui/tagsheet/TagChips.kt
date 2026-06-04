package com.wrplayer.ui.tagsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.chipColors

/** A selectable tag chip in the Tag Sheet (PRD §5.3). Selected chips are filled and show an ✕. */
@Composable
fun SelectableTagChip(
    label: String,
    dimension: TagDimension,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val dark = WrTheme.colors.isDark
    val c = chipColors(dimension, if (selected) ChipState.INCLUDED else ChipState.UNSELECTED, dark)
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(c.background)
            .clickable { onClick() }
            .padding(start = 12.dp, end = if (selected) 9.dp else 12.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            color = c.content,
            fontSize = 13.5.sp,
            fontWeight = if (selected) FontWeight(650) else FontWeight(550),
        )
        if (selected) {
            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = c.content, modifier = Modifier.size(14.dp))
        }
    }
}

/** Dashed "＋ Add" chip that opens a freeform input (PRD §5.3). */
@Composable
fun AddChip(onClick: () -> Unit) {
    val colors = WrTheme.colors
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = colors.border,
                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
                )
            }
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = colors.text2, modifier = Modifier.size(15.dp))
        Text("Add", color = colors.text2, fontSize = 13.sp, fontWeight = FontWeight(600))
    }
}
