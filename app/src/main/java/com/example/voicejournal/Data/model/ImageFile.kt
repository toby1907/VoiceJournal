package com.example.voicejournal.Data.model

import android.net.Uri

data class ImageFile(
    val uri: Uri,
    val name: String,
    val isSelected: Boolean = false,
    val bucketName: String = "Photos",
    val dateAddedSeconds: Long = 0L
)