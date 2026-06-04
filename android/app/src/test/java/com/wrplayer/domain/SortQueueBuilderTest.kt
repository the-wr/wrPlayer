package com.wrplayer.domain

import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.model.SortOrder
import org.junit.Test

class SortQueueBuilderTest {
    private val inbox = listOf(
        InboxTrack("a", fileMtime = 100, sortScore = 0),
        InboxTrack("b", fileMtime = 300, sortScore = -1),
        InboxTrack("c", fileMtime = 200, sortScore = 1),
    )

    @Test fun newestFirst_ordersByMtimeDescending() {
        assertThat(SortQueueBuilder.build(inbox, SortOrder.NEWEST_FIRST))
            .containsExactly("b", "c", "a").inOrder()
    }

    @Test fun closestToThreshold_ordersByAbsScoreDescending() {
        // |−1| and |1| tie above |0|; a/0 is last.
        assertThat(SortQueueBuilder.build(inbox, SortOrder.CLOSEST_TO_THRESHOLD).last()).isEqualTo("a")
    }

    @Test fun random_keepsAllTracks() {
        assertThat(SortQueueBuilder.build(inbox, SortOrder.RANDOM))
            .containsExactly("a", "b", "c")
    }
}
