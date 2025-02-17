package com.example.voicejournal.ui.main.mainScreen

import com.example.voicejournal.Data.model.VoiceJournal

data class NotesState(
    val notes: List<VoiceJournal> = emptyList(),
    val noteIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)