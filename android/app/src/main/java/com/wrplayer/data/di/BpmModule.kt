package com.wrplayer.data.di

import com.wrplayer.data.bpm.BpmDetector
import com.wrplayer.data.bpm.TarsosBpmDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the production [BpmDetector]. Swap to `StubBpmDetector` here if real detection is disabled
 * (PRD §9 — a null BPM is an acceptable MVP outcome).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BpmModule {
    @Binds
    abstract fun bindBpmDetector(impl: TarsosBpmDetector): BpmDetector
}
