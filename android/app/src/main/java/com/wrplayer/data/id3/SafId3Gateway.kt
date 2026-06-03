package com.wrplayer.data.id3

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

/**
 * Bridges a SAF document [Uri] to the [File]-based [Id3Reader]/[Id3Writer] (PRD §8.3). JAudioTagger
 * needs a real `java.io.File`, but watched-folder files are SAF documents, so we copy the document
 * into app cache, operate there, and copy the result back (truncating the original) on write.
 */
class SafId3Gateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reader: Id3Reader,
    private val writer: Id3Writer,
) {
    fun read(uri: Uri): Mp3TagData {
        val temp = copyToCache(uri)
        return try {
            reader.read(temp)
        } finally {
            temp.delete()
        }
    }

    fun write(uri: Uri, data: Mp3TagData) {
        val temp = copyToCache(uri)
        try {
            writer.write(temp, data)
            copyBack(temp, uri)
        } finally {
            temp.delete()
        }
    }

    private fun copyToCache(uri: Uri): File {
        val temp = File.createTempFile("wr-id3", ".mp3", context.cacheDir)
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Cannot open input stream for $uri")
        input.use { src -> temp.outputStream().use { src.copyTo(it) } }
        return temp
    }

    /** Truncate the original document and overwrite it with the edited cache copy. */
    private fun copyBack(temp: File, uri: Uri) {
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("Cannot open output stream for $uri")
        output.use { dst -> temp.inputStream().use { it.copyTo(dst) } }
    }
}
