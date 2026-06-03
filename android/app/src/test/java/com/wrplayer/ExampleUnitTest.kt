package com.wrplayer

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Smoke test proving the JVM unit-test pipeline works. */
class ExampleUnitTest {
    @Test
    fun arithmetic_isCorrect() {
        assertThat(2 + 2).isEqualTo(4)
    }
}
