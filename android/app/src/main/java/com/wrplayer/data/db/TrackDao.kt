package com.wrplayer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsertTrack(track: TrackEntity)

    @Insert
    suspend fun insertTags(rows: List<TrackTagEntity>)

    @Query("DELETE FROM track_tags WHERE document_uri = :uri")
    suspend fun deleteTagsFor(uri: String)

    /**
     * Upsert a track and rebuild its `track_tags` rows **in the same transaction** (PRD §10.2), so
     * the index never drifts from the cache. Only Library tracks are indexed (§10.3).
     */
    @Transaction
    suspend fun upsertWithTags(track: TrackEntity) {
        upsertTrack(track)
        deleteTagsFor(track.documentUri)
        if (track.isLibrary) insertTags(track.toTagRows())
    }

    @Query("DELETE FROM tracks WHERE document_uri = :uri")
    suspend fun deleteTrack(uri: String)

    /**
     * Re-key a row to a new document URI (after a promotion move, §8.3) and rebuild its tags, all in
     * one transaction. Deleting the old row cascades its `track_tags`; the new row rebuilds them.
     */
    @Transaction
    suspend fun rekeyAndUpsert(oldUri: String, track: TrackEntity) {
        if (oldUri != track.documentUri) deleteTrack(oldUri)
        upsertWithTags(track)
    }

    @Query("SELECT * FROM tracks WHERE document_uri = :uri")
    suspend fun getByUri(uri: String): TrackEntity?

    @Query("SELECT * FROM tracks")
    suspend fun getAll(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE document_uri IN (:uris)")
    suspend fun getByUris(uris: List<String>): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE status = :status")
    suspend fun getByStatus(status: String): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE status = 'inbox'")
    suspend fun getInbox(): List<TrackEntity>

    @Query("UPDATE tracks SET sort_score = :score WHERE document_uri = :uri")
    suspend fun updateScore(uri: String, score: Int)

    @Query("UPDATE tracks SET bpm_detected = :bpm WHERE document_uri = :uri")
    suspend fun updateBpmDetected(uri: String, bpm: Int)

    @Query("SELECT DISTINCT value FROM track_tags WHERE dimension = :dimension")
    suspend fun distinctTagValues(dimension: String): List<String>

    @Query("SELECT * FROM tracks WHERE status = 'library' AND (artist = :artist OR album = :album)")
    suspend fun matchingLibraryTracks(artist: String?, album: String?): List<TrackEntity>

    @Query("SELECT COUNT(*) FROM tracks WHERE status = 'inbox'")
    fun observeInboxCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tracks WHERE status = 'library'")
    fun observeLibraryCount(): Flow<Int>

    /** All `track_tags` rows — the Library tag projection consumed by the faceted queries (§6.2). */
    @Query("SELECT * FROM track_tags")
    fun observeAllTags(): Flow<List<TrackTagEntity>>

    @Query("SELECT * FROM track_tags WHERE document_uri = :uri")
    suspend fun getTagsFor(uri: String): List<TrackTagEntity>
}
