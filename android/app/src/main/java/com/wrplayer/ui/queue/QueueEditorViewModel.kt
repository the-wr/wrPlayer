package com.wrplayer.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.toLibraryTrackTags
import com.wrplayer.data.playback.PlayerConnection
import com.wrplayer.data.playback.toMediaItem
import com.wrplayer.data.prefs.AppPreferences
import com.wrplayer.data.repo.PresetRepository
import com.wrplayer.data.repo.SavedPreset
import com.wrplayer.domain.facet.FacetFilter
import com.wrplayer.domain.facet.FacetValue
import com.wrplayer.domain.facet.LibraryTrackTags
import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One dimension's section in the editor body: its chips, already search-filtered (PRD §6.2). */
data class DimensionSection(
    val dimension: TagDimension,
    val chips: List<FacetValue>,
)

/** A chip in the active-filters row (PRD §6.2): a selected value, included or excluded. */
data class ActiveChip(
    val dimension: TagDimension,
    val value: String,
    val excluded: Boolean,
)

/** A row in the hold-to-preview overlay (PRD §6.2). */
data class PreviewTrack(
    val title: String,
    val artist: String,
)

data class QueueEditorUiState(
    val sections: List<DimensionSection> = emptyList(),
    val activeFilters: List<ActiveChip> = emptyList(),
    val presets: List<SavedPreset> = emptyList(),
    val matchCount: Int = 0,
    val search: String = "",
    val hasActiveFilters: Boolean = false,
    val staleMessage: String? = null,
)

/**
 * Drives the Queue Editor (PRD §6.2): projects `track_tags` to [LibraryTrackTags], combines it with
 * the session [QueueEditorStateHolder] filter and a search string into faceted sections + a live
 * match count, and runs the three CTAs (Shuffle & Play / Enqueue / Play Next) over the matching URIs.
 */
@HiltViewModel
class QueueEditorViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val stateHolder: QueueEditorStateHolder,
    private val presetRepository: PresetRepository,
    private val player: PlayerConnection,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val library: StateFlow<List<LibraryTrackTags>> =
        trackDao.observeAllTags()
            .map { it.toLibraryTrackTags() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val search = MutableStateFlow("")
    private val staleMessage = MutableStateFlow<String?>(null)

    private val _preview = MutableStateFlow<List<PreviewTrack>?>(null)
    /** Non-null while the hold-to-preview overlay is open: the matching tracks to list (PRD §6.2). */
    val preview: StateFlow<List<PreviewTrack>?> = _preview.asStateFlow()

    val state: StateFlow<QueueEditorUiState> = combine(
        library,
        stateHolder.filter,
        presetRepository.observeAll(),
        search,
        staleMessage,
    ) { lib, filter, presets, query, stale ->
        buildState(lib, filter, presets, query, stale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), QueueEditorUiState())

    private fun buildState(
        lib: List<LibraryTrackTags>,
        filter: FilterState,
        presets: List<SavedPreset>,
        query: String,
        stale: String?,
    ): QueueEditorUiState {
        val facets = FacetFilter.facets(lib, filter)
        val needle = query.trim().lowercase()
        val sections = facets.mapNotNull { (dimension, chips) ->
            val visible = if (needle.isEmpty()) chips
            else chips.filter { it.value.lowercase().contains(needle) }
            if (visible.isEmpty()) null else DimensionSection(dimension, visible)
        }
        val active = buildList {
            for (dimension in TagDimension.entries) {
                filter.included[dimension].orEmpty().forEach { add(ActiveChip(dimension, it, excluded = false)) }
                filter.excluded[dimension].orEmpty().forEach { add(ActiveChip(dimension, it, excluded = true)) }
            }
        }
        return QueueEditorUiState(
            sections = sections,
            activeFilters = active,
            presets = presets,
            matchCount = FacetFilter.matchCount(lib, filter),
            search = query,
            hasActiveFilters = active.isNotEmpty(),
            staleMessage = stale,
        )
    }

    fun onSearch(query: String) { search.value = query }

    fun toggle(dimension: TagDimension, value: String) = stateHolder.toggle(dimension, value)
    fun clear(dimension: TagDimension, value: String) = stateHolder.clear(dimension, value)
    fun reset() = stateHolder.reset()

    // ---- Presets ----

    fun savePreset(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { presetRepository.save(trimmed, stateHolder.filter.value) }
    }

    fun loadPreset(preset: SavedPreset) {
        stateHolder.set(preset.filter)
        val stale = FacetFilter.staleSelections(library.value, preset.filter)
        staleMessage.value = if (stale.isEmpty()) null else {
            val n = stale.values.sumOf { it.size }
            "$n saved ${if (n == 1) "filter" else "filters"} no longer match any track"
        }
    }

    fun deletePreset(name: String) {
        viewModelScope.launch { presetRepository.delete(name) }
    }

    fun renamePreset(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { presetRepository.rename(oldName, trimmed) }
    }

    fun dismissStaleMessage() { staleMessage.value = null }

    // ---- Preview ----

    fun openPreview() {
        viewModelScope.launch {
            val uris = FacetFilter.matchingUris(library.value, stateHolder.filter.value)
            val byUri = trackDao.getByUris(uris).associateBy { it.documentUri }
            _preview.value = uris.mapNotNull { uri ->
                byUri[uri]?.let {
                    PreviewTrack(
                        title = it.title?.ifBlank { null } ?: it.filePath.substringAfterLast('/'),
                        artist = it.artist.orEmpty(),
                    )
                }
            }
        }
    }

    fun closePreview() { _preview.value = null }

    // ---- CTAs ----

    /** Replace the queue with a shuffled match set and start playback (PRD §6.2). */
    fun shuffleAndPlay(onStarted: () -> Unit) = withMatches { items ->
        player.setQueueAndPlay(items)
        persistQueue()
        onStarted()
    }

    /** Append the shuffled match set to the queue (PRD §6.2 "Enqueue"). */
    fun enqueue(onDone: () -> Unit) = withMatches { items ->
        player.addToQueue(items)
        persistQueue()
        onDone()
    }

    /** Insert the shuffled match set after the current track (PRD §6.2 "Play Next"). */
    fun playNext(onDone: () -> Unit) = withMatches { items ->
        player.playNext(items)
        persistQueue()
        onDone()
    }

    private fun withMatches(action: (List<androidx.media3.common.MediaItem>) -> Unit) {
        viewModelScope.launch {
            val uris = FacetFilter.matchingUris(library.value, stateHolder.filter.value).shuffled()
            if (uris.isEmpty()) return@launch
            val byUri = trackDao.getByUris(uris).associateBy { it.documentUri }
            val items = uris.mapNotNull { byUri[it]?.toMediaItem() }
            if (items.isEmpty()) return@launch
            action(items)
        }
    }

    private fun persistQueue() {
        prefs.queueUris = player.queueMediaIds()
        prefs.queueIndex = player.currentIndex()
    }
}
