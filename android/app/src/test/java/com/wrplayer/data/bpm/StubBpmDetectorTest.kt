package com.wrplayer.data.bpm

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class StubBpmDetectorTest {
    @Test
    fun alwaysReturnsNull() = runTest {
        assertThat(StubBpmDetector().detect(File("nonexistent.mp3"))).isNull()
    }
}
