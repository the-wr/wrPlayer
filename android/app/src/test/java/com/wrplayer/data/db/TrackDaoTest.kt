package com.wrplayer.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TrackDaoTest {

    private lateinit var db: WrDatabase
    private lateinit var dao: TrackDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WrDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.trackDao()
    }

    @After
    fun tearDown() = db.close()

    private fun libraryTrack(uri: String) = TrackEntity(
        documentUri = uri,
        filePath = "/music/$uri.mp3",
        status = "library",
        title = "T",
        artist = "Artist A",
        album = "Album X",
        genre = listOf("Rock", "Jazz").joinMultiValue(),
        mood = listOf("Hype").joinMultiValue(),
        pace = "fast",
        labels = listOf("gym").joinMultiValue(),
    )

    @Test
    fun upsertLibraryTrack_explodesMultiValueTagRows() = runTest {
        dao.upsertWithTags(libraryTrack("a"))

        val rows = dao.getTagsFor("a").map { it.dimension to it.value }.toSet()
        assertThat(rows).containsExactly(
            "genre" to "Rock",
            "genre" to "Jazz",
            "mood" to "Hype",
            "pace" to "fast",
            "artist" to "Artist A",
            "album" to "Album X",
            "labels" to "gym",
        )
    }

    @Test
    fun inboxTrack_hasNoTagRows() = runTest {
        dao.upsertWithTags(libraryTrack("a").copy(status = "inbox"))
        assertThat(dao.getTagsFor("a")).isEmpty()
    }

    @Test
    fun reUpsert_rebuildsTagRows_noStaleEntries() = runTest {
        dao.upsertWithTags(libraryTrack("a"))
        // Re-tag: drop Jazz, change pace.
        dao.upsertWithTags(libraryTrack("a").copy(genre = listOf("Rock").joinMultiValue(), pace = "slow"))

        val rows = dao.getTagsFor("a").map { it.dimension to it.value }.toSet()
        assertThat(rows).contains("genre" to "Rock")
        assertThat(rows).doesNotContain("genre" to "Jazz")
        assertThat(rows).contains("pace" to "slow")
        assertThat(rows).doesNotContain("pace" to "fast")
    }

    @Test
    fun deleteTrack_cascadesTagRows() = runTest {
        dao.upsertWithTags(libraryTrack("a"))
        dao.deleteTrack("a")
        assertThat(dao.getTagsFor("a")).isEmpty()
        assertThat(dao.getByUri("a")).isNull()
    }

    @Test
    fun multiValueRoundTrip_throughNullSeparatedColumn() = runTest {
        dao.upsertWithTags(libraryTrack("a").copy(genre = listOf("Hip-Hop", "R&B").joinMultiValue()))
        val stored = dao.getByUri("a")!!
        assertThat(stored.genre.splitMultiValue()).containsExactly("Hip-Hop", "R&B")
    }
}
