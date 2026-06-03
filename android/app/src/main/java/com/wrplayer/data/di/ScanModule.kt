package com.wrplayer.data.di

import com.wrplayer.data.saf.SafDocumentEnumerator
import com.wrplayer.data.saf.SafTrackTagSource
import com.wrplayer.data.saf.SafTreeManager
import com.wrplayer.data.scan.DocumentEnumerator
import com.wrplayer.data.scan.TrackTagSource
import com.wrplayer.data.scan.WatchedTreeSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the SAF-backed reconciliation seams (PRD §8.2). */
@Module
@InstallIn(SingletonComponent::class)
abstract class ScanModule {
    @Binds
    abstract fun bindWatchedTreeSource(impl: SafTreeManager): WatchedTreeSource

    @Binds
    abstract fun bindDocumentEnumerator(impl: SafDocumentEnumerator): DocumentEnumerator

    @Binds
    abstract fun bindTrackTagSource(impl: SafTrackTagSource): TrackTagSource
}
