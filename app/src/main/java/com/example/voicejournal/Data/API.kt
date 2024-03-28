package com.example.voicejournal.Data

import com.example.voicejournal.ui.main.search.SearchResult
import kotlinx.coroutines.delay
import java.io.IOException

class FakeSearchAPI {

    suspend fun getResults(searchTerm: String): List<SearchResultSchema> {
        delay(1500)
        return when {
            searchTerm.length > 10 -> emptyList()
            searchTerm == "error" -> throw IOException()
            else -> generateFakeResults()
        }
    }

    private fun generateFakeResults(): List<SearchResultSchema> = List(size = 20) { i ->
        SearchResultSchema(
            id = i,
            title = "Title of result $i",
            subtitle = "Subtitle of result $i"
        )
    }
}

data class SearchResultSchema(
    val id: Int,
    val title: String,
    val subtitle: String,
)

class GetSearchResults(private val searchAPI: FakeSearchAPI) {

    sealed interface Result {
        object Error: Result
        data class Success(val results: List<SearchResult>) : Result
    }

    suspend operator fun invoke(searchTerm: String): Result = try {
        val results = searchAPI.getResults(searchTerm).map { searchResultSchema ->
            SearchResult(
                id = searchResultSchema.id,
                title = searchResultSchema.title,
                subtitle = searchResultSchema.subtitle,
            )
        }
        Result.Success(results = results)
    } catch (e: IOException) {
        Result.Error
    }
}