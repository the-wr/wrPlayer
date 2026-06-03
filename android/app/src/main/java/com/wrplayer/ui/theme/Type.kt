@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.wrplayer.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.wrplayer.R

/**
 * The mocks' two type families: Hanken Grotesk (UI) and JetBrains Mono (numbers/paths). Both ship
 * as variable fonts, so each weight binds the `wght` axis explicitly.
 */
private fun hanken(weight: Int) = Font(
    resId = R.font.hanken_grotesk,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private fun mono(weight: Int) = Font(
    resId = R.font.jetbrains_mono,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val HankenGrotesk = FontFamily(
    hanken(400), hanken(500), hanken(600), hanken(700), hanken(800),
)

val JetBrainsMono = FontFamily(
    mono(400), mono(500), mono(600), mono(700),
)
