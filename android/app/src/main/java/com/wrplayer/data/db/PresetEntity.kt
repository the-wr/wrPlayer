package com.wrplayer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

/**
 * The `presets` table (PRD §10.3): a named, serialized Queue Editor filter. Saving under an
 * existing name overwrites it (name is the primary key).
 */
@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "filter_state") val filterState: String,
)
