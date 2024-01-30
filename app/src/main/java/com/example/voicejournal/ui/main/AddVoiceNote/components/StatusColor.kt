package com.example.voicejournal.ui.main.AddVoiceNote.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import com.example.voicejournal.ui.theme.Variables
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetStatusBarContentColor(darkIcons: Boolean) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = if(darkIcons) Variables.SchemesPrimaryContainer else Variables.SchemesPrimary,
            darkIcons = darkIcons
        )
    }
}