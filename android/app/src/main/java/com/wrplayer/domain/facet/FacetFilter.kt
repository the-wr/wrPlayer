package com.wrplayer.domain.facet

import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension

/**
 * Pure faceted-filter logic for the Queue Editor (PRD §6.2). Operates on an in-memory projection
 * of the `track_tags` index ([LibraryTrackTags]); never touches ID3 or the filesystem (§10.1).
 *
 * Semantics:
 * - Within a dimension, included values combine as **OR**.
 * - Across dimensions, included sets combine as **AND**.
 * - Excluded values are always **AND NOT**.
 * - With no included chips, the whole Library matches (excluded-only selections narrow it).
 */
object FacetFilter {

    /** Whether [track] satisfies [filter]. */
    fun matches(track: LibraryTrackTags, filter: FilterState): Boolean {
        // AND NOT: any excluded value present disqualifies the track.
        for ((dimension, values) in filter.excluded) {
            if (values.isEmpty()) continue
            if (track.tags[dimension].orEmpty().any { it in values }) return false
        }
        // AND across dimensions; OR within a dimension.
        for ((dimension, values) in filter.included) {
            if (values.isEmpty()) continue
            if (track.tags[dimension].orEmpty().none { it in values }) return false
        }
        return true
    }

    /** Document URIs of all matching tracks. */
    fun matchingUris(tracks: List<LibraryTrackTags>, filter: FilterState): List<String> =
        tracks.filter { matches(it, filter) }.map { it.documentUri }

    /** Size of the match set — the figure shown on the CTAs / "N tracks match" (PRD §6.2). */
    fun matchCount(tracks: List<LibraryTrackTags>, filter: FilterState): Int =
        tracks.count { matches(it, filter) }

    /**
     * The chips to render per dimension (PRD §6.2). For each candidate value the [FacetValue.count]
     * is the *prospective* match count if that chip were toggled to included given the current
     * selection. Values with a prospective count of zero are hidden, except a value currently set
     * to excluded (which stays visible so the exclusion can be removed). Sorted by count desc,
     * then value asc for stable ordering.
     */
    fun facets(
        tracks: List<LibraryTrackTags>,
        filter: FilterState,
    ): Map<TagDimension, List<FacetValue>> {
        val result = LinkedHashMap<TagDimension, List<FacetValue>>()
        for (dimension in TagDimension.entries) {
            val candidates = candidateValues(tracks, filter, dimension)
            val facetValues = candidates.mapNotNull { value ->
                val state = filter.stateOf(dimension, value)
                val prospective = matchCount(tracks, filter.withIncluded(dimension, value))
                if (prospective == 0 && state != ChipState.EXCLUDED) {
                    null
                } else {
                    FacetValue(value, prospective, state)
                }
            }.sortedWith(compareByDescending<FacetValue> { it.count }.thenBy { it.value })
            if (facetValues.isNotEmpty()) result[dimension] = facetValues
        }
        return result
    }

    /**
     * Selected values in [filter] (included or excluded) that no longer match any track — i.e. their
     * prospective included-count is zero (PRD §6.2 "stale chips"). Used to surface the stale-preset
     * toast after loading a saved preset. Returned per dimension; only non-empty dimensions appear.
     */
    fun staleSelections(
        tracks: List<LibraryTrackTags>,
        filter: FilterState,
    ): Map<TagDimension, Set<String>> {
        val result = LinkedHashMap<TagDimension, Set<String>>()
        for (dimension in TagDimension.entries) {
            val selected = filter.included[dimension].orEmpty() + filter.excluded[dimension].orEmpty()
            // A value is stale if, on its own, it matches no track — independent of the rest of the
            // selection (PRD §6.2: a saved value that "no longer matches any track").
            val stale = selected.filterTo(mutableSetOf()) { value ->
                matchCount(tracks, FilterState().withIncluded(dimension, value)) == 0
            }
            if (stale.isNotEmpty()) result[dimension] = stale
        }
        return result
    }

    /** Every value present in [dimension], plus any value the filter currently excludes there. */
    private fun candidateValues(
        tracks: List<LibraryTrackTags>,
        filter: FilterState,
        dimension: TagDimension,
    ): Set<String> {
        val present = tracks.flatMapTo(mutableSetOf()) { it.tags[dimension].orEmpty() }
        present += filter.excluded[dimension].orEmpty()
        return present
    }
}
