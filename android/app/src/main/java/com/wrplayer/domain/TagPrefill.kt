package com.wrplayer.domain

/**
 * Pre-fills tags from existing Library tracks by the same artist or album (PRD §5.3). When tracks
 * disagree the most common value wins: for multi-value dimensions, each value held by a majority of
 * the matching tracks; for single-value Pace, the most common bucket.
 */
object TagPrefill {
    fun multiValue(perTrackValues: List<Set<String>>): Set<String> {
        if (perTrackValues.isEmpty()) return emptySet()
        val counts = HashMap<String, Int>()
        perTrackValues.forEach { values -> values.forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        val majority = perTrackValues.size / 2.0
        return counts.filterValues { it > majority }.keys
    }

    fun singleValue(values: List<String>): String? =
        values.filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .maxByOrNull { it.value }?.key
}
