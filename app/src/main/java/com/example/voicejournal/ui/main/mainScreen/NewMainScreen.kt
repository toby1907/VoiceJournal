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

val voiceJournals = listOf(
    VoiceJournal(
        title = "Journal 1",
        content = "What is something you’ve always wanted to do but haven’t yet? What’s holding you back?",
        created = System.currentTimeMillis() - 86400000, // Yesterday
        fileName = "journal1.mp3",
        color = VoiceJournal.noteColors.random().toArgb(),
        imageUris = emptyList(),
        tags = emptyList()
    ),
    VoiceJournal(
        title = "Journal 2",
        content = "What is something you’ve always wanted to do but haven’t yet? What’s holding you back?",
        created = System.currentTimeMillis() - 172800000, // Two days ago
        fileName = "journal2.mp3",
        color = VoiceJournal.noteColors.random().toArgb(),
        imageUris = emptyList(),
        tags = emptyList()
    ),
    VoiceJournal(
        title = "Journal 3",
        content = "What is something you’ve always wanted to do but haven’t yet? What’s holding you back?",
        created = System.currentTimeMillis() - 259200000, // Three days ago
        fileName = "journal3.mp3",
        color = VoiceJournal.noteColors.random().toArgb(),
        imageUris = emptyList(),
        tags = emptyList()
    ),
    VoiceJournal(
        title = "Journal 4",
        content = "What is something you’ve always wanted to do but haven’t yet? What’s holding you back?",
        created = System.currentTimeMillis() - 259200000, // Three days ago
        fileName = "journal3.mp3",
        color = VoiceJournal.noteColors.random().toArgb(),
        imageUris = emptyList(),
        tags = emptyList()
    ),
    VoiceJournal(
        title = "Journal 5",
        content = "What is something you’ve always wanted to do but haven’t yet? What’s holding you back?",
        created = System.currentTimeMillis() - 259200000, // Three days ago
        fileName = "journal3.mp3",
        color = VoiceJournal.noteColors.random().toArgb(),
        imageUris = emptyList(),
        tags = emptyList()
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MyScaffold() {

        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Jorie") },
                navigationIcon = { Icon(Icons.Filled.Menu, contentDescription = null) },
                actions = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.cloud_icon), contentDescription = null)
                    }
                },
                elevation = 8.dp
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(painterResource(id = R.drawable.note), contentDescription = null)
                }
                Spacer(modifier = Modifier.weight(1f, true))

                Icon(Icons.Filled.Favorite, contentDescription = null)
                Icon(painterResource(id = R.drawable.calendar), contentDescription = null)
                Icon(Icons.Filled.Notifications, contentDescription = null)
            }



        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        content = { it ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    contentScale = ContentScale.FillBounds,
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = "",
                    modifier = Modifier.matchParentSize()
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    // item{NoteList(sortedVoiceJournals)}
                    /* item {
                     Text(
                         text = "January 3, 2024. Wed",
                         modifier = Modifier.padding(16.dp),
                         style = MaterialTheme.typography.headlineSmall
                     )
                 }
                 item {
                     Card(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(16.dp),
                         elevation = CardDefaults.cardElevation()
                     ) {
                         Column(
                             modifier = Modifier.padding(16.dp)
                         ) {
                             ListItem(
                                 text = { Text(text = "My Thought") },
                                 secondaryText = { Text(text = "Brief description") },
                                 icon = {
                                     Row {
                                         Icon(painterResource(id = R.drawable.cloud_icon), contentDescription = null)
                                         Icon(Icons.Filled.Favorite, contentDescription = null)
                                     }
                                 },
                                 trailing = { Text(text = "9:32am") },
                                 modifier = Modifier.fillMaxWidth()
                             )
                             Divider()
                             ListItem(
                                 text = { Text(text = "My Thought") },
                                 secondaryText = { Text(text = "Brief description") },
                                 icon = {
                                     Row {
                                         Icon(painterResource(id = R.drawable.cloud_icon), contentDescription = null)
                                         Icon(Icons.Filled.Favorite, contentDescription = null)
                                     }
                                 },
                                 trailing = { Text(text = "10:45am") },
                                 modifier = Modifier.fillMaxWidth()
                             )
                             Divider()
                             ListItem(
                                 text = { Text(text = "My Thought") },
                                 secondaryText = { Text(text = "Brief description") },
                                 icon = {
                                     Row {
                                         Icon(painterResource(id = R.drawable.cloud_icon), contentDescription = null)
                                         Icon(Icons.Filled.Favorite, contentDescription = null)
                                     }
                                 },
                                 trailing = { Text(text = "11:15am") },
                                 modifier = Modifier.fillMaxWidth()
                             )
                         }
                     }
                 }*/
                }
            }
        }
    )

}

/*
* @Composable
fun JournalItem(
    modifier: Modifier,
    voiceJournal: VoiceJournal){

    Column {
        ListItem(
            headlineText = {
                Text(
                text = voiceJournal.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) },

            supportingText = {
                voiceJournal.content?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
             },
            trailingContent = {
                val simpleDate = SimpleDateFormat("d/MMM/yy", Locale.getDefault())
                val journalDate = simpleDate.format(voiceJournal.created)
                Column(){
                    Text(text = journalDate)
                    if (voiceJournal.fileName != "") Icon(
                        painter = painterResource(id = R.drawable.play_button_24),
                        contentDescription = ""
                    )
                }
                
            },
            leadingContent = {
                
                Row(modifier = Modifier.clip(CircleShape)
                ){
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = Color(voiceJournal.color))
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Text(text = voiceJournal.title.getOrNull(0).toString().toUpperCase(Locale.getDefault()))
                    }
                }
            },

            modifier = modifier
        )
        Divider()
    }

   /* Surface(modifier.padding(8.dp)) {
        Row(){
            Column(Modifier.size(width = 240.dp, height = 60.dp)) {
                Text(text = voiceJournal.title, fontSize = 24.sp)
                voiceJournal.content?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier =Modifier.size(24.dp) )
          if(voiceJournal.fileName!="")  Icon(painter = painterResource(id =R.drawable.play_button_24 ) , contentDescription = "")
        }
    }*/
}*/
@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    voiceJournal: VoiceJournal
) {
    Surface(
        modifier = Modifier.padding(8.dp),
        border = BorderStroke(2.dp, Color.Red),
        contentColor = Color.Blue,
        shape = CircleShape
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = voiceJournal.title, fontWeight = FontWeight.Bold)
            voiceJournal.content?.let { Text(text = it) }
            val simpleDate = SimpleDateFormat("d/MMM/yy", Locale.getDefault())
            val journalDate = simpleDate.format(voiceJournal.created)

            Column() {
                Text(text = journalDate)
                if (voiceJournal.fileName != "") Icon(
                    painter = painterResource(id = R.drawable.audio_file),
                    contentDescription = ""
                )
            }
        }
    }

}

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
    /*@Composable
fun VoiceJournalList(voiceJournals: List<VoiceJournal>) {
    // Group the list by created date
    val groupedVoiceJournals = voiceJournals.groupBy { SimpleDateFormat("d/MMM/yy", Locale.getDefault()).format(it.created) }

    LazyColumn {
        groupedVoiceJournals.forEach { (date, journals) ->
            // Add a sticky header with the date
            stickyHeader {
                JournalHeader(date)
            }

            // Add items for each journal in the group
            items(journals) { journal ->
                JournalItem(journal)
            }
        }
    }
}*/

    LazyColumn(
        modifier =
        modifier
            .background(color = Color.Transparent)
            .padding(start = 8.dp, end = 8.dp)

    ) {

        groupedVoiceJournals.forEach { (date, journals) ->


            stickyHeader {
                StickyHeader(text = date,
                    )
            }


            items(journals.size) { note ->

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
                        voiceJournal = journals[note],
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    Screen.AddEditNoteScreen.route +
                                            "?noteId=${journals[note].id}&noteColor=${journals[note].color}"
                                )
                            }
                    )
                }

            }

        }
    }

    }



    @Composable
    @Preview
    fun NewMainScreenPreview(
    ) {
        MyScaffold()
    }

