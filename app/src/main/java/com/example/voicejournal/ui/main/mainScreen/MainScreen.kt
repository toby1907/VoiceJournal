package com.example.voicejournal.ui.main

import android.graphics.drawable.shapes.OvalShape
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.AddVoiceNote.AddEditNoteEvent
import com.example.voicejournal.ui.main.mainScreen.NoteList
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    modifier: Modifier =Modifier,
    navController: NavController,
    onNavigateToAddVoice: ()-> Unit,
    voiceNoteViewModel: VoiceNoteViewModel
) {
Surface(color = MaterialTheme.colorScheme.primary) {
    ContentMain(onNavigateToAddVoice,voiceNoteViewModel,navController)
}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentMain(
    onNavigateToAddVoice:()->Unit,
    voiceNoteViewModel: VoiceNoteViewModel,
    navController: NavController
) {


val voiceNotes = voiceNoteViewModel.state.value
    val voiceNotesList:List<VoiceJournal> = voiceNotes.notes

    val lazyListState = rememberLazyListState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Journals") }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
                               FloatingActionButton(
                                   onClick = onNavigateToAddVoice)
                               {
                                   Icon(
                                       painter = painterResource(id = R.drawable.add_36),
                                       contentDescription =""
                                   )
                               }
        },
        content = { innerPadding->
          /*  LazyColumn(
                modifier = Modifier.consumedWindowInsets(innerPadding),
                contentPadding = innerPadding
            ){
                items(voiceNotes.notes){note->
                  JournalItem(
                      voiceJournal = note,
                      modifier = Modifier
                          .fillMaxWidth()
                          .clickable {
                              navController.navigate(
                                  Screen.AddEditNoteScreen.route +
                                          "?noteId=${note.id}&noteColor=${note.color}"
                              )
                          }
                  )
                    
                }
            }*/
NoteList(journals = voiceNotes.notes, navController =navController,modifier =Modifier.consumedWindowInsets(innerPadding) )
        }
            )
}
   
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalItem(
    modifier: Modifier=Modifier,
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
                        painter = painterResource(id = R.drawable.audio_file),
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
}




