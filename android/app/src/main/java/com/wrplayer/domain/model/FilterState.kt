package com.wrplayer.domain.model

/**
 * The Queue Editor's chip selection (PRD §6.2). Per dimension, a set of [included] values
 * (OR within the dimension, AND across dimensions) and a set of [excluded] values (AND NOT).
 * Empty sets are never stored as keys.
 */
data class FilterState(
    val included: Map<TagDimension, Set<String>> = emptyMap(),
    val excluded: Map<TagDimension, Set<String>> = emptyMap(),
) {
    /** True when no chip is included — in which case the whole Library matches (PRD §6.2). */
    val hasAnyIncluded: Boolean
        get() = included.values.any { it.isNotEmpty() }

    val isEmpty: Boolean
        get() = !hasAnyIncluded && excluded.values.all { it.isEmpty() }

    fun stateOf(dimension: TagDimension, value: String): ChipState = when {
        included[dimension]?.contains(value) == true -> ChipState.INCLUDED
        excluded[dimension]?.contains(value) == true -> ChipState.EXCLUDED
        else -> ChipState.UNSELECTED
    }

    /** Advance one chip through the three-state cycle (PRD §6.2). */
    fun toggle(dimension: TagDimension, value: String): FilterState =
        when (stateOf(dimension, value)) {
            ChipState.UNSELECTED -> withMoved(dimension, value, include = true)
            ChipState.INCLUDED -> withMoved(dimension, value, exclude = true)
            ChipState.EXCLUDED -> withCleared(dimension, value)
        }

    /** State as if [value] were toggled to *included* in [dimension] (used for prospective counts). */
    fun withIncluded(dimension: TagDimension, value: String): FilterState =
        withMoved(dimension, value, include = true)

    private fun withMoved(
        dimension: TagDimension,
        value: String,
        include: Boolean = false,
        exclude: Boolean = false,
    ): FilterState {
        val inc = included.mutate(dimension) { if (include) it + value else it - value }
        val exc = excluded.mutate(dimension) { if (exclude) it + value else it - value }
        return FilterState(inc, exc)
    }

    private fun withCleared(dimension: TagDimension, value: String): FilterState {
        val inc = included.mutate(dimension) { it - value }
        val exc = excluded.mutate(dimension) { it - value }
        return FilterState(inc, exc)
    }

    private fun Map<TagDimension, Set<String>>.mutate(
        dimension: TagDimension,
        op: (Set<String>) -> Set<String>,
    ): Map<TagDimension, Set<String>> {
        val updated = op(this[dimension].orEmpty())
        val out = toMutableMap()
        if (updated.isEmpty()) out.remove(dimension) else out[dimension] = updated
        return out
    }

    companion object {
        val EMPTY = FilterState()
    }
}
