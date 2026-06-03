package com.wrplayer.data.scan

import com.wrplayer.data.id3.Mp3TagData

/**
 * Seams that isolate the SAF/filesystem specifics from the reconciliation orchestration so the
 * latter (the risky part — PRD §8.2) is unit-testable with fakes. Tree/document identities are
 * passed as URI strings to keep these interfaces free of Android types.
 */
interface WatchedTreeSource {
    /** The persisted watched-folder tree URIs (PRD §8). */
    suspend fun persistedTrees(): List<String>

    /** Whether a tree is currently mounted and its permission still held (PRD §8.2 guard). */
    suspend fun isReachable(treeUri: String): Boolean
}

interface DocumentEnumerator {
    /** All MP3 documents found under [treeUri] (PRD §8.2). */
    suspend fun enumerate(treeUri: String): List<DiscoveredFile>
}

interface TrackTagSource {
    /** Read ID3 tags for a document URI (PRD §8.2). */
    suspend fun read(documentUri: String): Mp3TagData
}
