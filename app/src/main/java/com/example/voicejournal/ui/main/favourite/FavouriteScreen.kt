package com.example.voicejournal.ui.main.favourite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp

@Composable
fun FavouriteScreen(
    journals: List<Journal>,
    onAddJournalClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
   Column {

        LazyColumn{
            items(journals.size) { journal ->
                JournalItem(journals[journal])
            }
        }
    }
}

@Composable
fun JournalItem(journal: Journal) {
    // Display individual journal item here
    // Customize as needed
    Text(
        text = journal.title,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

data class Journal(val id: Int, val title: String)

// Usage example:
val sampleJournals = listOf(
    Journal(id = 1, title = "Journal 1"),
    Journal(id = 2, title = "Journal 2"),
    // Add more journals here
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreenMain() {


        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = List(100) { "Text $it" }
            items(count = list.size) {
                Text(list[it], Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
        }

  /*  FavouriteScreen(
        journals = sampleJournals,
        onAddJournalClick = { *//* Handle add journal click *//* },
        onSearchQueryChanged = { *//* Handle search query changes *//* }
    )*/
}