package com.example.voicejournal.ui.main.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voicejournal.ui.main.mainScreen.NewJournalItem

@Composable
fun JournalPreviewScreen (
    journalIds: List<Int>?,
    navController: NavController,
)
{
    val viewModel:CalendarViewModel = hiltViewModel()
    val allJournalsList = viewModel.state.value.notes
    val journals = allJournalsList.filter { journalIds?.contains(it.id) ?: false }
    Column {
        LazyColumn{
            items(journals.size){index->
                NewJournalItem(
                    voiceJournal = journals[index],
                    modifier = Modifier
                        .clickable {
                            navController.navigate(
                                "preview" + "?noteId=${journals[index].id}&noteColor=${journals[index].color}&noteIndex=${
                                    allJournalsList.indexOf(
                                        journals[index]
                                    )
                                }"
                            )
                        }
                )

            }
        }
    }
}