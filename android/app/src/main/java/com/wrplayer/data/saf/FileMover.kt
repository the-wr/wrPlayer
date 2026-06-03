package com.wrplayer.data.saf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Moves a promoted file into the flat `Library/` folder at the root of its watched tree (PRD §8.1).
 * Collisions get a numeric suffix. Returns the new document URI so the caller can re-key the DB row
 * (§8.3); returns the original URI unchanged when the file already lives under `Library/`.
 */
class FileMover @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun moveToLibrary(documentUri: String, treeUri: String): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val tree = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
            ?: error("Unreachable tree $treeUri")
        val sourceUri = Uri.parse(documentUri)

        // Already under Library/ → leave in place (re-tagging never moves a library track, §8.1).
        if (isUnderLibrary(documentUri)) return@withContext documentUri

        val library = tree.findFile(LIBRARY_DIR)?.takeIf { it.isDirectory }
            ?: tree.createDirectory(LIBRARY_DIR)
            ?: error("Could not create $LIBRARY_DIR under $treeUri")

        val source = DocumentFile.fromSingleUri(context, sourceUri)
        val desiredName = source?.name ?: "track.mp3"
        val existing = library.listFiles().mapNotNull { it.name }.toSet()
        val uniqueName = FileNaming.uniqueName(desiredName, existing)

        val destUri = DocumentsContract.createDocument(
            resolver, library.uri, "audio/mpeg", uniqueName,
        ) ?: error("Could not create document $uniqueName")

        resolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Cannot read source $documentUri" }
            resolver.openOutputStream(destUri).use { output ->
                requireNotNull(output) { "Cannot write destination $destUri" }
                input.copyTo(output)
            }
        }
        DocumentsContract.deleteDocument(resolver, sourceUri)
        destUri.toString()
    }

    private fun isUnderLibrary(documentUri: String): Boolean {
        // Document IDs are path-derived (e.g. "primary:Music/Library/x.mp3").
        val decoded = Uri.decode(documentUri)
        return decoded.contains("/$LIBRARY_DIR/")
    }

    private companion object {
        const val LIBRARY_DIR = "Library"
    }
}
