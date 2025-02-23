package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.voicejournal.ui.main.calendar.clickable
import com.example.voicejournal.ui.theme.Variables
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreenTopAppBar(
    navController: NavController,
    addVoiceNoteViewModel: AddVoiceNoteViewModel,
    note: AddVoiceNoteViewModel.NoteState,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    nav: () -> Unit,
    datePickerDialog: () -> Unit,
    contentSaved: () -> Unit,
    state: RichTextState

) {
    val titleState = addVoiceNoteViewModel.noteTitle.value

    val journalDateLong = addVoiceNoteViewModel.created.value
    val simpleDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val journalDate = simpleDate.format(journalDateLong.created)

    /*
                                if(result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(NotesEvent.RestoreNote)
                                }*/
    CenterAlignedTopAppBar(
        title = {

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    datePickerDialog()

                },


                ) {
                Text(
                    text = journalDate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight(500),
                        color = Variables.SchemesOnPrimaryContainer,
                    )
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "ArrowDropDown",
                    tint = Variables.SchemesOnPrimaryContainer
                )
            }
        },
        navigationIcon = {


            if (state.annotatedString.text.isNotBlank() && addVoiceNoteViewModel.noteContent.value.text?.isNotBlank()==true ) {
                IconButton(onClick = {
                    contentSaved()

                    if (state.annotatedString.text.isNotBlank()) {
                        navController.navigateUp()
                        //    navController.navigate(Screen.VoicesScreen.route)
                       /* addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNote(
                            note = state.toHtml()
                        ))*/
                    } else {
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("Kindly Enter a title be for you save"))
                    }


                })

                {
                    Icon(
                        painter = painterResource(R.drawable.check_24),
                        contentDescription = "Delete Journal",
                        tint = Variables.SchemesOnPrimaryContainer
                    )
                }

            } else {
                IconButton(onClick = {
                    navController.navigate(Screen.VoicesScreen.route)
                    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel",
                        tint = Variables.SchemesOnPrimaryContainer
                    )

                }
            }
        },
        actions = {

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Variables.SchemesPrimaryContainer
        )
    )
}
