package com.wrplayer.domain.model

/** Inbox feed ordering chosen in the Sort Order picker (PRD §4.1 / §5.1). */
enum class SortOrder(val key: String) {
    NEWEST_FIRST("newest_first"),
    RANDOM("random"),
    CLOSEST_TO_THRESHOLD("closest_to_threshold");

    companion object {
        fun fromKey(key: String?): SortOrder = entries.firstOrNull { it.key == key } ?: NEWEST_FIRST
    }
}
