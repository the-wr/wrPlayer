package com.wrplayer.data.id3

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX
import java.io.File
import javax.inject.Inject

/**
 * Reads ID3 metadata from a local MP3 [File] via JAudioTagger (PRD §2.3). Pure JVM — no Android
 * dependency — so it is unit-testable off-device. SAF URIs are bridged to a [File] by
 * [SafId3Gateway].
 */
class Id3Reader @Inject constructor() {

    fun read(file: File): Mp3TagData {
        val tag: Tag = AudioFileIO.read(file).tag ?: return Mp3TagData()
        return Mp3TagData(
            title = tag.firstOrNull(FieldKey.TITLE),
            artist = tag.firstOrNull(FieldKey.ARTIST),
            album = tag.firstOrNull(FieldKey.ALBUM),
            genres = tag.allSplit(FieldKey.GENRE),
            moods = tag.allSplit(FieldKey.MOOD),
            bpm = tag.firstOrNull(FieldKey.BPM)?.toIntOrNull(),
            pace = tag.readTxxx(Txxx.PACE).firstOrNull(),
            labels = tag.readTxxx(Txxx.LABELS),
            status = tag.readTxxx(Txxx.STATUS).firstOrNull(),
            hasArt = tag.artworkList.isNotEmpty(),
        )
    }

    private fun Tag.firstOrNull(key: FieldKey): String? =
        try { getFirst(key).takeIf { it.isNotBlank() } } catch (e: Exception) { null }

    /** All values for a multi-value key, defensively split on the ID3v2.4 null separator. */
    private fun Tag.allSplit(key: FieldKey): List<String> =
        try {
            getAll(key)
                .flatMap { it.split(ID3_NULL_SEPARATOR) }
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }

    /** Values of the `TXXX` frame whose description matches [description]. */
    private fun Tag.readTxxx(description: String): List<String> {
        val out = mutableListOf<String>()
        for (field in getFields("TXXX")) {
            val body = (field as? AbstractID3v2Frame)?.body as? FrameBodyTXXX ?: continue
            if (body.description == description) {
                out += body.values.filter { it.isNotBlank() }
            }
        }
        return out
    }
}
