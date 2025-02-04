package com.example.voicejournal.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalDatabase
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.core.AudioPlayer
import com.example.voicejournal.core.AudioPlayerImpl
import com.example.voicejournal.core.AudioRecorder
import com.example.voicejournal.core.AudioRecorderImpl
import com.example.voicejournal.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application): VoiceJournalDatabase {
        return Room.databaseBuilder(app.applicationContext, VoiceJournalDatabase::class.java, "voice.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(db: VoiceJournalDatabase): VoiceJournalRepositoryImpl {
        return VoiceJournalRepositoryImpl(db.voiceJournalDao())
    }

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.dataStore
        }

        @Provides
        @Singleton
        fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository {
            return SettingsRepository(dataStore)
        }

    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder {
        return AudioRecorderImpl()
    }


    @Provides
    @Singleton
    fun provideAudioPlayer(): AudioPlayer {
        return AudioPlayerImpl()
    }



}