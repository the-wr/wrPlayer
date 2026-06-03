package com.wrplayer.data.scan

import com.google.common.truth.Truth.assertThat
import com.wrplayer.data.saf.FileNaming
import com.wrplayer.data.saf.SafMembership
import org.junit.Test

class ReconcilePlannerTest {

    private fun file(uri: String, mtime: Long) = DiscoveredFile(uri, "/p/$uri", mtime)

    @Test
    fun insertsNewFiles() {
        val plan = ReconcilePlanner.plan(emptyMap(), listOf(file("a", 1), file("b", 1)))
        assertThat(plan.toInsert.map { it.documentUri }).containsExactly("a", "b")
        assertThat(plan.toUpdate).isEmpty()
        assertThat(plan.toRemove).isEmpty()
    }

    @Test
    fun updatesWhenMtimeChanged_onlyWhenDifferent() {
        val existing = mapOf("a" to 100L, "b" to 200L)
        val plan = ReconcilePlanner.plan(existing, listOf(file("a", 100), file("b", 999)))
        assertThat(plan.toUpdate.map { it.documentUri }).containsExactly("b")
        assertThat(plan.toInsert).isEmpty()
        assertThat(plan.toRemove).isEmpty()
    }

    @Test
    fun removesAbsentFiles() {
        val existing = mapOf("a" to 1L, "gone" to 1L)
        val plan = ReconcilePlanner.plan(existing, listOf(file("a", 1)))
        assertThat(plan.toRemove).containsExactly("gone")
    }
}

class SafMembershipTest {
    private val tree = "content://auth/tree/AAAA"

    @Test fun childIsUnderTree() {
        assertThat(SafMembership.isUnderTree("$tree/document/AAAA%2Fsong.mp3", tree)).isTrue()
    }

    @Test fun unrelatedUriIsNotUnderTree() {
        assertThat(SafMembership.isUnderTree("content://auth/tree/BBBB/document/x", tree)).isFalse()
    }
}

class FileNamingTest {
    @Test fun returnsNameWhenFree() {
        assertThat(FileNaming.uniqueName("song.mp3", emptySet())).isEqualTo("song.mp3")
    }

    @Test fun appendsSuffixOnCollision() {
        assertThat(FileNaming.uniqueName("song.mp3", setOf("song.mp3"))).isEqualTo("song_2.mp3")
    }

    @Test fun incrementsUntilUnique() {
        assertThat(FileNaming.uniqueName("song.mp3", setOf("song.mp3", "song_2.mp3")))
            .isEqualTo("song_3.mp3")
    }
}
