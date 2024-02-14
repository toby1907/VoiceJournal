package com.example.voicejournal.Data.model

import android.net.Uri

data class ImageFile(
    val uri: Uri,
    val name: String,
    var isSelected: Boolean = false
)