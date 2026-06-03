package com.wrplayer.data.bpm

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.PaceDeriver
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Verifies the MP3 → PCM → TarsosDSP pipeline on-device (PRD §9). Ground-truth BPM is unknown for
 * the fixture, so the gate is: detection completes without crashing and yields a plausible tempo in
 * the 90–180 band. The detected value is logged for the go/no-go report.
 */
@RunWith(AndroidJUnit4::class)
class TarsosBpmDetectorTest {

    private lateinit var file: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        file = File(context.filesDir, "bpm-sample-${System.nanoTime()}.mp3")
        InstrumentationRegistry.getInstrumentation().context.assets
            .open("fixtures/sample.mp3").use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
    }

    @Test
    fun detectsPlausibleBpm() = runBlocking {
        val bpm = TarsosBpmDetector().detect(file)
        Log.i("wrPlayerBpm", "Detected BPM = $bpm, pace = ${PaceDeriver.fromBpm(bpm)}")
        assertThat(bpm).isNotNull()
        assertThat(bpm!!).isIn(90..179)
    }
}
