package com.wrplayer.data.id3

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v24Frame
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX
import org.jaudiotagger.tag.id3.valuepair.TextEncoding
import java.io.File
import javax.inject.Inject

/**
 * Writes [Mp3TagData] to a local MP3 [File] as ID3v2.4 (PRD §2.3 / §5.3). Pure JVM — unit-testable
 * off-device. Builds a fresh ID3v2.4 tag from the model (the model captures every field wrPlayer
 * cares about), preserving any embedded artwork from the existing tag.
 *
 * Multi-value Genre/Mood/Labels are stored as native ID3v2.4 null-separated lists; STATUS/PACE/
 * LABELS are custom `TXXX` frames.
 */
class Id3Writer @Inject constructor() {

    fun write(file: File, data: Mp3TagData) {
        val audioFile = AudioFileIO.read(file)
        val newTag = ID3v24Tag()

        // Preserve existing embedded artwork (the model only tracks presence, not bytes).
        audioFile.tag?.artworkList?.forEach { artwork ->
            runCatching { newTag.setField(artwork) }
        }

        newTag.setOrDelete(FieldKey.TITLE, data.title)
        newTag.setOrDelete(FieldKey.ARTIST, data.artist)
        newTag.setOrDelete(FieldKey.ALBUM, data.album)
        newTag.setOrDelete(FieldKey.BPM, data.bpm?.toString())
        newTag.setMulti(FieldKey.GENRE, data.genres)
        newTag.setMulti(FieldKey.MOOD, data.moods)

        newTag.addTxxx(Txxx.STATUS, listOfNotNull(data.status))
        newTag.addTxxx(Txxx.PACE, listOfNotNull(data.pace))
        newTag.addTxxx(Txxx.LABELS, data.labels)

        audioFile.tag = newTag
        AudioFileIO.write(audioFile)
    }

    private fun ID3v24Tag.setOrDelete(key: FieldKey, value: String?) {
        deleteField(key)
        if (!value.isNullOrBlank()) setField(key, value)
    }

    private fun ID3v24Tag.setMulti(key: FieldKey, values: List<String>) {
        deleteField(key)
        values.filter { it.isNotBlank() }.forEachIndexed { index, value ->
            if (index == 0) setField(key, value) else addField(key, value)
        }
    }

    private fun ID3v24Tag.addTxxx(description: String, values: List<String>) {
        val clean = values.filter { it.isNotBlank() }
        if (clean.isEmpty()) return
        val frame = ID3v24Frame("TXXX")
        val body = FrameBodyTXXX(TextEncoding.UTF_8, description, clean.first())
        clean.drop(1).forEach { body.addTextValue(it) }
        frame.body = body
        addField(frame)
    }
}
