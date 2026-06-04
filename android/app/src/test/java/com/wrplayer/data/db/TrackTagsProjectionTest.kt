package com.wrplayer.data.db

import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.model.TagDimension
import org.junit.Test

/** Grouping of flat `track_tags` rows into the per-track facet projection (PRD §6.2). */
class TrackTagsProjectionTest {

    private fun row(uri: String, dim: TagDimension, value: String) = TrackTagEntity(uri, dim.key, value)

    @Test
    fun groupsRowsByUriAndDimension() {
        val rows = listOf(
            row("a", TagDimension.GENRE, "Rock"),
            row("a", TagDimension.GENRE, "Jazz"),
            row("a", TagDimension.MOOD, "Hype"),
            row("b", TagDimension.GENRE, "Ambient"),
        )
        val projected = rows.toLibraryTrackTags()

        assertThat(projected.map { it.documentUri }).containsExactly("a", "b")
        val a = projected.first { it.documentUri == "a" }
        assertThat(a.tags[TagDimension.GENRE]).containsExactly("Rock", "Jazz")
        assertThat(a.tags[TagDimension.MOOD]).containsExactly("Hype")
        val b = projected.first { it.documentUri == "b" }
        assertThat(b.tags[TagDimension.GENRE]).containsExactly("Ambient")
    }

    @Test
    fun ignoresUnknownDimensionKeys() {
        val rows = listOf(
            TrackTagEntity("a", "bogus", "x"),
            row("a", TagDimension.PACE, "fast"),
        )
        val a = rows.toLibraryTrackTags().single()
        assertThat(a.tags.keys).containsExactly(TagDimension.PACE)
    }

    @Test
    fun emptyInputYieldsEmpty() {
        assertThat(emptyList<TrackTagEntity>().toLibraryTrackTags()).isEmpty()
    }
}
