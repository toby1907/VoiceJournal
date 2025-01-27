package com.example.voicejournal.ui.main.search
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicejournal.Data.GetSearchResults
import com.example.voicejournal.Data.VoiceJournalRepositoryImpl
import com.example.voicejournal.Data.model.VoiceJournal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val getSearchResults: GetSearchResults
) : ViewModel() {

    sealed class ViewState {
        object IdleScreen : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
        object NoResults : ViewState()
        data class SearchResultsFetched(val results: List<VoiceJournal>) : ViewState()
    }
    sealed class SearchFieldState {
        object Idle : SearchFieldState()
        object EmptyActive : SearchFieldState()
        object WithInputActive : SearchFieldState()
    }

    private val _searchFieldState: MutableStateFlow<SearchFieldState> = MutableStateFlow(SearchFieldState.Idle)
    val searchFieldState: StateFlow<SearchFieldState> = _searchFieldState

    private val allVoiceJournalFlow: MutableStateFlow<List<VoiceJournal>> = MutableStateFlow(emptyList())
    val allVoiceJournal: StateFlow<List<VoiceJournal>> = allVoiceJournalFlow

    private val _viewState: MutableStateFlow<ViewState> =
        MutableStateFlow(ViewState.IdleScreen)
    val viewState: StateFlow<ViewState> = _viewState

    private val _inputText: MutableStateFlow<String> =
        MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText

    private val isInitialized = AtomicBoolean(false)

   /* @FlowPreview
    fun initialize() {
    //    if (isInitialized.compareAndSet(false, true)) {
            viewModelScope.launch {
                inputText.debounce(300).collectLatest { input ->
                    Log.d("Initialize", "Input received: $input")
                    if (input.blankOrEmpty()) {
                        _viewState.update { ViewState.IdleScreen }
                        return@collectLatest
                    }

                    // Create a new coroutine scope for the search operation
                    viewModelScope.async(Dispatchers.IO){
                        Log.d("ViewModelSCope 2", " launched")
                        try {
//                            when (val result = getSearchResults(input)) {
//                                is GetSearchResults.Result.Success -> {
//                                    Log.d("SearchBar", "$result")
//                                    if (result.results.isEmpty()) {
//                                        _viewState.update { ViewState.NoResults }
//                                    } else {
//                                        Log.d("SearchBar2", "$result")
//                                        _viewState.update { ViewState.SearchResultsFetched(result.results) }
//                                    }
//                                }
//                                is GetSearchResults.Result.Error -> {
//                                    Log.d("SearchBar", "Error fetching results")
//                                    _viewState.update { ViewState.Error }
//                                }
//                            }
                            _viewState.update {
                                val result = mutableListOf<VoiceJournal>()
                                    voiceJournalRepository.searchDatabase(input).collect{ voiceJournalList ->
                                   result.addAll(voiceJournalList)
                                }
                                Log.d("result","$result")
                                ViewState.SearchResultsFetched(
                                results = result
                                )

                            }

                            } catch (e: Exception) {
                            Log.e("Initialize", "Error fetching results", e)
                            _viewState.update { ViewState.Error }
                        }
                    }.await()
                }
            }
     //   }
    }*/

    init {
        viewModelScope.launch {
            voiceJournalRepository.getAllVoiceJournals().collectLatest { voiceJournalList ->
                Log.d("SearchBar", "$voiceJournalList")
                if (voiceJournalList.isEmpty()) {
                    _viewState.update { ViewState.NoResults }
                } else {
                    _viewState.update { ViewState.SearchResultsFetched(voiceJournalList) }
                    allVoiceJournalFlow.update {
                        voiceJournalList
                    }
                }
            }
        }
    }



    fun updateInput(inputText: String) {
        _inputText.update { inputText }
        activateSearchField()

        if (inputText.blankOrEmpty().not()) {
            _viewState.update { ViewState.Loading }
        }
    }

    fun searchFieldActivated() {
        activateSearchField()
    }

    fun clearInput() {
        _viewState.update { ViewState.Loading }
        _inputText.update { "" }
        _searchFieldState.update { SearchFieldState.EmptyActive }
    }

    fun revertToInitialState() {
        _viewState.update { ViewState.IdleScreen }
        _inputText.update { "" }
        _searchFieldState.update { SearchFieldState.Idle }
    }

    private fun activateSearchField() {
        if (inputText.value.blankOrEmpty().not()) {
            _searchFieldState.update { SearchFieldState.WithInputActive }
        } else {
            _searchFieldState.update { SearchFieldState.EmptyActive }
        }
    }

    private fun String.blankOrEmpty() = this.isBlank() || this.isEmpty()


    fun searchFor(searchQuery: String) {
     viewModelScope.launch(Dispatchers.IO) {
       if (searchQuery.isNotBlank())  {
             voiceJournalRepository.searchDatabase("%$searchQuery%").collect { voiceJournalList ->
                 _viewState.update {
                     ViewState.SearchResultsFetched(
                         results = voiceJournalList
                     )
                 }
             }
         }
         else{
             voiceJournalRepository.getAllVoiceJournals().collectLatest {voiceJournalList ->
                 _viewState.update {
                     ViewState.SearchResultsFetched(
                         results = voiceJournalList
                     )
                 }
             }
       }
     }
    }
}
data class SearchResult(
    val id: Int,
    val title: String,
    val subtitle: String,
)



