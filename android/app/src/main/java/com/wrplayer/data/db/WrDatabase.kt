package com.wrplayer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class, PresetEntity::class, TrackTagEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class WrDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun presetDao(): PresetDao

    companion object {
        const val NAME = "wrplayer.db"
    }
}
