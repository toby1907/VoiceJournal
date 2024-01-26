package com.example.voicejournal

fun Long.formatTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val remainingSeconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

fun convertIntToTimeFormat(input: Int): String {
    val totalSeconds = input / 1000
    val remainingSeconds = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val remainingMinutes = totalMinutes % 60
    val totalHours = totalMinutes / 60
    return String.format("%02d:%02d:%02d", totalHours, remainingMinutes, remainingSeconds)
}