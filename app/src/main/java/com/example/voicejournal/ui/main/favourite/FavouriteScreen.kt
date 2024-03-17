package com.example.voicejournal.ui.main.favourite

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreen(
    journals: List<Journal>,
    onAddJournalClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Favorite Journals") },
                actions = {
                    // Search bar
                    var searchQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChanged(it)
                        },
                        label = { Text(text = "Search") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                }
            )
        },
    ) {it->
        // List of favorite journals
        LazyColumn(modifier = Modifier.padding(it)

        ) {
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

@Composable
fun FavouriteScreenMain() {
    FavouriteScreen(
        journals = sampleJournals,
        onAddJournalClick = { /* Handle add journal click */ },
        onSearchQueryChanged = { /* Handle search query changes */ }
    )
}