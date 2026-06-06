package com.wrplayer.data.playback

/** Snapshot of player state exposed to the UI (PRD §4.1). */
data class PlaybackState(
    val isConnected: Boolean = false,
    val isPlaying: Boolean = false,
    val isEnded: Boolean = false,
    val currentMediaId: String? = null,
    val currentIndex: Int = 0,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val queueSize: Int = 0,
    val queue: List<QueueTrack> = emptyList(),
)

/** One entry in the player queue, for the Current Queue screen (PRD §6.3). */
data class QueueTrack(
    val mediaId: String,
    val title: String,
    val artist: String,
)
