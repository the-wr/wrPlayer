package com.wrplayer.data.di

import android.content.Context
import androidx.room.Room
import com.wrplayer.data.db.PresetDao
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.db.WrDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WrDatabase =
        Room.databaseBuilder(context, WrDatabase::class.java, WrDatabase.NAME).build()

    @Provides
    fun provideTrackDao(db: WrDatabase): TrackDao = db.trackDao()

    @Provides
    fun providePresetDao(db: WrDatabase): PresetDao = db.presetDao()
}
