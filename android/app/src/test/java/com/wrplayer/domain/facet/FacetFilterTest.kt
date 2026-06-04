package com.wrplayer.domain.facet

import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.model.ChipState
import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension
import org.junit.Test

/** Exhaustive truth tables for the faceted filter logic (PRD §6.2). */
class FacetFilterTest {

    private fun track(uri: String, vararg pairs: Pair<TagDimension, Set<String>>) =
        LibraryTrackTags(uri, pairs.toMap())

    // (Rock OR Jazz) AND Hype AND NOT Slow — the PRD's worked example.
    private val library = listOf(
        track("a", TagDimension.GENRE to setOf("Rock"), TagDimension.MOOD to setOf("Hype"), TagDimension.PACE to setOf("fast")),
        track("b", TagDimension.GENRE to setOf("Jazz"), TagDimension.MOOD to setOf("Hype"), TagDimension.PACE to setOf("slow")),
        track("c", TagDimension.GENRE to setOf("Rock"), TagDimension.MOOD to setOf("Chill"), TagDimension.PACE to setOf("fast")),
        track("d", TagDimension.GENRE to setOf("Electronic"), TagDimension.MOOD to setOf("Hype"), TagDimension.PACE to setOf("fast")),
        track("e", TagDimension.GENRE to setOf("Rock", "Jazz"), TagDimension.MOOD to setOf("Hype"), TagDimension.PACE to setOf("slow")),
    )

    private fun included(vararg pairs: Pair<TagDimension, Set<String>>) =
        FilterState(included = pairs.toMap())

    @Test
    fun noIncludedChips_matchesWholeLibrary() {
        assertThat(FacetFilter.matchCount(library, FilterState.EMPTY)).isEqualTo(library.size)
    }

    @Test
    fun orWithinDimension() {
        // Genre Rock OR Jazz → a, b, c, e
        val f = included(TagDimension.GENRE to setOf("Rock", "Jazz"))
        assertThat(FacetFilter.matchingUris(library, f)).containsExactly("a", "b", "c", "e")
    }

    @Test
    fun andAcrossDimensions() {
        // (Rock OR Jazz) AND Hype → a, b, e
        val f = included(
            TagDimension.GENRE to setOf("Rock", "Jazz"),
            TagDimension.MOOD to setOf("Hype"),
        )
        assertThat(FacetFilter.matchingUris(library, f)).containsExactly("a", "b", "e")
    }

    @Test
    fun excludedIsAndNot() {
        // (Rock OR Jazz) AND Hype AND NOT Slow → a (b and e are slow)
        val f = FilterState(
            included = mapOf(
                TagDimension.GENRE to setOf("Rock", "Jazz"),
                TagDimension.MOOD to setOf("Hype"),
            ),
            excluded = mapOf(TagDimension.PACE to setOf("slow")),
        )
        assertThat(FacetFilter.matchingUris(library, f)).containsExactly("a")
    }

    @Test
    fun excludedOnly_narrowsWholeLibrary() {
        // No includes, exclude Slow → everything except b and e
        val f = FilterState(excluded = mapOf(TagDimension.PACE to setOf("slow")))
        assertThat(FacetFilter.matchingUris(library, f)).containsExactly("a", "c", "d")
    }

    @Test
    fun crossDimensionValueCollision_doesNotMerge() {
        // A Genre "Jazz" and a Label "Jazz" are distinct facets (PRD §10.3).
        val tracks = listOf(
            track("x", TagDimension.GENRE to setOf("Jazz")),
            track("y", TagDimension.LABELS to setOf("Jazz")),
        )
        val byGenre = included(TagDimension.GENRE to setOf("Jazz"))
        val byLabel = included(TagDimension.LABELS to setOf("Jazz"))
        assertThat(FacetFilter.matchingUris(tracks, byGenre)).containsExactly("x")
        assertThat(FacetFilter.matchingUris(tracks, byLabel)).containsExactly("y")
    }

    @Test
    fun facets_prospectiveCount_reflectsAddingChip() {
        // Empty filter: Genre Rock prospective = tracks having Rock = a, c, e = 3
        val facets = FacetFilter.facets(library, FilterState.EMPTY)
        val genre = facets[TagDimension.GENRE].orEmpty().associate { it.value to it.count }
        assertThat(genre["Rock"]).isEqualTo(3)
        assertThat(genre["Jazz"]).isEqualTo(2)
        assertThat(genre["Electronic"]).isEqualTo(1)
    }

    @Test
    fun facets_prospectiveCount_givenExistingSelection() {
        // With Mood=Hype included, Genre Rock prospective = Hype AND Rock = a, e = 2
        val f = included(TagDimension.MOOD to setOf("Hype"))
        val facets = FacetFilter.facets(library, f)
        val genre = facets[TagDimension.GENRE].orEmpty().associate { it.value to it.count }
        assertThat(genre["Rock"]).isEqualTo(2)   // a, e
        assertThat(genre["Jazz"]).isEqualTo(2)   // b, e
        assertThat(genre["Electronic"]).isEqualTo(1) // d
    }

    @Test
    fun facets_hideZeroProspective_exceptExcluded() {
        // Exclude Genre "Rock"; it has a prospective included-count but is excluded → stays visible.
        val f = FilterState(excluded = mapOf(TagDimension.GENRE to setOf("Rock")))
        val genre = FacetFilter.facets(library, f)[TagDimension.GENRE].orEmpty()
        val rock = genre.firstOrNull { it.value == "Rock" }
        assertThat(rock).isNotNull()
        assertThat(rock!!.state).isEqualTo(ChipState.EXCLUDED)
    }

    @Test
    fun facets_excludedChip_showsProspectiveIncludedCount() {
        // Excluded Rock chip shows the included-count (3), not its exclusion effect.
        val f = FilterState(excluded = mapOf(TagDimension.GENRE to setOf("Rock")))
        val rock = FacetFilter.facets(library, f)[TagDimension.GENRE].orEmpty()
            .first { it.value == "Rock" }
        assertThat(rock.count).isEqualTo(3)
    }

    @Test
    fun facets_sortedByCountDescending() {
        val genre = FacetFilter.facets(library, FilterState.EMPTY)[TagDimension.GENRE].orEmpty()
        val counts = genre.map { it.count }
        assertThat(counts).isInOrder(Comparator.reverseOrder<Int>())
    }

    @Test
    fun matchCount_zero_forImpossibleSelection() {
        // No track is both Jazz and Electronic → CTAs disable (PRD §6.2).
        val f = included(TagDimension.GENRE to setOf("Jazz")).withIncluded(TagDimension.MOOD to "Nope")
        assertThat(FacetFilter.matchCount(library, f)).isEqualTo(0)
    }

    @Test
    fun staleSelections_flagsValuesThatNoLongerMatch() {
        // "gym" was saved in a preset but no library track carries it now → stale.
        val f = included(TagDimension.GENRE to setOf("Rock")).copy(
            included = mapOf(
                TagDimension.GENRE to setOf("Rock"),
                TagDimension.LABELS to setOf("gym"),
            ),
        )
        val stale = FacetFilter.staleSelections(library, f)
        assertThat(stale[TagDimension.LABELS]).containsExactly("gym")
        assertThat(stale).doesNotContainKey(TagDimension.GENRE)
    }

    @Test
    fun staleSelections_emptyWhenAllValuesPresent() {
        val f = included(TagDimension.GENRE to setOf("Rock"))
        assertThat(FacetFilter.staleSelections(library, f)).isEmpty()
    }

    private fun FilterState.withIncluded(pair: Pair<TagDimension, String>) =
        withIncluded(pair.first, pair.second)
}
