package com.wrplayer.domain

/**
 * Decides which Play Mode queue entries to drop when a reconciliation walk removes their DB rows
 * (PRD §6.1). The **currently playing** entry is never dropped — it continues to its natural end from
 * the already-open file handle; only other entries whose row no longer exists are pruned. Returns the
 * indices to remove in **descending** order so a caller can delete them without reindexing issues.
 */
object QueuePruner {
    fun indicesToRemove(
        queueMediaIds: List<String>,
        existingUris: Set<String>,
        currentIndex: Int,
    ): List<Int> =
        queueMediaIds.indices
            .filter { it != currentIndex && queueMediaIds[it] !in existingUris }
            .sortedDescending()
}
