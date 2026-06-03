package com.wrplayer.data.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.wrplayer.data.scan.WatchedTreeSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages persisted SAF watched-folder trees (PRD §8.3 / §8.4): takes/releases persistable URI
 * permissions and checks reachability before a walk (the unmounted-SD guard, §8.2).
 */
@Singleton
class SafTreeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : WatchedTreeSource {

    private val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    /** Persist read/write access to a tree picked via ACTION_OPEN_DOCUMENT_TREE. */
    fun persist(treeUri: Uri) {
        context.contentResolver.takePersistableUriPermission(treeUri, flags)
    }

    fun release(treeUri: Uri) {
        runCatching { context.contentResolver.releasePersistableUriPermission(treeUri, flags) }
    }

    override suspend fun persistedTrees(): List<String> =
        context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { it.uri.toString() }

    override suspend fun isReachable(treeUri: String): Boolean = withContext(Dispatchers.IO) {
        val doc = DocumentFile.fromTreeUri(context, Uri.parse(treeUri))
        doc != null && doc.exists() && doc.canRead()
    }
}
