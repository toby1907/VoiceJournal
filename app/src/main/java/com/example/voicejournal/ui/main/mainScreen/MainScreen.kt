package com.example.voicejournal.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.voicejournal.Data.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.main.favourite.FavouriteScreenMain
import com.example.voicejournal.ui.main.mainScreen.NoteList
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel
import com.example.voicejournal.ui.main.mainScreen.components.BottomNavPanel
import com.example.voicejournal.ui.theme.Variables

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToAddVoice: () -> Unit,
    voiceNoteViewModel: VoiceNoteViewModel
) {
    SetStatusBarContentColor(false)
    Surface(
        color = Color.Transparent
    ) {

        ContentMain(onNavigateToAddVoice, voiceNoteViewModel, navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentMain(
    onNavigateToAddVoice: () -> Unit,
    voiceNoteViewModel: VoiceNoteViewModel,
    navController: NavController
) {
    val navController2: NavHostController = rememberNavController()

    val voiceNotes = voiceNoteViewModel.state.value
    val voiceNotesList: List<VoiceJournal> = voiceNotes.notes

    val lazyListState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            contentScale = ContentScale.FillBounds,
            painter = painterResource(id = R.drawable.background),
            contentDescription = "",
            modifier = Modifier.matchParentSize()
        )
        Scaffold(
            topBar = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Jorie",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight(900),
                                    color = Variables.SchemesOnPrimary,
                                )
                            )
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(tint = Variables.SchemesOnPrimary,
                                        painter = painterResource(id = R.drawable.cloud_icon), contentDescription = "Upload Status")
                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        tint = Variables.SchemesOnPrimary,
                                        painter = painterResource(id = R.drawable.search_bar), contentDescription = "Search Icon")

                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(tint = Variables.SchemesOnPrimary,
                                        painter = painterResource(id = R.drawable.menu_vertical), contentDescription = "Menu Option")

                                }
                            }
                        },
                        backgroundColor = Variables.SchemesPrimary,
                        contentColor = Variables.SchemesOnPrimary
                    )
// Child views.
                }

            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.shadow(
                        0.dp,
                        spotColor = Color(0xFF000000),
                        ambientColor = Color(0xFF000000)
                    ),
                    shape = RoundedCornerShape(size = 100.dp),
                    containerColor = Variables.SchemesOnPrimaryContainer,
                    contentColor = Color(0xFFffffff),
                    onClick = onNavigateToAddVoice
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.add_36),
                        contentDescription = ""
                    )
                }
            },
            content = { it ->
                /*  LazyColumn(
                      modifier = Modifier.consumedWindowInsets(innerPadding),
                      contentPadding = innerPadding
                  ){
                      items(voiceNotes.notes){note->
                        JournalItem(
                            voiceJournal = note,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        Screen.AddEditNoteScreen.route +
                                                "?noteId=${note.id}&noteColor=${note.color}"
                                    )
                                }
                        )

                      }
                  }*/

                NavHost(
                    modifier = Modifier.padding(it),
                    navController = navController2,
                    startDestination = "home"
                ) {
                    composable("home") {
                     NoteListScreen(navController =navController , notes =voiceNotes.notes )
                    }
                    composable("favourite") {
                        FavouriteScreenMain()
                    }
                    }
                    },
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavPanel(navController2)
            }
        )
    }

}
@Composable
fun NoteListScreen(navController:NavController,notes:List<VoiceJournal>){
    Spacer(modifier = Modifier.padding(32.dp))
    NoteList(
        journals =notes,
        navController = navController,
    )
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalItem(
    modifier: Modifier = Modifier,
    voiceJournal: VoiceJournal
) {

    Column {
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = Variables.SchemesSurface
            ),
            headlineText = {
                Text(
                    text = voiceJournal.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },

            supportingText = {
                voiceJournal.content?.let {
                    Text(
                        text = it,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            trailingContent = {
                val simpleDate = SimpleDateFormat("d/MMM/yy", Locale.getDefault())
                val journalDate = simpleDate.format(voiceJournal.created)
                Column() {
                    Text(text = journalDate)
                    if (voiceJournal.fileName != "") Icon(
                        painter = painterResource(id = R.drawable.audio_file),
                        contentDescription = ""
                    )
                }

            },
            leadingContent = {

                Row(
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color = Color(voiceJournal.color))
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = voiceJournal.title.getOrNull(0).toString()
                                .toUpperCase(Locale.getDefault())
                        )
                    }
                }
            },

            modifier = modifier
        )
        Divider()

    }


}*/




