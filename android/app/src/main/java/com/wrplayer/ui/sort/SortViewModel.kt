package com.wrplayer.ui.sort

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.bpm.BpmDetector
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.playback.PlayerConnection
import com.wrplayer.data.playback.toMediaItem
import com.wrplayer.data.prefs.AppPreferences
import com.wrplayer.data.repo.TrackRepository
import com.wrplayer.domain.InboxTrack
import com.wrplayer.domain.SortOutcome
import com.wrplayer.domain.SortQueueBuilder
import com.wrplayer.domain.SortReducer
import com.wrplayer.domain.SortSession
import com.wrplayer.domain.model.SortOrder
import com.wrplayer.ui.nowplaying.NowPlayingUi
import com.wrplayer.ui.tagsheet.TagSheetLoader
import com.wrplayer.ui.tagsheet.TagSheetState
import com.wrplayer.ui.tagsheet.toMp3TagData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class SortPhase { LOADING, PICKER, PLAYING, END }

data class SortUiState(
    val phase: SortPhase = SortPhase.LOADING,
    val nowPlaying: NowPlayingUi? = null,
    val plusSelected: Boolean = false,
    val remaining: Int = 0,
    val inboxCount: Int = 0,
    val pickerOrder: SortOrder = SortOrder.NEWEST_FIRST,
    val emptyInbox: Boolean = false,
    val tagSheet: TagSheetState? = null,
    val scanning: Boolean = false,
)

/**
 * Drives Sort Mode (PRD §5): builds the static forward-only queue, plays one track at a time,
 * applies the voting state machine, opens the Tag Sheet at +2, deletes at −2, and triggers BPM
 * detection on first play.
 */
@HiltViewModel
class SortViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val repository: TrackRepository,
    private val player: PlayerConnection,
    private val bpmDetector: BpmDetector,
    private val prefs: AppPreferences,
    private val tagSheetLoader: TagSheetLoader,
    private val scanStatus: com.wrplayer.data.scan.ScanStatus,
) : ViewModel() {

    private val _state = MutableStateFlow(SortUiState())
    val state: StateFlow<SortUiState> = _state.asStateFlow()

    private var queue: List<String> = emptyList()
    private var index = 0
    private var current: TrackEntity? = null
    private var session = SortSession(0)
    private var tagSheetUri: String? = null
    private val bpmTriggered = mutableSetOf<String>()
    private var endedHandled = false

    init {
        player.connect()
        viewModelScope.launch {
            scanStatus.isScanning.collect { s -> _state.update { it.copy(scanning = s) } }
        }
        viewModelScope.launch {
            player.state.collect { ps ->
                _state.value.nowPlaying?.let { np ->
                    _state.update {
                        it.copy(nowPlaying = np.copy(isPlaying = ps.isPlaying, durationMs = ps.durationMs.takeIf { d -> d > 0 } ?: np.durationMs))
                    }
                }
                if (ps.isEnded && _state.value.phase == SortPhase.PLAYING && !endedHandled) {
                    endedHandled = true
                    advance() // track ended → commit pending +1 and move on (§5.2)
                } else if (!ps.isEnded) {
                    endedHandled = false
                }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(500)
                if (_state.value.phase == SortPhase.PLAYING) {
                    _state.value.nowPlaying?.let { np ->
                        _state.update { it.copy(nowPlaying = np.copy(positionMs = player.currentPositionMs())) }
                    }
                }
            }
        }
    }

    /** Enter Sort Mode: empty inbox → end state; otherwise show the order picker (§4.1). */
    fun enter() {
        viewModelScope.launch {
            val inbox = trackDao.getInbox()
            if (inbox.isEmpty()) {
                _state.update { it.copy(phase = SortPhase.END, emptyInbox = true, inboxCount = 0) }
            } else {
                _state.update { it.copy(phase = SortPhase.PICKER, pickerOrder = prefs.lastSortOrder, inboxCount = inbox.size) }
            }
        }
    }

    fun confirmOrder(order: SortOrder) {
        prefs.lastSortOrder = order
        viewModelScope.launch {
            val inbox = trackDao.getInbox().map { InboxTrack(it.documentUri, it.fileMtime, it.sortScore) }
            queue = SortQueueBuilder.build(inbox, order)
            index = 0
            loadCurrent()
        }
    }

    private suspend fun loadCurrent() {
        if (index >= queue.size) {
            _state.update { it.copy(phase = SortPhase.END, emptyInbox = false, nowPlaying = null) }
            return
        }
        val track = trackDao.getByUri(queue[index])
        if (track == null) { // row vanished (e.g. deleted) → skip
            index++
            loadCurrent()
            return
        }
        current = track
        session = SortSession(track.sortScore)
        endedHandled = false
        player.setQueueAndPlay(listOf(track.toMediaItem()))
        _state.update {
            it.copy(
                phase = SortPhase.PLAYING,
                plusSelected = false,
                remaining = queue.size - index,
                nowPlaying = NowPlayingUi(
                    title = track.title?.ifBlank { null } ?: track.filePath.substringAfterLast('/'),
                    artist = track.artist.orEmpty(),
                    album = track.album.orEmpty(),
                    positionMs = 0,
                    durationMs = 0,
                    isPlaying = true,
                    showPrevious = false,
                ),
            )
        }
        triggerBpm(track)
    }

    fun onPlusOne() {
        val out = SortReducer.tapPlusOne(session)
        session = out.session
        _state.update { it.copy(plusSelected = session.pendingPlusOne) }
        if (out.openTagSheet) current?.let { openTagSheet(it) }
    }

    fun onMinusOne() {
        val uri = current?.documentUri ?: return
        viewModelScope.launch {
            when (val out = SortReducer.tapMinusOne(session)) {
                is SortOutcome.Delete -> repository.delete(uri)
                is SortOutcome.Advance -> trackDao.updateScore(uri, out.committedScore)
                else -> {}
            }
            advanceToNext()
        }
    }

    fun onSkip() = advance()
    fun onNext() = advance()

    private fun advance() {
        val uri = current?.documentUri ?: return
        viewModelScope.launch {
            val out = SortReducer.advance(session)
            trackDao.updateScore(uri, out.committedScore)
            advanceToNext()
        }
    }

    private suspend fun advanceToNext() {
        index++
        loadCurrent()
    }

    fun onSeekFraction(fraction: Float) {
        val dur = _state.value.nowPlaying?.durationMs ?: return
        player.seekTo((fraction * dur).toLong())
    }

    fun onPlayPause() = player.playPause()

    // ---- Tag Sheet ----

    private fun openTagSheet(track: TrackEntity) {
        tagSheetUri = track.documentUri
        viewModelScope.launch {
            val sheet = tagSheetLoader.load(track, isEdit = false)
            _state.update { it.copy(tagSheet = sheet) }
        }
    }

    fun updateTagSheet(transform: (TagSheetState) -> TagSheetState) {
        _state.update { it.copy(tagSheet = it.tagSheet?.let(transform)) }
    }

    fun confirmTagSheet() {
        val sheet = _state.value.tagSheet ?: return
        val uri = tagSheetUri ?: return
        viewModelScope.launch {
            repository.promote(uri, sheet.toMp3TagData(status = "library"))
            _state.update { it.copy(tagSheet = null) }
            tagSheetUri = null
        }
    }

    fun dismissTagSheet() {
        _state.update { it.copy(tagSheet = null) }
        tagSheetUri = null
    }

    // ---- BPM ----

    private fun triggerBpm(track: TrackEntity) {
        if (track.bpmDetected != null || track.documentUri in bpmTriggered) return
        bpmTriggered += track.documentUri
        viewModelScope.launch {
            val temp = copyToCache(track.documentUri) ?: return@launch
            try {
                bpmDetector.detect(temp)?.let { trackDao.updateBpmDetected(track.documentUri, it) }
            } finally {
                temp.delete()
            }
        }
    }

    /** TEMPORARY: seed inbox rows for fixtures pushed to the app's external dir, for verification. */
    fun seedDemoInbox() {
        viewModelScope.launch {
            val dir = context.getExternalFilesDir(null)
            listOf("sample1.mp3", "sample2.mp3").forEach { name ->
                val f = File(dir, name)
                if (f.exists()) {
                    trackDao.upsertWithTags(
                        TrackEntity(
                            documentUri = Uri.fromFile(f).toString(),
                            filePath = f.absolutePath,
                            status = "inbox",
                            fileMtime = f.lastModified(),
                        ),
                    )
                }
            }
            enter()
        }
    }

    private fun copyToCache(uri: String): File? = try {
        val temp = File.createTempFile("wr-bpm", ".mp3", context.cacheDir)
        context.contentResolver.openInputStream(Uri.parse(uri))!!.use { input ->
            temp.outputStream().use { input.copyTo(it) }
        }
        temp
    } catch (e: Exception) {
        null
    }
}
