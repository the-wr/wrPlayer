package com.wrplayer.ui.tagsheet

import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.db.splitMultiValue
import com.wrplayer.data.id3.Mp3TagData
import com.wrplayer.domain.FilenameParser
import com.wrplayer.domain.PaceDeriver
import com.wrplayer.domain.PredefinedTags
import com.wrplayer.domain.TagPrefill
import com.wrplayer.domain.TagSuperset
import com.wrplayer.domain.model.TagDimension
import javax.inject.Inject

/**
 * Builds a prefilled [TagSheetState] from a track and the Library (PRD §5.3): superset chip options,
 * filename-parsed Title/Artist fallback, and artist/album plurality pre-fill.
 */
class TagSheetLoader @Inject constructor(
    private val trackDao: TrackDao,
) {
    suspend fun load(track: TrackEntity, isEdit: Boolean): TagSheetState {
        val genreOptions = TagSuperset.forDimension(PredefinedTags.GENRES, trackDao.distinctTagValues(TagDimension.GENRE.key))
        val moodOptions = TagSuperset.forDimension(PredefinedTags.MOODS, trackDao.distinctTagValues(TagDimension.MOOD.key))
        val labelOptions = TagSuperset.forDimension(emptyList(), trackDao.distinctTagValues(TagDimension.LABELS.key))

        val parsed = if (track.title.isNullOrBlank() && track.artist.isNullOrBlank()) {
            FilenameParser.parse(track.filePath)
        } else null

        val matches = if (!track.artist.isNullOrBlank() || !track.album.isNullOrBlank()) {
            trackDao.matchingLibraryTracks(track.artist, track.album)
        } else emptyList()

        val genres = track.genre.splitMultiValue().toSet()
            .ifEmpty { TagPrefill.multiValue(matches.map { it.genre.splitMultiValue().toSet() }) }
        val moods = track.mood.splitMultiValue().toSet()
            .ifEmpty { TagPrefill.multiValue(matches.map { it.mood.splitMultiValue().toSet() }) }
        val labels = track.labels.splitMultiValue().toSet()
            .ifEmpty { TagPrefill.multiValue(matches.map { it.labels.splitMultiValue().toSet() }) }

        val bpm = track.bpm ?: track.bpmDetected
        val pace = track.pace
            ?: PaceDeriver.fromBpm(bpm)?.key
            ?: TagPrefill.singleValue(matches.mapNotNull { it.pace })

        return TagSheetState(
            title = track.title?.ifBlank { null } ?: parsed?.title ?: "",
            artist = track.artist?.ifBlank { null } ?: parsed?.artist ?: "",
            album = track.album?.ifBlank { null } ?: parsed?.album ?: "",
            genres = genres,
            moods = moods,
            pace = pace,
            bpm = bpm,
            labels = labels,
            hasArt = track.hasArt,
            genreOptions = genreOptions,
            moodOptions = moodOptions,
            labelOptions = labelOptions,
            isEdit = isEdit,
        )
    }
}

/** Convert the sheet's edited state into ID3 data to write on confirm (PRD §5.3). */
fun TagSheetState.toMp3TagData(status: String): Mp3TagData = Mp3TagData(
    title = title.ifBlank { null },
    artist = artist.ifBlank { null },
    album = album.ifBlank { null },
    genres = genres.toList(),
    moods = moods.toList(),
    pace = pace,
    bpm = bpm,
    labels = labels.toList(),
    status = status,
)
