package com.example.voicejournal.ui.main.AddVoiceNote

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.drawable.PaintDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import android.widget.DatePicker
import android.widget.Space
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voicejournal.Data.VoiceJournal

import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.AddVoiceNote.components.TransparentHintTextField
import com.example.voicejournal.ui.main.ContentMain
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.math.absoluteValue


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddVoiceNoteScreen(
    navController: NavController,
    noteColor: Int,
    addVoiceNoteViewModel: AddVoiceNoteViewModel = hiltViewModel()
){
    val titleState = addVoiceNoteViewModel.noteTitle.value
    val contentState = addVoiceNoteViewModel.noteContent.value
    val fileNameState = addVoiceNoteViewModel.noteFileName.value
    val note = addVoiceNoteViewModel.noteState.value
 val playNoteState = addVoiceNoteViewModel.playNoteState.value
    var playState by remember { mutableStateOf(false) }
    var playButtonState by remember {
        mutableStateOf(false)
    }
    var recordState by remember {
        mutableStateOf(false)
    }
    var mediaState by remember {
        mutableStateOf(false)
    }
    var colorIntState by remember {
        mutableStateOf(Int.MAX_VALUE)
    }

    val scaffoldState = rememberScaffoldState()
    val noteBackgroundAnimatable = remember{
        Animatable(
            Color(if (noteColor != -1) noteColor else addVoiceNoteViewModel.noteColor.value)
        )
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = true){
        addVoiceNoteViewModel.eventFlow.collectLatest {event ->
            when(event){
                is AddVoiceNoteViewModel.UiEvent.Recording -> {}
                is AddVoiceNoteViewModel.UiEvent.StopRecord -> {

                }
                is AddVoiceNoteViewModel.UiEvent.PlayNote -> {
                    playState= true
                }
                is AddVoiceNoteViewModel.UiEvent.StopPlay -> {
                   mediaState = true
                }
                is AddVoiceNoteViewModel.UiEvent.SaveNote -> {
                    navController.navigateUp()
                }
                is AddVoiceNoteViewModel.UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }


            }
        }
    }

  Surface(
     color = MaterialTheme.colorScheme.primary
  ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(
                            if (titleState.text != "") "Edit Journal" else "New Journal",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.VoicesScreen.route) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cancel"
                            )
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
                                    contentDescription = "Delete Journal"
                                )
                            }
                        }
                        IconButton(onClick = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNote)
                            navController.navigate(Screen.VoicesScreen.route)
                        })
                        {
                            Text(text = "Save")
                        }

                    }
                )
            },

            bottomBar = {

                BottomAppBar(
                    Modifier
                        .wrapContentWidth()
                        .background(Color(colorIntState)),
                    containerColor = Color(colorIntState)
                )
                {
                    // Leading icons should typically have a high content alpha
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                        IconButton(onClick = {
                            playState = !playState

                            if (playState && playNoteState) {
                                addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Play(fileNameState.text))
                            } else {
                                addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                            }
                        })
                        {
                            if (playNoteState) {

                                Icon(
                                    painter = painterResource(if (!playState || mediaState) R.drawable.play_button_24 else R.drawable.pause_button_24),
                                    contentDescription = "Localized description"
                                )
                            }
                        }
                    }
                    // The actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    Spacer(Modifier.weight(1f, true))
                    IconButton(onClick = {

                        recordState = !recordState
                        mediaState = false
                        playState = false
                        if (recordState) {
                            playButtonState = false

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Recording(fileNameState.text))
                        } else {

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                            playButtonState = true
                        }


                    }) {
                        if (!playNoteState) {
                            Icon(
                                painter = painterResource(if (recordState == false) R.drawable.baseline_mic_black_36 else R.drawable.stop_button_36),
                                contentDescription = "Localized description"
                            )
                        }

                    }

                }
            },
            content = { innerPadding ->
                Column(
                    // consume insets as scaffold doesn't do it by default (yet)
                    modifier = Modifier
                        .consumedWindowInsets(innerPadding)
                        .fillMaxSize()
                        .background(noteBackgroundAnimatable.value)
                        .padding(16.dp),


                    ) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        VoiceJournal.noteColors.forEach { color ->
                            val colorInt = color.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .shadow(15.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = 3.dp,
                                        color = if (addVoiceNoteViewModel.noteColor.value == colorInt) {
                                            Color.Black
                                        } else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        colorIntState = color.toArgb()
                                        scope.launch {
                                            noteBackgroundAnimatable.animateTo(
                                                targetValue = Color(colorInt),
                                                animationSpec = tween(
                                                    durationMillis = 500
                                                )
                                            )
                                        }
                                        addVoiceNoteViewModel.onEvent(
                                            AddEditNoteEvent.ChangeColor(
                                                colorInt
                                            )
                                        )
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TransparentHintTextField(
                        text = titleState.text,
                        hint = titleState.hint,
                        onValueChange = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredTitle(it))
                        },
                        onFocusChange = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.ChangeTitleFocus(it))
                        },
                        isHintVisible = titleState.isHintVisible,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TransparentHintTextField(
                        text = contentState.text ?: "",
                        hint = contentState.hint,
                        onValueChange = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredContent(it))
                        },
                        onFocusChange = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.ChangeContentFocus(it))
                        },
                        isHintVisible = contentState.isHintVisible,
                        textStyle = androidx.compose.material.MaterialTheme.typography.body1,
                        modifier = Modifier.fillMaxHeight()
                    )
                }

            }
        )
    }

}
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDatePicker(
    context: Context,
    getTitleAndContent:()->Unit,
) {
val year: Int
val month: Int
val day: Int

val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month =  calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()
    val date = remember{
        mutableStateOf("$day/$month/$year")
    }
    //converting calendar to something we can store in the model
    var dateInMillis = calendar.timeInMillis

    val datePickerDialog = DatePickerDialog(
        context,{_: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            date.value = "$dayOfMonth/$month/$year"

        }, year, month, day
    )
    var titleText by remember{ mutableStateOf("")}
    var contentText by remember{ mutableStateOf("")}
 Column(
     modifier = Modifier.fillMaxSize(),
     verticalArrangement =  Arrangement.Center,
     horizontalAlignment = Alignment.CenterHorizontally
 ) {
     Box(modifier =Modifier){
         TextField(
             value = titleText,
             onValueChange = { titleText = it },
             label = { Text(text = "Title") },
             maxLines = 1,
             modifier = Modifier.fillMaxWidth()
             )
     }
     Spacer(modifier = Modifier.height(16.dp))
    Box(modifier = Modifier) {
         TextField(
             value = contentText,
             onValueChange = { newText ->
                 contentText = newText.trimStart { it == '0' }
             },
             label = { Text(text = "Content") },
             modifier = Modifier.fillMaxWidth()

         )
        }
     TextButton(onClick = { datePickerDialog.show()}){
         Text(text =  "Selected Date: ${date.value}")
     }

 }
}*/



/*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.ui.input.nestedscroll.nestedScroll

val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
        SmallTopAppBar(
            title = {
                Text(
                    "Small TopAppBar",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Localized description"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Localized description"
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    },
    content = { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val list = (0..75).map { it.toString() }
            items(count = list.size) {
                Text(
                    text = list[it],
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
)

*/
@Composable
@Preview
fun AddVoiceNoteScreenPreview(){
    val context= LocalContext.current
  AddVoiceNoteScreen(navController =NavController(context), noteColor =Color.Blue.toArgb() )
}
