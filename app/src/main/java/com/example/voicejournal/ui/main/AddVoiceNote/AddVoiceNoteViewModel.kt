package com.example.voicejournal.ui.main.AddVoiceNote

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.*
import com.example.voicejournal.Data.InvalidNoteException
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
private const val LOG_TAG = "AudioRecordTest"
@HiltViewModel
class AddVoiceNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val context: Context
    ) : ViewModel() {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    private val now = Date()


    private val _noteTitle = mutableStateOf(NoteTextFieldState(
        hint = "Enter title..."
    ))
    private val _noteFileName = mutableStateOf(
      NoteFileNameFieldState()
    )
    private val _playNoteState = mutableStateOf(false
    )

    val noteTitle: State<NoteTextFieldState> = _noteTitle
    val noteFileName: State<NoteFileNameFieldState> = _noteFileName
    val playNoteState: State<Boolean> =_playNoteState

    private val _noteContent = mutableStateOf(NoteContentTextFieldState(
        hint = "Enter some content"
    ))
    val noteContent: State<NoteContentTextFieldState> = _noteContent

    private val _noteColor = mutableStateOf(VoiceJournal.noteColors.random().toArgb())
    val noteColor: State<Int> = _noteColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null


    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if(noteId != -1) {
               _playNoteState.value= true
                viewModelScope.launch {

                    voiceJournalRepository.getNote(noteId)?.also { note ->
                        currentNoteId = note.id
                        _noteTitle.value = noteTitle.value.copy(
                            text = note.title,
                            isHintVisible = false
                        )
                        _noteContent.value = _noteContent.value.copy(
                            text = note.content,
                            isHintVisible = false
                        )
                        _noteColor.value = note.color
                        _noteFileName.value = _noteFileName.value.copy(
                            text = note.fileName
                        )
                        _playNoteState.value =noteFileName.value.text!=""

                    }

                }
            }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        when(event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = noteTitle.value.copy(
                    text = event.value
                )
            }
            is AddEditNoteEvent.ChangeTitleFocus -> {
                _noteTitle.value = noteTitle.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            noteTitle.value.text.isBlank()
                )
            }
            is AddEditNoteEvent.EnteredContent -> {
                _noteContent.value = _noteContent.value.copy(
                    text = event.value
                )
            }
            is AddEditNoteEvent.ChangeContentFocus -> {
                _noteContent.value = _noteContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            _noteContent.value.text!!.isBlank()
                )
            }
            is AddEditNoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        voiceJournalRepository.save(
                            VoiceJournal(
                            title = noteTitle.value.text,
                            content = noteContent.value.text,
                            created = System.currentTimeMillis(),
                                fileName =noteFileName.value.text,
                                id = currentNoteId,
                                color = noteColor.value
                        ) )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch(e: InvalidNoteException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
            }
            is AddEditNoteEvent.Play -> {
             viewModelScope.launch {
                 startPlaying()
                 _eventFlow.emit(UiEvent.PlayNote)
             }
            }
            is AddEditNoteEvent.Recording ->{
               viewModelScope.launch {
                   startRecording()
                   _eventFlow.emit(UiEvent.Recording)
               }
            }
            is AddEditNoteEvent.StopRecording ->{
              viewModelScope.launch  {
                  stopRecording()
                  _eventFlow.emit(UiEvent.StopRecord)
              }
            }
            is AddEditNoteEvent.StopPlay ->{
                viewModelScope.launch {
                    stopPlaying()
                    _eventFlow.emit(UiEvent.StopPlay)
                }
            }
            is AddEditNoteEvent.ChangeColor -> {
                _noteColor.value = event.color
            }
        }
    }

    val created = System.currentTimeMillis()
    private val _saved = MutableLiveData<Boolean>()
    val saved: LiveData<Boolean>
        get() = _saved


    sealed class UiEvent {
        data class ShowSnackbar(val message: String): UiEvent()
        object SaveNote: UiEvent()
        object PlayNote: UiEvent()
        object StopPlay: UiEvent()
        object Recording: UiEvent()
        object StopRecord: UiEvent()
    }

    private suspend fun  startPlaying() {

        player = MediaPlayer().apply {
            setOnCompletionListener {
               viewModelScope.launch { _eventFlow.emit(UiEvent.StopPlay) }
            }

            try {
                setDataSource(noteFileName.value.text)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }
  suspend  fun stopPlaying() {

        player?.release()
        player = null
    }
   suspend fun startRecording() {
        recorder = MediaRecorder().apply {
            _noteFileName.value =_noteFileName.value.copy(
                text = "${context.getDir("AudioJournal",0)?.absolutePath}/Recording+ ${formatter.format(now)}+.3gp"
            )
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(noteFileName.value.text)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()

            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }
    suspend fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

}