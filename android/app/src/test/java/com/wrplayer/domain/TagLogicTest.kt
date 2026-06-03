package com.wrplayer.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FilenameParserTest {
    @Test fun artistDashTitle() {
        val r = FilenameParser.parse("Eric Speed - Starfighter.mp3")
        assertThat(r.artist).isEqualTo("Eric Speed")
        assertThat(r.title).isEqualTo("Starfighter")
    }

    @Test fun numberDashTitle_noFolders() {
        val r = FilenameParser.parse("01 - Song.mp3")
        assertThat(r.title).isEqualTo("Song")
        assertThat(r.artist).isNull()
    }

    @Test fun numberWithArtistAlbumFolders() {
        val r = FilenameParser.parse("Music/Daft Punk/Discovery/03 - Aerodynamic.mp3")
        assertThat(r.title).isEqualTo("Aerodynamic")
        assertThat(r.album).isEqualTo("Discovery")
        assertThat(r.artist).isEqualTo("Daft Punk")
    }

    @Test fun noPattern_wholeNameIsTitle() {
        val r = FilenameParser.parse("randomtrack.mp3")
        assertThat(r.title).isEqualTo("randomtrack")
        assertThat(r.artist).isNull()
    }
}

class TagSupersetTest {
    @Test fun mergesPredefinedAndLibraryValues() {
        val result = TagSuperset.forDimension(
            predefined = listOf("Rock", "Jazz"),
            libraryValues = listOf("Jazz", "Vaporwave", "Phonk"),
        )
        assertThat(result).containsExactly("Rock", "Jazz", "Phonk", "Vaporwave").inOrder()
    }
}

class TagPrefillTest {
    @Test fun multiValue_picksMajorityValues() {
        // Rock in 2/3, Jazz in 1/3 → only Rock.
        val result = TagPrefill.multiValue(listOf(setOf("Rock"), setOf("Rock", "Jazz"), setOf("Pop")))
        assertThat(result).containsExactly("Rock")
    }

    @Test fun singleValue_picksMode() {
        assertThat(TagPrefill.singleValue(listOf("fast", "fast", "slow"))).isEqualTo("fast")
    }
}

class TagSheetValidationTest {
    private fun confirm(
        title: String = "T", artist: String = "A",
        genres: Set<String> = emptySet(), moods: Set<String> = emptySet(),
        pace: String? = null, labels: Set<String> = emptySet(),
    ) = TagSheetValidation.canConfirm(title, artist, genres, moods, pace, labels)

    @Test fun requiresTitleArtistAndOneDescriptiveTag() {
        assertThat(confirm(genres = setOf("Rock"))).isTrue()
        assertThat(confirm()).isFalse() // no descriptive tag
        assertThat(confirm(title = "", genres = setOf("Rock"))).isFalse()
        assertThat(confirm(artist = "", genres = setOf("Rock"))).isFalse()
    }

    @Test fun autoPaceFromBpmCounts() {
        assertThat(confirm(pace = "fast")).isTrue()
    }
}
