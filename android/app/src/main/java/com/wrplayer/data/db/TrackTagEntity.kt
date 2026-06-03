package com.wrplayer.data.db

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Normalized faceted-query index (PRD §10.3): one row per (track, dimension, value). Derived from
 * the owning `tracks` row and rebuilt in the same transaction whenever that row's tags change
 * (§10.2) — never a second source of truth. Only Library tracks have rows here.
 */
@Entity(
    tableName = "track_tags",
    primaryKeys = ["document_uri", "dimension", "value"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["document_uri"],
            childColumns = ["document_uri"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["dimension", "value"]),
        Index(value = ["document_uri"]),
    ],
)
data class TrackTagEntity(
    @ColumnInfo(name = "document_uri") val documentUri: String,
    val dimension: String,
    val value: String,
)
