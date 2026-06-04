package com.wrplayer.ui.sort

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wrplayer.domain.model.SortOrder
import com.wrplayer.ui.theme.WrTheme

/** Sort Order picker bottom sheet shown on entry to Sort Mode (PRD §4.1 / §5.1). */
@Composable
fun SortOrderPicker(
    initial: SortOrder,
    trackCount: Int,
    onConfirm: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = WrTheme.colors
    var selected by remember { mutableStateOf(initial) }

    Box(
        Modifier.fillMaxSize().background(Color(0x6B000000)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(colors.surface2)
                .clickable(enabled = false) {}
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 20.dp),
        ) {
            Text("Sort order", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight(800), letterSpacing = (-0.02).em)
            Text(
                "Builds a fresh queue from $trackCount track${if (trackCount == 1) "" else "s"}",
                color = colors.text3, fontSize = 12.5.sp, fontWeight = FontWeight(500),
            )
            Spacer(Modifier.height(16.dp))

            OrderOption("Newest first", "By file date, newest at top", selected == SortOrder.NEWEST_FIRST) { selected = SortOrder.NEWEST_FIRST }
            Spacer(Modifier.height(8.dp))
            OrderOption("Random", "Shuffle the whole inbox", selected == SortOrder.RANDOM) { selected = SortOrder.RANDOM }
            Spacer(Modifier.height(8.dp))
            OrderOption("Closest to threshold", "Most decisively-voted first", selected == SortOrder.CLOSEST_TO_THRESHOLD) { selected = SortOrder.CLOSEST_TO_THRESHOLD }

            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent)
                    .clickable { onConfirm(selected) }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Start sorting", color = colors.accentFg, fontSize = 15.5.sp, fontWeight = FontWeight(700))
            }
        }
    }
}

@Composable
private fun OrderOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = WrTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.5.dp, if (selected) colors.accent else colors.border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.text, fontSize = 14.5.sp, fontWeight = FontWeight(700))
            Text(subtitle, color = colors.text3, fontSize = 12.sp)
        }
        Box(
            Modifier.size(21.dp).clip(CircleShape).border(2.dp, if (selected) colors.accent else colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(colors.accent))
        }
    }
}
