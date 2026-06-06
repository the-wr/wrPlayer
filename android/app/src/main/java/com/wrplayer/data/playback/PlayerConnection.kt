package com.wrplayer.data.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps a Media3 [MediaController] bound to [PlaybackService] and exposes playback state as a
 * [StateFlow] plus simple commands (PRD §4.1 / §6.1). In Sort Mode the queue is forward-only; in
 * Play Mode it is the persisted queue.
 */
@Singleton
class PlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var controller: MediaController? = null
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = pushState()
    }

    /** Connect to the session service; safe to call repeatedly. */
    fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            controller = future.get().also { it.addListener(listener) }
            pushState()
        }, ContextCompat.getMainExecutor(context))
    }

    fun release() {
        controller?.release()
        controller = null
        _state.value = PlaybackState()
    }

    fun setQueueAndPlay(items: List<MediaItem>, startIndex: Int = 0) {
        val c = controller ?: return
        c.setMediaItems(items, startIndex, 0L)
        c.prepare()
        c.play()
    }

    /** Append items to the end of the current queue (PRD §6.2 "Add to Queue" / "Enqueue"). */
    fun addToQueue(items: List<MediaItem>) {
        val c = controller ?: return
        if (items.isEmpty()) return
        c.addMediaItems(items)
        c.prepare()
    }

    /** Insert items immediately after the currently playing track (PRD §6.2 "Play Next"). */
    fun playNext(items: List<MediaItem>) {
        val c = controller ?: return
        if (items.isEmpty()) return
        val at = if (c.mediaItemCount == 0) 0 else c.currentMediaItemIndex + 1
        c.addMediaItems(at, items)
        c.prepare()
    }

    /** Document URIs of the current queue, in order (for persistence, §6.3). */
    fun queueMediaIds(): List<String> {
        val c = controller ?: return emptyList()
        return (0 until c.mediaItemCount).mapNotNull { c.getMediaItemAt(it).mediaId }
    }

    fun currentIndex(): Int = controller?.currentMediaItemIndex ?: 0

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    /** Restore a persisted queue without starting playback (PRD §4.1 / §6.3): prepared but paused. */
    fun restoreQueue(items: List<MediaItem>, startIndex: Int, positionMs: Long) {
        val c = controller ?: return
        if (items.isEmpty()) return
        val index = startIndex.coerceIn(0, items.lastIndex)
        c.setMediaItems(items, index, positionMs)
        c.prepare()
    }

    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }

    /** Reorder the queue (drag-to-reorder, PRD §6.3). */
    fun moveItem(from: Int, to: Int) { controller?.moveMediaItem(from, to) }

    /** Remove a queued track (swipe-to-remove, PRD §6.3). */
    fun removeItem(index: Int) { controller?.removeMediaItem(index) }

    /** Jump to and play a queued track (tap-to-jump, PRD §6.3). */
    fun seekToItem(index: Int) {
        val c = controller ?: return
        c.seekTo(index, 0L)
        c.play()
    }

    /** Current position for smooth progress updates (the listener only fires on discrete events). */
    fun currentPositionMs(): Long = controller?.currentPosition ?: 0L

    private fun pushState() {
        val c = controller
        if (c == null) {
            _state.value = PlaybackState()
            return
        }
        val meta = c.currentMediaItem?.mediaMetadata
        val queue = (0 until c.mediaItemCount).map { i ->
            val item = c.getMediaItemAt(i)
            QueueTrack(
                mediaId = item.mediaId,
                title = item.mediaMetadata.title?.toString().orEmpty(),
                artist = item.mediaMetadata.artist?.toString().orEmpty(),
            )
        }
        _state.value = PlaybackState(
            isConnected = true,
            isPlaying = c.isPlaying,
            isEnded = c.playbackState == Player.STATE_ENDED,
            currentMediaId = c.currentMediaItem?.mediaId,
            currentIndex = c.currentMediaItemIndex,
            title = meta?.title?.toString(),
            artist = meta?.artist?.toString(),
            album = meta?.albumTitle?.toString(),
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L),
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem(),
            queueSize = c.mediaItemCount,
            queue = queue,
        )
    }
}
