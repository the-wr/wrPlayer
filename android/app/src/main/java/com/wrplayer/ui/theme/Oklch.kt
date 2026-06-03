package com.wrplayer.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Converts an OKLCh color to an sRGB [Color] (Björn Ottosson's formulas). The design system
 * (mocks `shared.jsx`) specifies all chip/dimension colors in OKLCh, so we convert here to match
 * the mock's computed colors exactly (PRD §6.2).
 *
 * @param l lightness 0..1, @param c chroma, @param hDegrees hue in degrees.
 */
fun oklch(l: Double, c: Double, hDegrees: Double): Color {
    val h = hDegrees * Math.PI / 180.0
    val a = c * cos(h)
    val b = c * sin(h)

    val l_ = l + 0.3963377774 * a + 0.2158037573 * b
    val m_ = l - 0.1055613458 * a - 0.0638541728 * b
    val s_ = l - 0.0894841775 * a - 1.2914855480 * b

    val lCubed = l_ * l_ * l_
    val mCubed = m_ * m_ * m_
    val sCubed = s_ * s_ * s_

    val rLin = 4.0767416621 * lCubed - 3.3077115913 * mCubed + 0.2309699292 * sCubed
    val gLin = -1.2684380046 * lCubed + 2.6097574011 * mCubed - 0.3413193965 * sCubed
    val bLin = -0.0041960863 * lCubed - 0.7034186147 * mCubed + 1.7076147010 * sCubed

    return Color(
        red = gamma(rLin),
        green = gamma(gLin),
        blue = gamma(bLin),
    )
}

private fun gamma(linear: Double): Float {
    val x = linear.coerceIn(0.0, 1.0)
    val srgb = if (x <= 0.0031308) 12.92 * x else 1.055 * x.pow(1.0 / 2.4) - 0.055
    return srgb.coerceIn(0.0, 1.0).toFloat()
}
