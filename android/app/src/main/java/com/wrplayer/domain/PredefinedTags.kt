package com.wrplayer.domain

/** Predefined Genre/Mood lists for the MVP (PRD §11). Users may add freeform values on top. */
object PredefinedTags {
    val GENRES = listOf(
        "Electronic", "Rock", "Hip-Hop", "Jazz", "Classical", "Ambient", "Folk",
        "Metal", "R&B", "Pop", "Funk", "Soul", "Reggae", "Latin", "World",
    )

    val MOODS = listOf(
        "Hype", "Energetic", "Happy", "Melancholy", "Chill",
        "Focus", "Dark", "Romantic", "Nostalgic", "Aggressive",
    )
}
