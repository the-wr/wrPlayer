package com.wrplayer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * wrPlayer's surface/text/accent tokens, mirroring the CSS custom properties in the mocks
 * (`now-playing.jsx`). Dimension/chip colors are derived separately via OKLCh ([dimensionHue]).
 */
@Immutable
data class WrColors(
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val text: Color,
    val text2: Color,
    val text3: Color,
    val border: Color,
    val accent: Color,
    val accentFg: Color,
    val isDark: Boolean,
)

val LightWrColors = WrColors(
    surface = Color(0xFFF7F6F3),
    surface2 = Color(0xFFFFFFFF),
    surface3 = Color(0xFFEEECE7),
    text = Color(0xFF1B1A18),
    text2 = Color(0xFF6B6863),
    text3 = Color(0xFF9C988F),
    border = Color(0xFFE7E3DC),
    accent = Color(0xFF5B5BD6),
    accentFg = Color.White,
    isDark = false,
)

val DarkWrColors = WrColors(
    surface = Color(0xFF131316),
    surface2 = Color(0xFF1B1B1F),
    surface3 = Color(0xFF26262B),
    text = Color(0xFFF2F1EE),
    text2 = Color(0xFFA4A19B),
    text3 = Color(0xFF6F6C67),
    border = Color(0xFF2A2A30),
    accent = Color(0xFF5B5BD6),
    accentFg = Color.White,
    isDark = true,
)

val LocalWrColors = staticCompositionLocalOf { LightWrColors }
