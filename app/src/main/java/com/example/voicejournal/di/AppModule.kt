package com.example.voicejournal.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.VoiceJournalDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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



}