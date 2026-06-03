package com.wrplayer.data.id3

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Verifies the SAF copy-round-trip and that JAudioTagger behaves on Android ART, not just the JVM
 * (PRD §2.3 / §8.3). Uses a file:// document URI as a stand-in for a SAF tree document; the real
 * SAF tree picker is exercised in Phase 4.
 */
@RunWith(AndroidJUnit4::class)
class SafId3GatewayTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private lateinit var uri: Uri
    private lateinit var file: File
    private val gateway = SafId3Gateway(context, Id3Reader(), Id3Writer())

    @Before
    fun setUp() {
        file = File(context.filesDir, "sample-${System.nanoTime()}.mp3")
        // Fixtures are packaged in the *test* APK, so read them from the instrumentation context.
        val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
        testAssets.open("fixtures/sample.mp3").use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        uri = Uri.fromFile(file)
    }

    @Test
    fun writeThenRead_roundTripsThroughSaf() {
        gateway.write(
            uri,
            Mp3TagData(
                title = "Starfighter",
                artist = "Eric Speed",
                genres = listOf("Hip-Hop", "R&B"),
                pace = "fast",
                bpm = 128,
                labels = listOf("gym"),
                status = "library",
            ),
        )
        val data = gateway.read(uri)
        assertThat(data.title).isEqualTo("Starfighter")
        assertThat(data.genres).containsExactly("Hip-Hop", "R&B").inOrder()
        assertThat(data.status).isEqualTo("library")
        assertThat(data.bpm).isEqualTo(128)
        assertThat(data.labels).containsExactly("gym")
    }

    @Test
    fun writeLeavesFilePlayableOnDevice() {
        gateway.write(uri, Mp3TagData(title = "X", status = "library"))
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            assertThat(durationMs).isGreaterThan(0L)
        } finally {
            retriever.release()
        }
    }
}
