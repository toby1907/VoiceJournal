package com.example.voicejournal.ui.main.AddVoiceNote.components



import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.text.BasicTextField

import androidx.compose.ui.focus.onFocusChanged

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.SolidColor

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowDropDown

import androidx.compose.material.icons.filled.Close

import androidx.compose.material.icons.filled.GraphicEq

import androidx.compose.material.icons.filled.Lock

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
import androidx.compose.ui.platform.testTag

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController

import com.example.voicejournal.R

import com.example.voicejournal.Screen

import com.example.voicejournal.ui.main.AddVoiceNote.AddEditNoteEvent

import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteViewModel

import com.example.voicejournal.ui.main.calendar.clickable

import com.example.voicejournal.ui.theme.Variables

import com.example.voicejournal.ui.common.richeditor.RichTextState

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.launch

import java.text.SimpleDateFormat

import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun EditScreenTopAppBar(

    navController: NavController,

    addVoiceNoteViewModel: AddVoiceNoteViewModel,

    note: AddVoiceNoteViewModel.NoteState,

    canApplyTemplate: Boolean = true,

    onTemplateClick: () -> Unit = {},

    hasFolder: Boolean = false,

    onFolderClick: () -> Unit = {},

    isLocked: Boolean = false,

    onLockClick: () -> Unit = {},

    hasVoiceRecording: Boolean = false,

    isMediaPanelVisible: Boolean = false,

    isVoicePlaying: Boolean = false,

    onVoiceIndicatorClick: () -> Unit = {},

    scope: CoroutineScope,

    snackbarHostState: SnackbarHostState,

    nav: () -> Unit,

    datePickerDialog: () -> Unit,

    contentSaved: () -> Unit,

    state: RichTextState

) {

    val journalDateLong = addVoiceNoteViewModel.created.value

    val simpleDate = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

    val journalDate = simpleDate.format(journalDateLong.created)



    val contentState = addVoiceNoteViewModel.noteContent.value



    CenterAlignedTopAppBar(

        title = {

            Column(

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.Center,

                modifier = Modifier

                    .fillMaxWidth(0.85f)

                    .padding(horizontal = 4.dp)

            ) {

                // Editable Note Title directly above the date

                Box(

                    contentAlignment = Alignment.Center,

                    modifier = Modifier.fillMaxWidth()

                ) {

                    val titleText = contentState.text ?: ""

                    BasicTextField(

                        value = titleText,

                        onValueChange = {

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredContent(it))

                        },

                        singleLine = true,

                        enabled = !isLocked,

                        textStyle = TextStyle(

                            fontSize = 16.sp,

                            fontWeight = FontWeight.SemiBold,

                            color = Variables.SchemesOnPrimaryContainer,

                            textAlign = TextAlign.Center

                        ),

                        cursorBrush = SolidColor(Variables.SchemesOnPrimaryContainer),

                        modifier = Modifier

                            .fillMaxWidth()

                            .testTag("note_title_input")

                            .onFocusChanged {

                                addVoiceNoteViewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))

                            }

                    )

                    if (titleText.isEmpty()) {

                        Text(

                            text = if (contentState.hint.isNotBlank()) contentState.hint else "Title",

                            style = TextStyle(

                                fontSize = 16.sp,

                                fontWeight = FontWeight.Normal,

                                color = Variables.SchemesOnPrimaryContainer.copy(alpha = 0.55f),

                                textAlign = TextAlign.Center

                            ),

                            maxLines = 1,

                            overflow = TextOverflow.Ellipsis

                        )

                    }

                }



                // Date Picker trigger directly below Title

                Row(

                    horizontalArrangement = Arrangement.Center,

                    verticalAlignment = Alignment.CenterVertically,

                    modifier = Modifier.clickable {

                        datePickerDialog()

                    }

                ) {

                    Text(

                        text = journalDate,

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis,

                        style = TextStyle(

                            fontSize = 12.sp,

                            fontWeight = FontWeight.Normal,

                            color = Variables.SchemesOnPrimaryContainer.copy(alpha = 0.8f),

                        )

                    )

                    Icon(

                        imageVector = Icons.Default.ArrowDropDown,

                        contentDescription = "Select Date",

                        tint = Variables.SchemesOnPrimaryContainer.copy(alpha = 0.8f),

                        modifier = Modifier.size(16.dp)

                    )

                }

            }

        },

        navigationIcon = {

            if (state.annotatedString.text.isNotBlank() || addVoiceNoteViewModel.hasPersistableAttachments()) {

                IconButton(onClick = {

                    contentSaved()

                }) {

                    Icon(

                        painter = painterResource(R.drawable.check_24),

                        contentDescription = "Save Note",

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

            if (hasVoiceRecording) {

                IconButton(
                    onClick = onVoiceIndicatorClick,
                    modifier = Modifier.testTag("topbar_voice_indicator")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = when {
                                    isVoicePlaying -> Variables.SchemesPrimary
                                    isMediaPanelVisible -> Variables.SchemesPrimary.copy(alpha = 0.18f)
                                    else -> Color.Transparent
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = if (isMediaPanelVisible) {
                                "Hide Voice Player"
                            } else {
                                "Show Voice Player"
                            },
                            tint = if (isVoicePlaying) {
                                Variables.SchemesOnPrimary
                            } else {
                                Variables.SchemesOnPrimaryContainer
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        if (isVoicePlaying && !isMediaPanelVisible) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(7.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                        }
                    }
                }

            }

            if (isLocked) {

                IconButton(onClick = onLockClick) {

                    Icon(

                        imageVector = Icons.Default.Lock,

                        contentDescription = "Note is locked",

                        tint = Variables.SchemesOnPrimaryContainer

                    )

                }

            }

            IconButton(onClick = {

                if (note.voiceJournal != null) {

                    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.DeleteJournal(note.voiceJournal))

                    scope.launch {

                        navController.navigate(Screen.VoicesScreen.route)

                        snackbarHostState.showSnackbar(

                            message = "Note deleted",

                            actionLabel = "Undo"

                        )

                    }

                } else {

                    navController.navigate(Screen.VoicesScreen.route)

                }

            }) {

                Icon(

                    painter = painterResource(R.drawable.ic_baseline_delete_forever_24),

                    contentDescription = "Delete Journal",

                    tint = Variables.SchemesOnPrimaryContainer

                )

            }

        },

        colors = TopAppBarDefaults.topAppBarColors(

            containerColor = Variables.SchemesPrimaryContainer

        )

    )

}

