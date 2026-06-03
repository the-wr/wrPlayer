package com.wrplayer.data.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.TrackEntity
import com.wrplayer.data.db.WrDatabase
import com.wrplayer.data.id3.Mp3TagData
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Verifies the repository write paths against real Room with a fake file store (PRD §5.3/§7/§10.2). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TrackRepositoryImplTest {

    private lateinit var db: WrDatabase
    private lateinit var dao: TrackDao
    private lateinit var repo: TrackRepositoryImpl

    private val fakeStore = object : TrackFileStore {
        var movedTo: String = ""
        val written = mutableListOf<Pair<String, Mp3TagData>>()
        val deleted = mutableListOf<String>()
        override suspend fun writeTags(documentUri: String, data: Mp3TagData) {
            written += documentUri to data
        }
        override suspend fun promoteFile(documentUri: String): String = movedTo.ifEmpty { documentUri }
        override suspend fun deleteFile(documentUri: String) { deleted += documentUri }
    }

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WrDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.trackDao()
        repo = TrackRepositoryImpl(fakeStore, dao)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun promote_writesFileFirst_reKeysRow_rebuildsTags() = runTest {
        val oldUri = "content://t/document/inbox.mp3"
        val newUri = "content://t/document/Library%2Finbox.mp3"
        fakeStore.movedTo = newUri
        dao.upsertWithTags(
            TrackEntity(oldUri, "/m/inbox.mp3", "inbox", sortScore = 1, bpmDetected = 128, title = "Old"),
        )

        repo.promote(oldUri, Mp3TagData(title = "Song", artist = "Artist", genres = listOf("Rock")))

        // File written before DB; library status set.
        assertThat(fakeStore.written.first().first).isEqualTo(oldUri)
        assertThat(fakeStore.written.first().second.status).isEqualTo("library")
        // Row re-keyed.
        assertThat(dao.getByUri(oldUri)).isNull()
        val row = dao.getByUri(newUri)!!
        assertThat(row.status).isEqualTo("library")
        assertThat(row.title).isEqualTo("Song")
        assertThat(row.bpmDetected).isEqualTo(128) // app-only column preserved
        // track_tags rebuilt under the new key.
        assertThat(dao.getTagsFor(newUri).map { it.dimension to it.value }).contains("genre" to "Rock")
        assertThat(dao.getTagsFor(oldUri)).isEmpty()
    }

    @Test
    fun editTags_updatesInPlace_noReKey() = runTest {
        val uri = "content://t/document/Library%2Fsong.mp3"
        dao.upsertWithTags(TrackEntity(uri, "/m/song.mp3", "library", title = "Old", genre = "Rock"))

        repo.editTags(uri, Mp3TagData(title = "New Title", artist = "A", genres = listOf("Jazz")))

        val row = dao.getByUri(uri)!!
        assertThat(row.title).isEqualTo("New Title")
        assertThat(row.status).isEqualTo("library") // unchanged
        assertThat(dao.getTagsFor(uri).map { it.value }).contains("Jazz")
        assertThat(dao.getTagsFor(uri).map { it.value }).doesNotContain("Rock")
    }

    @Test
    fun delete_removesFileThenRow() = runTest {
        val uri = "content://t/document/inbox.mp3"
        dao.upsertWithTags(TrackEntity(uri, "/m/inbox.mp3", "inbox"))

        repo.delete(uri)

        assertThat(fakeStore.deleted).containsExactly(uri)
        assertThat(dao.getByUri(uri)).isNull()
    }
}
