package com.example.voicejournal.ui.media_player

import java.util.Locale
import java.util.concurrent.TimeUnit

fun Long.toTimeText(): String {
    val position = this
    val time = String.format(
        Locale.ROOT,
        "%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(position),
        TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(position)
        )
    )
    return time
}
