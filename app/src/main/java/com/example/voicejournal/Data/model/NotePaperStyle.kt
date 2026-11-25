package com.example.voicejournal.Data.model

enum class NotePaperStyle(
    val id: String,
    val displayName: String,
    val description: String
) {
    LINED(
        id = "LINED",
        displayName = "Lined Notebook",
        description = "Ruled lines & left margin"
    ),
    PLAIN(
        id = "PLAIN",
        displayName = "Plain Paper",
        description = "Clean pages without lines"
    );

    companion object {
        fun fromId(value: String?): NotePaperStyle {
            return values().firstOrNull { it.id.equals(value, ignoreCase = true) } ?: LINED
        }
    }
}
