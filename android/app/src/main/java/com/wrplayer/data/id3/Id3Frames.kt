package com.wrplayer.data.id3

/** Custom `TXXX` frame descriptions used by wrPlayer (PRD §2.3). */
object Txxx {
    const val STATUS = "STATUS"
    const val PACE = "PACE"
    const val LABELS = "LABELS"
}

/** The null byte (`0x00`) that separates values in a native ID3v2.4 multi-value frame. */
val ID3_NULL_SEPARATOR: String = Char(0).toString()
