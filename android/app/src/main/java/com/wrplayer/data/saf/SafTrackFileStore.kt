package com.wrplayer.data.saf

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.wrplayer.data.id3.Mp3TagData
import com.wrplayer.data.id3.SafId3Gateway
import com.wrplayer.data.repo.TrackFileStore
import com.wrplayer.data.scan.WatchedTreeSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** SAF-backed [TrackFileStore]: ID3 writes via [SafId3Gateway], moves via [FileMover] (PRD §8). */
class SafTrackFileStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gateway: SafId3Gateway,
    private val fileMover: FileMover,
    private val trees: WatchedTreeSource,
) : TrackFileStore {

    override suspend fun writeTags(documentUri: String, data: Mp3TagData) =
        withContext(Dispatchers.IO) { gateway.write(Uri.parse(documentUri), data) }

    override suspend fun promoteFile(documentUri: String): String {
        val tree = trees.persistedTrees().firstOrNull { SafMembership.isUnderTree(documentUri, it) }
            ?: return documentUri
        return fileMover.moveToLibrary(documentUri, tree)
    }

    override suspend fun deleteFile(documentUri: String) = withContext(Dispatchers.IO) {
        DocumentsContract.deleteDocument(context.contentResolver, Uri.parse(documentUri))
        Unit
    }
}
