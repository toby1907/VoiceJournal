package com.example.voicejournal.ui.main.favourite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.mainScreen.NewJournalItem
import com.example.voicejournal.ui.main.mainScreen.StickyHeader
import com.example.voicejournal.ui.theme.Variables
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteScreenMain(
    viewModel: FavouriteViewModel = hiltViewModel(),
    navController: NavController
) {


val journals = viewModel.state.value
    val sortedList = journals.notes.filter { it.favourite==true }

    val groupedVoiceJournals = sortedList.sortedBy { it.created }.groupBy {
        /*    val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.created
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis*/
        SimpleDateFormat("MMMM d, yyyy . EEE", Locale.getDefault()).format(it.created)

    }
        /*LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(count = sortedList.notes.size) {
              NewJournalItem(voiceJournal = sortedList.notes[it])
            }
        }*/
    LazyColumn(
        modifier =
        Modifier
            .background(color = Color.Transparent)
            .padding(start = 16.dp, end = 16.dp)

    ) {


        groupedVoiceJournals.forEach { (date, groupJournals) ->

            item {
                StickyHeader(
                    text = date,
                )
            }

            item {
                Column(
                    modifier =
                    Modifier
                        .background(
                            color = Variables.SchemesSurface,
                            shape = RoundedCornerShape(size = 24.dp)
                        ),
                    verticalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    groupJournals.sortedBy { it.created }.forEachIndexed { index, journal ->
                        //   if (groupJournals.size==0)
                        Column {

                            NewJournalItem(
                                voiceJournal = journal,
                                modifier = Modifier
                                    .clickable {

                                        navController.navigate(
                                            Screen.AddEditNoteScreen.route +
                                                    "?noteId=${journal.id}&noteColor=${journal.color}"
                                        )
                                        navController.navigate(
                                            "preview" + "?noteId=${journal.id}&noteColor=${journal.color}&noteIndex=${
                                                journals.notes.indexOf(
                                                    journal
                                                )
                                            }"
                                        )
                                    }
                            )
                            if (index < groupJournals.size - 1) {
                                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }

                    }
                }
            }


        }
    }

}