package com.wrplayer.domain

import com.wrplayer.domain.model.Pace

/**
 * Derives the Pace bucket from a BPM value (PRD §9, MVP defaults): slow &lt; 90, medium 90–140,
 * fast &gt; 140. Returns null when BPM is unknown.
 */
object PaceDeriver {
    fun fromBpm(bpm: Int?): Pace? = when {
        bpm == null -> null
        bpm < 90 -> Pace.SLOW
        bpm <= 140 -> Pace.MEDIUM
        else -> Pace.FAST
    }
}
