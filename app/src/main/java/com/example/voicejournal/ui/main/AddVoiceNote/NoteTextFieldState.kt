package com.example.voicejournal.ui.main.AddVoiceNote

data class NoteContentTextFieldState(
    val text: String? = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
data class NoteTextFieldState(
    var text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
