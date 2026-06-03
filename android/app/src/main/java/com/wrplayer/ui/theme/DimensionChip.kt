package com.wrplayer.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension

/**
 * A tag chip in one of the three Queue Editor states (PRD §6.2), colored per dimension. Mirrors
 * `chipStyle(dim, state, 'tinted', dark)` from the mocks: pill, 1.5dp border (dashed when excluded),
 * optional monospace count.
 */
@Composable
fun DimensionChip(
    label: String,
    dimension: TagDimension,
    state: ChipState,
    modifier: Modifier = Modifier,
    count: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val dark = WrTheme.colors.isDark
    val c = chipColors(dimension, state, dark)
    val shape = RoundedCornerShape(999.dp)

    val base = modifier
        .clip(shape)
        .background(c.background)
        .then(
            if (state == ChipState.EXCLUDED) Modifier.dashedBorder(c.border)
            else Modifier.border(1.5.dp, c.border, shape),
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 12.dp, vertical = 7.dp)

    Row(base, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            text = label,
            color = c.content,
            fontSize = 13.5.sp,
            fontWeight = if (state == ChipState.INCLUDED) FontWeight(650) else FontWeight(550),
            textDecoration = if (state == ChipState.EXCLUDED) TextDecoration.LineThrough else TextDecoration.None,
        )
        if (count != null) {
            Text(
                text = count.toString(),
                color = c.content.copy(alpha = 0.55f),
                fontSize = 11.5.sp,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight(600),
            )
        }
    }
}

private fun Modifier.dashedBorder(color: Color) = drawBehind {
    val w = 1.5.dp.toPx()
    val radius = size.height / 2f
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
    )
}
