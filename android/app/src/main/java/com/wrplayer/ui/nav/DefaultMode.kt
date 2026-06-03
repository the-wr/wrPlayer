package com.wrplayer.ui.nav

/** Where the app opens on launch (PRD §3). */
enum class StartDestination { WATCHED_FOLDERS, NOW_PLAYING, QUEUE_EDITOR }

/**
 * Default launch routing (PRD §3): with no watched folder, open Watched Folders; otherwise open in
 * Play Mode — restored at the persisted queue if it has tracks, else the Queue Editor.
 */
object DefaultMode {
    fun resolve(hasWatchedFolders: Boolean, queueHasTracks: Boolean): StartDestination = when {
        !hasWatchedFolders -> StartDestination.WATCHED_FOLDERS
        queueHasTracks -> StartDestination.NOW_PLAYING
        else -> StartDestination.QUEUE_EDITOR
    }
}
