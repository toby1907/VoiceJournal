package com.example.voicejournal.ui.media_player.default_views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FileNotFoundText(modifier: Modifier = Modifier) {
    Text(
        "File not found",
        color = Color.Red,
        modifier = modifier
    )
}
