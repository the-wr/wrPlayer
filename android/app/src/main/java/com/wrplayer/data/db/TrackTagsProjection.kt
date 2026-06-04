package com.wrplayer.data.db

import com.wrplayer.domain.facet.LibraryTrackTags
import com.wrplayer.domain.model.TagDimension

/**
 * Group flat `track_tags` rows ([TrackTagEntity]) into the per-track projection the Queue Editor's
 * faceted logic consumes ([LibraryTrackTags], PRD §6.2). One [LibraryTrackTags] per document URI,
 * with values bucketed by dimension. Rows with an unknown dimension key are ignored.
 */
fun List<TrackTagEntity>.toLibraryTrackTags(): List<LibraryTrackTags> {
    val byUri = LinkedHashMap<String, MutableMap<TagDimension, MutableSet<String>>>()
    for (row in this) {
        val dimension = TagDimension.fromKey(row.dimension) ?: continue
        val tags = byUri.getOrPut(row.documentUri) { mutableMapOf() }
        tags.getOrPut(dimension) { mutableSetOf() }.add(row.value)
    }
    return byUri.map { (uri, tags) -> LibraryTrackTags(uri, tags) }
}
