package com.wrplayer.domain

/**
 * Confirm-enable rule for the Tag Sheet (PRD §5.3): a track needs Title, Artist, and at least one
 * descriptive tag from Genre / Mood / Pace / Labels. A Pace auto-derived from a detected BPM counts
 * (it makes the track reachable via the Pace filter). BPM and Album are optional.
 */
object TagSheetValidation {
    fun canConfirm(
        title: String,
        artist: String,
        genres: Set<String>,
        moods: Set<String>,
        pace: String?,
        labels: Set<String>,
    ): Boolean {
        val hasNames = title.isNotBlank() && artist.isNotBlank()
        val hasDescriptiveTag =
            genres.isNotEmpty() || moods.isNotEmpty() || !pace.isNullOrBlank() || labels.isNotEmpty()
        return hasNames && hasDescriptiveTag
    }
}
