package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.main.AddVoiceNote.NoteContentTextFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.NoteFileNameFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.NoteTextFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.UriState
import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag
import com.example.voicejournal.ui.main.mainScreen.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VoiceJournalPreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val settingsRepository: SettingsRepository,
    private val context: Context,

    ) : ViewModel() {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    private val now = Date()
    private var recentlyDeletedJournal: VoiceJournal? = null
    // recentlyDeletedJournal to be implemented later TODO()

//Lists of Notes
    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> =_state

    private val _noteTitle = mutableStateOf(
        NoteTextFieldState(
            hint = "Enter title..."
        )
    )
    private val _noteFileName = mutableStateOf(
        NoteFileNameFieldState()
    )
    private val _tempImageUris = mutableStateOf(
        UriState()
    )
    private val _playNoteState = mutableStateOf(
        false
    )
    private val _doneButtonState = MutableStateFlow<Boolean>(false)

    private val _tags = mutableStateOf(emptyList<Tag>())


    private val _recordState = MutableStateFlow<Boolean>(false)
    private val _playingState = MutableStateFlow<Boolean>(false)

    // Timer for the Recorder Panel
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    //Timer for the Play panel
    private val _timer2 = MutableStateFlow(0L)
    val timer2 = _timer2.asStateFlow()

    private var timerJob2: Job? = null


    val noteTitle: State<NoteTextFieldState> = _noteTitle
    val noteFileName: State<NoteFileNameFieldState> = _noteFileName
    val tempImageUris: State<UriState> = _tempImageUris
    val playNoteState: State<Boolean> = _playNoteState
    val recordState: StateFlow<Boolean> = _recordState
    val playingState: StateFlow<Boolean> = _playingState
    val doneButtonState: StateFlow<Boolean> = _doneButtonState

    // A state variable to store the list of tags
    val tags: State<List<Tag>> = _tags

    private val _noteContent = mutableStateOf(
        NoteContentTextFieldState(
            hint = "Enter some content"
        )
    )
    val noteContent: State<NoteContentTextFieldState> = _noteContent

    private val _noteColor = mutableStateOf(VoiceJournal.noteColors.random().toArgb())
    private val _noteState = mutableStateOf(AddVoiceNoteViewModel.NoteState())
    val noteState: State<AddVoiceNoteViewModel.NoteState> = _noteState
    val noteColor: State<Int> = _noteColor


    private val _eventFlow = MutableSharedFlow<AddVoiceNoteViewModel.UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null
    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                _playNoteState.value = true
                viewModelScope.launch {

                    voiceJournalRepository.getNote(noteId).collect { note ->
                        _noteState.value = noteState.value.copy(voiceJournal = note)
                        if (note != null) {
                            currentNoteId = note.id
                        }
                        if (note != null) {
                            _noteTitle.value = noteTitle.value.copy(
                                text = note.title,
                                isHintVisible = false
                            )
                        }
                        if (note != null) {
                            _noteContent.value = _noteContent.value.copy(
                                text = note.content,
                                isHintVisible = false
                            )
                        }
                        if (note != null) {
                            _noteColor.value = note.color
                        }
                        if (note != null) {
                            _noteFileName.value = _noteFileName.value.copy(
                                text = note.fileName
                            )
                        }
                        if (note != null) {
                            _noteFileName.value = _noteFileName.value.copy(
                                imageFileUris = note.imageUris
                            )
                        }
                        if (note != null) {
                            if(note.tags!=null) {
                                _tags.value = note.tags!!
                            }
                        }

                        _playNoteState.value = noteFileName.value.text != ""

                    }

                }
            }
        }

        getNotes()

    }

    private fun getNotes(){
        viewModelScope.launch {
            voiceJournalRepository.getAllVoiceJournals()
                .collect{
                    _state.value= NotesState(
                        notes = it
                    )
                }
        }
    }

    fun getCurrentNoteIndex(): Int {
        return _state.value.notes.indexOfFirst { it.id == currentNoteId }
    }

    }
