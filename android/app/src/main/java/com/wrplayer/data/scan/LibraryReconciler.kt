package com.wrplayer.data.scan

import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.saf.SafMembership
import javax.inject.Inject

/**
 * Reconciles the DB cache with the watched folders (PRD §8.2): inserts newly found files, updates
 * rows whose mtime changed (preserving `sort_score`/`bpm_detected`), and removes rows for files
 * confirmed absent — but only within folders that were successfully enumerated. If no folder is
 * reachable, nothing is removed (the unmounted-SD guard).
 */
class LibraryReconciler @Inject constructor(
    private val trees: WatchedTreeSource,
    private val enumerator: DocumentEnumerator,
    private val tagSource: TrackTagSource,
    private val trackDao: TrackDao,
) {
    suspend fun reconcile() {
        val reachable = trees.persistedTrees().filter { trees.isReachable(it) }
        if (reachable.isEmpty()) return // Nothing enumerated → never remove (guard, §8.2).

        val discovered = reachable.flatMap { enumerator.enumerate(it) }
        val existing = trackDao.getAll()
        val existingByUri = existing.associateBy { it.documentUri }

        // Only rows under a reachable, enumerated tree are eligible for removal.
        val eligible = existing
            .filter { row -> reachable.any { SafMembership.isUnderTree(row.documentUri, it) } }
            .associate { it.documentUri to it.fileMtime }

        val plan = ReconcilePlanner.plan(eligible, discovered)
        val now = System.currentTimeMillis()

        for (file in plan.toInsert) {
            val tags = tagSource.read(file.documentUri)
            trackDao.upsertWithTags(TrackMapping.buildInsert(file, tags, now))
        }
        for (file in plan.toUpdate) {
            val tags = tagSource.read(file.documentUri)
            val existingRow = existingByUri.getValue(file.documentUri)
            trackDao.upsertWithTags(TrackMapping.buildUpdate(existingRow, file, tags))
        }
        for (uri in plan.toRemove) {
            trackDao.deleteTrack(uri)
        }
    }
}
