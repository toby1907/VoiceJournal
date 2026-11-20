package com.example.voicejournal.ui.main.AddVoiceNote

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.model.InvalidNoteException
import com.example.voicejournal.Data.SettingsRepository
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockFactory
import com.example.voicejournal.Data.model.NoteBlockOps
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.hashNotePin
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.encodeDrawingPath
import com.example.voicejournal.Data.model.extractPlainTextFromHtml
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.Data.model.primaryBodyPreviewText
import com.example.voicejournal.Data.model.primaryRichTitleHtml
import com.example.voicejournal.Data.model.resolvedNoteBlocks
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.core.AudioPlayer
import com.example.voicejournal.core.AudioRecorder

import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag
import com.example.voicejournal.ui.theme.Variables
import com.example.voicejournal.ui.common.richeditor.RichTextState
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

private const val LOG_TAG = "AudioRecordTest"

@HiltViewModel
class AddVoiceNoteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    /* private var recorder: MediaRecorder? = null
     private var player: MediaPlayer? = null*/
    private val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
    private val now = Date()
    private var recentlyDeletedJournal: VoiceJournal? = null
    // recentlyDeletedJournal to be implemented later TODO()


    private val _voiceJournalId = mutableStateOf(-1)
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
    private val _favourite = mutableStateOf(
        FavouriteState()
    )
    private val _folderName = mutableStateOf("")
    private val _noteLocked = mutableStateOf(false)
    private val _noteBlocks = mutableStateListOf<NoteBlock>()
    private val _currentBlockPreviewAudioUri = mutableStateOf<String?>(null)
    private var notePinHash: String? = null
    private var tempUris: List<String> = listOf()
    private var tempFileName: String = ""
    private val _imageUris = mutableStateOf(
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

    val voiceJournalId: State<Int> = _voiceJournalId
    val noteTitle: State<NoteTextFieldState> = _noteTitle
    val noteFileName: State<NoteFileNameFieldState> = _noteFileName
    val tempImageUris: State<UriState> = _tempImageUris
    val favourite: State<FavouriteState> = _favourite
    val folderName: State<String> = _folderName
    val noteLocked: State<Boolean> = _noteLocked
    val noteBlocks: List<NoteBlock> get() = _noteBlocks
    val currentBlockPreviewAudioUri: State<String?> = _currentBlockPreviewAudioUri

    // val imageUris: State<UriState> =  _imageUris
    val playNoteState: State<Boolean> = _playNoteState
    val recordState: StateFlow<Boolean> = _recordState
    val playingState: StateFlow<Boolean> = _playingState
    val doneButtonState: StateFlow<Boolean> = _doneButtonState

    // A state variable to store the list of tags
    val tags: State<List<Tag>> = _tags

    //The content is used in place of the Title and the Title in place of the content
    private val _noteContent = mutableStateOf(
        NoteContentTextFieldState(
            hint = " "
        )
    )
    val noteContent: State<NoteContentTextFieldState> = _noteContent

    private val _created = mutableStateOf(
        NoteContentTextFieldState(
            created = System.currentTimeMillis()
        )
    )

    val created: State<NoteContentTextFieldState> = _created


    private val _noteColor = mutableStateOf(
        Variables.SchemesSurface.toArgb()
        /*VoiceJournal.noteColors.random().toArgb()*/

    )
    private val _noteState = mutableStateOf(NoteState())
    val noteState: State<NoteState> = _noteState
    val noteColor: State<Int> = _noteColor


    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null


    init {
        savedStateHandle.get<Int>("noteId")?.let { noteId ->
            if (noteId != -1) {
                //_playNoteState.value = true
                viewModelScope.launch {
                    Log.d("NoteId", noteId.toString())
                    voiceJournalRepository.getNote(noteId).collect { note ->
                        _noteState.value = noteState.value.copy(voiceJournal = note)
                        if (note != null) {
                            migrateLegacyNoteBlocksIfNeeded(note)
                        }
                        if (note != null) {
                            currentNoteId = note.id
                            val hydratedTitle = note.primaryRichTitleHtml()
                            val hydratedContent = note.primaryBodyPreviewText()
                            val hydratedAudioPath = note.primaryAudioPath()
                            val hydratedImageUris = note.imageUrisFromBlocks()
                            setNoteBlocks(note.resolvedNoteBlocks())

                            _noteTitle.value =
                                _noteTitle.value.copy(text = hydratedTitle, isHintVisible = false)
                            Log.d("NoteTitleNew", hydratedTitle)

                            _noteContent.value = _noteContent.value.copy(
                                text = hydratedContent,
                                isHintVisible = false
                            )

                            _created.value = _created.value.copy(
                                created = note.created,
                                isHintVisible = false
                            )

                            _noteColor.value = note.color

                            tempFileName = hydratedAudioPath
                            _noteFileName.value = _noteFileName.value.copy(
                                text = tempFileName
                            )

                            if (hydratedImageUris.isNotEmpty()) {
                                tempUris = hydratedImageUris
                                _tempImageUris.value = _tempImageUris.value.copy(
                                    imageFileUris = hydratedImageUris
                                )
                                Log.d("Image from file", "$hydratedImageUris")
                            }

                            if (note.tags != null) {
                                _tags.value = note.tags!!
                            }

                            if (note.favourite != null) {
                                _favourite.value = _favourite.value.copy(
                                    favourite = note.favourite!!
                                )
                            }
                            _folderName.value = note.folderName.orEmpty()
                            _noteLocked.value = note.isNoteLocked()
                            notePinHash = note.lockPinHash

                        }

                    }

                }
            }
        }
        Log.d("_NoteTitle", _noteTitle.value.text)
        getSelectedImageUris()

    }


    // A function to update the checked state of a tag
    fun onTagChecked(tagName: String, isChecked: Boolean) {
        // Find the index of the tag with the given name
        val index = _tags.value.indexOfFirst { it.name == tagName }
        // If the tag is found, update its checked state
        if (index != -1) {
            _tags.value[index].isChecked = isChecked
        } else {
            // If the tag is not found, add it to the list and check it
            _tags.value = _tags.value + Tag(tagName, isChecked)
            // Sort the list by isChecked in descending order only when a new tag is added
            _tags.value = _tags.value.sortedByDescending { it.isChecked }
        }
    }

    fun onEvent(event: AddEditNoteEvent) {
        when (event) {
            is AddEditNoteEvent.EnteredTitle -> {
                _noteTitle.value = _noteTitle.value.copy(
                    text = event.value
                )

            }

            is AddEditNoteEvent.EnteredDate -> {
                _created.value = created.value.copy(
                    created = event.value
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
                    if (event.note.trim().isNotEmpty() ) {
                        // Set title to "Untitled" if it's also empty or blank
                        if (_noteContent.value.text?.isBlank() == true) {
                            _noteContent.value = _noteContent.value.copy(text = "Untitled")
                        }

                        // Show snackbar
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = "Content cannot be empty"
                            )
                        )
                        return@launch // Don't save the note
                    }
                    try {
                        voiceJournalRepository.save(
                            VoiceJournal(
                                title = noteTitle.value.text,
                                content = noteContent.value.text,
                                created = created.value.created,
                                fileName = noteFileName.value.text,
                                id = currentNoteId,
                                color = noteColor.value,
                                imageUris = tempImageUris.value.imageFileUris,
                                tags = tags.value,
                                favourite = favourite.value.favourite,
                                noteBlocks = buildPersistableNoteBlocks(noteTitle.value.text),
                                folderName = _folderName.value.trim().ifBlank { null },
                                isLocked = _noteLocked.value,
                                lockPinHash = if (_noteLocked.value) notePinHash else null
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveNote)
                    } catch (e: InvalidNoteException) {
                        Log.d("Note", "could not save")
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
                removeSelectedImageUris()
            }

            is AddEditNoteEvent.Play -> {
                viewModelScope.launch {
                    startPlaying()
                    _eventFlow.emit(UiEvent.PlayNote)
                }
            }

            is AddEditNoteEvent.Recording -> {
                viewModelScope.launch {
                    startRecording()
                    changeRecordState(true)
                    _eventFlow.emit(UiEvent.Recording)
                }
            }

            is AddEditNoteEvent.StopRecording -> {
                viewModelScope.launch {
                    stopRecording()
                    changeRecordState(false)
                    _eventFlow.emit(UiEvent.StopRecord)

                }
            }

            is AddEditNoteEvent.StopPlay -> {
                viewModelScope.launch {
                    stopPlaying()
                    _eventFlow.emit(UiEvent.StopPlay)
                }
            }

            is AddEditNoteEvent.ChangeColor -> {
                _noteColor.value = event.color
            }

            is AddEditNoteEvent.DeleteJournal -> {
                viewModelScope.launch {
                    event.voiceJournal?.let { voiceJournalRepository.delete(it) }
                    recentlyDeletedJournal = event.voiceJournal

                }
            }

            is AddEditNoteEvent.RestoreJournal -> {
                viewModelScope.launch {
                    voiceJournalRepository.save(recentlyDeletedJournal ?: return@launch)
                    recentlyDeletedJournal = null
                }
            }

            is AddEditNoteEvent.ChangeStyle -> {


            }

            is AddEditNoteEvent.Error -> {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowSnackbar(event.message))
                }
            }

            is AddEditNoteEvent.SaveNoteBeforeNav -> {
                viewModelScope.launch {
                    if (event.note.trim().isEmpty()  ) {
                        // Set title to "Untitled" if it's also empty or blank
                        if (_noteContent.value.text?.isBlank() == true) {
                            _noteContent.value = _noteContent.value.copy(text = "Untitled")
                        }

                        // Show snackbar
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = "Content cannot be empty"
                            )
                        )
                        return@launch // Don't save the note
                    }
                    try {
                        val voiceJournal = VoiceJournal(
                            title = event.note,
                            content = noteContent.value.text ?:"Untitled",
                            created = created.value.created,
                            fileName = noteFileName.value.text,
                            id = currentNoteId,
                            color = noteColor.value,
                            imageUris = tempImageUris.value.imageFileUris,
                            tags = tags.value,
                            favourite = favourite.value.favourite,
                            noteBlocks = buildPersistableNoteBlocks(event.note),
                            folderName = _folderName.value.trim().ifBlank { null },
                            isLocked = _noteLocked.value,
                            lockPinHash = if (_noteLocked.value) notePinHash else null
                        )

                        val id = voiceJournalRepository.saveAndId(voiceJournal)
                        val voiceJournalId = voiceJournal.copy(id = id.toInt())
                        event.value.invoke(voiceJournalId)

                    } catch (e: InvalidNoteException) {
                        Log.d("Note", "could not save")
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save note"
                            )
                        )
                    }
                }
                removeSelectedImageUris()



            }

            is AddEditNoteEvent.SaveNoteOnly -> {

                saveNote(false,event.note)
            }
        }
    }


    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object
        SaveNote : UiEvent()

        object PlayNote : UiEvent()
        object StopPlay : UiEvent()
        object Recording : UiEvent()
        object StopRecord : UiEvent()
    }

    data class NoteState(
        val voiceJournal: VoiceJournal? = null,
       val state: RichTextState =  RichTextState()
    )

    private suspend fun startPlaying() {
        try {
            audioPlayer.setOnCompletionListener {
                viewModelScope.launch { _eventFlow.emit(UiEvent.StopPlay) }
            }
            audioPlayer.start(noteFileName.value.text)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private suspend fun audioDuration(): Int {
        return withContext(Dispatchers.IO) {
            var player: MediaPlayer? = null
            try {
                player = MediaPlayer()
                player.setDataSource(noteFileName.value.text)
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
            audioPlayer.stop()
        } catch (e: IllegalStateException) {
            // Handle the exception (e.g., log it or show an error message)
        }
        _currentBlockPreviewAudioUri.value = null
    }

    fun toggleAudioBlockPlayback(audioUri: String?) {
        val uri = audioUri?.trim().orEmpty()
        if (uri.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("Audio block has no playable file"))
            }
            return
        }
        if (_currentBlockPreviewAudioUri.value == uri) {
            stopAudioBlockPlayback()
            return
        }
        viewModelScope.launch {
            try {
                stopPlaying()
                audioPlayer.setOnCompletionListener {
                    _currentBlockPreviewAudioUri.value = null
                }
                audioPlayer.start(uri)
                _currentBlockPreviewAudioUri.value = uri
            } catch (e: IOException) {
                _currentBlockPreviewAudioUri.value = null
                _eventFlow.emit(UiEvent.ShowSnackbar("Unable to play selected audio block"))
            } catch (e: IllegalStateException) {
                _currentBlockPreviewAudioUri.value = null
                _eventFlow.emit(UiEvent.ShowSnackbar("Audio player is not ready"))
            }
        }
    }

    fun stopAudioBlockPlayback() {
        try {
            audioPlayer.stop()
        } catch (e: IllegalStateException) {
            // Ignore invalid state while stopping preview.
        }
        _currentBlockPreviewAudioUri.value = null
    }

    private fun startRecording() {
        try {
            val outputFile = "${
                context.getDir(
                    "AudioJournal",
                    0
                )?.absolutePath
            }/Recording+${formatter.format(now)}+.3gp"
            _noteFileName.value = _noteFileName.value.copy(text = outputFile)
            audioRecorder.start(outputFile)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
    }

    private fun stopRecording() {
        audioRecorder.stop()
    }

    fun onCancelRecord() {
        _noteFileName.value = _noteFileName.value.copy(text = "")
        _playNoteState.value = false
    }

    private fun pauseRecording() {
        // Check the device's version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Pause the recording
            audioRecorder.pause()
        } else {
            // Pause is not supported on lower versions
            Toast.makeText(context, "Pause is not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun resumeRecording() {
        // Check the device's version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Resume the recording
            audioRecorder.resume()
        } else {
            // Resume is not supported on lower versions
            Toast.makeText(context, "Resume is not supported on this device", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun changeRecordState(newState: Boolean) {
        //is to switch the recorder panel
        _recordState.value = newState
    }

    fun doneButtonState(newState: Boolean) {
        _doneButtonState.value = newState

    }

    fun noteFileName(imageFileUris: List<String>?) {
        _noteFileName.value = _noteFileName.value.copy(
            imageFileUris = imageFileUris
        )

    }

    fun getDuration(): Int {
        // use runBlocking to wait for the result
        return runBlocking {
            // use async to get a Deferred object
            val deferred = async {
                // use withContext to switch to IO dispatcher
                withContext(Dispatchers.IO) {
                    // call your audioDuration function
                    audioDuration()
                }
            }
            // use await to get the result from the Deferred object
            deferred.await()
        }
    }

    // Timer for Record panel
    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun stopTimer() {
        _timer.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        timerJob2?.cancel()
        stopAudioBlockPlayback()
    }

    // Play panel Timer
    fun startTimer2() {
        // get the duration value from the audio file
        val duration = getDuration()
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

    fun getSelectedImageUris() {
        viewModelScope.launch {
            settingsRepository.getSelectedUris()
                .collect {
                    if (it.isNotEmpty()) {
                        // Add the new URIs to the existing list

                        _tempImageUris.value = _tempImageUris.value.copy(
                            imageFileUris = _tempImageUris.value.imageFileUris?.plus(it.toList())
                        )
                        Log.d("Temp fr get", "$tempUris")
                    }
                }
        }


    }

    fun removeSelectedImageUris() {
        viewModelScope.launch {
            settingsRepository.removeSelectedUris()
            _tempImageUris.value = _tempImageUris.value.copy(
                imageFileUris = tempUris
            )
            Log.d("Temp fr remove", "$tempUris")
        }

    }

    fun saveSelectedUris(selectedUris: List<String>) {
        viewModelScope.launch {
            settingsRepository.saveSelectedUris(selectedUris)
        }
    }

    private fun migrateLegacyNoteBlocksIfNeeded(note: VoiceJournal) {
        if (!note.noteBlocks.isNullOrEmpty()) return
        val migratedBlocks = NoteBlockFactory.createForNote(
            titleHtml = note.title,
            content = note.content,
            audioPath = note.fileName,
            imageUris = note.imageUris
        )
        viewModelScope.launch {
            voiceJournalRepository.update(note.copy(noteBlocks = migratedBlocks))
        }
        setNoteBlocks(migratedBlocks)
    }

    fun refreshBlocksFromCurrentNote(titleHtml: String) {
        setNoteBlocks(buildPersistableNoteBlocks(titleHtml))
    }

    fun addTextBlockFromEditor(titleHtml: String) {
        val plainText = extractPlainTextFromHtml(titleHtml).ifBlank { null }
        val block = NoteBlock(
            id = java.util.UUID.randomUUID().toString(),
            order = _noteBlocks.size,
            type = NoteBlockType.TEXT.name,
            text = plainText,
            richTextHtml = titleHtml.takeIf { it.isNotBlank() }
        )
        setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + block))
    }

    fun addFloatingTextBox(pageIndex: Int = 0, initialText: String = "Text") {
        val block = NoteBlock(
            id = java.util.UUID.randomUUID().toString(),
            order = _noteBlocks.size,
            type = NoteBlockType.TEXT.name,
            text = initialText,
            richTextHtml = "<p>$initialText</p>",
            canvasPageIndex = pageIndex,
            canvasX = 40f,
            canvasY = 80f,
            canvasWidth = 200f,
            canvasHeight = 90f
        )
        setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + block))
    }

    fun updateBlockText(blockId: String, newText: String) {
        val updated = _noteBlocks.map {
            if (it.id == blockId) {
                it.copy(text = newText, richTextHtml = "<p>$newText</p>")
            } else it
        }
        setNoteBlocks(updated)
    }

    fun addAudioBlockFromCurrentFile() {
        val audioPath = noteFileName.value.text.trim()
        if (audioPath.isBlank()) return
        val block = NoteBlock(
            id = java.util.UUID.randomUUID().toString(),
            order = _noteBlocks.size,
            type = NoteBlockType.AUDIO.name,
            mediaUri = audioPath
        )
        setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + block))
    }

    fun addChecklistBlock() {
        val block = NoteBlock(
            id = java.util.UUID.randomUUID().toString(),
            order = _noteBlocks.size,
            type = NoteBlockType.CHECKLIST.name,
            text = "Checklist item",
            isChecked = false
        )
        setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + block))
    }

    fun toggleChecklistBlockChecked(blockId: String) {
        setNoteBlocks(NoteBlockOps.toggleChecklistChecked(_noteBlocks, blockId))
    }

    fun applyTranscriptToCurrentAudio(rawTranscript: String) {
        val transcript = rawTranscript.trim()
        if (transcript.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("No speech captured to transcribe"))
            }
            return
        }
        val currentAudioPath = noteFileName.value.text.trim()
        val updated = NoteBlockOps.upsertAudioTranscript(_noteBlocks, currentAudioPath, transcript)
        val hadAudioBlock = _noteBlocks.any { it.type == NoteBlockType.AUDIO.name }
        if (!hadAudioBlock && currentAudioPath.isNotBlank()) {
            val block = NoteBlock(
                id = java.util.UUID.randomUUID().toString(),
                order = _noteBlocks.size,
                type = NoteBlockType.AUDIO.name,
                mediaUri = currentAudioPath,
                transcript = transcript
            )
            setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + block))
            return
        }
        if (!hadAudioBlock && currentAudioPath.isBlank()) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("Record audio before adding a transcript"))
            }
            return
        }
        setNoteBlocks(updated)
    }

    fun addImageBlocksFromCurrentUris() {
        val uris = tempImageUris.value.imageFileUris.orEmpty().filter { it.isNotBlank() }
        if (uris.isEmpty()) return
        val blocksToAdd = uris.mapIndexed { index, uri ->
            NoteBlock(
                id = java.util.UUID.randomUUID().toString(),
                order = _noteBlocks.size + index,
                type = NoteBlockType.IMAGE.name,
                mediaUri = uri
            )
        }
        setNoteBlocks(NoteBlockOps.normalized(_noteBlocks + blocksToAdd))
    }

    fun moveBlockUp(blockId: String) {
        setNoteBlocks(NoteBlockOps.moveUp(_noteBlocks, blockId))
    }

    fun moveBlockDown(blockId: String) {
        setNoteBlocks(NoteBlockOps.moveDown(_noteBlocks, blockId))
    }

    fun removeBlock(blockId: String) {
        setNoteBlocks(NoteBlockOps.remove(_noteBlocks, blockId))
    }

    fun updateBlockCanvasLayout(
        blockId: String,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float? = null,
        height: Float? = null
    ) {
        setNoteBlocks(
            NoteBlockOps.updateCanvasLayout(
                blocks = _noteBlocks,
                blockId = blockId,
                pageIndex = pageIndex,
                x = x,
                y = y,
                width = width,
                height = height
            )
        )
    }

    fun resizeBlockCanvas(blockId: String, width: Float?, height: Float?) {
        setNoteBlocks(
            NoteBlockOps.resizeCanvasBlock(
                blocks = _noteBlocks,
                blockId = blockId,
                width = width,
                height = height
            )
        )
    }

    fun bringBlockToFront(blockId: String) {
        setNoteBlocks(NoteBlockOps.moveToFrontOnPage(_noteBlocks, blockId))
    }

    fun sendBlockToBack(blockId: String) {
        setNoteBlocks(NoteBlockOps.moveToBackOnPage(_noteBlocks, blockId))
    }

    fun addDrawingStroke(
        pageIndex: Int,
        normalizedPoints: List<Pair<Float, Float>>,
        colorArgb: Long,
        strokeWidthPx: Float,
        alpha: Float,
        tool: String
    ) {
        if (normalizedPoints.size < 2) return
        val encoded = encodeDrawingPath(normalizedPoints)
        setNoteBlocks(
            NoteBlockOps.addDrawingStroke(
                blocks = _noteBlocks,
                pageIndex = pageIndex,
                normalizedPath = encoded,
                color = colorArgb,
                strokeWidth = strokeWidthPx,
                alpha = alpha,
                tool = tool
            )
        )
    }

    fun eraseLastDrawingStroke(pageIndex: Int) {
        setNoteBlocks(NoteBlockOps.eraseLastDrawingStroke(_noteBlocks, pageIndex))
    }

    fun eraseDrawingStrokeAtPoint(
        pageIndex: Int,
        normalizedX: Float,
        normalizedY: Float,
        normalizedRadius: Float
    ) {
        setNoteBlocks(
            NoteBlockOps.eraseDrawingStrokeAtPoint(
                blocks = _noteBlocks,
                pageIndex = pageIndex,
                normalizedX = normalizedX,
                normalizedY = normalizedY,
                normalizedRadius = normalizedRadius
            )
        )
    }

    fun setNotePin(pin: String) {
        notePinHash = hashNotePin(pin)
        _noteLocked.value = true
    }

    fun clearNotePin() {
        notePinHash = null
        _noteLocked.value = false
    }

    fun verifyNotePin(pin: String): Boolean {
        val expectedHash = notePinHash ?: return !_noteLocked.value
        return hashNotePin(pin) == expectedHash
    }

    fun changeNotePin(currentPin: String, newPin: String): Boolean {
        if (!verifyNotePin(currentPin)) return false
        setNotePin(newPin)
        return true
    }

    fun setFolderName(name: String) {
        _folderName.value = name
    }

    private fun setNoteBlocks(blocks: List<NoteBlock>) {
        _noteBlocks.clear()
        _noteBlocks.addAll(
            NoteBlockOps.ensureCanvasLayout(
                NoteBlockOps.normalized(blocks)
            )
        )
    }

    private fun buildPersistableNoteBlocks(titleHtml: String): List<NoteBlock> {
        noteState.value.state.normalizePageMarkers()
        val blocks = if (_noteBlocks.isEmpty()) {
            NoteBlockFactory.createForNote(
                titleHtml = titleHtml,
                content = noteContent.value.text,
                audioPath = noteFileName.value.text,
                imageUris = tempImageUris.value.imageFileUris
            ).toMutableList()
        } else {
            _noteBlocks.toMutableList()
        }

        val cleanTitleHtml = titleHtml.trim()
        val titlePlainText = if (cleanTitleHtml.isNotBlank()) extractPlainTextFromHtml(cleanTitleHtml) else ""
        if (cleanTitleHtml.isNotBlank()) {
            val richIndex = blocks.indexOfFirst {
                it.type == NoteBlockType.TEXT.name && !it.richTextHtml.isNullOrBlank()
            }
            if (richIndex >= 0) {
                blocks[richIndex] = blocks[richIndex].copy(
                    richTextHtml = cleanTitleHtml,
                    text = titlePlainText.ifBlank { blocks[richIndex].text }
                )
            } else {
                blocks.add(
                    0,
                    NoteBlock(
                        id = java.util.UUID.randomUUID().toString(),
                        order = 0,
                        type = NoteBlockType.TEXT.name,
                        text = titlePlainText.ifBlank { null },
                        richTextHtml = cleanTitleHtml
                    )
                )
            }
        }

        val contentText = noteContent.value.text.orEmpty().trim()
        val bodyIndex = blocks.indexOfFirst {
            it.type == NoteBlockType.TEXT.name && it.richTextHtml.isNullOrBlank()
        }
        if (contentText.isNotBlank()) {
            if (bodyIndex >= 0) {
                blocks[bodyIndex] = blocks[bodyIndex].copy(text = contentText)
            } else {
                blocks.add(
                    NoteBlock(
                        id = java.util.UUID.randomUUID().toString(),
                        order = blocks.size,
                        type = NoteBlockType.TEXT.name,
                        text = contentText
                    )
                )
            }
        }

        val audioPath = noteFileName.value.text.trim()
        val audioIndex = blocks.indexOfFirst { it.type == NoteBlockType.AUDIO.name }
        if (audioPath.isNotBlank()) {
            if (audioIndex >= 0) {
                blocks[audioIndex] = blocks[audioIndex].copy(mediaUri = audioPath)
            } else {
                blocks.add(
                    NoteBlock(
                        id = java.util.UUID.randomUUID().toString(),
                        order = blocks.size,
                        type = NoteBlockType.AUDIO.name,
                        mediaUri = audioPath
                    )
                )
            }
        } else {
            blocks.removeAll { it.type == NoteBlockType.AUDIO.name }
        }

        val latestImages = tempImageUris.value.imageFileUris.orEmpty()
        val imageSynced = NoteBlockOps.syncImageBlocks(blocks, latestImages)

        return NoteBlockOps.ensureCanvasLayout(NoteBlockOps.normalized(imageSynced))
    }

    private fun saveNote(navigate: Boolean, note: String) {
        viewModelScope.launch {
            // Check if content is empty
            if (note.trim().isEmpty()) {
                // Set title to "Untitled" if it's also empty or blank
                if (_noteContent.value.text?.isBlank() == true) {
                    _noteContent.value = noteContent.value.copy(text = "Untitled")
                }

                // Show snackbar
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = "Content cannot be empty"
                    )
                )
                return@launch // Don't save the note
            }

            try {
                voiceJournalRepository.save(
                    VoiceJournal(
                        title = note,
                        content = noteContent.value.text ?: "Untitled",
                        created = created.value.created,
                        fileName = noteFileName.value.text,
                        id = currentNoteId,
                        color = noteColor.value,
                        imageUris = tempImageUris.value.imageFileUris,
                        tags = tags.value,
                        favourite = favourite.value.favourite,
                        noteBlocks = buildPersistableNoteBlocks(note),
                        folderName = _folderName.value.trim().ifBlank { null },
                        isLocked = _noteLocked.value,
                        lockPinHash = if (_noteLocked.value) notePinHash else null
                    )
                )
                if (navigate) {
                    _eventFlow.emit(UiEvent.SaveNote)
                }
            } catch (e: InvalidNoteException) {
                Log.d("Note", "could not save")
                _eventFlow.emit(
                    UiEvent.ShowSnackbar(
                        message = e.message ?: "Couldn't save note"
                    )
                )
            }
        }
        removeSelectedImageUris()
    }


}
