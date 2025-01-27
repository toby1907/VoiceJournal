package com.example.voicejournal.Data

import android.content.Context
import android.util.Log
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.ui.main.search.SearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class JournalSearchAPI @Inject constructor(
    private val voiceJournalRepository: VoiceJournalRepositoryImpl,
    private val settingsRepository: SettingsRepository,
    private val context: Context,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    suspend fun getResults(searchTerm: String): List<SearchResultSchema> {
        delay(1500)
        return when {
            searchTerm.length > 10 -> emptyList()
            searchTerm == "error" -> throw IOException()
            else -> generateFakeResults()
        }
    }

    suspend fun getResults2(searchTerm: String): List<VoiceJournal> {
        Log.d("SearchTerm", "getResults started")
        return when {
            searchTerm.length > 20-> emptyList()
            searchTerm == "error" -> throw IOException()
            else -> {
                val resultsList = mutableListOf<VoiceJournal>()

                    voiceJournalRepository.getAllVoiceJournals().collect { voiceJournalList ->
                        resultsList.addAll(voiceJournalList)
                        Log.d("SearchTerm2", "$resultsList")
                    }

                resultsList
            }
        }
    }

    private fun generateFakeResults(): List<SearchResultSchema> = List(size = 20) { i ->
        SearchResultSchema(
            id = i,
            title = "Title of result $i",
            subtitle = "Subtitle of result $i"
        )
    }

    suspend fun generateResults(): List<VoiceJournal> = withContext(Dispatchers.IO) {
        val voiceJournalList = mutableListOf<VoiceJournal>()
        try {
            voiceJournalRepository.getAllVoiceJournals().collect { list ->
                voiceJournalList.addAll(list)
                Log.d("Results", "$list")
            }
        } catch (e: Exception) {
            Log.e("Error", "Failed to collect voice journals", e)
        }
        Log.d("Results2", "$voiceJournalList")
        voiceJournalList
    }
}


data class SearchResultSchema(
    val id: Int,
    val title: String,
    val subtitle: String,
)

class GetSearchResults(private val searchAPI: JournalSearchAPI) {

    sealed interface Result {
        object Error : Result
        data class Success(val results: List<VoiceJournal>) : Result
    }

    suspend operator fun invoke(searchTerm: String): Result = try {
      //  val results = searchAPI.getResults2(searchTerm)
      val results = searchAPI.generateResults()

        Log.d("results", "${searchAPI.getResults2(searchTerm)}")
        Result.Success(results = results)

    } catch (e: IOException) {
        Result.Error
    }
}