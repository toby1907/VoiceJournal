package com.example.voicejournal.ui

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.voicejournal.Screen
import com.example.voicejournal.ui.main.AddVoiceNote.AddVoiceNoteScreen
import com.example.voicejournal.ui.main.MainScreen
import com.example.voicejournal.ui.main.SplashScreen
import com.example.voicejournal.ui.main.calendar.JournalPreviewScreen
import com.example.voicejournal.ui.main.calendar.parseCalendarItemIds
import com.example.voicejournal.ui.main.camera.CameraScreen
import com.example.voicejournal.ui.main.gallery.GalleryScreen
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel
import com.example.voicejournal.ui.main.search.SearchScreen
import com.example.voicejournal.ui.main.voiceJournalPreviewScreen.VoiceJournalPreviewScreen

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.VoicesScreen.route,
    voiceNoteViewModel: VoiceNoteViewModel = hiltViewModel()

) {

    val snackbarHostState = remember { SnackbarHostState() }


    val navController: NavHostController = rememberNavController()


    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Screen.VoicesScreen.route) {
            MainScreen(
                onNavigateToAddVoice = {
                    navController.navigate(Screen.AddEditNoteScreen.route)
                },
                voiceNoteViewModel = voiceNoteViewModel,
                navController = navController,
                snackbarHostState = snackbarHostState
            )
        }
        composable(
            route = Screen.AddEditNoteScreen.route +
                    "?noteId={noteId}&noteColor={noteColor}&note={note}&initialDateMillis={initialDateMillis}",
            arguments = listOf(
                navArgument(
                    name = "noteId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "noteColor"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "note"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(
                    name = "initialDateMillis"
                ) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            )
        ) { entry ->
            val color = entry.arguments?.getInt("noteColor") ?: -1
            val note = Uri.decode(entry.arguments?.getString("note")) ?: ""


            AddVoiceNoteScreen(
               navController =  navController,
                noteColor = color,
                note = note,
                snackbarHostState = snackbarHostState,
            )
        }
        composable("splash") {
            SplashScreen() {
                navController.navigate(Screen.VoicesScreen.route)
            }
        }
        composable(route = "gallery"+"?noteId={noteId}&noteColor={noteColor}&note={note}",
            arguments = listOf(
                navArgument(
                    name = "noteId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "noteColor"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "note"
                ) {
                    type = NavType.StringType
                    defaultValue = ""
                },
            )
            ) { entry ->
            val color = entry.arguments?.getInt("noteColor") ?: -1
            GalleryScreen(
                navController = navController,
                noteColor = color
            )
        }
        composable("camera") {
            CameraScreen(navController = navController
            ) {
                navController.navigate(Screen.AddEditNoteScreen.route)
            }
        }
        composable("search") {
            SearchScreen(navController = navController
            ) {
                navController.navigate(Screen.VoicesScreen.route)
            }
        }

        composable(route = "preview"+"?noteId={noteId}&noteColor={noteColor}&noteIndex={noteIndex}",
            arguments = listOf(
                navArgument(
                    name = "noteId"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "noteColor"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument(
                    name = "noteIndex"
                ) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            )
        ) {entry ->
            val color = entry.arguments?.getInt("noteColor") ?: -1
            val index = entry.arguments?.getInt("noteIndex") ?: -1
            VoiceJournalPreviewScreen(
                navController = navController,
                noteColor = color,
                noteIndex = index
            )
        }

        composable(route = "JournalPreviewScreen/{itemIds}",
            arguments = listOf(
                navArgument("itemIds"){
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {backStackEntry ->
            val itemIds = parseCalendarItemIds(backStackEntry.arguments?.getString("itemIds"))

            JournalPreviewScreen(
                journalIds = itemIds,
                navController = navController,
            )
        }

    }

}