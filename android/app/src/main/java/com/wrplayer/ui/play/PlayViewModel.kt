package com.wrplayer.ui.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.db.splitMultiValue
import com.wrplayer.data.playback.PlayerConnection
import com.wrplayer.data.prefs.AppPreferences
import com.wrplayer.data.repo.TrackRepository
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
    val tagSheet: TagSheetState? = null,
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
) : ViewModel() {

    private val _state = MutableStateFlow(PlayUiState())
    val state: StateFlow<PlayUiState> = _state.asStateFlow()

    private var currentUri: String? = null
    private var editUri: String? = null

    init {
        player.connect()
        viewModelScope.launch {
            player.state.collect { ps ->
                _state.update {
                    it.copy(
                        queueCount = ps.queueSize,
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
            }
        }
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(500)
                _state.value.nowPlaying?.let { np ->
                    _state.update { it.copy(nowPlaying = np.copy(positionMs = player.currentPositionMs())) }
                }
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
