package com.example.voicejournal.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(entities = [VoiceJournal::class], version = 1, exportSchema = false)
abstract class VoiceJournalDatabase : RoomDatabase() {

    abstract fun voiceJournalDao(): VoiceJournalDao

    companion object {

        @Volatile
        private var instance: VoiceJournalDatabase? = null

        /**
         * Returns an instance of Room Database.
         *
         * @param context application context
         * @return The singleton LetterDatabase
         */
        fun getInstance(context: Context): VoiceJournalDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context, VoiceJournalDatabase::class.java, "voice.db")
                    .build()
            }
        }
    }
}