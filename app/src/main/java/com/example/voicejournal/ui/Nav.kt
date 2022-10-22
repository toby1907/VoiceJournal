package com.example.voicejournal.ui

import androidx.compose.runtime.Composable
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
import com.example.voicejournal.ui.main.mainScreen.VoiceNoteViewModel

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.VoicesScreen.route,
    voiceNoteViewModel: VoiceNoteViewModel = hiltViewModel()

){
   val navController: NavHostController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController ,
        startDestination = startDestination
    ){
        composable(Screen.VoicesScreen.route){
MainScreen(
    onNavigateToAddVoice ={
        navController.navigate(Screen.AddEditNoteScreen.route)
    },
    voiceNoteViewModel = voiceNoteViewModel,
    navController = navController
)
        }
        composable(
            route = Screen.AddEditNoteScreen.route +
                    "?noteId={noteId}&noteColor={noteColor}",
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
            )
        ){
                val color = it.arguments?.getInt("noteColor") ?: -1
                AddVoiceNoteScreen(
                    navController,
                    color
                )
            }
        composable("splash"){
            SplashScreen() {
                navController.navigate(Screen.VoicesScreen.route)
            }
        }

    }

}