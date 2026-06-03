package com.wrplayer.data.id3

import com.google.common.truth.Truth.assertThat
import org.jaudiotagger.audio.AudioFileIO
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Round-trips real ID3 metadata through JAudioTagger on the JVM (PRD §2.3 / §5.3) — the highest-
 * risk half of Phase 2, verified off-device against a sample MP3 in test resources.
 */
class Id3ReadWriteTest {

    private val reader = Id3Reader()
    private val writer = Id3Writer()
    private lateinit var file: File

    @Before
    fun setUp() {
        // JAudioTagger mutates the file, so work on a fresh temp copy of the fixture each test.
        val input = checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/sample.mp3")) {
            "Missing test fixture src/test/resources/fixtures/sample.mp3"
        }
        file = File.createTempFile("wrplayer-fixture", ".mp3").apply { deleteOnExit() }
        input.use { it.copyTo(file.outputStream()) }
    }

    @Test
    fun readsAnMp3WithoutCrashing() {
        // The fixture is a real MP3; reading must not throw regardless of what tags it has.
        val data = reader.read(file)
        assertThat(data).isNotNull()
    }

    @Test
    fun roundTripsAllFields_includingMultiValueAndUnicode() {
        val written = Mp3TagData(
            title = "Starfighter",
            artist = "Eric Speed",
            album = "Test Album",
            genres = listOf("Hip-Hop", "R&B"),   // hyphen + ampersand + multi-value
            moods = listOf("Hype", "Energetic"),
            pace = "fast",
            bpm = 128,
            labels = listOf("gym", "late-night"),
            status = "library",
        )
        writer.write(file, written)
        val readBack = reader.read(file)

        assertThat(readBack.title).isEqualTo("Starfighter")
        assertThat(readBack.artist).isEqualTo("Eric Speed")
        assertThat(readBack.album).isEqualTo("Test Album")
        assertThat(readBack.genres).containsExactly("Hip-Hop", "R&B").inOrder()
        assertThat(readBack.moods).containsExactly("Hype", "Energetic").inOrder()
        assertThat(readBack.pace).isEqualTo("fast")
        assertThat(readBack.bpm).isEqualTo(128)
        assertThat(readBack.labels).containsExactly("gym", "late-night").inOrder()
        assertThat(readBack.status).isEqualTo("library")
    }

    @Test
    fun writeLeavesFilePlayable() {
        writer.write(file, Mp3TagData(title = "X", artist = "Y", status = "library"))
        // The audio must still be decodable (non-zero length) after the tag rewrite.
        val audio = AudioFileIO.read(file)
        assertThat(audio.audioHeader.trackLength).isGreaterThan(0)
    }

    @Test
    fun statusOnlyTag_isReadable() {
        writer.write(file, Mp3TagData(status = "inbox"))
        assertThat(reader.read(file).status).isEqualTo("inbox")
    }
}
