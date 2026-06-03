package com.wrplayer.data.saf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.wrplayer.data.scan.DiscoveredFile
import com.wrplayer.data.scan.DocumentEnumerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Enumerates MP3 documents under a SAF tree using [DocumentsContract] (PRD §8.2). Walks children
 * via `buildChildDocumentsUriUsingTree` — the dominant scan cost — rather than per-file
 * `DocumentFile`, and recurses into subfolders iteratively.
 */
class SafDocumentEnumerator @Inject constructor(
    @ApplicationContext private val context: Context,
) : DocumentEnumerator {

    override suspend fun enumerate(treeUri: String): List<DiscoveredFile> =
        withContext(Dispatchers.IO) {
            val tree = Uri.parse(treeUri)
            val out = mutableListOf<DiscoveredFile>()
            val pending = ArrayDeque<String>()
            pending.add(DocumentsContract.getTreeDocumentId(tree))

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            )

            while (pending.isNotEmpty()) {
                val parentDocId = pending.removeLast()
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentDocId)
                runCatching {
                    context.contentResolver.query(childrenUri, projection, null, null, null)
                }.getOrNull()?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(0)
                        val name = cursor.getString(1) ?: continue
                        val mime = cursor.getString(2)
                        val mtime = cursor.getLong(3)
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            pending.add(docId)
                        } else if (isMp3(name, mime)) {
                            val docUri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)
                            out += DiscoveredFile(docUri.toString(), docId, mtime)
                        }
                    }
                }
            }
            out
        }

    private fun isMp3(name: String, mime: String?): Boolean =
        name.endsWith(".mp3", ignoreCase = true) || mime == "audio/mpeg"
}
