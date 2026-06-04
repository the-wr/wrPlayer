package com.wrplayer.ui.tagsheet

import com.wrplayer.domain.TagSheetValidation

/**
 * Editable state of the Tag Sheet (PRD §5.3). [genreOptions]/[moodOptions]/[labelOptions] are the
 * superset chips offered (predefined ∪ existing library values). [canConfirm] gates the Confirm CTA.
 */
data class TagSheetState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val genres: Set<String> = emptySet(),
    val moods: Set<String> = emptySet(),
    val pace: String? = null,
    val bpm: Int? = null,
    val labels: Set<String> = emptySet(),
    val hasArt: Boolean = false,
    val genreOptions: List<String> = emptyList(),
    val moodOptions: List<String> = emptyList(),
    val labelOptions: List<String> = emptyList(),
    val isEdit: Boolean = false,
) {
    val canConfirm: Boolean
        get() = TagSheetValidation.canConfirm(title, artist, genres, moods, pace, labels)
}

/** Callbacks the Tag Sheet raises; kept as one object to avoid a long parameter list. */
data class TagSheetCallbacks(
    val onTitle: (String) -> Unit = {},
    val onArtist: (String) -> Unit = {},
    val onAlbum: (String) -> Unit = {},
    val onToggleGenre: (String) -> Unit = {},
    val onAddGenre: (String) -> Unit = {},
    val onToggleMood: (String) -> Unit = {},
    val onAddMood: (String) -> Unit = {},
    val onSetPace: (String?) -> Unit = {},
    val onSetBpm: (Int?) -> Unit = {},
    val onToggleLabel: (String) -> Unit = {},
    val onAddLabel: (String) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
