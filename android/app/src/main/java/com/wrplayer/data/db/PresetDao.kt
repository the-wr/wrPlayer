package com.wrplayer.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    /** Saving under an existing name overwrites that preset (PRD §10.3). */
    @Upsert
    suspend fun upsert(preset: PresetEntity)

    @Query("DELETE FROM presets WHERE name = :name")
    suspend fun delete(name: String)

    @Query("SELECT * FROM presets WHERE name = :name")
    suspend fun getByName(name: String): PresetEntity?

    @Query("SELECT * FROM presets ORDER BY name")
    fun observeAll(): Flow<List<PresetEntity>>
}
