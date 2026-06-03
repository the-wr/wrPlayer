package com.wrplayer

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

/** Smoke test proving the instrumented pipeline + Hilt test application work. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {
    @Test
    fun appContext_hasCorrectPackage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // HiltTestApplication runs under the app's instrumentation target package.
        assertThat(context.packageName).isEqualTo("com.wrplayer")
    }
}
