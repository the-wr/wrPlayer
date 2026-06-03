package com.wrplayer.domain

/**
 * The selectable chips offered for Genre/Mood/Labels: the superset of the predefined list and
 * every value already present on a Library track in that dimension (PRD §5.3). Predefined values
 * keep their order; additional library-sourced values follow, sorted.
 */
object TagSuperset {
    fun forDimension(predefined: List<String>, libraryValues: Collection<String>): List<String> {
        val result = LinkedHashSet<String>(predefined)
        val extras = libraryValues.filter { it.isNotBlank() && it !in result }.sorted()
        result.addAll(extras)
        return result.toList()
    }
}
