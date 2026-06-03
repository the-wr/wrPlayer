package com.wrplayer.data.scan

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

/**
 * Verifies the reconciliation orchestration (PRD §8.2) against real Room with fake SAF seams:
 * inserts, mtime-updates that preserve score/BPM, removals, classification by STATUS, and the
 * unmounted-folder guard.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LibraryReconcilerTest {

    private val tree = "content://auth/tree/T"
    private fun uri(name: String) = "$tree/document/T%2F$name"

    private lateinit var db: WrDatabase
    private lateinit var dao: TrackDao

    private val reachableTrees = mutableListOf(tree)
    private val reachable = mutableMapOf(tree to true)
    private val discovered = mutableMapOf<String, List<DiscoveredFile>>()
    private val tags = mutableMapOf<String, Mp3TagData>()

    private val treeSource = object : WatchedTreeSource {
        override suspend fun persistedTrees() = reachableTrees.toList()
        override suspend fun isReachable(treeUri: String) = reachable[treeUri] == true
    }
    private val enumerator = object : DocumentEnumerator {
        override suspend fun enumerate(treeUri: String) = discovered[treeUri].orEmpty()
    }
    private val tagSource = object : TrackTagSource {
        override suspend fun read(documentUri: String) = tags[documentUri] ?: Mp3TagData()
    }

    private lateinit var reconciler: LibraryReconciler

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WrDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.trackDao()
        reconciler = LibraryReconciler(treeSource, enumerator, tagSource, dao)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertsNewFiles_classifiedByStatusTag() = runTest {
        discovered[tree] = listOf(
            DiscoveredFile(uri("a.mp3"), "/m/a.mp3", 10),
            DiscoveredFile(uri("b.mp3"), "/m/b.mp3", 10),
        )
        tags[uri("a.mp3")] = Mp3TagData(title = "A", status = "library", genres = listOf("Rock"))
        tags[uri("b.mp3")] = Mp3TagData(title = "B") // no status → inbox

        reconciler.reconcile()

        assertThat(dao.getByUri(uri("a.mp3"))!!.status).isEqualTo("library")
        assertThat(dao.getByUri(uri("b.mp3"))!!.status).isEqualTo("inbox")
        // Library track is indexed; inbox track is not (§10.3).
        assertThat(dao.getTagsFor(uri("a.mp3"))).isNotEmpty()
        assertThat(dao.getTagsFor(uri("b.mp3"))).isEmpty()
    }

    @Test
    fun mtimeUpdate_preservesScoreAndDetectedBpm() = runTest {
        dao.upsertWithTags(
            TrackEntity(
                documentUri = uri("a.mp3"), filePath = "/m/a.mp3", status = "inbox",
                sortScore = 1, bpmDetected = 128, title = "Old", fileMtime = 10,
            ),
        )
        discovered[tree] = listOf(DiscoveredFile(uri("a.mp3"), "/m/a.mp3", 20)) // mtime changed
        tags[uri("a.mp3")] = Mp3TagData(title = "New From External Edit")

        reconciler.reconcile()

        val row = dao.getByUri(uri("a.mp3"))!!
        assertThat(row.title).isEqualTo("New From External Edit") // re-read
        assertThat(row.sortScore).isEqualTo(1)    // preserved
        assertThat(row.bpmDetected).isEqualTo(128) // preserved
        assertThat(row.fileMtime).isEqualTo(20)
    }

    @Test
    fun absentFile_isRemoved() = runTest {
        dao.upsertWithTags(TrackEntity(uri("gone.mp3"), "/m/gone.mp3", "inbox", fileMtime = 1))
        discovered[tree] = emptyList()

        reconciler.reconcile()

        assertThat(dao.getByUri(uri("gone.mp3"))).isNull()
    }

    @Test
    fun unreachableTree_removesNothing() = runTest {
        dao.upsertWithTags(TrackEntity(uri("a.mp3"), "/m/a.mp3", "inbox", sortScore = 1, fileMtime = 1))
        reachable[tree] = false // SD card unmounted

        reconciler.reconcile()

        // Row left untouched — must not be misread as a mass deletion (§8.2).
        assertThat(dao.getByUri(uri("a.mp3"))).isNotNull()
        assertThat(dao.getByUri(uri("a.mp3"))!!.sortScore).isEqualTo(1)
    }
}
