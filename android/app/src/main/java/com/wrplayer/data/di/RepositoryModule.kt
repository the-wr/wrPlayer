package com.wrplayer.data.di

import com.wrplayer.data.repo.PresetRepository
import com.wrplayer.data.repo.PresetRepositoryImpl
import com.wrplayer.data.repo.TrackFileStore
import com.wrplayer.data.repo.TrackRepository
import com.wrplayer.data.repo.TrackRepositoryImpl
import com.wrplayer.data.saf.SafTrackFileStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindTrackRepository(impl: TrackRepositoryImpl): TrackRepository

    @Binds
    abstract fun bindTrackFileStore(impl: SafTrackFileStore): TrackFileStore

    @Binds
    abstract fun bindPresetRepository(impl: PresetRepositoryImpl): PresetRepository
}
