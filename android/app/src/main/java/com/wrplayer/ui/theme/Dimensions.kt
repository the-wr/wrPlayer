package com.wrplayer.ui.theme

import androidx.compose.ui.graphics.Color
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension

/** Per-dimension OKLCh hue (mocks `shared.jsx` DIMS — PRD §6.2). */
fun dimensionHue(dimension: TagDimension): Double = when (dimension) {
    TagDimension.GENRE -> 256.0
    TagDimension.MOOD -> 305.0
    TagDimension.PACE -> 152.0
    TagDimension.LABELS -> 58.0
    TagDimension.ARTIST -> 196.0
    TagDimension.ALBUM -> 88.0
}

/** Resolved colors for one chip, matching `chipStyle(dim, state, 'tinted', dark)` in the mocks. */
data class ChipColors(val background: Color, val content: Color, val border: Color)

private const val EXCLUDED_HUE = 24.0 // red

/** Tinted-scheme chip colors per state, light/dark (mocks `shared.jsx`). */
fun chipColors(dimension: TagDimension, state: ChipState, dark: Boolean): ChipColors {
    val h = dimensionHue(dimension)
    return when (state) {
        ChipState.UNSELECTED ->
            if (dark) ChipColors(Color(0xFF232328), Color(0xFFA4A19B), Color(0xFF303036))
            else ChipColors(Color(0xFFF0EEE9), Color(0xFF6F6C66), Color(0xFFE3DFD8))

        // Solid scheme (mocks `chipStyle` scheme='solid'): filled hue, no visible border.
        ChipState.INCLUDED ->
            if (dark) ChipColors(
                background = oklch(0.58, 0.13, h),
                content = oklch(0.16, 0.02, h),
                border = oklch(0.58, 0.13, h),
            ) else ChipColors(
                background = oklch(0.60, 0.15, h),
                content = Color.White,
                border = oklch(0.60, 0.15, h),
            )

        ChipState.EXCLUDED ->
            if (dark) ChipColors(
                background = oklch(0.30, 0.07, EXCLUDED_HUE),
                content = oklch(0.80, 0.12, EXCLUDED_HUE),
                border = oklch(0.45, 0.10, EXCLUDED_HUE),
            ) else ChipColors(
                background = oklch(0.96, 0.035, EXCLUDED_HUE),
                content = oklch(0.52, 0.18, EXCLUDED_HUE),
                border = oklch(0.85, 0.10, EXCLUDED_HUE),
            )
    }
}

/** Small dimension-dot color used in section labels (mocks `dimDot`). */
fun dimensionDot(dimension: TagDimension, dark: Boolean): Color {
    val h = dimensionHue(dimension)
    return if (dark) oklch(0.70, 0.13, h) else oklch(0.58, 0.15, h)
}
