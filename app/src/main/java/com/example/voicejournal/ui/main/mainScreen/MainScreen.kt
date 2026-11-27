package com.example.voicejournal.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ScaffoldState
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.R
import com.example.voicejournal.ui.main.AddVoiceNote.components.SetStatusBarContentColor
import com.example.voicejournal.ui.main.calendar.Example8Page
import com.example.voicejournal.ui.main.favourite.FavouriteScreenMain
import com.example.voicejournal.ui.main.mainScreen.NoteList
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel
import com.example.voicejournal.ui.main.mainScreen.components.BottomNavPanel
import com.example.voicejournal.ui.main.media.MediaScreen
import com.example.voicejournal.ui.theme.Variables
import java.util.Locale

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToAddVoice: () -> Unit,
    voiceNoteViewModel: VoiceNoteViewModel,
    snackbarHostState: SnackbarHostState
) {
    SetStatusBarContentColor(false)
    Surface(
        color = Color.Transparent
    ) {

        ContentMain(onNavigateToAddVoice, voiceNoteViewModel, navController,snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentMain(
    onNavigateToAddVoice: () -> Unit,
    voiceNoteViewModel: VoiceNoteViewModel,
    navController: NavController,
   snackbarHostState: SnackbarHostState
) {

    val navController2: NavHostController = rememberNavController()

    val voiceNotes = voiceNoteViewModel.state.value
    val voiceNotesList: List<VoiceJournal> = voiceNotes.notes
    val folderOptions = voiceNotesList.mapNotNull { it.folderName?.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.getDefault()) }
        .sortedBy { it.lowercase(Locale.getDefault()) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var folderFilterDialogVisible by remember { mutableStateOf(false) }
    LaunchedEffect(folderOptions, selectedFolder) {
        if (!selectedFolder.isNullOrBlank() && folderOptions.none { it == selectedFolder }) {
            selectedFolder = null
        }
    }
    val filteredHomeNotes = if (selectedFolder.isNullOrBlank()) {
        voiceNotes.notes
    } else {
        voiceNotes.notes.filter { it.folderName?.trim() == selectedFolder }
    }

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
        Scaffold(modifier = Modifier.imePadding(),
            topBar = {
             JournalTopAppBar(
                 navController = navController,
                 selectedFolder = selectedFolder,
                 onFolderFilterClick = { folderFilterDialogVisible = true }
             )
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(size = 18.dp),
                        spotColor = Variables.SchemesPrimary.copy(alpha = 0.45f),
                        ambientColor = Variables.SchemesPrimary.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(size = 18.dp),
                    containerColor = Variables.SchemesPrimary,
                    contentColor = Variables.SchemesOnPrimary,
                    onClick = onNavigateToAddVoice
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_36),
                        contentDescription = "Add Note"
                    )
                }
            },
            content = { innerPadding ->
                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController2,
                    startDestination = "home"
                ) {
                    composable("home") {
                        NoteListScreen(
                            navController = navController,
                            notes = filteredHomeNotes,
                            allNotes = voiceNotesList,
                            selectedFolder = selectedFolder,
                            onClearFolder = { selectedFolder = null },
                            onCreateNote = onNavigateToAddVoice
                        )
                    }
                    composable("favourite") {
                        FavouriteScreenMain(navController = navController)
                    }
                    composable("calendar") {
                        Example8Page(horizontal = true, journals = voiceNotesList, navController = navController)
                    }
                    composable("media") {
                        MediaScreen(navController)
                    }
                }
            },
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavPanel(navController2)
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)

            }
        )
        if (folderFilterDialogVisible) {
            AlertDialog(
                onDismissRequest = { folderFilterDialogVisible = false },
                shape = com.example.voicejournal.ui.theme.JournalModalDefaults.DialogShape,
                containerColor = com.example.voicejournal.ui.theme.JournalModalDefaults.ContainerColor,
                titleContentColor = com.example.voicejournal.ui.theme.JournalModalDefaults.TitleColor,
                textContentColor = com.example.voicejournal.ui.theme.JournalModalDefaults.TextColor,
                icon = {
                    com.example.voicejournal.ui.theme.JournalDialogIconBadge(
                        imageVector = androidx.compose.material.icons.Icons.Default.Folder
                    )
                },
                title = {
                    Text(
                        text = "Filter by folder",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Variables.SchemesOnPrimaryContainer
                        )
                    )
                },
                text = {
                    androidx.compose.foundation.layout.Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val allSelected = selectedFolder == null
                        Surface(
                            onClick = {
                                selectedFolder = null
                                folderFilterDialogVisible = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (allSelected) {
                                com.example.voicejournal.ui.theme.JournalModalDefaults.ActiveCardColor
                            } else {
                                com.example.voicejournal.ui.theme.JournalModalDefaults.SoftCardColor
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (allSelected) 1.5.dp else 1.dp,
                                color = if (allSelected) {
                                    Variables.SchemesPrimary
                                } else {
                                    com.example.voicejournal.ui.theme.JournalModalDefaults.SubtleBorderColor
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (allSelected) "All folders ✓" else "All folders",
                                    fontWeight = if (allSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (allSelected) Variables.SchemesOnPrimaryContainer else Variables.SchemesOnSurface
                                )
                            }
                        }

                        folderOptions.forEach { folder ->
                            val isSelected = selectedFolder == folder
                            Surface(
                                onClick = {
                                    selectedFolder = folder
                                    folderFilterDialogVisible = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) {
                                    com.example.voicejournal.ui.theme.JournalModalDefaults.ActiveCardColor
                                } else {
                                    com.example.voicejournal.ui.theme.JournalModalDefaults.SoftCardColor
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) {
                                        Variables.SchemesPrimary
                                    } else {
                                        com.example.voicejournal.ui.theme.JournalModalDefaults.SubtleBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSelected) "$folder ✓" else folder,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Variables.SchemesOnPrimaryContainer else Variables.SchemesOnSurface
                                    )
                                }
                            }
                        }

                        if (folderOptions.isEmpty()) {
                            Text(
                                text = "No folders available yet.",
                                color = Variables.SchemesOnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = { folderFilterDialogVisible = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = com.example.voicejournal.ui.theme.JournalModalDefaults.primaryButtonColors()
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }

}
@Composable
fun NoteListScreen(
    navController: NavController,
    notes: List<VoiceJournal>,
    allNotes: List<VoiceJournal> = notes,
    selectedFolder: String? = null,
    onClearFolder: (() -> Unit)? = null,
    onCreateNote: (() -> Unit)? = null,
) {
    NoteList(
        journals = notes,
        navController = navController,
        allNotes = allNotes,
        selectedFolder = selectedFolder,
        onClearFolder = onClearFolder,
        onCreateNote = onCreateNote,
    )
}

@Composable
fun JournalTopAppBar(
    navController: NavController,
    selectedFolder: String?,
    onFolderFilterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Variables.SchemesPrimary)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Jorie",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                        color = Variables.SchemesOnPrimary,
                    )
                )
            },
            actions = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(
                            tint = Variables.SchemesOnPrimary,
                            painter = painterResource(id = R.drawable.search_bar),
                            contentDescription = "Search Icon"
                        )
                    }
                    IconButton(onClick = onFolderFilterClick) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                tint = Variables.SchemesOnPrimary,
                                painter = painterResource(id = R.drawable.menu_vertical),
                                contentDescription = "Folder Filter"
                            )
                            if (!selectedFolder.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Variables.SchemesPrimaryContainer)
                                )
                            }
                        }
                    }
                }
            },
            backgroundColor = Variables.SchemesPrimary,
            contentColor = Variables.SchemesOnPrimary,
            elevation = 0.dp
        )
    }
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
