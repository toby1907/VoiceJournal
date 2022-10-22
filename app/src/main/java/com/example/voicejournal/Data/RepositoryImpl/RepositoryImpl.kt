package com.example.voicejournal.Data.RepositoryImpl

import com.example.voicejournal.Data.VoiceJournal
import kotlinx.coroutines.flow.Flow

interface VoiceJournalRepository {
    suspend fun getNote(id: Int): VoiceJournal?
 fun delete(voiceJournal: VoiceJournal)
  suspend  fun save(voiceJournal: VoiceJournal)
    fun getAllVoiceJournals() : Flow<List<VoiceJournal>>
}
