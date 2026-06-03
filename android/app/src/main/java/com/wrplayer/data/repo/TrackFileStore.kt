package com.wrplayer.data.repo

import com.wrplayer.data.id3.Mp3TagData

/**
 * Seam over the SAF file operations the repository performs (write ID3, move on promotion, delete),
 * so the repository's DB orchestration is unit-testable with a fake (PRD §10.2).
 */
interface TrackFileStore {
    /** Write tags to the file at [documentUri] (file is the source of truth, written first). */
    suspend fun writeTags(documentUri: String, data: Mp3TagData)

    /** Move the file into its watched tree's `Library/`; returns the new document URI (or the same). */
    suspend fun promoteFile(documentUri: String): String

    /** Permanently delete the file. */
    suspend fun deleteFile(documentUri: String)
}
