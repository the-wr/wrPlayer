package com.wrplayer.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wrplayer.domain.model.TagDimension
import com.wrplayer.domain.model.TrackStatus

/**
 * The `tracks` table (PRD §10.3): a queryable cache of each MP3's ID3 metadata plus app-only state
 * (`sort_score`, `bpm_detected`). Keyed by the persisted SAF document URI. The file is the source
 * of truth; this row follows it (§10.1/§10.2).
 *
 * Multi-value columns (genre/mood/labels) hold a null-separated list; see [TagFormat].
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey @ColumnInfo(name = "document_uri") val documentUri: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    val status: String,
    @ColumnInfo(name = "sort_score") val sortScore: Int = 0,
    @ColumnInfo(name = "bpm_detected") val bpmDetected: Int? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val genre: String? = null,
    val mood: String? = null,
    val pace: String? = null,
    val bpm: Int? = null,
    val labels: String? = null,
    @ColumnInfo(name = "date_added") val dateAdded: Long = 0L,
    @ColumnInfo(name = "file_mtime") val fileMtime: Long = 0L,
    @ColumnInfo(name = "has_art") val hasArt: Boolean = false,
)

/**
 * Explode a track row into its normalized `track_tags` rows (PRD §10.3). Only Library tracks are
 * indexed — inbox tracks are never queried by tag — so callers must gate on status before insert.
 */
fun TrackEntity.toTagRows(): List<TrackTagEntity> {
    val rows = mutableListOf<TrackTagEntity>()

    fun addMulti(dimension: TagDimension, raw: String?) {
        raw.splitMultiValue().forEach { value ->
            rows += TrackTagEntity(documentUri, dimension.key, value)
        }
    }

    fun addSingle(dimension: TagDimension, value: String?) {
        if (!value.isNullOrBlank()) rows += TrackTagEntity(documentUri, dimension.key, value)
    }

    addMulti(TagDimension.GENRE, genre)
    addMulti(TagDimension.MOOD, mood)
    addMulti(TagDimension.LABELS, labels)
    addSingle(TagDimension.PACE, pace)
    addSingle(TagDimension.ARTIST, artist)
    addSingle(TagDimension.ALBUM, album)
    return rows
}

val TrackEntity.isLibrary: Boolean
    get() = TrackStatus.fromKey(status) == TrackStatus.LIBRARY
