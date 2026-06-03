package com.wrplayer.domain.model

/** Track lifecycle state, sourced from the `TXXX:STATUS` tag only (PRD §2.1 / §8.2). */
enum class TrackStatus(val key: String) {
    INBOX("inbox"),
    LIBRARY("library");

    companion object {
        fun fromKey(key: String?): TrackStatus = entries.firstOrNull { it.key == key } ?: INBOX
    }
}
