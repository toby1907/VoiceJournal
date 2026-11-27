package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.NotePaperStyle
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.Data.model.primaryBodyPreviewText
import com.example.voicejournal.Data.model.primaryRichTitleHtml
import com.example.voicejournal.Data.model.resolvedNoteBlocks
import com.example.voicejournal.Data.model.shareText
import com.example.voicejournal.Data.model.verifyPin
import com.example.voicejournal.R
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.common.richeditor.RichTextEditor
import com.example.voicejournal.ui.common.richeditor.RichTextEditorDefaults
import com.example.voicejournal.ui.common.richeditor.rememberRichTextState
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import com.example.voicejournal.ui.main.AddVoiceNote.components.DrawingCanvasOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.FloatingImageBoxOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.FloatingTextBoxOverlay
import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialog
import com.example.voicejournal.ui.main.AddVoiceNote.components.NotePinDialogMode
import com.example.voicejournal.ui.main.AddVoiceNote.components.PlayRecordPanel
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.main.mainScreen.DynamicDateRow
import com.example.voicejournal.ui.main.snackbar.SnackbarAction
import com.example.voicejournal.ui.main.snackbar.SnackbarController
import com.example.voicejournal.ui.main.snackbar.SnackbarEvent
import com.example.voicejournal.ui.main.voiceJournalPreviewScreen.VoiceJournalPreviewViewModel.FavouriteScreenEvent
import com.example.voicejournal.ui.media_player.MediaPlayerViewModel
import com.example.voicejournal.ui.theme.Variables
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
        SetStatusBarContentColor(false)

        val viewModel: MediaPlayerViewModel = hiltViewModel()
        val lifecycleOwner = LocalLifecycleOwner.current
        val currentOnStopPlay by rememberUpdatedState(
            newValue = {
                viewModel.pause()
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
            viewModel.pause()
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
        val commandsScope = rememberCoroutineScope()

        val audioState by viewModel.state.collectAsStateWithLifecycle()

        Log.d("noteIndexPreviewScreen", "$noteIndex")

        val pagerState = rememberPagerState(
            initialPage = noteIndex.takeIf { it in voiceNotesList.indices } ?: 0,
            pageCount = { voiceNotesList.size }
        )
        val scope = rememberCoroutineScope()
        val unlockedNoteIds = remember { mutableStateListOf<Int>() }
        var isMediaPanelExpanded by remember { mutableStateOf(false) }
        val activePageIndex = pagerState.currentPage.coerceIn(0, (voiceNotesList.size - 1).coerceAtLeast(0))

        val selectedAudioPath = if (
            activePageIndex in voiceNotesList.indices
        ) {
            voiceNotesList[activePageIndex].primaryAudioPath()
        } else {
            ""
        }

        LaunchedEffect(activePageIndex, selectedAudioPath) {
            isMediaPanelExpanded = false
            if (selectedAudioPath.isNotBlank()) {
                viewModel.initUri(Uri.parse(selectedAudioPath))
            } else {
                viewModel.pause()
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

        val toolbarHeight = 52.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Variables.SchemesSurface),
        ) {

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                pageSize = PageSize.Fill,
                userScrollEnabled = false
            ) { index ->
                val currentNote = voiceNotesList[index]
                val isLocked = currentNote.isNoteLocked()
                val isUnlocked = !isLocked || unlockedNoteIds.contains(currentNote.id ?: -1)
                val titleHtml = currentNote.primaryRichTitleHtml(revealLockedContent = isUnlocked)
                val bodyPreview = currentNote.primaryBodyPreviewText(revealLockedContent = isUnlocked)
                val audioPath = currentNote.primaryAudioPath(revealLockedContent = isUnlocked)
                val imageUris = currentNote.imageUrisFromBlocks(revealLockedContent = isUnlocked)
                val pageRichState = remember(currentNote.id, titleHtml) {
                    com.example.voicejournal.ui.common.richeditor.RichTextState().apply {
                        setHtml(titleHtml)
                    }
                }

                val favoriteState = remember(currentNote.id, currentNote.favourite) {
                    mutableStateOf(currentNote.favourite)
                }
                val scrollState = rememberScrollState()

                val hasVoiceRecording = isUnlocked && audioPath.isNotEmpty()
                val loadedAudioState = audioState as? MediaPlayerViewModel.State.Loaded
                val isAudioPlaying = loadedAudioState?.playingState is MediaPlayerViewModel.PlayingState.Playing
                val isAudioPaused = loadedAudioState != null &&
                    loadedAudioState.playingState is MediaPlayerViewModel.PlayingState.Pause &&
                    loadedAudioState.mediaPlayerPosition > 0
                val showVoiceMediaPanel = hasVoiceRecording && isMediaPanelExpanded && index == activePageIndex

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Unified TopBar + Status Bar + expandable Media Panel surface
                    Surface(
                        color = Variables.SchemesPrimary,
                        shape = RoundedCornerShape(
                            bottomStart = if (showVoiceMediaPanel) 20.dp else 0.dp,
                            bottomEnd = if (showVoiceMediaPanel) 20.dp else 0.dp
                        ),
                        shadowElevation = if (showVoiceMediaPanel) 6.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                        ) {
                            TopAppBar(
                                modifier = Modifier
                                    .height(toolbarHeight),
                                elevation = 0.dp,
                                title = {
                                    if (bodyPreview.isNotBlank()) {
                                        Text(
                                            text = bodyPreview,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = Variables.SchemesOnPrimary
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        viewModel.pause()
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
                                        if (hasVoiceRecording) {
                                            IconButton(
                                                onClick = {
                                                    isMediaPanelExpanded = !isMediaPanelExpanded
                                                },
                                                modifier = Modifier.testTag("preview_topbar_voice_indicator")
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(
                                                            color = when {
                                                                isAudioPlaying -> Variables.SchemesPrimaryContainer
                                                                showVoiceMediaPanel -> Variables.SchemesOnPrimary.copy(alpha = 0.18f)
                                                                else -> Color.Transparent
                                                            },
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.GraphicEq,
                                                        contentDescription = if (showVoiceMediaPanel) {
                                                            "Hide Voice Player"
                                                        } else {
                                                            "Show Voice Player"
                                                        },
                                                        tint = if (isAudioPlaying) {
                                                            Variables.SchemesOnPrimaryContainer
                                                        } else {
                                                            Variables.SchemesOnPrimary
                                                        },
                                                        modifier = Modifier.size(21.dp)
                                                    )
                                                    if (isAudioPlaying && !showVoiceMediaPanel) {
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

                                        if (voiceNotesList.isNotEmpty() && index in voiceNotesList.indices) {

                                            IconButton(onClick = {

                                                scope.launch {
                                                    viewModel.pause()
                                                    voiceJournalPreviewViewModel.onEvent(
                                                        FavouriteScreenEvent.DeleteJournal(
                                                            currentNote
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
                                                    tint = Variables.SchemesPrimaryContainer
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
                                                    }

                                                ),
                                                tint = if (favoriteState.value == true) Variables.SchemesPrimaryContainer else Variables.SchemesOnPrimary
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
                                                        viewModel.pause()
                                                        val encodedString =
                                                            Uri.encode(titleHtml)
                                                        Log.d("previewhtmlString", titleHtml)
                                                        navController.navigate(
                                                            Screen.AddEditNoteScreen.route +
                                                                    "?noteId=${currentNote.id}&noteColor=${currentNote.color}&note=${encodedString}"
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

                            AnimatedVisibility(
                                visible = showVoiceMediaPanel,
                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                            ) {
                                PlayRecordPanel(
                                    currentPositionMs = loadedAudioState?.mediaPlayerPosition ?: 0,
                                    durationMs = loadedAudioState?.mediaPlayerDuration ?: 0,
                                    isPlaying = isAudioPlaying,
                                    isPaused = isAudioPaused,
                                    playbackSpeed = loadedAudioState?.playbackSpeed ?: 1f,
                                    onPlayPause = {
                                        commandsScope.launch {
                                            viewModel.togglePlayPause()
                                        }
                                    },
                                    onStop = {
                                        commandsScope.launch {
                                            viewModel.resetToStart()
                                        }
                                    },
                                    onSeekTo = { positionMs ->
                                        viewModel.onSeek(positionMs.toFloat())
                                    },
                                    onSeekBy = { deltaMs ->
                                        viewModel.seekBy(deltaMs)
                                    },
                                    onCycleSpeed = {
                                        viewModel.cyclePlaybackSpeed()
                                    },
                                    onCollapse = {
                                        isMediaPanelExpanded = false
                                    },
                                    showDeleteButton = false,
                                    containerColor = Variables.SchemesPrimary,
                                    contentColor = Variables.SchemesOnPrimary,
                                    accentColor = Variables.SchemesPrimaryContainer,
                                    onAccentColor = Variables.SchemesOnPrimaryContainer
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            Spacer(modifier = Modifier.padding(8.dp))
                            Row {
                                DynamicDateRow(created = currentNote.created)
                            }

                            if (isUnlocked) {
                                val noteBlocks = currentNote.resolvedNoteBlocks()
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
                                        state = pageRichState,
                                        colors = RichTextEditorDefaults.richTextEditorColors(
                                            textColor = Variables.SchemesOnSurface,
                                            containerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                        readOnly = true,
                                        paperStyle = NotePaperStyle.fromId(currentNote.paperStyle),
                                        drawingBlocks = drawingBlocks
                                    )

                                    // Positioned image boxes overlay (read-only)
                                    FloatingImageBoxOverlay(
                                        blocks = noteBlocks,
                                        drawingEnabled = false,
                                        modifier = Modifier.matchParentSize(),
                                        pageCount = pageRichState.pageCount(),
                                        readOnly = true
                                    )

                                    // Floating text boxes overlay
                                    FloatingTextBoxOverlay(
                                        blocks = noteBlocks,
                                        currentPageIndex = pageRichState.currentPageIndex(),
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

                            Spacer(modifier = Modifier.height(72.dp))
                        }

                        if (scrollState.maxValue > 0) {
                            val trackColor = Variables.SchemesOnSurface.copy(alpha = 0.12f)
                            val thumbColor = Variables.SchemesOnSurface.copy(alpha = 0.45f)
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxSize()
                            ) {
                                val barWidth = 5.dp.toPx()
                                val rightMargin = 3.dp.toPx()
                                val topMargin = 8.dp.toPx()
                                val bottomMargin = 64.dp.toPx()
                                val trackTop = topMargin
                                val trackBottom = (size.height - bottomMargin).coerceAtLeast(trackTop + 1f)
                                val trackHeight = (trackBottom - trackTop).coerceAtLeast(1f)
                                val totalContentHeight = (size.height + scrollState.maxValue).coerceAtLeast(1f)
                                val minThumbHeight = 40.dp.toPx().coerceAtMost(trackHeight)
                                val thumbHeight = (trackHeight * (size.height / totalContentHeight)).coerceIn(minThumbHeight, trackHeight)
                                val fraction = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
                                val thumbTop = trackTop + fraction * (trackHeight - thumbHeight)
                                val barLeft = size.width - rightMargin - barWidth
                                val corner = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)

                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(barLeft, trackTop),
                                    size = androidx.compose.ui.geometry.Size(barWidth, trackHeight),
                                    cornerRadius = corner
                                )
                                drawRoundRect(
                                    color = thumbColor,
                                    topLeft = Offset(barLeft, thumbTop),
                                    size = androidx.compose.ui.geometry.Size(barWidth, thumbHeight),
                                    cornerRadius = corner
                                )
                            }
                        }
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
