package com.wrplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/** Accessor for wrPlayer's custom design tokens inside composables: `WrTheme.colors`. */
object WrTheme {
    val colors: WrColors
        @Composable get() = LocalWrColors.current
}

@Composable
fun WrPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkWrColors else LightWrColors

    // Map our tokens onto a Material scheme so stock M3 components stay on-brand.
    val material = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.accentFg,
            background = colors.surface,
            surface = colors.surface2,
            onSurface = colors.text,
            outline = colors.border,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.accentFg,
            background = colors.surface,
            surface = colors.surface2,
            onSurface = colors.text,
            outline = colors.border,
        )
    }

    CompositionLocalProvider(LocalWrColors provides colors) {
        MaterialTheme(colorScheme = material) {
            // Default all text to Hanken Grotesk on the surface text color (mocks --ui-font).
            val baseStyle = LocalTextStyle.current.copy(
                fontFamily = HankenGrotesk,
                color = colors.text,
            )
            CompositionLocalProvider(LocalTextStyle provides baseStyle, content = content)
        }
    }
}
