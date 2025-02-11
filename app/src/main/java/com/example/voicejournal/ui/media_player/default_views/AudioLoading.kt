package com.example.voicejournal.ui.media_player.default_views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AudioLoading(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally))
}