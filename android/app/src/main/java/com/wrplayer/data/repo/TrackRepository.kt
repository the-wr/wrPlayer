package com.wrplayer.data.repo

import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.id3.Mp3TagData
import com.wrplayer.data.scan.TrackMapping
import com.wrplayer.domain.model.TrackStatus
import javax.inject.Inject

/**
 * Write paths for track metadata (PRD §5.3 / §7 / §10.2). The file is always written first; the DB
 * cache then follows. If a DB update fails after a successful file write, the row's mtime differs
 * from disk and the next reconciliation walk repairs it.
 */
interface TrackRepository {
    /** Tag Sheet confirm on an inbox track: write tags + STATUS=library, move to Library/, re-key. */
    suspend fun promote(documentUri: String, data: Mp3TagData)

    /** Edit tags on an existing track without moving it (PRD §7). */
    suspend fun editTags(documentUri: String, data: Mp3TagData)

    /** Permanently delete a track (the −2 path, PRD §2.2). */
    suspend fun delete(documentUri: String)
}

class TrackRepositoryImpl @Inject constructor(
    private val fileStore: TrackFileStore,
    private val trackDao: TrackDao,
) : TrackRepository {

    override suspend fun promote(documentUri: String, data: Mp3TagData) {
        val libraryData = data.copy(status = TrackStatus.LIBRARY.key)
        fileStore.writeTags(documentUri, libraryData)          // file first
        val newUri = fileStore.promoteFile(documentUri)        // move → new URI
        val existing = trackDao.getByUri(documentUri) ?: return
        val row: TrackEntity = TrackMapping.applyTags(
            existing = existing,
            tags = libraryData,
            status = TrackStatus.LIBRARY.key,
            newUri = newUri,
        )
        trackDao.rekeyAndUpsert(documentUri, row)
    }

    override suspend fun editTags(documentUri: String, data: Mp3TagData) {
        val existing = trackDao.getByUri(documentUri) ?: return
        val tagged = data.copy(status = existing.status)
        fileStore.writeTags(documentUri, tagged)               // file first; never moves (§7)
        trackDao.upsertWithTags(TrackMapping.applyTags(existing, tagged, status = existing.status))
    }

    override suspend fun delete(documentUri: String) {
        fileStore.deleteFile(documentUri)                      // file first
        trackDao.deleteTrack(documentUri)
    }
}
