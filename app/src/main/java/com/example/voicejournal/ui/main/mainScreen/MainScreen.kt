package com.example.voicejournal.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel

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
            SmallTopAppBar(
                title = { Text("Voice Journal") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
                               ExtendedFloatingActionButton(
                                   onClick = onNavigateToAddVoice)
                               {
                                   Icon(
                                       painter = painterResource(id = R.drawable.add_36),
                                       contentDescription =""
                                   )
                               }
        },
        content = { innerPadding->
            LazyColumn(
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
            }

        }
            )
}
   
@Composable
fun JournalItem(
    modifier: Modifier,

    voiceJournal: VoiceJournal){

    Surface(modifier.padding(8.dp)) {
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
            Icon(painter = painterResource(id =R.drawable.play_button_24 ) , contentDescription = "")
        }
    }
}




