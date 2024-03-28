package com.example.voicejournal.ui.main.mainScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.Screen
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
    val groupedVoiceJournals = journals.groupBy {
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
            .padding(start = 8.dp, end = 8.dp)

    ) {

        groupedVoiceJournals.forEach { (date, groupJournals) ->


            stickyHeader {
                StickyHeader(text = date,
                    )
            }


            items(groupJournals.size) { note ->

                Column(
                    modifier =
                    Modifier

                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(size = 12.dp)
                        ),
                    verticalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterVertically
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    
                    NewJournalItem(
                        voiceJournal = groupJournals[note],
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                /* navController.navigate(
                                    Screen.AddEditNoteScreen.route +
                                            "?noteId=${groupJournals[note].id}&noteColor=${groupJournals[note].color}"
                                )*/
                                navController.navigate(
                                    "preview" + "?noteId=${groupJournals[note].id}&noteColor=${groupJournals[note].color}&noteIndex=${
                                        journals.indexOf(
                                            groupJournals[note]
                                        )
                                    }"
                                )
                            }
                    )
                }

            }

        }
    }

    }


