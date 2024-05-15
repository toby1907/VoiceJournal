package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.mainScreen.DynamicDateRow
import com.example.voicejournal.ui.main.voiceJournalPreviewScreen.components.PlayRecordPanel
import com.example.voicejournal.ui.theme.Variables
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceJournalPreviewScreen(
    voiceJournalPreviewViewModel: VoiceJournalPreviewViewModel = hiltViewModel(),
    navController: NavHostController,
    noteColor: Int,
    noteIndex: Int,
) {
    BackHandler(onBack = {
        navController.navigate(Screen.VoicesScreen.route)
    })

    val voiceNotes by voiceJournalPreviewViewModel.state.collectAsState()
    val noteState = voiceJournalPreviewViewModel.noteState
    val voiceNotesList: List<VoiceJournal> = voiceNotes.notes
    val fileNameState = voiceJournalPreviewViewModel.noteFileName.value
    val timerValue by voiceJournalPreviewViewModel.timer2.collectAsState()
    val playingState by voiceJournalPreviewViewModel.playingState.collectAsState()

    // val favoriteState = remember { mutableStateOf(false) }

    Log.d("noteIndexPreviewScreen", "$noteIndex")

    val state = rememberRichTextState()
    val pagerState = rememberPagerState(initialPage = noteIndex.takeIf { it != -1 } ?: 0,
        pageCount = { voiceNotesList.size })
    val scope = rememberCoroutineScope()
    val currentIndex = remember { mutableIntStateOf(0) }
    currentIndex.intValue = noteIndex

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
            state.setHtml(voiceNotesList[index].title)
            val currentNote = voiceNotesList[index]
            val favoriteState = remember { mutableStateOf(currentNote.favourite) }
            val timerValue2 = voiceJournalPreviewViewModel.getDuration(currentNote.fileName)

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
                        if (voiceNotesList[currentIndex.intValue].content != null) {
                            Text(
                                text = voiceNotesList[currentIndex.intValue].content!!,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(Screen.VoicesScreen.route) }) {
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
                                //val currentNote = voiceNotesList[currentIndex.intValue]
                                // val favoriteState = remember { mutableStateOf(currentNote.favourite) }

                                IconButton(onClick = { /* Handle share action */ }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Variables.SchemesOnPrimary
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

                                    ),  tint = if (favoriteState.value==true) Variables.SchemesError else Variables.SchemesOnPrimary
                                    )

                                IconButton(
                                    onClick = {
                                        navController.navigate(
                                            Screen.AddEditNoteScreen.route +
                                                    "?noteId=${voiceNotesList[currentIndex.intValue].id}&noteColor=${voiceNotesList[currentIndex.intValue].color}&note=${voiceNotesList[currentIndex.intValue].title}"
                                        )
                                    })
                                {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Variables.SchemesOnPrimary,
                                    )
                                }

                                // Add more icons as needed (e.g., three-dot menu)
                            }
                        }
                    },
                    backgroundColor = Variables.SchemesPrimary,
                    contentColor = Variables.SchemesOnPrimary
                )

             val filteredList = voiceNotesList[index].imageUris?.filter { it.isNotEmpty()}

            if (filteredList?.isNotEmpty() == true) {
                 Image(
                     painter = rememberAsyncImagePainter(filteredList.first()),
                     contentDescription = null,
                     contentScale = ContentScale.Crop,
                     modifier = Modifier
                         .aspectRatio(1f)

                 )
             }


                Spacer(modifier = Modifier.padding(8.dp))
             Row   {
                 DynamicDateRow(created = voiceNotesList[index].created)
                if( voiceNotesList[index].fileName.isNotEmpty()) {
                     PlayRecordPanel(
                         timerValue = timerValue,
                         timerValue2 = timerValue2,
                         onPlay = {
                             voiceJournalPreviewViewModel.startTimer2(voiceNotesList[index].fileName)
                             voiceJournalPreviewViewModel.onEvent(
                                 VoiceJournalPreviewViewModel.FavouriteScreenEvent.Play(
                                     filename = voiceNotesList[index].fileName
                                 )
                             )
                         },
                         onCancelRecord = {
                             voiceJournalPreviewViewModel.onEvent(
                                 VoiceJournalPreviewViewModel.FavouriteScreenEvent.StopPlay
                             )
                             voiceJournalPreviewViewModel.stopTimer2()
                         },
                         playingState = playingState
                     )
                 }
             }

                RichTextEditor(
                    modifier = Modifier
                        .fillMaxWidth(),
                    state = state,
                    colors = RichTextEditorDefaults.richTextEditorColors(
                        textColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,

                        ),
                    readOnly = true
                )
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
                    tint = MaterialTheme.colorScheme.primary
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

}
