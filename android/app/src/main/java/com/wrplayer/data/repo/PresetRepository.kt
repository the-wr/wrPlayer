package com.wrplayer.data.repo

import com.wrplayer.data.db.PresetDao
import com.wrplayer.data.db.PresetEntity
import com.wrplayer.domain.model.FilterState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** A saved Queue Editor filter (PRD §6.2): a name plus the deserialized chip selection. */
data class SavedPreset(
    val name: String,
    val filter: FilterState,
)

/** Persists named Queue Editor filters (PRD §6.2 / §10.3), (de)serializing via [FilterStateJson]. */
interface PresetRepository {
    fun observeAll(): Flow<List<SavedPreset>>
    suspend fun save(name: String, filter: FilterState)
    suspend fun delete(name: String)
    suspend fun rename(oldName: String, newName: String)
}

class PresetRepositoryImpl @Inject constructor(
    private val presetDao: PresetDao,
) : PresetRepository {

    override fun observeAll(): Flow<List<SavedPreset>> =
        presetDao.observeAll().map { rows ->
            rows.map { SavedPreset(it.name, FilterStateJson.decode(it.filterState)) }
        }

    override suspend fun save(name: String, filter: FilterState) {
        presetDao.upsert(PresetEntity(name = name, filterState = FilterStateJson.encode(filter)))
    }

    override suspend fun delete(name: String) = presetDao.delete(name)

    override suspend fun rename(oldName: String, newName: String) {
        if (oldName == newName) return
        val existing = presetDao.getByName(oldName) ?: return
        presetDao.upsert(PresetEntity(name = newName, filterState = existing.filterState))
        presetDao.delete(oldName)
    }
}
