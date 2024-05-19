package com.example.voicejournal.ui.main.mainScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.ui.theme.Variables
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun StickyHeader(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .padding(8.dp),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = Variables.SchemesOnPrimaryContainer,
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NoteList(
    journals: List<VoiceJournal>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val groupedVoiceJournals = journals.sortedByDescending { it.created }.groupBy {
        /*    val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.created
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis*/
        SimpleDateFormat("MMMM d, yyyy . EEE", Locale.getDefault()).format(it.created)

    }

    LazyColumn(
        modifier =
        modifier
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
                    groupJournals.sortedByDescending { it.created }.forEachIndexed { index, journal ->
                    //   if (groupJournals.size==0)
                   Column {

                        NewJournalItem(
                            voiceJournal = journal,
                            modifier = Modifier
                                .clickable {
                                    /* navController.navigate(
                                        Screen.AddEditNoteScreen.route +
                                                "?noteId=${journal.id}&noteColor=${journal.color}"
                                    )*/
                                    navController.navigate(
                                        "preview" + "?noteId=${journal.id}&noteColor=${journal.color}&noteIndex=${
                                            journals.indexOf(
                                                journal
                                            )
                                        }"
                                    )
                                }
                                ,
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


