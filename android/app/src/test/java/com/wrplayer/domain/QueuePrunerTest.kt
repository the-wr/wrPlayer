package com.wrplayer.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Queue-pruning rules when reconciliation removes rows mid-session (PRD §6.1). */
class QueuePrunerTest {

    @Test
    fun dropsRemovedNonCurrentEntries_descending() {
        val queue = listOf("a", "b", "c", "d")
        val existing = setOf("a", "c") // b and d removed
        // current = a (index 0); b(1) and d(3) pruned, descending
        assertThat(QueuePruner.indicesToRemove(queue, existing, currentIndex = 0))
            .containsExactly(3, 1).inOrder()
    }

    @Test
    fun neverDropsCurrentEvenIfRemoved() {
        val queue = listOf("a", "b", "c")
        val existing = setOf("a") // b and c removed, but c is current
        assertThat(QueuePruner.indicesToRemove(queue, existing, currentIndex = 2))
            .containsExactly(1)
    }

    @Test
    fun noChangesWhenAllPresent() {
        val queue = listOf("a", "b", "c")
        assertThat(QueuePruner.indicesToRemove(queue, setOf("a", "b", "c"), currentIndex = 1)).isEmpty()
    }

    @Test
    fun emptyQueue() {
        assertThat(QueuePruner.indicesToRemove(emptyList(), emptySet(), currentIndex = 0)).isEmpty()
    }
}
