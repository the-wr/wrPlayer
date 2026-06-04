package com.wrplayer.data.repo

import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Round-trip and resilience of preset filter (de)serialization (PRD §6.2). Uses org.json (Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FilterStateJsonTest {

    @Test
    fun roundTripsIncludedAndExcluded() {
        val filter = FilterState(
            included = mapOf(
                TagDimension.GENRE to setOf("Rock", "Jazz"),
                TagDimension.MOOD to setOf("Hype"),
            ),
            excluded = mapOf(TagDimension.PACE to setOf("slow")),
        )
        val decoded = FilterStateJson.decode(FilterStateJson.encode(filter))
        assertThat(decoded).isEqualTo(filter)
    }

    @Test
    fun emptyRoundTrips() {
        val decoded = FilterStateJson.decode(FilterStateJson.encode(FilterState.EMPTY))
        assertThat(decoded).isEqualTo(FilterState.EMPTY)
    }

    @Test
    fun preservesValuesWithSpecialCharacters() {
        val filter = FilterState(included = mapOf(TagDimension.LABELS to setOf("late-night", "deep \"work\"", "a,b")))
        val decoded = FilterStateJson.decode(FilterStateJson.encode(filter))
        assertThat(decoded.included[TagDimension.LABELS]).containsExactly("late-night", "deep \"work\"", "a,b")
    }

    @Test
    fun malformedJsonDecodesToEmpty() {
        assertThat(FilterStateJson.decode("not json")).isEqualTo(FilterState.EMPTY)
    }

    @Test
    fun unknownDimensionKeysAreDropped() {
        val decoded = FilterStateJson.decode("""{"included":{"bogus":["x"],"genre":["Rock"]}}""")
        assertThat(decoded.included.keys).containsExactly(TagDimension.GENRE)
        assertThat(decoded.included[TagDimension.GENRE]).containsExactly("Rock")
    }
}
