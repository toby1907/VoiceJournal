package com.example.voicejournal.Data.model

import java.security.MessageDigest

private const val LOCKED_TITLE_TEXT = "Locked note"
private const val LOCKED_BODY_TEXT = "Unlock this note to view its content."

fun VoiceJournal.isNoteLocked(): Boolean {
    return isLocked == true && !lockPinHash.isNullOrBlank()
}

fun VoiceJournal.matchesVisibleMetadata(query: String): Boolean {
    if (query.isBlank()) return true
    val normalizedQuery = query.trim()
    if (!isNoteLocked()) {
        return doesMatchSearchQuery(normalizedQuery)
    }
    return listOf(
        LOCKED_TITLE_TEXT,
        LOCKED_BODY_TEXT,
        folderName.orEmpty()
    ).any {
        it.contains(normalizedQuery, ignoreCase = true)
    }
}

fun VoiceJournal.verifyPin(pin: String): Boolean {
    if (!isNoteLocked()) return true
    val expectedHash = lockPinHash ?: return false
    return hashNotePin(pin) == expectedHash
}

fun hashNotePin(pin: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(pin.trim().toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun VoiceJournal.shareText(revealLockedContent: Boolean = true): String {
    val titleHtml = primaryRichTitleHtml(revealLockedContent = revealLockedContent)
    val title = extractPlainTextFromHtml(titleHtml).ifBlank { "Untitled" }
    val body = primaryBodyPreviewText(revealLockedContent = revealLockedContent)
    val folder = folderName?.trim().orEmpty()
    val selectedTags = tags.orEmpty()
        .filter { it.isChecked }
        .joinToString(", ") { it.name }

    return buildString {
        append(title)
        if (body.isNotBlank()) {
            append("\n\n")
            append(body)
        }
        if (folder.isNotBlank()) {
            append("\n\n")
            append("Folder: ")
            append(folder)
        }
        if (selectedTags.isNotBlank()) {
            append("\n\n")
            append("Tags: ")
            append(selectedTags)
        }
    }
}

fun lockedTitleHtml(): String = "<p>$LOCKED_TITLE_TEXT</p>"

fun lockedBodyText(): String = LOCKED_BODY_TEXT
