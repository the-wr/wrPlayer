package com.wrplayer.ui.debug

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.wrplayer.data.playback.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * TEMPORARY: drives [PlayerConnection] for manual Phase 5 verification of the Media3 service.
 * Replaced by the real Now Playing / Sort / Play flows in Phase 8. Plays fixtures pushed to
 * /sdcard/Music/ via adb.
 */
@HiltViewModel
class DebugPlaybackViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val player: PlayerConnection,
) : ViewModel() {

    // App's own external-files dir is always readable without storage permission (unlike /sdcard).
    private val sampleDir = context.getExternalFilesDir(null)

    val state = player.state

    init {
        player.connect()
    }

    fun playSamples() {
        val items = SAMPLE_FILES
            .map { File(sampleDir, it) }
            .filter { it.exists() }
            .map { file ->
                MediaItem.Builder()
                    .setMediaId(file.absolutePath)
                    .setUri(Uri.fromFile(file))
                    .setMediaMetadata(MediaMetadata.Builder().setTitle(file.name).build())
                    .build()
            }
        if (items.isNotEmpty()) player.setQueueAndPlay(items)
    }

    fun playPause() = player.playPause()
    fun next() = player.next()
    fun previous() = player.previous()

    private companion object {
        val SAMPLE_FILES = listOf("sample1.mp3", "sample2.mp3")
    }
}
