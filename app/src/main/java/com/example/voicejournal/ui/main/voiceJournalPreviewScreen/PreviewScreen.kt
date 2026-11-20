package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.Data.model.primaryBodyPreviewText
import com.example.voicejournal.Data.model.primaryRichTitleHtml
import com.example.voicejournal.Data.model.shareText
import com.example.voicejournal.Data.model.verifyPin
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialog
import androidx.compose.foundation.layout.heightIn
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingCanvasOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.FloatingTextBoxOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialogMode
import com.example.voicejournal.ui.main.mainScreen.DynamicDateRow
import com.example.voicejournal.ui.main.snackbar.SnackbarAction
import com.example.voicejournal.ui.main.snackbar.SnackbarController
import com.example.voicejournal.ui.main.snackbar.SnackbarEvent
import com.example.voicejournal.ui.main.voiceJournalPreviewScreen.VoiceJournalPreviewViewModel.FavouriteScreenEvent
import com.example.voicejournal.ui.main.voiceJournalPreviewScreen.components.PlayRecordPanel
import com.example.voicejournal.ui.media_player.MediaPlayerViewModel
import com.example.voicejournal.ui.theme.Variables
import com.example.voicejournal.ui.common.richeditor.RichTextEditor
import com.example.voicejournal.ui.common.richeditor.RichTextEditorDefaults
import com.example.voicejournal.ui.common.richeditor.rememberRichTextState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceJournalPreviewScreen(
    voiceJournalPreviewViewModel: VoiceJournalPreviewViewModel = hiltViewModel(),
    navController: NavHostController,
    noteColor: Int,
    noteIndex: Int,


) {
val state by voiceJournalPreviewViewModel.state.collectAsState()
    // Loading indicator
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Error message
    if (state.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error: ${state.error}")
        }
        return
    }
    if (state.notes.isNotEmpty()){
        val commands: Channel<MediaPlayerViewModel.Commands> = Channel()
        PreviewContent(
            voiceJournalPreviewViewModel = voiceJournalPreviewViewModel,
            navController = navController,
            noteColor = noteColor,
            noteIndex = noteIndex,
            commandsFlow =  commands.receiveAsFlow(),
            commands = commands
            )
    }
}

   @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
   @Composable
   fun PreviewContent(
       voiceJournalPreviewViewModel: VoiceJournalPreviewViewModel,
       navController: NavHostController,
       noteColor: Int,
       noteIndex: Int,
       commandsFlow: Flow<MediaPlayerViewModel.Commands>,
       commands: Channel<MediaPlayerViewModel.Commands>,

       ){
        val lifecycleOwner = LocalLifecycleOwner.current
        val currentOnStopPlay by rememberUpdatedState(
            newValue = {
                voiceJournalPreviewViewModel.onEvent(FavouriteScreenEvent.StopPlay)
            }
        )

        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    currentOnStopPlay()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        BackHandler(onBack = {
            voiceJournalPreviewViewModel.onEvent(FavouriteScreenEvent.StopPlay)
            navController.navigate(Screen.VoicesScreen.route)
        })


        val voiceNotes by voiceJournalPreviewViewModel.state.collectAsState()
        val context = LocalContext.current
        val noteState = voiceJournalPreviewViewModel.noteState
        val voiceNotesList: List<VoiceJournal> = voiceNotes.notes
        val fileNameState = voiceJournalPreviewViewModel.noteFileName.value
        val timerValue by voiceJournalPreviewViewModel.timer2.collectAsState()
        val playingState by voiceJournalPreviewViewModel.playingState.collectAsState()
      //  val commands: Channel<MediaPlayerViewModel.Commands> = Channel()
        val commandsScope = rememberCoroutineScope()
      //  val commandsFlow: Flow<MediaPlayerViewModel.Commands> = commands.receiveAsFlow()

        val viewModel: MediaPlayerViewModel = hiltViewModel()


        val audioState by viewModel.state.collectAsStateWithLifecycle()

        Log.d("noteIndexPreviewScreen", "$noteIndex")

        val state = rememberRichTextState()
        val pagerState = rememberPagerState(initialPage = noteIndex.takeIf { it != -1 } ?: 0,
            pageCount = { voiceNotesList.size })
        val scope = rememberCoroutineScope()
        val unlockedNoteIds = remember { mutableStateListOf<Int>() }
        val currentIndex = remember { mutableIntStateOf(0) }
        currentIndex.intValue = noteIndex

        val selectedAudioPath = if (
            currentIndex.intValue in voiceNotesList.indices
        ) {
            voiceNotesList[currentIndex.intValue].primaryAudioPath()
        } else {
            ""
        }

        LaunchedEffect(currentIndex.intValue, pagerState, selectedAudioPath) {
            if (selectedAudioPath.isNotBlank()) {
                viewModel.initUri(Uri.parse(selectedAudioPath))
            }
        }

        LaunchedEffect(selectedAudioPath) {
            viewModel.effect.onEach {
                when (it) {
                    MediaPlayerViewModel.Effect.AudioListened -> {

                    }

                    MediaPlayerViewModel.Effect.FileNotFound -> {

                    }
                }
            }.collect()
        }

        LaunchedEffect(selectedAudioPath) {
            commandsFlow.onEach {
                when (it) {
                    MediaPlayerViewModel.Commands.Pause -> viewModel.pause()
                    MediaPlayerViewModel.Commands.Play -> viewModel.play()
                    MediaPlayerViewModel.Commands.Cancel -> viewModel.resetToStart()
                }
            }.collect()
        }

        val toolbarHeight = 48.dp
        val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }
        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightPx, 0f)
                    return Offset.Zero
                }
            }
        }


        @Stable
        fun <T> spring(
            dampingRatio: Float = Spring.DampingRatioNoBouncy,
            stiffness: Float = Spring.StiffnessHigh,
            visibilityThreshold: T? = null
        ): SpringSpec<T> =
            SpringSpec(dampingRatio, stiffness, visibilityThreshold)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .background(color = Variables.SchemesSurface),
        ) {

            val paddingOffset = with(LocalDensity.current) { toolbarOffsetHeightPx.value.toDp() }
            HorizontalPager(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pagerState,
                pageSize = PageSize.Fill,
                contentPadding = PaddingValues(top = paddingOffset),
                userScrollEnabled = false
                // flingBehavior = PagerDefaults.flingBehavior(state = pagerState, snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) { index ->
                val currentNote = voiceNotesList[index]
                val isLocked = currentNote.isNoteLocked()
                val isUnlocked = !isLocked || unlockedNoteIds.contains(currentNote.id ?: -1)
                val titleHtml = currentNote.primaryRichTitleHtml(revealLockedContent = isUnlocked)
                val bodyPreview = currentNote.primaryBodyPreviewText(revealLockedContent = isUnlocked)
                val audioPath = currentNote.primaryAudioPath(revealLockedContent = isUnlocked)
                val imageUris = currentNote.imageUrisFromBlocks(revealLockedContent = isUnlocked)
                state.setHtml(titleHtml)

                val favoriteState = remember { mutableStateOf(currentNote.favourite) }
                val timerValue2 = voiceJournalPreviewViewModel.getDuration(audioPath)

                currentIndex.intValue = index

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),

                    ) {
                    TopAppBar(
                        modifier = Modifier
                            .height(toolbarHeight)
                        //   .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx.value.roundToInt()) }
                        ,
                        title = {
                            if (bodyPreview.isNotBlank()) {
                                Text(
                                    text = bodyPreview,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                voiceJournalPreviewViewModel.onEvent(FavouriteScreenEvent.StopPlay)
                                navController.navigate(Screen.VoicesScreen.route)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Variables.SchemesOnPrimary
                                )
                            }
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                if (voiceNotesList.isNotEmpty() && currentIndex.intValue in voiceNotesList.indices) {

                                    IconButton(onClick = {

                                        scope.launch {
                                            voiceJournalPreviewViewModel.onEvent(
                                                FavouriteScreenEvent.DeleteJournal(
                                                    voiceNotesList[currentIndex.intValue]
                                                )
                                            )
                                            SnackbarController.sendEvent(
                                                event = SnackbarEvent(
                                                    message = "Note deleted",
                                                    action = SnackbarAction(
                                                        name = "Undo",
                                                        action = {
                                                            voiceJournalPreviewViewModel.onEvent(
                                                                FavouriteScreenEvent.RestoreJournal
                                                            )
                                                        }
                                                    )
                                                )
                                            )

                                        }
                                    })
                                    {

                                        Icon(
                                            painter = painterResource(R.drawable.ic_baseline_delete_forever_24),
                                            contentDescription = "Delete Journal",
                                            tint = Variables.SchemesError
                                        )

                                    }


                                    Icon(
                                        imageVector = if (favoriteState.value == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        modifier = Modifier.toggleable(
                                            value = favoriteState.value ?: false,
                                            onValueChange = { input ->
                                                voiceJournalPreviewViewModel.onChangeFavourite(
                                                    change = input,
                                                    currentNote = currentNote
                                                )
                                                favoriteState.value = input
                                                // voiceJournalPreviewViewModel.getNotes()
                                            }

                                        ),
                                        tint = if (favoriteState.value == true) Variables.SchemesError else Variables.SchemesOnPrimary
                                    )

                                    if (isUnlocked) {
                                        IconButton(
                                            onClick = {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, currentNote.shareText(revealLockedContent = true))
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(shareIntent, "Share note")
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share note",
                                                tint = Variables.SchemesOnPrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val encodedString =
                                                    Uri.encode(titleHtml)
                                                Log.d("previewhtmlString", titleHtml)
                                                navController.navigate(
                                                    Screen.AddEditNoteScreen.route +
                                                            "?noteId=${voiceNotesList[currentIndex.intValue].id}&noteColor=${voiceNotesList[currentIndex.intValue].color}&note=${encodedString}"
                                                )
                                            })
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Variables.SchemesOnPrimary,
                                            )
                                        }
                                    }

                                }
                            }
                        },
                        backgroundColor = Variables.SchemesPrimary,
                        contentColor = Variables.SchemesOnPrimary
                    )

                    val filteredList = imageUris.filter { it.isNotEmpty() }

                    if (filteredList.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(filteredList.first()),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                        )
                    }


                    Spacer(modifier = Modifier.padding(8.dp))
                    Row {
                        DynamicDateRow(created = voiceNotesList[index].created)
                        if (audioPath.isNotEmpty()) {

                            viewModel.initUri(Uri.parse(audioPath))
                            when (audioState) {
                                is MediaPlayerViewModel.State.Loaded -> PlayRecordPanel(
                                    onPlay = {
                                        commandsScope.launch {
                                    //        commands.send(MediaPlayerViewModel.Commands.Play)
                                            viewModel.play()
                                        }
                                        // viewModel.play()
                                    },
                                    onCancelRecord = {
                                        commandsScope.launch {
                                        //    commands.send(MediaPlayerViewModel.Commands.Cancel)
                                            viewModel.pause()
                                        }
                                    },
                                    onPaused = {
                                        commandsScope.launch {
                                         //   commands.send(MediaPlayerViewModel.Commands.Pause)
                                            viewModel.pause()
                                        }
                                    },
                                    state = audioState as MediaPlayerViewModel.State.Loaded
                                )

                                MediaPlayerViewModel.State.FileNotFound -> {}
                                MediaPlayerViewModel.State.None -> {}
                                MediaPlayerViewModel.State.Preparing -> {}
                            }
                        }
                    }

                    if (isUnlocked) {
                        val noteBlocks = currentNote.noteBlocks.orEmpty()
                        val drawingBlocks = noteBlocks.filter {
                            it.type == NoteBlockType.DRAWING.name
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 400.dp)
                        ) {
                            RichTextEditor(
                                modifier = Modifier.fillMaxWidth(),
                                state = state,
                                colors = RichTextEditorDefaults.richTextEditorColors(
                                    textColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                readOnly = true,
                                drawingBlocks = drawingBlocks
                            )

                            // Floating text boxes overlay
                            FloatingTextBoxOverlay(
                                blocks = noteBlocks,
                                currentPageIndex = state.currentPageIndex(),
                                drawingEnabled = false,
                                onPositionChange = { _, _, _, _, _, _ -> },
                                onResize = { _, _, _ -> },
                                onTextChange = { _, _ -> },
                                onDelete = { _ -> }
                            )
                        }
                    } else {
                        Text(
                            text = "This note is locked. Enter the PIN to view or edit it.",
                            modifier = Modifier.padding(16.dp),
                            color = Variables.SchemesOnSurface
                        )
                        NotePinDialog(
                            mode = NotePinDialogMode.UNLOCK,
                            onDismiss = { navController.navigate(Screen.VoicesScreen.route) },
                            onConfirmUnlock = { pin ->
                                val unlocked = currentNote.verifyPin(pin)
                                if (unlocked) {
                                    currentNote.id?.let { unlockedNoteIds.add(it) }
                                }
                                unlocked
                            }
                        )
                    }
                }


            }


            Box(
                modifier = Modifier
                    .offset(y = -(16).dp)
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(100))
                    .background(Color.Transparent)
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                IconButton(
                    onClick = {

                        scope.launch {
                            pagerState.scrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Go back",
                        tint = Variables.SchemesOnSurface
                    )
                }
                IconButton(
                    onClick = {

                        scope.launch {
                            pagerState.scrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go forward",
                        tint = Variables.SchemesOnSurface
                    )
                }
            }
        }

}
