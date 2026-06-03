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

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }

    /** Current position for smooth progress updates (the listener only fires on discrete events). */
    fun currentPositionMs(): Long = controller?.currentPosition ?: 0L

    private fun pushState() {
        val c = controller
        if (c == null) {
            _state.value = PlaybackState()
            return
        }
        val meta = c.currentMediaItem?.mediaMetadata
        _state.value = PlaybackState(
            isConnected = true,
            isPlaying = c.isPlaying,
            currentMediaId = c.currentMediaItem?.mediaId,
            title = meta?.title?.toString(),
            artist = meta?.artist?.toString(),
            album = meta?.albumTitle?.toString(),
            positionMs = c.currentPosition.coerceAtLeast(0L),
            durationMs = c.duration.coerceAtLeast(0L),
            hasNext = c.hasNextMediaItem(),
            hasPrevious = c.hasPreviousMediaItem(),
            queueSize = c.mediaItemCount,
        )
    }
}
