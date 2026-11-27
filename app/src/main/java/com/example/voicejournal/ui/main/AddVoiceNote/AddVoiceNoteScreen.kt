package com.example.voicejournal.ui.main.AddVoiceNote



import android.app.Activity

import android.content.Intent

import android.net.Uri

import android.speech.RecognizerIntent

import android.util.Log

import android.widget.DatePicker

import androidx.activity.compose.BackHandler

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.zIndex

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.interaction.MutableInteractionSource

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.IntrinsicSize

import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.imePadding

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape

import com.example.voicejournal.ui.main.AddVoiceNote.components.FloatingImageBoxOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.FloatingTextBoxOverlay

import androidx.compose.material3.BottomSheetDefaults

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.FabPosition

import androidx.compose.material3.FloatingActionButton

import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold

import androidx.compose.material3.SnackbarHost

import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Surface

import androidx.compose.material3.Text

import androidx.compose.material3.rememberModalBottomSheetState

import androidx.compose.runtime.Composable

import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.LaunchedEffect

import androidx.compose.runtime.collectAsState

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.rememberUpdatedState

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.shadow

import androidx.compose.ui.focus.FocusRequester

import androidx.compose.ui.focus.focusRequester

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.toArgb

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.platform.LocalLifecycleOwner

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.ParagraphStyle

import androidx.compose.ui.text.SpanStyle

import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.font.FontStyle

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.style.TextDecoration

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.Lifecycle

import androidx.lifecycle.LifecycleEventObserver

import androidx.lifecycle.LifecycleOwner

import androidx.navigation.NavController

import com.example.voicejournal.R

import com.example.voicejournal.Screen

import com.example.voicejournal.Data.model.isNoteLocked

import com.example.voicejournal.ui.main.AddVoiceNote.components.AlignStyleBottomSheet
import com.example.voicejournal.ui.main.AddVoiceNote.components.TextColorBottomSheet

import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomAppPanel

import com.example.voicejournal.ui.main.AddVoiceNote.components.BottomSheet

import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool

import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasNoteBoard

import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingCanvasOverlay

import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingLayerControls

import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingToolbar

import com.example.voicejournal.ui.main.AddVoiceNote.components.EditScreenTopAppBar

import com.example.voicejournal.ui.main.AddVoiceNote.components.FileChooser

import com.example.voicejournal.ui.main.AddVoiceNote.components.ImageBottomSheet

import com.example.voicejournal.ui.main.AddVoiceNote.components.MoreOptionsBottomSheet

import com.example.voicejournal.ui.main.AddVoiceNote.components.NoteBlockEditorPanel

import com.example.voicejournal.ui.main.AddVoiceNote.components.NoteFolderDialog

import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialog

import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialogMode

import com.example.voicejournal.ui.main.AddVoiceNote.components.NoteTemplateDialog

import com.example.voicejournal.ui.main.AddVoiceNote.components.PlayRecordPanel

import com.example.voicejournal.ui.main.AddVoiceNote.components.RecordPanelComponent

import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor

import com.example.voicejournal.ui.main.AddVoiceNote.components.TagDialog

import com.example.voicejournal.ui.main.AddVoiceNote.components.TransparentHintTextField

import com.example.voicejournal.ui.main.snackbar.SnackbarController

import com.example.voicejournal.ui.main.snackbar.SnackbarEvent

import com.example.voicejournal.ui.theme.Variables

import com.example.voicejournal.Data.model.NoteBlockType

import com.example.voicejournal.ui.common.richeditor.RichTextEditor

import com.example.voicejournal.ui.common.richeditor.RichTextEditorDefaults

import com.example.voicejournal.ui.common.richeditor.rememberRichTextState

import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch

import java.util.Calendar

import java.util.Date

import java.util.Locale



@OptIn(

    ExperimentalMaterial3Api::class

)

@Composable

fun AddVoiceNoteScreen(

    navController: NavController,

    noteColor: Int,

    addVoiceNoteViewModel: AddVoiceNoteViewModel = hiltViewModel(),

    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,

    note: String,

    snackbarHostState: SnackbarHostState

) {

    val state = rememberRichTextState()

    val scope = rememberCoroutineScope()

    val speechRecognizerLauncher = rememberLauncherForActivityResult(

        contract = ActivityResultContracts.StartActivityForResult()

    ) { result ->

        if (result.resultCode != Activity.RESULT_OK) {

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("Speech transcription canceled"))

            return@rememberLauncherForActivityResult

        }

        val transcript = result.data

            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

            ?.firstOrNull()

            .orEmpty()

        if (transcript.isBlank()) {

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("No speech captured to transcribe"))

            return@rememberLauncherForActivityResult

        }

        addVoiceNoteViewModel.applyTranscriptToCurrentAudio(transcript)

    }



   LaunchedEffect(Unit) {

      // val titleState = addVoiceNoteViewModel.noteTitle.value.text

       state.setHtml(note)

      addVoiceNoteViewModel.refreshBlocksFromCurrentNote(state.toHtml())

      Log.d("htmlString", note)

   }



   /* LaunchedEffect(state.annotatedString) {



        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredTitle(state.toHtml()))



    }*/









    val context = LocalContext.current



    fun startSpeechTranscription() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {

            putExtra(

                RecognizerIntent.EXTRA_LANGUAGE_MODEL,

                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM

            )

            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)

        }

        try {

            speechRecognizerLauncher.launch(intent)

        } catch (e: Exception) {

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("Speech recognition is unavailable on this device"))

        }

    }

    //Date data

    val year: Int

    val month: Int

    val day: Int



    val calendar = Calendar.getInstance()

    year = calendar.get(Calendar.YEAR)

    month = calendar.get(Calendar.MONTH)

    day = calendar.get(Calendar.DAY_OF_MONTH)

    calendar.time = Date()

    val date = remember {

        mutableStateOf("$day/$month/$year")

    }





    val datePickerDialog = android.app.DatePickerDialog(

        context, { _: DatePicker, yearr: Int, monthh: Int, dayOfMonth: Int ->

            val selectedDateTime = Calendar.getInstance().apply {

                set(yearr, monthh, dayOfMonth)

            }.timeInMillis

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredDate(selectedDateTime))





        }, year, month, day

    )

    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnStopPlay by rememberUpdatedState(

        newValue = {

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)

        }

    )



    val currentOnSaveNote by rememberUpdatedState(

        newValue = {

            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNoteOnly(state.toHtml()))

        }

    )



    val onSave = remember { mutableStateOf(false) }

    val lifecycle = lifecycleOwner.lifecycle

    LaunchedEffect(state.currentPageIndex()) {
        addVoiceNoteViewModel.setActivePageIndex(state.currentPageIndex())
    }

    DisposableEffect(lifecycle) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_PAUSE) {

                if (!onSave.value && (state.annotatedString.text.trim().isNotEmpty() || addVoiceNoteViewModel.hasPersistableAttachments())) { currentOnSaveNote() }

                currentOnStopPlay()

            }      }

        lifecycle.addObserver(observer)

        onDispose {

            lifecycle.removeObserver(observer)

        }



    }

    //record panel state

    val myState by addVoiceNoteViewModel.recordState.collectAsState()

    val timerValue by addVoiceNoteViewModel.timer.collectAsState()



    //auto focus

    val focusRequester = remember {

        FocusRequester()

    }

    //uris

    val uris = addVoiceNoteViewModel.tempImageUris.value.imageFileUris?.filter { it.isNotEmpty() }

        ?.map { Uri.parse(it) }





    BackHandler(onBack = {

        addVoiceNoteViewModel.removeSelectedImageUris()

        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)

        navController.popBackStack()

    })



    SetStatusBarContentColor(true)



    val contentState = addVoiceNoteViewModel.noteContent.value

    val fileNameState = addVoiceNoteViewModel.noteFileName.value

    val voicePlaybackState by addVoiceNoteViewModel.voicePlaybackState.collectAsState()

    val hasVoiceRecording = fileNameState.text.isNotBlank() && !myState

    var isMediaPanelExpanded by rememberSaveable { mutableStateOf(false) }

    val noteBlocks = addVoiceNoteViewModel.noteBlocks

    val folderName = addVoiceNoteViewModel.folderName.value

    val paperStyle = addVoiceNoteViewModel.paperStyle.value

    val playingBlockAudioUri = addVoiceNoteViewModel.currentBlockPreviewAudioUri.value

    var drawingEnabled by rememberSaveable { mutableStateOf(false) }

    var drawingTool by rememberSaveable { mutableStateOf(CanvasDrawingTool.PEN.name) }

    var drawingColorArgb by rememberSaveable { mutableStateOf(0xFF1A73E8L) }

    var drawingStrokeWidth by rememberSaveable { mutableStateOf(4f) }

    var drawingAlpha by rememberSaveable { mutableStateOf(1f) }

    val loadedNote = addVoiceNoteViewModel.noteState.value.voiceJournal

    val isNoteLocked = addVoiceNoteViewModel.noteLocked.value

    val requiresUnlockForEditing = loadedNote?.isNoteLocked() == true

    var lockDialogMode by rememberSaveable(loadedNote?.id) { mutableStateOf<NotePinDialogMode?>(null) }

    var folderDialogVisible by rememberSaveable(loadedNote?.id) { mutableStateOf(false) }

    var templateDialogVisible by rememberSaveable(loadedNote?.id) { mutableStateOf(false) }

    var unlockForEditDialogVisible by rememberSaveable(loadedNote?.id) { mutableStateOf(requiresUnlockForEditing) }

    var isUnlockedForEditing by rememberSaveable(loadedNote?.id) { mutableStateOf(!requiresUnlockForEditing) }



    LaunchedEffect(requiresUnlockForEditing) {

        if (!requiresUnlockForEditing) {

            isUnlockedForEditing = true

            unlockForEditDialogVisible = false

        } else if (!isUnlockedForEditing) {

            unlockForEditDialogVisible = true

        }

    }



    //  Log.d("SetText1", titleState.text)



    val contentSaved = remember { mutableStateOf(false) }

    // val selectedImageUris = galleryScreenViewModel.selectedUris.collectAsState(initial = emptySet())



    /* LaunchedEffect( Unit) {



         state.setHtml(titleState.text)

         Log.d("SetText2", titleState.text)



     }*/









    val isImportant = remember { mutableStateOf(false) }





    val isImportants = remember { mutableStateOf(false) }



    val textStyle = if (addVoiceNoteViewModel.noteContent.value.text?.isEmpty() == true) {

        TextStyle(

            fontSize = 16.sp,

            color = Color.Gray,

            textAlign = TextAlign.Center,

            fontStyle = FontStyle.Italic

        )

    } else {

        TextStyle(

            fontSize = 24.sp,

            fontWeight = FontWeight.Bold,

            textAlign = TextAlign.Center

        )

    }





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





    var openBottomSheet by remember { mutableStateOf(false) }

    var openImageSheet by rememberSaveable {

        mutableStateOf(false)

    }

    var openAlignStyleBottomSheet by remember { mutableStateOf(false) }

    var openTextColorBottomSheet by remember { mutableStateOf(false) }

    var openMoreOptionsSheet by rememberSaveable { mutableStateOf(false) }

    var fileChooserState by rememberSaveable {

        mutableStateOf(false)

    }



    fun showFileChooser() {

        fileChooserState = true

    }



    fun hideFileChooser() {

        fileChooserState = false

    }



    val skipPartiallyExpanded by remember { mutableStateOf(false) }

    val edgeToEdgeEnabled by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(

        skipPartiallyExpanded = skipPartiallyExpanded

    )

    val ImageSheetState = rememberModalBottomSheetState(

        skipPartiallyExpanded = skipPartiallyExpanded

    )

    val alignStylebottomSheetState = rememberModalBottomSheetState(

        skipPartiallyExpanded = skipPartiallyExpanded

    )

    val textColorSheetState = rememberModalBottomSheetState(

        skipPartiallyExpanded = true

    )

    val moreOptionsSheetState = rememberModalBottomSheetState(

        skipPartiallyExpanded = skipPartiallyExpanded

    )



    val noteBackgroundAnimatable = remember {

        Animatable(

            Color(if (noteColor != -1) noteColor else addVoiceNoteViewModel.noteColor.value)

        )

    }



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

                    onSave.value = true

                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.VoicesScreen.route)
                    }

                    scope.launch  {

                        snackbarHostState.showSnackbar(

                            message = "Journal Saved"

                        )

                    }

                }



                is AddVoiceNoteViewModel.UiEvent.ShowSnackbar -> {

                   scope.launch {

                       SnackbarController.sendEvent(

                           event = SnackbarEvent(

                               message = event.message

                           )

                       )



                    }

                }

            }

        }

    }



    Surface(

        color = MaterialTheme.colorScheme.primary

    ) {

        Scaffold(modifier = Modifier,

            topBar = {



                EditScreenTopAppBar(

                    navController = navController,

                    addVoiceNoteViewModel = addVoiceNoteViewModel,

                    note = note,

                    canApplyTemplate = !requiresUnlockForEditing || isUnlockedForEditing,

                    onTemplateClick = {

                        if (requiresUnlockForEditing && !isUnlockedForEditing) {

                            unlockForEditDialogVisible = true

                        } else {

                            templateDialogVisible = true

                        }

                    },

                    hasFolder = folderName.isNotBlank(),

                    onFolderClick = { folderDialogVisible = true },

                    isLocked = isNoteLocked,

                    onLockClick = {

                        if (isNoteLocked && !isUnlockedForEditing) {

                            unlockForEditDialogVisible = true

                        } else {

                            lockDialogMode = if (isNoteLocked) {

                                NotePinDialogMode.CHANGE

                            } else {

                                NotePinDialogMode.CREATE

                            }

                        }

                    },

                    hasVoiceRecording = hasVoiceRecording,

                    isMediaPanelVisible = isMediaPanelExpanded,

                    isVoicePlaying = voicePlaybackState.isPlaying,

                    onVoiceIndicatorClick = {

                        isMediaPanelExpanded = !isMediaPanelExpanded

                    },

                    scope = scope,

                    snackbarHostState = snackbarHostState,

                    nav = {

                        if (addVoiceNoteViewModel.noteContent.value.text?.isNotEmpty() == true) {

                            //    navController.navigate(Screen.VoicesScreen.route)

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)

                        } else {

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.Error("Kindly Enter a title be for you save"))

                        }

                    },

                    datePickerDialog = {

                        datePickerDialog.show()

                    },

                    contentSaved = {

                        contentSaved.value = true
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopPlay)
                        addVoiceNoteViewModel.onEvent(AddEditNoteEvent.SaveNote(state.toHtml()))

                    },

                    state = state

                )

            },

            floatingActionButtonPosition = FabPosition.Center,

            floatingActionButton = {
                Box {





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

                            addVoiceNoteViewModel.onCancelRecord()

                            isMediaPanelExpanded = false

                        },

                        onDoneClick = {

                            addVoiceNoteViewModel.doneButtonState(true)

                            addVoiceNoteViewModel.onEvent(AddEditNoteEvent.StopRecording)

                            addVoiceNoteViewModel.stopTimer()

                            addVoiceNoteViewModel.changeRecordState(false)

                            doneButton.value = true

                            isMediaPanelExpanded = true

                        },





                        )

                } else {

                    if (!drawingEnabled && fileNameState.text.isEmpty()) {

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

                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,

                            contentColor = Color(0xFFffffff),



                            ) {

                            Icon(

                                painter = painterResource(id = R.drawable.baseline_mic_24),

                                contentDescription = "Record icon"

                            )

                        }

                    }

                }



                }
            },

            bottomBar = {
                Box(modifier = Modifier.imePadding()) {

                if (drawingEnabled) {

                    DrawingToolbar(

                        selectedTool = CanvasDrawingTool.valueOf(drawingTool),

                        selectedColorArgb = drawingColorArgb,

                        selectedStrokeWidth = drawingStrokeWidth,

                        onToolSelected = { tool ->

                            drawingTool = tool.name

                            if (tool == CanvasDrawingTool.HIGHLIGHTER && drawingColorArgb == 0xFF1A73E8L) {

                                drawingColorArgb = 0xFFFFD54FL

                                drawingAlpha = 0.5f

                            } else if (tool == CanvasDrawingTool.PEN) {

                                drawingAlpha = 1f

                            }

                        },

                        onColorSelected = { drawingColorArgb = it },

                        onStrokeWidthSelected = { drawingStrokeWidth = it },

                        onUndoStroke = {

                            addVoiceNoteViewModel.eraseLastDrawingStroke(state.currentPageIndex())

                        },

                        onDoneClick = { drawingEnabled = false }

                    )

                } else {

                    BottomAppPanel(

                        cardVisibleState = {

                            openBottomSheet = !openBottomSheet

                        },

                        imageUri = uris,

                        imageSheetVisibleState = {

                            openImageSheet = !openImageSheet

                        },

                        tagVisiblestate = { showDialog() },

                        onBoldClick = {

                            state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

                        },

                        onItalicClick = {

                            state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))

                        },

                        onStrikethroughClick = {

                            state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))

                        },

                        onUnderlineClick = {

                            state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))

                        },

                        onTextColorClick = {

                            openTextColorBottomSheet = true

                        },

                        onChecklistClick = {

                            state.toggleChecklistLine()

                        },

                        onAddTextBoxClick = {

                            addVoiceNoteViewModel.addFloatingTextBox(state.currentPageIndex(), "")

                        },

                        state = state,

                        alignStyleSheetVisiblity = {

                            openAlignStyleBottomSheet = !openAlignStyleBottomSheet

                        },

                        drawingEnabled = drawingEnabled,

                        onToggleDrawing = { drawingEnabled = !drawingEnabled },

                        onMoreOptionsClick = { openMoreOptionsSheet = true }

                    )

                }

                }
            },

            snackbarHost = { SnackbarHost(snackbarHostState) }



        ) { innerPadding ->



            Column(

                // consume insets as scaffold doesn't do it by default (yet)

                modifier = Modifier

                    .fillMaxSize()

                    .background(noteBackgroundAnimatable.value)

                    .padding(top = innerPadding.calculateTopPadding())

                    .imePadding(),

                ) {



                Spacer(modifier = Modifier.height(16.dp))



                if (requiresUnlockForEditing && !isUnlockedForEditing) {

                    Text(

                        text = "This note is locked. Enter the PIN to edit or change its content.",

                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),

                        color = Variables.SchemesOnSurface

                    )

                } else {

                    Box(

                        modifier = Modifier

                            .fillMaxWidth()

                            .weight(1f)

                    ) {

                        var editorScrollY by remember { androidx.compose.runtime.mutableIntStateOf(0) }



                        RichTextEditor(

                            placeholder = { Text("Enter your content here!") },

                            modifier = Modifier.fillMaxSize(),

                            state = state,

                            paperStyle = paperStyle,

                            colors = RichTextEditorDefaults.richTextEditorColors(

                                containerColor = Color.Transparent,

                                focusedIndicatorColor = Color.Transparent,

                                unfocusedIndicatorColor = Color.Transparent,

                                textColor = Variables.SchemesOnSurface

                            ),

                            drawingBlocks = noteBlocks.filter { it.type == NoteBlockType.DRAWING.name },

                            drawingEnabled = drawingEnabled,

                            drawingTool = CanvasDrawingTool.valueOf(drawingTool),

                            drawingColorArgb = drawingColorArgb,

                            drawingStrokeWidthDp = drawingStrokeWidth,

                            drawingAlpha = drawingAlpha,

                            onStrokeDrawn = { pageIndex, normalizedPoints, colorArgb, strokeWidthPx, alpha, tool ->

                                addVoiceNoteViewModel.addDrawingStroke(

                                    pageIndex = pageIndex,

                                    normalizedPoints = normalizedPoints,

                                    colorArgb = colorArgb,

                                    strokeWidthPx = strokeWidthPx,

                                    alpha = alpha,

                                    tool = tool

                                )

                            },

                            onEraseStroke = { pageIndex, normalizedX, normalizedY, normalizedRadius ->

                                addVoiceNoteViewModel.eraseDrawingStrokeAtPoint(

                                    pageIndex = pageIndex,

                                    normalizedX = normalizedX,

                                    normalizedY = normalizedY,

                                    normalizedRadius = normalizedRadius

                                )

                            },

                            onScrollChanged = { _, scrollY ->

                                editorScrollY = scrollY

                            }

                        )



                        // Floating Image Boxes Overlay (draggable, resizable, removable)

                        val blocksSnapshot = noteBlocks.toList()

                        FloatingImageBoxOverlay(

                            blocks = blocksSnapshot,

                            drawingEnabled = drawingEnabled,

                            modifier = Modifier.matchParentSize(),

                            scrollY = editorScrollY,

                            pageCount = state.pageCount(),

                            onPositionChange = { blockId, pageIndex, x, y, width, height ->

                                addVoiceNoteViewModel.updateBlockCanvasLayout(blockId, pageIndex, x, y, width, height)

                            },

                            onResize = { blockId, width, height ->

                                addVoiceNoteViewModel.resizeBlockCanvas(blockId, width, height)

                            },

                            onBringToFront = { blockId ->

                                addVoiceNoteViewModel.bringBlockToFront(blockId)

                            },

                            onDelete = { blockId ->

                                addVoiceNoteViewModel.removeBlock(blockId)

                            }

                        )

                        // Floating Text Boxes Overlay (Samsung Notes style)

                        FloatingTextBoxOverlay(

                            blocks = blocksSnapshot,

                            currentPageIndex = state.currentPageIndex(),

                            drawingEnabled = drawingEnabled,

                            scrollY = editorScrollY,

                            onPositionChange = { blockId, pageIndex, x, y, width, height ->

                                addVoiceNoteViewModel.updateBlockCanvasLayout(blockId, pageIndex, x, y, width, height)

                            },

                            onResize = { blockId, width, height ->

                                addVoiceNoteViewModel.resizeBlockCanvas(blockId, width, height)

                            },

                            onTextChange = { blockId, newText ->

                                addVoiceNoteViewModel.updateBlockText(blockId, newText)

                            },

                            onDelete = { blockId ->

                                addVoiceNoteViewModel.removeBlock(blockId)

                            }

                        )



                        // Floating Voice Media Panel on top of the note canvas

                        androidx.compose.animation.AnimatedVisibility(
                            visible = hasVoiceRecording && isMediaPanelExpanded,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(10f)
                        ) {
                            PlayRecordPanel(
                                currentPositionMs = voicePlaybackState.currentPositionMs,
                                durationMs = voicePlaybackState.durationMs,
                                isPlaying = voicePlaybackState.isPlaying,
                                isPaused = voicePlaybackState.isPaused,
                                playbackSpeed = voicePlaybackState.playbackSpeed,
                                onPlayPause = {
                                    addVoiceNoteViewModel.togglePlayPauseRecording()
                                },
                                onStop = {
                                    addVoiceNoteViewModel.stopRecordingPlayback()
                                },
                                onSeekTo = { positionMs ->
                                    addVoiceNoteViewModel.seekRecordingTo(positionMs)
                                },
                                onSeekBy = { deltaMs ->
                                    addVoiceNoteViewModel.seekRecordingBy(deltaMs)
                                },
                                onCycleSpeed = {
                                    addVoiceNoteViewModel.cyclePlaybackSpeed()
                                },
                                onRemove = {
                                    isMediaPanelExpanded = false
                                    addVoiceNoteViewModel.removeVoiceRecording()
                                },
                                onCollapse = {
                                    isMediaPanelExpanded = false
                                }
                            )
                        }

                        // Page indicator badge (e.g. "1 / 2")

                        if (state.pageCount() > 1) {

                            Surface(

                                modifier = Modifier

                                    .align(Alignment.TopEnd)

                                    .padding(end = 16.dp, top = 8.dp),

                                color = Variables.SchemesPrimaryContainer.copy(alpha = 0.85f),

                                shape = RoundedCornerShape(12.dp),

                                shadowElevation = 2.dp

                            ) {

                                Text(

                                    text = "${state.currentPageIndex() + 1} / ${state.pageCount()}",

                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),

                                    style = MaterialTheme.typography.labelSmall,

                                    fontWeight = FontWeight.SemiBold,

                                    color = Variables.SchemesOnPrimaryContainer

                                )

                            }

                        }

                    }

                }

            }

        }

        // Sheet content

        if (folderDialogVisible) {

            NoteFolderDialog(

                initialFolder = folderName,

                onDismiss = { folderDialogVisible = false },

                onSave = { addVoiceNoteViewModel.setFolderName(it) },

                onClear = { addVoiceNoteViewModel.setFolderName("") }

            )

        }

        if (templateDialogVisible) {

            NoteTemplateDialog(

                selectedPaperStyle = paperStyle,

                onSelectPaperStyle = { addVoiceNoteViewModel.setPaperStyle(it) },

                onDismiss = { templateDialogVisible = false },

                onApply = { template ->

                    state.setHtml(template.titleHtml)

                    addVoiceNoteViewModel.onEvent(AddEditNoteEvent.EnteredContent(template.body))

                    template.folder?.let { addVoiceNoteViewModel.setFolderName(it) }

                    templateDialogVisible = false

                }

            )

        }

        lockDialogMode?.let { mode ->

            NotePinDialog(

                mode = mode,

                onDismiss = { lockDialogMode = null },

                onConfirmCreate = { pin ->

                    addVoiceNoteViewModel.setNotePin(pin)

                },

                onConfirmUnlock = { pin ->

                    addVoiceNoteViewModel.verifyNotePin(pin)

                },

                onConfirmChange = { currentPin, newPin ->

                    addVoiceNoteViewModel.changeNotePin(currentPin, newPin)

                },

                onRemoveLock = if (isNoteLocked) {

                    { addVoiceNoteViewModel.clearNotePin() }

                } else {

                    null

                }

            )

        }

        if (unlockForEditDialogVisible && requiresUnlockForEditing) {

            NotePinDialog(

                mode = NotePinDialogMode.UNLOCK,

                onDismiss = {

                    unlockForEditDialogVisible = false

                    navController.navigateUp()

                },

                onConfirmUnlock = { pin ->

                    val unlocked = addVoiceNoteViewModel.verifyNotePin(pin)

                    if (unlocked) {

                        isUnlockedForEditing = true

                        unlockForEditDialogVisible = false

                    }

                    unlocked

                }

            )

        }

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
                    openBottomSheet = false
                    navController.navigate(
                        "gallery?noteId=${note.voiceJournal?.id ?: -1}&noteColor=${noteColor}&note="
                    )
                },

                showFileChooser = {

                    showFileChooser()

                },

                onCameraClick = {

                    navController.navigate("camera")

                }

            )



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

                        navController.navigate("preview")

                    },

                    imageUris = uris

                )

            }

        }

        //Align style sheet

        if (openAlignStyleBottomSheet) {

            val windowInsets = if (edgeToEdgeEnabled)

                WindowInsets(0) else BottomSheetDefaults.windowInsets



            AlignStyleBottomSheet(

                onDismissRequest = { openAlignStyleBottomSheet = false },

                sheetState = alignStylebottomSheetState,

                windowInsets = windowInsets,

                onClick = {

                    scope.launch { alignStylebottomSheetState.hide() }.invokeOnCompletion {



                    }

                },

                state = state,

                onStartAlignClick = {

                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))

                },

                onEndAlignClick = {

                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End))

                },

                onCenterAlignClick = {

                    state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

                },

                onOrderedListClick = { state.toggleOrderedList() },

                onUnorderedListClick = { state.toggleUnorderedList() }

            )



        }

        if (openTextColorBottomSheet) {

            TextColorBottomSheet(

                onDismissRequest = { openTextColorBottomSheet = false },

                sheetState = textColorSheetState,

                selectedColor = state.currentSpanStyle.color,

                onColorSelected = { chosenColor ->

                    state.setTextColor(chosenColor)

                    openTextColorBottomSheet = false

                }

            )

        }



        if (openMoreOptionsSheet) {

            MoreOptionsBottomSheet(

                onDismissRequest = { openMoreOptionsSheet = false },

                sheetState = moreOptionsSheetState,

                currentPageIndex = state.currentPageIndex(),

                pageCount = state.pageCount(),

                onPreviousPageClick = { state.goToPreviousPage() },

                onNextPageClick = { state.goToNextPage() },

                onPageBreakClick = {

                    state.insertPageBreak()

                    openMoreOptionsSheet = false

                },

                onRemovePageBreakClick = {

                    state.removeCurrentPageBreak()

                },

                canRemovePageBreak = state.pageBreakCount() > 0,

                selectedPaperStyle = paperStyle,

                onSelectPaperStyle = { addVoiceNoteViewModel.setPaperStyle(it) },

                onTemplateClick = {

                    openMoreOptionsSheet = false

                    if (requiresUnlockForEditing && !isUnlockedForEditing) {

                        unlockForEditDialogVisible = true

                    } else {

                        templateDialogVisible = true

                    }

                },

                onFolderClick = {

                    openMoreOptionsSheet = false

                    folderDialogVisible = true

                },

                folderName = folderName,

                onLockClick = {

                    openMoreOptionsSheet = false

                    if (isNoteLocked && !isUnlockedForEditing) {

                        unlockForEditDialogVisible = true

                    } else {

                        lockDialogMode = if (isNoteLocked) {

                            NotePinDialogMode.CHANGE

                        } else {

                            NotePinDialogMode.CREATE

                        }

                    }

                },

                isLocked = isNoteLocked,

                onChecklistClick = {

                    addVoiceNoteViewModel.addChecklistBlock()

                    openMoreOptionsSheet = false

                },

                onTranscribeClick = {

                    openMoreOptionsSheet = false

                    startSpeechTranscription()

                },

                onSyncBlocksClick = {

                    addVoiceNoteViewModel.refreshBlocksFromCurrentNote(state.toHtml())

                    openMoreOptionsSheet = false

                }

            )

        }



        if (showDialog.value) {



            TagDialog(

                addVoiceNoteViewModel = addVoiceNoteViewModel,

                onTagChecked = addVoiceNoteViewModel::onTagChecked,

                onCancel = {



                    hideDialog()



                },

                onOk = {

                    hideDialog()/* do something with the selected tags */

                }

            )

        }

        if (fileChooserState) {



            FileChooser(

                onOk = {

                    hideFileChooser()

                    openBottomSheet = false

                },

                saveSelectedUris = addVoiceNoteViewModel::saveSelectedUris,

                fileChooserState = fileChooserState

            )

        }



    }



}

