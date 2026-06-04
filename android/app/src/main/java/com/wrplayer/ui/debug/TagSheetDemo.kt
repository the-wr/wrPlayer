package com.wrplayer.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wrplayer.domain.PredefinedTags
import com.wrplayer.ui.tagsheet.TagSheet
import com.wrplayer.ui.tagsheet.TagSheetCallbacks
import com.wrplayer.ui.tagsheet.TagSheetState

/** TEMPORARY Phase 7 host to view the Tag Sheet. Replaced by the Sort/Play wiring in Phase 8. */
@Composable
fun TagSheetDemo(modifier: Modifier = Modifier) {
    var state by remember {
        mutableStateOf(
            TagSheetState(
                title = "Starfighter",
                artist = "Eric Speed",
                bpm = 136,
                pace = "fast",
                genres = setOf("Electronic"),
                moods = setOf("Hype"),
                genreOptions = PredefinedTags.GENRES,
                moodOptions = PredefinedTags.MOODS,
                labelOptions = listOf("gym", "commute", "late-night"),
            ),
        )
    }
    fun Set<String>.toggle(v: String) = if (v in this) this - v else this + v

    val cb = TagSheetCallbacks(
        onTitle = { state = state.copy(title = it) },
        onArtist = { state = state.copy(artist = it) },
        onAlbum = { state = state.copy(album = it) },
        onToggleGenre = { state = state.copy(genres = state.genres.toggle(it)) },
        onAddGenre = { state = state.copy(genres = state.genres + it, genreOptions = state.genreOptions + it) },
        onToggleMood = { state = state.copy(moods = state.moods.toggle(it)) },
        onAddMood = { state = state.copy(moods = state.moods + it, moodOptions = state.moodOptions + it) },
        onSetPace = { state = state.copy(pace = it) },
        onSetBpm = { state = state.copy(bpm = it) },
        onToggleLabel = { state = state.copy(labels = state.labels.toggle(it)) },
        onAddLabel = { state = state.copy(labels = state.labels + it, labelOptions = state.labelOptions + it) },
    )

    Box(modifier.fillMaxSize().background(Color(0x6B000000)), contentAlignment = Alignment.BottomCenter) {
        TagSheet(state, cb)
    }
}
