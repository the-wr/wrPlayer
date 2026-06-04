package com.wrplayer.domain

import com.wrplayer.domain.model.SortOrder
import kotlin.math.abs

/** A minimal inbox-track projection for ordering the sort queue (PRD §5.1). */
data class InboxTrack(val documentUri: String, val fileMtime: Long, val sortScore: Int)

/**
 * Builds the static, forward-only sort queue from the current inbox in the chosen order (PRD §5.1).
 * Built once per entry into Sort Mode; not reordered mid-session.
 */
object SortQueueBuilder {
    fun build(inbox: List<InboxTrack>, order: SortOrder): List<String> = when (order) {
        SortOrder.NEWEST_FIRST -> inbox.sortedByDescending { it.fileMtime }
        SortOrder.RANDOM -> inbox.shuffled()
        SortOrder.CLOSEST_TO_THRESHOLD -> inbox.sortedByDescending { abs(it.sortScore) }
    }.map { it.documentUri }
}
