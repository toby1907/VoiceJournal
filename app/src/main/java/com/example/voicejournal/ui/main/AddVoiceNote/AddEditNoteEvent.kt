package com.example.voicejournal.ui.main.AddVoiceNote

import androidx.compose.ui.focus.FocusState

sealed class AddEditNoteEvent {
    data class EnteredTitle(val value: String): AddEditNoteEvent()
    data class ChangeTitleFocus(val focusState: FocusState): AddEditNoteEvent()
    data class EnteredContent(val value: String): AddEditNoteEvent()
    data class ChangeContentFocus(val focusState: FocusState): AddEditNoteEvent()
    data class Play(val filename:String): AddEditNoteEvent()
    data class Recording(val filename: String):AddEditNoteEvent()
    data class ChangeColor(val color: Int) : AddEditNoteEvent()

    //   data class ChangeColor(val color: Int): AddEditNoteEvent()
    object SaveNote: AddEditNoteEvent()
    object StopPlay: AddEditNoteEvent()
    object StopRecording: AddEditNoteEvent()
}
