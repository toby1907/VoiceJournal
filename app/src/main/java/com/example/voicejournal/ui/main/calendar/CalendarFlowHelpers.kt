package com.example.voicejournal.ui.main.calendar

import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.imageUrisFromBlocks
import com.example.voicejournal.Data.model.isNoteLocked
import com.example.voicejournal.Data.model.primaryAudioPath
import com.example.voicejournal.Data.model.primaryBodyPreviewText
import com.example.voicejournal.Data.model.primaryRichTitleHtml
import com.example.voicejournal.Screen
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val HTML_TAG_REGEX = Regex("<[^>]+>")
private val WHITESPACE_REGEX = Regex("\\s+")

/**
 * Groups journal entries by their local calendar date, sorting each day's entries newest-first.
 */
fun groupJournalsByLocalDate(
    journals: List<VoiceJournal>,
    zoneId: ZoneId = ZoneId.systemDefault()
): Map<LocalDate, List<VoiceJournal>> {
    return journals
        .sortedByDescending { it.created }
        .groupBy { journal ->
            Instant.ofEpochMilli(journal.created)
                .atZone(zoneId)
                .toLocalDate()
        }
}

/**
 * Returns the first unlocked image URI available across all entries on a given day, or null.
 * Uses block-aware resolution so both legacy `imageUris` and modern `IMAGE` note blocks work,
 * while never leaking thumbnails from locked notes.
 */
fun dayPreviewThumbnailUri(dayJournals: List<VoiceJournal>): String? {
    for (journal in dayJournals) {
        if (journal.isNoteLocked()) continue
        val uri = journal.imageUrisFromBlocks(revealLockedContent = true)
            .firstOrNull { it.isNotBlank() }
        if (uri != null) return uri
    }
    return null
}

/**
 * Returns true if any unlocked entry on the day contains a voice recording.
 */
fun dayHasAudio(dayJournals: List<VoiceJournal>): Boolean {
    return dayJournals.any { journal ->
        !journal.isNoteLocked() && journal.primaryAudioPath(revealLockedContent = true).isNotBlank()
    }
}

/**
 * Returns true if any entry on the day is PIN-locked.
 */
fun dayHasLockedNote(dayJournals: List<VoiceJournal>): Boolean {
    return dayJournals.any { it.isNoteLocked() }
}

/**
 * Counts the total number of journal entries within [yearMonth].
 */
fun countEntriesInMonth(
    groupedJournals: Map<LocalDate, List<VoiceJournal>>,
    yearMonth: YearMonth
): Int {
    return groupedJournals.entries
        .filter { (date, _) -> YearMonth.from(date) == yearMonth }
        .sumOf { (_, list) -> list.size }
}

/**
 * Safely parses comma-separated journal IDs passed via navigation arguments.
 * Blank or malformed entries are ignored instead of throwing NumberFormatException.
 */
fun parseCalendarItemIds(rawIds: String?): List<Int> {
    if (rawIds.isNullOrBlank()) return emptyList()
    return rawIds
        .split(",")
        .mapNotNull { token -> token.trim().toIntOrNull() }
        .distinct()
}

/**
 * Builds the route for `JournalPreviewScreen/{itemIds}` with compact comma-separated IDs.
 * Returns null if no entries have a valid database ID.
 */
fun buildJournalPreviewRoute(journals: List<VoiceJournal>): String? {
    val ids = journals.mapNotNull { it.id }.distinct()
    if (ids.isEmpty()) return null
    return "JournalPreviewScreen/${ids.joinToString(",")}"
}

/**
 * Builds the `preview` route with an index aligned to [allJournals] (matching VoiceJournalPreviewViewModel).
 */
fun buildPreviewRoute(
    journal: VoiceJournal,
    allJournals: List<VoiceJournal>
): String {
    val resolvedIndex = allJournals.indexOfFirst { it.id != null && it.id == journal.id }
        .let { found -> if (found >= 0) found else allJournals.indexOf(journal).coerceAtLeast(0) }
    return "preview?noteId=${journal.id ?: -1}&noteColor=${journal.color}&noteIndex=$resolvedIndex"
}

/**
 * Builds the `AddEditNoteScreen` route pre-dated to [date] while keeping a sensible time-of-day.
 */
fun buildNewNoteForDateRoute(
    date: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
    timeOfDay: LocalTime = LocalTime.now(zoneId)
): String {
    val epochMillis = date.atTime(timeOfDay).atZone(zoneId).toInstant().toEpochMilli()
    return "${Screen.AddEditNoteScreen.route}?noteId=-1&noteColor=-1&note=&initialDateMillis=$epochMillis"
}

fun formatCalendarMonthTitle(
    yearMonth: YearMonth,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    return yearMonth.format(formatter).replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(locale) else ch.toString()
    }
}

fun formatWeekdayShort(
    dayOfWeek: DayOfWeek,
    locale: Locale = Locale.getDefault()
): String {
    return dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
}

fun formatSelectedDateHeader(
    date: LocalDate,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", locale)
    return date.format(formatter)
}

fun formatEntryTime(
    createdMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", locale)
    return Instant.ofEpochMilli(createdMillis).atZone(zoneId).format(formatter)
}

fun formatEntryCountLabel(count: Int): String {
    return when (count) {
        0 -> "No entries"
        1 -> "1 entry"
        else -> "$count entries"
    }
}

internal fun stripHtmlToPlainText(raw: String): String {
    if (raw.isBlank()) return ""
    return raw
        .replace("<br>", " ", ignoreCase = true)
        .replace("<br/>", " ", ignoreCase = true)
        .replace("<br />", " ", ignoreCase = true)
        .replace("</p>", " ", ignoreCase = true)
        .replace("</div>", " ", ignoreCase = true)
        .replace(HTML_TAG_REGEX, "")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(WHITESPACE_REGEX, " ")
        .trim()
}

/**
 * Extracts a clean display title and optional preview snippet for calendar cards while respecting lock state.
 */
fun extractCalendarCardText(journal: VoiceJournal): Pair<String, String> {
    if (journal.isNoteLocked()) {
        return "Locked note" to "Unlock with your PIN to view this entry"
    }
    val heading = journal.primaryBodyPreviewText(revealLockedContent = true).trim()
    val richBody = stripHtmlToPlainText(journal.primaryRichTitleHtml(revealLockedContent = true))
    val hasAudio = journal.primaryAudioPath(revealLockedContent = true).isNotBlank()
    val hasImages = journal.imageUrisFromBlocks(revealLockedContent = true).isNotEmpty()

    return when {
        heading.isNotBlank() -> {
            val snippet = richBody.takeIf { it.isNotBlank() && !it.equals(heading, ignoreCase = true) }.orEmpty()
            heading to snippet
        }
        richBody.isNotBlank() -> richBody to ""
        hasAudio -> "Voice note" to ""
        hasImages -> "Photo memory" to ""
        else -> "Untitled note" to ""
    }
}
