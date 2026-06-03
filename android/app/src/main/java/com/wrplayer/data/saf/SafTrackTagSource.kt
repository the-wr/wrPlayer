package com.wrplayer.data.saf

import android.net.Uri
import com.wrplayer.data.id3.Mp3TagData
import com.wrplayer.data.id3.SafId3Gateway
import com.wrplayer.data.scan.TrackTagSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Adapts [SafId3Gateway] to the reconciler's [TrackTagSource] seam (PRD §8.2). */
class SafTrackTagSource @Inject constructor(
    private val gateway: SafId3Gateway,
) : TrackTagSource {
    override suspend fun read(documentUri: String): Mp3TagData = withContext(Dispatchers.IO) {
        runCatching { gateway.read(Uri.parse(documentUri)) }.getOrDefault(Mp3TagData())
    }
}
