package com.wrplayer.data.scan

/** The three actions a reconciliation walk produces (PRD §8.2). */
data class ReconcilePlan(
    val toInsert: List<DiscoveredFile>,
    val toUpdate: List<DiscoveredFile>,
    val toRemove: List<String>,
)

/**
 * Pure diff of the DB's cached state against what a walk discovered (PRD §8.2). The caller must
 * pass only existing rows that belong to **successfully enumerated** folders, so rows under an
 * unavailable folder are never proposed for removal (the unmounted-SD guard, §8.2).
 */
object ReconcilePlanner {

    fun plan(existingMtimes: Map<String, Long>, discovered: List<DiscoveredFile>): ReconcilePlan {
        val discoveredUris = discovered.mapTo(HashSet()) { it.documentUri }
        val toInsert = discovered.filter { it.documentUri !in existingMtimes }
        val toUpdate = discovered.filter { d ->
            val known = existingMtimes[d.documentUri]
            known != null && known != d.fileMtime
        }
        val toRemove = existingMtimes.keys.filter { it !in discoveredUris }
        return ReconcilePlan(toInsert, toUpdate, toRemove)
    }
}
