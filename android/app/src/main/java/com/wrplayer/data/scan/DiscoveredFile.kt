package com.wrplayer.data.scan

/** An MP3 found on disk during a reconciliation walk (PRD §8.2). */
data class DiscoveredFile(
    val documentUri: String,
    val filePath: String,
    val fileMtime: Long,
)
