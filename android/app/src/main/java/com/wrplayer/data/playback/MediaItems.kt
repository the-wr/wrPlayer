package com.wrplayer.data.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.wrplayer.data.db.TrackEntity

/** Build a Media3 [MediaItem] for a track, keyed by its document URI with display metadata. */
fun TrackEntity.toMediaItem(): MediaItem = MediaItem.Builder()
    .setMediaId(documentUri)
    .setUri(Uri.parse(documentUri))
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title?.takeIf { it.isNotBlank() } ?: filePath.substringAfterLast('/'))
            .setArtist(artist)
            .setAlbumTitle(album)
            .build(),
    )
    .build()
