package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.ui.main.AddVoiceNote.AddEditNoteEvent
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.main.AddVoiceNote.FavouriteState
import com.example.voicejournal.ui.main.AddVoiceNote.NoteContentTextFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.NoteFileNameFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.NoteTextFieldState
import com.example.voicejournal.ui.main.AddVoiceNote.UriState
import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag
import com.example.voicejournal.ui.main.mainScreen.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val LOG_TAG = "PreviewvmTest"
@HiltViewModel
class VoiceJournalPreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val settingsRepository: SettingsRepository,
    private val context: Context,

    ) : ViewModel() {



    private var player: MediaPlayer? = null
    private val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    private val now = Date()
    private var recentlyDeletedJournal: VoiceJournal? = null
    // recentlyDeletedJournal to be implemented later TODO()

    //Lists of Notes
    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    private val _favouriteScreenEventFlow = MutableSharedFlow<FavouriteScreenEvent>()
    val favoriteScreenEvent = _favouriteScreenEventFlow.asSharedFlow()

    private val _noteTitle = mutableStateOf(
        NoteTextFieldState(
            hint = "Enter title..."
        )
    )
    private val _noteFileName = mutableStateOf(
        NoteFileNameFieldState()
    )
    private var notes: List<VoiceJournal> = emptyList()
    private val _tempImageUris = mutableStateOf(
        UriState()
    )
    private val _playNoteState = mutableStateOf(
        false
    )
    private val _doneButtonState = MutableStateFlow<Boolean>(false)

    private val _tags = mutableStateOf(emptyList<Tag>())
    private val _favourite = mutableStateOf(
        FavouriteState()
    )


    private val _favouriteButtonState = MutableStateFlow<Boolean>(false)
    private val _playingState = MutableStateFlow<Boolean>(false)

    // Timer for the Recorder Panel
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    //Timer for the Play panel
    private val _timer2 = MutableStateFlow(0L)
    val timer2 = _timer2.asStateFlow()
    private val _currentNoteUri = MutableStateFlow<Uri?>(null)
    val currentNoteUri: StateFlow<Uri?> = _currentNoteUri.asStateFlow()

    private var timerJob2: Job? = null


    val noteTitle: State<NoteTextFieldState> = _noteTitle
    val noteFileName: State<NoteFileNameFieldState> = _noteFileName
    val tempImageUris: State<UriState> = _tempImageUris
    val playNoteState: State<Boolean> = _playNoteState
    val favouriteButtonState: StateFlow<Boolean> = _favouriteButtonState
    val playingState: StateFlow<Boolean> = _playingState
    val doneButtonState: StateFlow<Boolean> = _doneButtonState
    val favourite: State<FavouriteState> = _favourite

    // A state variable to store the list of tags
    val tags: State<List<Tag>> = _tags
    private val _noteColor = mutableStateOf(VoiceJournal.noteColors.random().toArgb())
    private val _noteState = mutableStateOf(AddVoiceNoteViewModel.NoteState())
    private val _noteContent = mutableStateOf(
        NoteContentTextFieldState(
            hint = "Enter some content"
        )
    )
    val noteContent: State<NoteContentTextFieldState> = _noteContent
    val noteState: State<AddVoiceNoteViewModel.NoteState> = _noteState


    val noteColor: State<Int> = _noteColor


    private val _eventFlow = MutableSharedFlow<PreviewUiEvent>()
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
                            if (note.tags != null) {
                                _tags.value = note.tags!!
                            }
                        }
                        if (note != null) {
                            if(note.favourite!=null) {
                                _favourite.value = _favourite.value.copy(
                                    favourite = note.favourite!!
                                )
                            }

                        }

                        _playNoteState.value = noteFileName.value.text != ""


                    }


                }

            }
            //getCurrentNoteIndex()
        }

getNotes()

    }

     fun getNotes():Int {
        viewModelScope.launch {
            voiceJournalRepository.getAllVoiceJournals()
                .collect {
                    notes = it
                    _state.value = NotesState(
                        notes = it,
                        noteIndex = notes.indexOf(noteState.value.voiceJournal)
                        )

                    Log.d("index in getNotes","${_state.value.noteIndex}")
                }

        }
        Log.d("NotesState from functio","${_state.value.noteIndex}")
        Log.d("NotesState from functio","$currentNoteId")
 return _state.value.noteIndex
    }

    fun getCurrentNoteIndex(): Int {
        Log.d("NotesState from functio","${state.value.noteIndex }")
        Log.d("NotesState from functio","${state.value.noteIndex }")
        return _state.value.notes.indexOfFirst { it.id == state.value.noteIndex }
    }

    fun onChangeFavourite(change:Boolean,currentNote:VoiceJournal){

        viewModelScope.launch {
            voiceJournalRepository.save(
                currentNote.copy(
                    favourite =change
                )
            )

        }


    }
    fun onEvent(event: FavouriteScreenEvent) {
        when (event) {
            is FavouriteScreenEvent.Favourite -> {


            }
            is FavouriteScreenEvent.Play -> {
                viewModelScope.launch {
                    startPlaying(event.filename)
                    _eventFlow.emit(PreviewUiEvent.PlayNote)
                }
            }

            is FavouriteScreenEvent.StopPlay ->{

                viewModelScope.launch {
                    stopPlaying()
                    _eventFlow.emit(PreviewUiEvent.StopPlay)
                }
            }
            is FavouriteScreenEvent.DeleteJournal -> {
                viewModelScope.launch {
                    event.voiceJournal?.let { voiceJournalRepository.delete(it) }
                    recentlyDeletedJournal = event.voiceJournal
                }
            }

            is FavouriteScreenEvent.RestoreJournal -> {
                viewModelScope.launch {
                    voiceJournalRepository.save(recentlyDeletedJournal ?: return@launch)
                    recentlyDeletedJournal = null
                }
            }


        }


        }

    sealed class FavouriteScreenEvent {
   //     data class ShowSnackbar(val message: String) : UiEvent()
        object StopPlay : FavouriteScreenEvent()
        data class Play(val filename:String): FavouriteScreenEvent()
        data class Favourite(val value: Boolean):FavouriteScreenEvent()
        data class DeleteJournal(val voiceJournal: VoiceJournal?): FavouriteScreenEvent()
        object RestoreJournal: FavouriteScreenEvent()


    }
    sealed class PreviewUiEvent {
        data class ShowSnackbar(val message: String) : PreviewUiEvent()
        object
        SaveNote : PreviewUiEvent()

        object PlayNote : PreviewUiEvent()
        object StopPlay : PreviewUiEvent()
        object Recording : PreviewUiEvent()
        object StopRecord : PreviewUiEvent()
    }


    private suspend fun startPlaying(filename: String) {

        player = MediaPlayer().apply {
            setOnCompletionListener {
                viewModelScope.launch {_eventFlow.emit(PreviewUiEvent.StopPlay) }
            }

            try {
                setDataSource(filename)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }

    }
    fun getDuration(fileName: String): Int {
        // use runBlocking to wait for the result
        return runBlocking {
            // use async to get a Deferred object
            val deferred = async {
                // use withContext to switch to IO dispatcher
                withContext(Dispatchers.IO) {
                    // call your audioDuration function
                    audioDuration(fileName)
                }
            }
            // use await to get the result from the Deferred object
            deferred.await()
        }
    }
    private suspend fun audioDuration(fileName: String): Int {
        return withContext(Dispatchers.IO) {
            var player: MediaPlayer? = null
            try {
                player = MediaPlayer()
                player.setDataSource(fileName)
                player.prepare()
                return@withContext player.duration ?: 0 // return 0 if duration is null
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed", e)
                return@withContext 0 // return 0 if an error occurs
            } finally {
                player?.release() // close the player in the finally block
            }
        }
    }


    private fun stopPlaying() {
        try {

            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    it.release()
                }
                player = null
            }
        } catch (e: IllegalStateException) {
            // Handle the exception (e.g., log it or show an error message)
        }
    }
    // Play panel Timer
    fun startTimer2(fileName: String) {
        // get the duration value from the audio file
        val duration = getDuration(fileName)
        // convert the duration value from milliseconds to seconds
        val durationInSeconds = duration / 1000
        timerJob2?.cancel()
        timerJob2 = viewModelScope.launch {
            while (true) {
                _playingState.value = true
                delay(1000)
                _timer2.value++
                // check if the timer value is equal to or greater than the duration value
                if (_timer2.value >= durationInSeconds) {
                    // stop the timer
                    _timer2.value = 0
                    timerJob2?.cancel()
                    _playingState.value = false
                    // break the loop
                    break
                }
            }
        }
    }

    fun pauseTimer2() {
        _playingState.value = false
        timerJob2?.cancel()
    }

    fun stopTimer2() {
        _timer2.value = 0
        _playingState.value = false
        timerJob2?.cancel()
    }

    fun updateCurrentNoteUri(uri: Uri?) {
        _currentNoteUri.value = uri
    }

}
