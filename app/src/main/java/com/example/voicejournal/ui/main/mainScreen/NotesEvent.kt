package com.example.voicejournal.ui.main.mainScreen

import com.example.voicejournal.Data.VoiceJournal

sealed class NotesEvent {
data class DeleteNote(val voiceJournal: VoiceJournal): NotesEvent()
}