package com.wrplayer.domain.model

/**
 * The six tag dimensions exposed in the Queue Editor (PRD §6.2 / §10.3).
 *
 * [key] is the stable string stored in `track_tags.dimension` and used in serialized filter
 * state. [multiValue] marks dimensions whose track column holds a native ID3v2.4 null-separated
 * list (Genre/Mood/Labels); the rest are single-valued.
 */
enum class TagDimension(val key: String, val multiValue: Boolean) {
    GENRE("genre", true),
    MOOD("mood", true),
    PACE("pace", false),
    LABELS("labels", true),
    ARTIST("artist", false),
    ALBUM("album", false);

    companion object {
        fun fromKey(key: String): TagDimension? = entries.firstOrNull { it.key == key }
    }
}
