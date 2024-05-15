package com.example.voicejournal.ui.main.calendar

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.ui.main.mainScreen.NotesEvent
import com.example.voicejournal.ui.main.mainScreen.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(private val voiceJournalRepository: VoiceJournalRepositoryImpl) : ViewModel() {

    private val _state = mutableStateOf(NotesState())
    val state: State<NotesState> =_state

    private val _filter = MutableLiveData<Boolean>()






    /*val voiceNotesListUiItems = voiceJournalRepository.getAllVoiceJournals().map { voice ->
        VoiceNotesItemUiState(
            title = voice.title,
            body = voice.content,
            // Business logic is passed as a lambda function  that the
            // UI calls on click events.
            onDelete = {
                voiceJournalRepository.delete(voice)
            },
            publicationDate = voice.created
        )
    }*/




    init {
        // Set default state value.
        getNotes()

    }

    /**
     * Update filter to a different state.
     *
     * @param itemName resource entry name identifier
     * @see R.menu.menu_list
     */
    fun onEvent(event: NotesEvent){
        when(event)  {
            is NotesEvent.DeleteNote -> {
                viewModelScope.launch {
                    voiceJournalRepository.delete(event.voiceJournal)
                }
            }
        }
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




}
