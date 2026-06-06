package com.wrplayer.ui.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wrplayer.ui.theme.JetBrainsMono
import com.wrplayer.ui.theme.WrTheme
import com.wrplayer.ui.theme.oklch

/**
 * Settings → Watched Folders (PRD §3 / §8, mock `settings.jsx`): the SAF watched-folder list with
 * add (system folder picker), per-folder remove, and a manual rescan. Dismissed by back-nav.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val colors = WrTheme.colors
    val state by viewModel.state.collectAsState()
    BackHandler { onBack() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) viewModel.addFolder(uri)
    }

    Column(Modifier.fillMaxSize().background(colors.surface)) {
        // Header.
        Row(
            Modifier.fillMaxWidth().background(colors.surface).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text,
                modifier = Modifier.size(22.dp).clickable { onBack() },
            )
            Spacer(Modifier.size(12.dp))
            Text("Settings", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight(800), letterSpacing = (-0.02).em)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("WATCHED FOLDERS", color = colors.text2, fontSize = 11.5.sp, fontWeight = FontWeight(700), letterSpacing = 0.07.em)
                Text("${state.folders.size} active", color = colors.text3, fontSize = 11.5.sp, fontFamily = JetBrainsMono)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                "New files in these folders appear in the inbox. Library/ is created on first promotion.",
                color = colors.text3, fontSize = 12.5.sp, lineHeight = 17.sp,
            )
            Spacer(Modifier.size(14.dp))

            state.folders.forEach { folder ->
                FolderRow(folder.name, folder.path, folder.isSdCard, folder.available) { viewModel.removeFolder(folder.treeUri) }
                Spacer(Modifier.size(9.dp))
            }
            if (state.loaded && state.folders.isEmpty()) {
                Text("No folders yet — add one to start filling the inbox.", color = colors.text3, fontSize = 12.5.sp)
                Spacer(Modifier.size(9.dp))
            }

            // Add folder (dashed).
            Spacer(Modifier.size(2.dp))
            Row(
                Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).dashedOutline(colors.border).clickable { picker.launch(null) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(19.dp))
                Spacer(Modifier.size(9.dp))
                Text("Add folder", color = colors.accent, fontSize = 14.5.sp, fontWeight = FontWeight(700))
            }
            Spacer(Modifier.size(8.dp))
            Text(
                "Opens the system folder picker (SAF) to grant read & write access.",
                color = colors.text3, fontSize = 11.5.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.size(20.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Spacer(Modifier.size(16.dp))

            // Rescan now.
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.accent).clickable(enabled = state.folders.isNotEmpty()) { viewModel.rescan() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = colors.accentFg, modifier = Modifier.size(19.dp))
                Spacer(Modifier.size(9.dp))
                Text(if (state.scanning) "Scanning…" else "Rescan now", color = colors.accentFg, fontSize = 15.sp, fontWeight = FontWeight(700))
            }
            Spacer(Modifier.size(8.dp))
            Text(
                "${state.libraryCount} tracks cached" + if (state.scanning) " · scanning now" else "",
                color = colors.text3, fontSize = 11.5.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(24.dp))
        }
    }
}

@Composable
private fun FolderRow(name: String, path: String, isSdCard: Boolean, available: Boolean, onRemove: () -> Unit) {
    val colors = WrTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface2).border(1.dp, colors.border, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(name, color = colors.text, fontSize = 14.5.sp, fontWeight = FontWeight(700), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.clip(RoundedCornerShape(5.dp)).background(colors.surface3).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(if (isSdCard) "SD CARD" else "INTERNAL", color = colors.text3, fontSize = 10.sp, fontWeight = FontWeight(700), letterSpacing = 0.04.em)
                }
            }
            Spacer(Modifier.size(3.dp))
            Text(path, color = colors.text3, fontSize = 12.sp, fontFamily = JetBrainsMono, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!available) {
                Spacer(Modifier.size(3.dp))
                Text("Unavailable — folder unreachable", color = oklch(0.55, 0.16, 24.0), fontSize = 11.5.sp, fontWeight = FontWeight(600))
            }
        }
        Spacer(Modifier.size(10.dp))
        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = colors.text3, modifier = Modifier.size(20.dp).clickable { onRemove() })
    }
}

/** Dashed rounded outline matching the mock's dashed "Add folder" affordance. */
private fun Modifier.dashedOutline(color: androidx.compose.ui.graphics.Color) = drawBehind {
    val w = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
        style = Stroke(width = w, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)),
    )
}
