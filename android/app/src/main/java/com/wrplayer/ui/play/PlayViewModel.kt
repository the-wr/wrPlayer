package com.wrplayer.ui.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.db.splitMultiValue
import com.wrplayer.data.playback.PlayerConnection
import com.wrplayer.data.playback.QueueTrack
import com.wrplayer.data.playback.toMediaItem
import com.wrplayer.data.prefs.AppPreferences
import com.wrplayer.data.repo.TrackRepository
import com.wrplayer.domain.QueuePruner
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.ui.nowplaying.NowPlayingUi
import com.wrplayer.ui.tagsheet.TagSheetLoader
import com.wrplayer.ui.tagsheet.TagSheetState
import com.wrplayer.ui.tagsheet.toMp3TagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayUiState(
    val nowPlaying: NowPlayingUi? = null,
    val tags: List<Pair<TagDimension, String>> = emptyList(),
    val queueCount: Int = 0,
    val queue: List<QueueTrack> = emptyList(),
    val currentIndex: Int = 0,
    val tagSheet: TagSheetState? = null,
    val scanning: Boolean = false,
)

/**
 * Drives Play Mode's Now Playing (PRD §4.1): reflects the live [PlayerConnection] queue/transport,
 * surfaces the current track's tags for the read-only chips, and hosts the Edit→Tag Sheet path.
 */
@HiltViewModel
class PlayViewModel @Inject constructor(
    private val trackDao: TrackDao,
    private val player: PlayerConnection,
    private val prefs: AppPreferences,
    private val tagSheetLoader: TagSheetLoader,
    private val repository: TrackRepository,
    private val scanStatus: com.wrplayer.data.scan.ScanStatus,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayUiState())
    val state: StateFlow<PlayUiState> = _state.asStateFlow()

    private var currentUri: String? = null
    private var editUri: String? = null
    private var restoreAttempted = false
    private var positionSaveTick = 0

    init {
        player.connect()
        viewModelScope.launch {
            scanStatus.isScanning.collect { s -> _state.update { it.copy(scanning = s) } }
        }
        viewModelScope.launch {
            player.state.collect { ps ->
                maybeRestoreQueue(ps)
                _state.update {
                    it.copy(
                        queueCount = ps.queueSize,
                        queue = ps.queue,
                        currentIndex = ps.currentIndex,
                        nowPlaying = ps.currentMediaId?.let { _ ->
                            NowPlayingUi(
                                title = ps.title.orEmpty(),
                                artist = ps.artist.orEmpty(),
                                album = ps.album.orEmpty(),
                                positionMs = ps.positionMs,
                                durationMs = ps.durationMs,
                                isPlaying = ps.isPlaying,
                                showPrevious = true,
                            )
                        },
                    )
                }
                if (ps.currentMediaId != currentUri) {
                    currentUri = ps.currentMediaId
                    loadTags(ps.currentMediaId)
                }
                // Persist queue structure + index on every player event (§6.3).
                if (ps.queueSize > 0) {
                    prefs.queueUris = ps.queue.map { it.mediaId }
                    prefs.queueIndex = ps.currentIndex
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(500)
                _state.value.nowPlaying?.let { np ->
                    _state.update { it.copy(nowPlaying = np.copy(positionMs = player.currentPositionMs())) }
                }
                // Persist play position roughly every 3s so a restart resumes near where it left off.
                if (++positionSaveTick % 6 == 0 && _state.value.queueCount > 0) {
                    prefs.queuePositionMs = player.currentPositionMs()
                }
            }
        }
        // Drop queue entries whose DB row was removed by a reconciliation walk (PRD §6.1); the
        // currently playing track is kept and finishes from its open handle.
        viewModelScope.launch {
            trackDao.observeAllUris().collect { uris ->
                val ps = player.state.value
                if (ps.queueSize == 0) return@collect
                val removals = QueuePruner.indicesToRemove(
                    queueMediaIds = ps.queue.map { it.mediaId },
                    existingUris = uris.toSet(),
                    currentIndex = ps.currentIndex,
                )
                removals.forEach { player.removeItem(it) }
            }
        }
    }

    /** Restore the persisted queue once, on first connect, if the player is empty (PRD §4.1 / §6.3). */
    private fun maybeRestoreQueue(ps: com.wrplayer.data.playback.PlaybackState) {
        if (restoreAttempted || !ps.isConnected || ps.queueSize > 0) return
        restoreAttempted = true
        val uris = prefs.queueUris
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val byUri = trackDao.getByUris(uris).associateBy { it.documentUri }
            val items = uris.mapNotNull { byUri[it]?.toMediaItem() }
            if (items.isNotEmpty()) {
                player.restoreQueue(items, prefs.queueIndex.coerceIn(0, items.lastIndex), prefs.queuePositionMs)
            }
        }
    }

    private fun loadTags(uri: String?) {
        if (uri == null) {
            _state.update { it.copy(tags = emptyList()) }
            return
        }
        viewModelScope.launch {
            val track = trackDao.getByUri(uri)
            _state.update { it.copy(tags = track?.toTagChips().orEmpty()) }
        }
    }

    fun onPlayPause() = player.playPause()
    fun onNext() = player.next()
    fun onPrevious() = player.previous()

    // ---- Current Queue ----
    fun onJump(index: Int) = player.seekToItem(index)
    fun onMove(from: Int, to: Int) = player.moveItem(from, to)
    fun onRemove(index: Int) = player.removeItem(index)

    fun onSeekFraction(fraction: Float) {
        val dur = _state.value.nowPlaying?.durationMs ?: return
        player.seekTo((fraction * dur).toLong())
    }

    // ---- Edit → Tag Sheet ----

    fun openEdit() {
        val uri = currentUri ?: return
        editUri = uri
        viewModelScope.launch {
            val track = trackDao.getByUri(uri) ?: return@launch
            _state.update { it.copy(tagSheet = tagSheetLoader.load(track, isEdit = true)) }
        }
    }

    fun updateTagSheet(transform: (TagSheetState) -> TagSheetState) {
        _state.update { it.copy(tagSheet = it.tagSheet?.let(transform)) }
    }

    fun confirmTagSheet() {
        val sheet = _state.value.tagSheet ?: return
        val uri = editUri ?: return
        viewModelScope.launch {
            repository.editTags(uri, sheet.toMp3TagData(status = "library"))
            _state.update { it.copy(tagSheet = null) }
            editUri = null
            loadTags(currentUri)
        }
    }

    fun dismissTagSheet() {
        _state.update { it.copy(tagSheet = null) }
        editUri = null
    }
}

/** The current track's tags as ordered (dimension, value) chips for the Play panel (PRD §4.1). */
private fun TrackEntity.toTagChips(): List<Pair<TagDimension, String>> {
    val chips = mutableListOf<Pair<TagDimension, String>>()
    genre.splitMultiValue().forEach { chips += TagDimension.GENRE to it }
    mood.splitMultiValue().forEach { chips += TagDimension.MOOD to it }
    pace?.takeIf { it.isNotBlank() }?.let { chips += TagDimension.PACE to it }
    labels.splitMultiValue().forEach { chips += TagDimension.LABELS to it }
    return chips
}
