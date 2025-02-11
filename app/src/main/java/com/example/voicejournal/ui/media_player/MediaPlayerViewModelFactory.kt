package com.example.voicejournal.ui.media_player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MediaPlayerViewModelFactory(private val playWhenReady: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MediaPlayerViewModel() as T
    }
}