package com.example.voicejournal.Data.model

import android.text.TextUtils
import androidx.core.text.HtmlCompat

fun VoiceJournal.resolvedNoteBlocks(): List<NoteBlock> {
    return if (noteBlocks.isNullOrEmpty()) {
        NoteBlockFactory.createForNote(
            titleHtml = title,
            content = content,
            audioPath = fileName,
            imageUris = imageUris
        )
    } else {
        noteBlocks.orEmpty().sortedBy { it.order }
    }
}

fun VoiceJournal.primaryRichTitleHtml(revealLockedContent: Boolean = true): String {
    if (!revealLockedContent && isNoteLocked()) return lockedTitleHtml()
    val titleBlock = resolvedNoteBlocks()
        .firstOrNull { it.type == NoteBlockType.TEXT.name && !it.richTextHtml.isNullOrBlank() }
    return titleBlock?.richTextHtml ?: title
}

fun VoiceJournal.primaryBodyPreviewText(revealLockedContent: Boolean = true): String {
    if (!revealLockedContent && isNoteLocked()) return lockedBodyText()
    val textBlocks = resolvedNoteBlocks().filter { it.type == NoteBlockType.TEXT.name }
    val bodyBlock = textBlocks.firstOrNull { it.richTextHtml.isNullOrBlank() && !it.text.isNullOrBlank() }
        ?: textBlocks.drop(1).firstOrNull()
    val fromBlock = bodyBlock?.text
        ?: bodyBlock?.richTextHtml?.let {
            HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        }
    return fromBlock?.takeIf { it.isNotBlank() } ?: content.orEmpty()
}

fun VoiceJournal.primaryAudioPath(revealLockedContent: Boolean = true): String {
    if (!revealLockedContent && isNoteLocked()) return ""
    val audioBlock = resolvedNoteBlocks().firstOrNull { it.type == NoteBlockType.AUDIO.name }
    return audioBlock?.mediaUri?.takeIf { it.isNotBlank() } ?: fileName
}

fun VoiceJournal.imageUrisFromBlocks(revealLockedContent: Boolean = true): List<String> {
    if (!revealLockedContent && isNoteLocked()) return emptyList()
    val blockUris = resolvedNoteBlocks()
        .filter { it.type == NoteBlockType.IMAGE.name }
        .mapNotNull { it.mediaUri?.takeIf { uri -> uri.isNotBlank() } }
    if (blockUris.isNotEmpty()) return blockUris
    return imageUris.orEmpty().filter { it.isNotBlank() }
}

fun textAsHtmlParagraph(text: String): String {
    return "<p>${TextUtils.htmlEncode(text)}</p>"
}
