package com.example.voicejournal.ui.main.AddVoiceNote

import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomAppPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.EditScreenTopAppBar
import com.example.voicejournal.ui.main.AddVoiceNote.components.FileChooser
import com.example.voicejournal.ui.main.AddVoiceNote.components.ImageBottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.PlayRecordPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.RecordPanelComponent
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.main.AddVoiceNote.components.TagDialog
import com.example.voicejournal.ui.main.AddVoiceNote.components.TransparentHintTextField
import com.example.voicejournal.ui.theme.Variables
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AddVoiceNoteScreen(
    navController: NavController,
    noteColor: Int,
    addVoiceNoteViewModel: AddVoiceNoteViewModel = hiltViewModel()
) {
    //uris
    val uris = if(addVoiceNoteViewModel.tempImageUris.value.imageFileUris!=null)
        addVoiceNoteViewModel.tempImageUris.value.imageFileUris?.map { Uri.parse(it) }
    else addVoiceNoteViewModel.noteFileName.value.imageFileUris?.map { Uri.parse(it) }

    // A composable function to handle the back button press from the edit screen
    BackHandler(onBack = {
        // Remove the selected image URIs
       addVoiceNoteViewModel.removeSelectedImageUris()
        // Navigate back to the main screen
        navController.popBackStack()
    })

    SetStatusBarContentColor(true)
    val titleState = addVoiceNoteViewModel.noteTitle.value
    val contentState = addVoiceNoteViewModel.noteContent.value
    val fileNameState = addVoiceNoteViewModel.noteFileName.value
   // val selectedImageUris = galleryScreenViewModel.selectedUris.collectAsState(initial = emptySet())

    val note = addVoiceNoteViewModel.noteState.value
    val playNoteState = addVoiceNoteViewModel.playNoteState.value
    var playState by remember { mutableStateOf(false) }
    val doneButton = remember {
        mutableStateOf(false)
    }

    val showDialog = remember { mutableStateOf(false) }
    // A function to show the dialog
    fun showDialog() {
        showDialog.value = true
    }

    // A function to hide the dialog
    fun hideDialog() {
        showDialog.value = false
    }

    var mediaState by remember {
        mutableStateOf(false)
    }
    var colorIntState by remember {
        mutableStateOf(Int.MAX_VALUE)
    }
    var cardVisibleState by remember {
        mutableStateOf(false)
    }

    val scaffoldState = rememberScaffoldState()
    val bottomState = rememberModalBottomSheetState()
    var openBottomSheet by remember { mutableStateOf(false) }
    var openImageSheet by rememberSaveable {
        mutableStateOf(false)
    }
    var fileChooserState by rememberSaveable {
        mutableStateOf(false)
    }
    fun showFileChooser(){
        fileChooserState = true
    }
    fun hideFileChooser(){
        fileChooserState = false
    }

    var skipPartiallyExpanded by remember { mutableStateOf(false) }
    var edgeToEdgeEnabled by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val ImageSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val noteBackgroundAnimatable = remember {
        Animatable(
            Color(if (noteColor != -1) noteColor else addVoiceNoteViewModel.noteColor.value)
        )
    }
    val scope = rememberCoroutineScope()
    val doneButtonState = addVoiceNoteViewModel.doneButtonState.collectAsState()
    LaunchedEffect(key1 = true) {
        addVoiceNoteViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddVoiceNoteViewModel.UiEvent.Recording -> {

                }
                is AddVoiceNoteViewModel.UiEvent.StopRecord -> {

                }

                is AddVoiceNoteViewModel.UiEvent.PlayNote -> {
                    playState = true
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
                /*  SmallTopAppBar(
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
                                  )*//*
                                if(result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(NotesEvent.RestoreNote)
                                }*//*
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
                )*/
                EditScreenTopAppBar(
                    titleState = titleState,
                    navController = navController,
                    addVoiceNoteViewModel = addVoiceNoteViewModel,
                    note = note,
                    scope = scope,
                    scaffoldState = scaffoldState,

                    )
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                /*   FloatingActionButton(
                       modifier = Modifier.shadow(
                           0.dp,
                           spotColor = Color(0xFF000000),
                           ambientColor = Color(0xFF000000)
                       ),
                       shape = RoundedCornerShape(size = 100.dp),
                       containerColor = Variables.SchemesOnPrimaryContainer,
                       contentColor = Color(0xFFffffff),
                       onClick = {
                           playState = !playState

                           if (playState && playNoteState) {
                               addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Play(fileNameState.text))
                           } else {
                               addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                           }
                       }
                   )
                   {

                           if (playNoteState) {

                               Icon(
                                   painter = painterResource(if (!playState || mediaState) R.drawable.play_button_24 else R.drawable.pause_button_24),
                                   contentDescription = "Localized description"
                               )
                           }

                   }*/
                val myState by addVoiceNoteViewModel.recordState.collectAsState()
                val timerValue by addVoiceNoteViewModel.timer.collectAsState()
                if (myState) {
                    RecordPanelComponent(
                        onTimerStart = {
                            addVoiceNoteViewModel.startTimer()
                        },
                        timerValue = timerValue,
                        onCancelRecord = {
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                            addVoiceNoteViewModel.stopTimer()
                            addVoiceNoteViewModel.changeRecordState(false)
                        },
                        onDoneClick = {
                            addVoiceNoteViewModel.doneButtonState(true)
                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)
                            addVoiceNoteViewModel.stopTimer()
                            addVoiceNoteViewModel.changeRecordState(false)
                            doneButton.value = true
                        },


                        )
                } else {
                    if (fileNameState.text.isEmpty() || !doneButtonState.value) {
                        FloatingActionButton(
                            onClick = {
                                addVoiceNoteViewModel.onEvent(
                                    AddEditNoteEvent.Recording(
                                        fileNameState.text
                                    )
                                )
                                addVoiceNoteViewModel.changeRecordState(true)
                            },
                            modifier = Modifier.shadow(
                                0.dp,
                                spotColor = Color(0xFF000000),
                                ambientColor = Color(0xFF000000)
                            ),
                            shape = RoundedCornerShape(size = 100.dp),
                            containerColor = Variables.SchemesOnPrimaryContainer,
                            contentColor = Color(0xFFffffff),

                            ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_mic_24),
                                contentDescription = "Record icon"
                            )
                        }
                    }
                }

            },
            bottomBar = {

                Log.d("uris","$uris")
             //   val uriList = fileNameState.imageFileUris?.map { Uri.parse(it) }
                BottomAppPanel(
                    cardVisibleState = {
                        // cardVisibleState = !cardVisibleState
                        openBottomSheet = !openBottomSheet
                    },

                    imageUri = uris,

                 imageSheetVisibleState = {
                    openImageSheet = !openImageSheet
                },
                 tagVisiblestate = { showDialog() })
                /* BottomAppBar(
                     Modifier
                         .wrapContentWidth()
                         .background(Color(colorIntState)),
                     containerColor = Color(colorIntState)
                 )
                 {
                     // Leading icons should typically have a high content alpha
                     CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {

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

                 }*/
            },
            content = { innerPadding ->

                Column(
                    // consume insets as scaffold doesn't do it by default (yet)
                    modifier = Modifier
                        .fillMaxSize()
                        .background(noteBackgroundAnimatable.value)
                        .padding(innerPadding),


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
                        modifier = Modifier.height(if (doneButtonState.value) IntrinsicSize.Min else IntrinsicSize.Max)

                        //
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    val timerValue by addVoiceNoteViewModel.timer2.collectAsState()
                    val playingState by addVoiceNoteViewModel.playingState.collectAsState()
                    val timerValue2 = addVoiceNoteViewModel.getDuration()
                    if (
                        doneButtonState.value
                    ) {
                        PlayRecordPanel(
                            timerValue = timerValue,
                            timerValue2 = timerValue2,
                            onPlay = { addVoiceNoteViewModel.startTimer2() },
                            onCancelRecord = {
                                addVoiceNoteViewModel.stopTimer2()
                            },
                            onRemove = {
                                addVoiceNoteViewModel.doneButtonState(false)

                            },
                            playingState = playingState
                        )
                    }


                }


            }
        )
        // Sheet content
        if (openBottomSheet) {
            val windowInsets = if (edgeToEdgeEnabled)
                WindowInsets(0) else BottomSheetDefaults.windowInsets
            BottomSheet(
                onDismissRequest = { openBottomSheet = false },
                sheetState = bottomSheetState,
                windowInsets = windowInsets,
                onClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            openBottomSheet = false
                        }
                    }
                },
                onImageClick = {
                    navController.navigate("gallery")
                },
                showFileChooser = { showFileChooser() }
            ) {
                navController.navigate("camera")
            }
        }
        // Sheet content
        if (openImageSheet) {
            val windowInsets = if (edgeToEdgeEnabled)
                WindowInsets(0) else BottomSheetDefaults.windowInsets
            if (uris != null) {
                ImageBottomSheet(
                    onDismissRequest = { openImageSheet = false },
                    sheetState = ImageSheetState,
                    windowInsets = windowInsets,
                    onClick = {
                        scope.launch { ImageSheetState.hide() }.invokeOnCompletion {
                            openBottomSheet = true
                            if (!ImageSheetState.isVisible) {
                                openImageSheet = false
                            }

                        }
                    },
                    onImageClick = {

                    },
                    imageUris =uris

                )
            }
        }
        /*
        BottomAppPopUpScreen(
            cardVisible = cardVisibleState,
        )*/
        // A conditional statement to display the dialog
        if (showDialog.value) {
            // Pass the list of tags and the onTagChecked function from the viewmodel to the dialog
            TagDialog(
             addVoiceNoteViewModel = addVoiceNoteViewModel,
                onTagChecked = addVoiceNoteViewModel::onTagChecked,
                onCancel = {

                    hideDialog()

                           },
                onOk = {
                    hideDialog()/* do something with the selected tags */ }
            )
        }
        if (fileChooserState){

            FileChooser(
                onOk ={
                    hideFileChooser()
                    openBottomSheet =false
                },
                saveSelectedUris = addVoiceNoteViewModel::saveSelectedUris,
                fileChooserState = fileChooserState
            )
        }

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
fun AddVoiceNoteScreenPreview() {
    val context = LocalContext.current
    AddVoiceNoteScreen(
        navController = NavController(context),
        noteColor = Color.Blue.toArgb()
    )
}
