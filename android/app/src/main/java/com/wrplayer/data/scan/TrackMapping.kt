package com.wrplayer.data.scan

import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.db.joinMultiValue
import com.wrplayer.data.id3.Mp3TagData
import com.wrplayer.domain.model.TrackStatus

/**
 * Builds/updates a [TrackEntity] cache row from freshly-read ID3 tags (PRD §8.2 / §10.2).
 *
 * Classification is by the `TXXX:STATUS` tag only — folder location is never used (§8.2). On
 * update the app-only columns `sort_score` and `bpm_detected` are **preserved** so an external tag
 * edit never resets a track's score or detected BPM (§8.2).
 */
object TrackMapping {

    fun buildInsert(discovered: DiscoveredFile, tags: Mp3TagData, now: Long): TrackEntity =
        TrackEntity(
            documentUri = discovered.documentUri,
            filePath = discovered.filePath,
            status = TrackStatus.fromKey(tags.status).key,
            sortScore = 0,
            bpmDetected = null,
            title = tags.title,
            artist = tags.artist,
            album = tags.album,
            genre = tags.genres.joinMultiValue(),
            mood = tags.moods.joinMultiValue(),
            pace = tags.pace,
            bpm = tags.bpm,
            labels = tags.labels.joinMultiValue(),
            dateAdded = now,
            fileMtime = discovered.fileMtime,
            hasArt = tags.hasArt,
        )

    fun buildUpdate(existing: TrackEntity, discovered: DiscoveredFile, tags: Mp3TagData): TrackEntity =
        buildInsert(discovered, tags, existing.dateAdded).copy(
            sortScore = existing.sortScore,
            bpmDetected = existing.bpmDetected,
        )

    /**
     * Apply edited [tags] to an existing row (Tag Sheet confirm / edit — PRD §5.3 / §7), keeping the
     * app-only columns. [newUri] re-keys the row after a promotion move (§8.3).
     */
    fun applyTags(
        existing: TrackEntity,
        tags: Mp3TagData,
        status: String,
        newUri: String = existing.documentUri,
    ): TrackEntity = existing.copy(
        documentUri = newUri,
        status = status,
        title = tags.title,
        artist = tags.artist,
        album = tags.album,
        genre = tags.genres.joinMultiValue(),
        mood = tags.moods.joinMultiValue(),
        pace = tags.pace,
        bpm = tags.bpm,
        labels = tags.labels.joinMultiValue(),
        hasArt = tags.hasArt,
    )
}
