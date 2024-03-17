package com.example.voicejournal.ui.main.voiceJournalPreviewScreen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.mainScreen.DynamicDateRow
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceJournalPreviewScreen(
    voiceJournalPreviewViewModel: VoiceJournalPreviewViewModel = hiltViewModel(),
    navController: NavHostController,
    noteColor: Int,
    noteIndex: Int,
) {

    //val gameUiState by gameViewModel.uiState.collectAsState()
    val voiceNotes by voiceJournalPreviewViewModel.state.collectAsState()
    val noteState = voiceJournalPreviewViewModel.noteState
    val voiceNotesList: List<VoiceJournal> = voiceNotes.notes
    // val noteIndex = remember { mutableIntStateOf(0) }
    /* LaunchedEffect(Unit) {
        voiceJournalPreviewViewModel.getNotes()
     }*/
//val noteIndex = voiceNotes.noteIndex
    //  noteIndex.intValue = voiceNotesList.indexOf(noteState.value.voiceJournal)

    Log.d("noteIndexPreviewScreen", "$noteIndex")

// This is a sample using NestedScroll and Pager.
// We use the toolbar offset changing example from
// androidx.compose.ui.samples.NestedScrollConnectionSample
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        // Add the key here
    ) {
        /* TopAppBar(
             modifier = Modifier
                 .height(toolbarHeight)
                 .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx.value.roundToInt()) },
             title = { Text("Toolbar offset is ${toolbarOffsetHeightPx.value}") }
         )*/


        val paddingOffset = with(LocalDensity.current) { toolbarOffsetHeightPx.value.toDp() }

        /*  HorizontalPager(
              modifier = Modifier.fillMaxSize(),
              state = pagerState,
              contentPadding = PaddingValues(top = paddingOffset)
          ) {
              Column(
                  modifier = Modifier
                      .fillMaxWidth()
                      .verticalScroll(rememberScrollState())
              ) {
                  repeat(20) {
                      Box(
                          modifier = Modifier
                              .fillMaxWidth()
                              .height(64.dp)
                              .padding(4.dp)
                              .background(if (it % 2 == 0) Color.Black else Color.Yellow),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = it.toString(),
                              color = if (it % 2 != 0) Color.Black else Color.Yellow
                          )
                      }
                  }
              }
          }*/


        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                TopAppBar(
                    modifier = Modifier
                        .height(toolbarHeight)
                        .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx.value.roundToInt()) },
                    title = { Text(text = "Top App Bar") },
                    navigationIcon = {
                        IconButton(onClick = { /* Handle back navigation */ }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        Row {
                            IconButton(onClick = { /* Handle share action */ }) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                            }
                            IconButton(onClick = { /* Handle favorite action */ }) {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite")
                            }
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        Screen.AddEditNoteScreen.route +
                                                "?noteId=${voiceNotesList[currentIndex.intValue].id}&noteColor=${voiceNotesList[currentIndex.intValue].color}"
                                    )

                                })
                            {
                                Icon(imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                )
                            }
                            // Add more icons as needed (e.g., three-dot menu)
                        }
                    }
                )
                var scale by remember { mutableStateOf(1f) }
                var offset by remember { mutableStateOf(Offset(0f, 0f)) }
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fill,
                    contentPadding = PaddingValues(top = paddingOffset)
                ) { index ->

                        state.setHtml(voiceNotesList[index].title)


                    currentIndex.intValue = index
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        voiceNotesList[index].imageUris?.get(0)?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                

                            )

                        }



                        Spacer(modifier = Modifier.padding(8.dp))
                        DynamicDateRow(created = voiceNotesList[index].created)

                        RichTextEditor(
                            modifier = Modifier
                                .fillMaxWidth(),
                            state = state,
                            colors = RichTextEditorDefaults.richTextEditorColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,

                                ),
                            readOnly = true
                        )
                    }


                }

            }

            Box(
                modifier = Modifier
                    .offset(y = -(16).dp)
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Go back"
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go forward"
                    )
                }
            }
        }
    }
}
