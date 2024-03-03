package com.example.voicejournal.ui.main.AddVoiceNote.components

import android.util.Log
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.AddVoiceNote.AddEditNoteEvent
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel
import com.example.voicejournal.ui.main.AddVoiceNote.NoteTextFieldState
import com.example.voicejournal.ui.theme.Variables
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreenTopAppBar(
    titleState: NoteTextFieldState,
    navController: NavController,
    addVoiceNoteViewModel: AddVoiceNoteViewModel,
    note: AddVoiceNoteViewModel.NoteState,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    nav: ()-> Unit

    ) {
    val simpleDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val journalDate = simpleDate.format(note.voiceJournal?.created ?: System.currentTimeMillis())

    /*
                                if(result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(NotesEvent.RestoreNote)
                                }*/
    CenterAlignedTopAppBar(
        title = {

            Text(
                text = journalDate  ,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight(500),
                    color = Variables.SchemesOnSecondaryContainer,
                )
            )
        },
        navigationIcon = {


            if (titleState.text != "") {
                IconButton(onClick = {
                    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNote)
                    Log.d("NoteNav","could not save")
                })

                {
                    Icon(
                        painter = painterResource(R.drawable.check_24),
                        contentDescription = "Delete Journal"
                    )
                }

            } else{
                IconButton(onClick = { navController.navigate(Screen.VoicesScreen.route) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel"
                    )

                }
            }
        },
        actions = {
            IconButton(onClick = {
                addVoiceNoteViewModel.onEvent(AddEditNoteEvent.DeleteJournal(note.voiceJournal))
                scope.launch {
                    navController.navigate(Screen.VoicesScreen.route)
                    val result = scaffoldState.snackbarHostState.showSnackbar(
                        message = "Note deleted",
                        actionLabel = "Undo"
                    )/*
                                        if(result == SnackbarResult.ActionPerformed) {
                                            viewModel.onEvent(NotesEvent.RestoreNote)
                                        }*/
                }
            }) {
                if (titleState.text != "") {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_delete_forever_24),
                        contentDescription = "Delete Journal",
                        tint = Variables.SchemesError
                    )
                }
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor =   Variables.SchemesPrimaryContainer
        )
    )
}