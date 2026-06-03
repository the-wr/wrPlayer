package com.wrplayer.domain.facet

import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.TagDimension

/**
 * A single Library track projected to its tag values per dimension — the in-memory shape of the
 * `track_tags` index that the faceted queries (PRD §6.2) operate on. Inbox tracks are excluded,
 * matching §10.3 (only library tracks have `track_tags` rows).
 */
data class LibraryTrackTags(
    val documentUri: String,
    val tags: Map<TagDimension, Set<String>>,
)

/** One selectable chip in a Queue Editor dimension section: its [value], prospective [count], and [state]. */
data class FacetValue(
    val value: String,
    val count: Int,
    val state: ChipState,
)
