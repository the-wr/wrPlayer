package com.wrplayer.ui.queue

import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the Queue Editor's chip selection ([FilterState]) for the lifetime of the app session
 * (PRD §6.2): it survives navigation away and the Sort↔Play toggle, and resets to empty only on
 * process death (the holder is a fresh [FilterState.EMPTY] when the singleton is recreated).
 */
@Singleton
class QueueEditorStateHolder @Inject constructor() {
    private val _filter = MutableStateFlow(FilterState.EMPTY)
    val filter: StateFlow<FilterState> = _filter.asStateFlow()

    fun toggle(dimension: TagDimension, value: String) {
        _filter.update { it.toggle(dimension, value) }
    }

    /** Remove a value from both included and excluded sets (the × on an active-filter chip). */
    fun clear(dimension: TagDimension, value: String) {
        _filter.update { current ->
            FilterState(
                included = current.included.without(dimension, value),
                excluded = current.excluded.without(dimension, value),
            )
        }
    }

    fun set(filter: FilterState) {
        _filter.value = filter
    }

    fun reset() {
        _filter.value = FilterState.EMPTY
    }

    private fun Map<TagDimension, Set<String>>.without(dimension: TagDimension, value: String): Map<TagDimension, Set<String>> {
        val updated = this[dimension].orEmpty() - value
        val out = toMutableMap()
        if (updated.isEmpty()) out.remove(dimension) else out[dimension] = updated
        return out
    }
}
