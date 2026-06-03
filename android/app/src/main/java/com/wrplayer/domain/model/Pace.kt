package com.wrplayer.domain.model

/** Pace bucket derived from BPM (PRD §9). Stored in `TXXX:PACE` / `tracks.pace` as [key]. */
enum class Pace(val key: String) {
    SLOW("slow"),
    MEDIUM("medium"),
    FAST("fast");

    companion object {
        fun fromKey(key: String?): Pace? = entries.firstOrNull { it.key == key }
    }
}
