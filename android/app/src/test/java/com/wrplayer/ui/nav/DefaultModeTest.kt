package com.wrplayer.ui.nav

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultModeTest {
    @Test fun noFolders_goesToWatchedFolders() {
        assertThat(DefaultMode.resolve(hasWatchedFolders = false, queueHasTracks = true))
            .isEqualTo(StartDestination.WATCHED_FOLDERS)
    }

    @Test fun foldersWithQueue_goesToNowPlaying() {
        assertThat(DefaultMode.resolve(hasWatchedFolders = true, queueHasTracks = true))
            .isEqualTo(StartDestination.NOW_PLAYING)
    }

    @Test fun foldersEmptyQueue_goesToQueueEditor() {
        assertThat(DefaultMode.resolve(hasWatchedFolders = true, queueHasTracks = false))
            .isEqualTo(StartDestination.QUEUE_EDITOR)
    }
}
