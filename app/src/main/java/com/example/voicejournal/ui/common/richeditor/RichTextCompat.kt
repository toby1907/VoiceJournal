package com.example.voicejournal.ui.common.richeditor

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BulletSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import android.graphics.DashPathEffect
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.decodeDrawingPath
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.compose.material3.Text

enum class HeadingStyle(val label: String, val sizeSp: Int, val isBold: Boolean) {
    TITLE("Title", 24, true),
    HEADING_1("Heading 1", 20, true),
    HEADING_2("Heading 2", 18, true),
    HEADING_3("Heading 3", 16, true),
    BODY_1("Body 1", 16, false),
    BODY_2("Body 2", 13, false);

    companion object {
        fun fromLabel(label: String): HeadingStyle =
            values().firstOrNull { it.label == label } ?: BODY_1
    }
}

@Composable
fun rememberRichTextState(): RichTextState = remember { RichTextState() }

private const val LEGACY_PAGE_BREAK_LABEL = "────────── Page break ──────────"
private const val PAGE_BREAK_TOKEN = "\u200B\u2060\u200B"
private const val PAGE_BREAK_MARKER = "\n$PAGE_BREAK_TOKEN\n\n"
private const val ALIGN_MARKER_TOKEN = "\u200B"
private val ORDERED_LIST_LINE_REGEX = Regex("^(\\d+)\\.\\s")
private const val UNORDERED_LIST_PREFIX = "• "
private const val CHECKLIST_UNCHECKED_PREFIX = "☐ "
private const val CHECKLIST_CHECKED_PREFIX = "☑ "

class RichTextState {
    private data class Snapshot(
        val content: SpannableStringBuilder,
        val selectionStart: Int,
        val selectionEnd: Int
    )

    private var editable: SpannableStringBuilder = SpannableStringBuilder()
    private var boundEditor: AppCompatEditText? = null
    private val undoStack = ArrayDeque<Snapshot>()
    private val redoStack = ArrayDeque<Snapshot>()
    private var isHistoryMuted = false
    var isProgrammaticChange = false

    var annotatedString by mutableStateOf(AnnotatedString(""))
        private set

    var currentSpanStyle by mutableStateOf(SpanStyle())
        private set

    var currentTextAlign by mutableStateOf(TextAlign.Start)
        private set

    private var pendingParagraphAlignment: android.text.Layout.Alignment? = null

    var currentFontSizeSp by mutableIntStateOf(16)
        private set

    var activeTypingFontSizeSp: Int? by mutableStateOf(null)
        private set

    var currentHeadingStyle by mutableStateOf(HeadingStyle.BODY_1)
        private set

    var isOrderedList by mutableStateOf(false)
        private set

    var isUnorderedList by mutableStateOf(false)
        private set

    var isChecklist by mutableStateOf(false)
        private set

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    private var selectionStart: Int = 0
    private var selectionEnd: Int = 0

    fun setHtml(html: String) {
        isProgrammaticChange = true
        try {
            var preprocessed = html
            // Normalize font-size spans to voicejournal-size links so fromHtml reliably preserves them
            preprocessed = preprocessed.replace(
                Regex("""<span[^>]*style="[^"]*font-size:\s*(\d+)[a-z%]*[^"]*"[^>]*>(.*?)</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val size = match.groupValues[1]
                val content = match.groupValues[2]
                "<a href=\"voicejournal-size:${size}\">$content</a>"
            }
            preprocessed = preprocessed.replace(
                Regex("""<span[^>]*data-font-size="(\d+)"[^>]*>(.*?)</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val size = match.groupValues[1]
                val content = match.groupValues[2]
                "<a href=\"voicejournal-size:${size}\">$content</a>"
            }
            preprocessed = preprocessed.replace(Regex("<h1>(.*?)</h1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))) {
                "<p><a href=\"voicejournal-size:28\"><b>${it.groupValues[1]}</b></a></p>"
            }
            preprocessed = preprocessed.replace(Regex("<h2>(.*?)</h2>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))) {
                "<p><a href=\"voicejournal-size:24\"><b>${it.groupValues[1]}</b></a></p>"
            }
            preprocessed = preprocessed.replace(Regex("<h3>(.*?)</h3>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))) {
                "<p><a href=\"voicejournal-size:20\"><b>${it.groupValues[1]}</b></a></p>"
            }
            // Bridge paragraph alignment markers and standard CSS text-align paragraphs
            preprocessed = preprocessed.replace(
                Regex("""<span[^>]*data-voicejournal-align="(center|end|right)"[^>]*>.*?</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val align = if (match.groupValues[1].equals("center", ignoreCase = true)) "center" else "end"
                "<a href=\"voicejournal-align:$align\">$ALIGN_MARKER_TOKEN</a>"
            }
            preprocessed = preprocessed.replace(
                Regex("""<p[^>]*style="[^"]*text-align:\s*(center|right|end)[^"]*"[^>]*>(.*?)</p>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val align = if (match.groupValues[1].equals("center", ignoreCase = true)) "center" else "end"
                val inner = match.groupValues[2]
                "<p><a href=\"voicejournal-align:$align\">$ALIGN_MARKER_TOKEN</a>$inner</p>"
            }
            // Bridge invisible page break spans and upgrade any legacy visible page break labels
            preprocessed = preprocessed.replace(
                Regex("""<span[^>]*data-voicejournal-page-break="true"[^>]*>.*?</span>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
                "<a href=\"voicejournal-page-break:1\">$PAGE_BREAK_TOKEN</a>"
            )
            preprocessed = preprocessed.replace(
                Regex("""<p[^>]*>(?:\s|<[^>]+>)*${Regex.escape(LEGACY_PAGE_BREAK_LABEL)}(?:\s|<[^>]+>)*</p>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)),
                "<a href=\"voicejournal-page-break:1\">$PAGE_BREAK_TOKEN</a>"
            )
            preprocessed = preprocessed.replace(
                LEGACY_PAGE_BREAK_LABEL,
                "<a href=\"voicejournal-page-break:1\">$PAGE_BREAK_TOKEN</a>"
            )

            val spanned = HtmlCompat.fromHtml(preprocessed, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val parsed = SpannableStringBuilder(spanned)
            // Strip any legacy BulletSpans that could cause double bullets
            parsed.getSpans(0, parsed.length, BulletSpan::class.java).forEach { parsed.removeSpan(it) }

            // Pass 1: restore font-size spans and page-break markers first so text lengths are final
            val urlSpans = parsed.getSpans(0, parsed.length, URLSpan::class.java)
            for (span in urlSpans) {
                val url = span.url
                if (url != null && url.startsWith("voicejournal-size:")) {
                    val size = url.removePrefix("voicejournal-size:").toIntOrNull()
                    if (size != null) {
                        val start = parsed.getSpanStart(span)
                        val end = parsed.getSpanEnd(span)
                        parsed.removeSpan(span)
                        if (start in 0..end && end <= parsed.length) {
                            parsed.setSpan(
                                AbsoluteSizeSpan(size, true),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                } else if (url == "voicejournal-page-break:1") {
                    val start = parsed.getSpanStart(span)
                    val end = parsed.getSpanEnd(span)
                    parsed.removeSpan(span)
                    if (start in 0..end && end <= parsed.length) {
                        parsed.replace(start, end, PAGE_BREAK_MARKER)
                    }
                }
            }

            // Pass 2: remove zero-width alignment tokens from bottom to top and apply per-line AlignmentSpan
            val alignUrlSpans = parsed.getSpans(0, parsed.length, URLSpan::class.java)
                .filter { it.url?.startsWith("voicejournal-align:") == true }
                .sortedByDescending { parsed.getSpanStart(it) }
            for (span in alignUrlSpans) {
                val alignKey = span.url?.removePrefix("voicejournal-align:").orEmpty()
                val alignment = when (alignKey.lowercase()) {
                    "center" -> android.text.Layout.Alignment.ALIGN_CENTER
                    "end", "right" -> android.text.Layout.Alignment.ALIGN_OPPOSITE
                    else -> android.text.Layout.Alignment.ALIGN_NORMAL
                }
                val start = parsed.getSpanStart(span)
                val end = parsed.getSpanEnd(span)
                parsed.removeSpan(span)
                if (start in 0..end && end <= parsed.length) {
                    parsed.delete(start, end)
                    if (alignment != android.text.Layout.Alignment.ALIGN_NORMAL) {
                        val safeOffset = start.coerceIn(0, parsed.length)
                        val (lineStart, lineEnd) = lineBoundsAt(parsed, safeOffset)
                        applyAlignmentToLine(parsed, lineStart, lineEnd, alignment)
                    }
                }
            }

            editable = parsed
            activeTypingFontSizeSp = null
            pendingParagraphAlignment = null
            currentFontSizeSp = 16
            clearHistory()
            pushToEditor()
            refreshStateSnapshot()
        } finally {
            isProgrammaticChange = false
        }
    }

    fun toHtml(): String {
        val current = currentEditable()
        if (current.isEmpty()) return ""

        val temp = SpannableStringBuilder(current)
        // Strip any legacy BulletSpans before serializing
        temp.getSpans(0, temp.length, BulletSpan::class.java).forEach { temp.removeSpan(it) }

        // Collect per-line non-normal alignments and replace with zero-width bridge tokens so
        // AlignmentSpan survives HtmlCompat.toHtml/fromHtml without interfering with other spans
        val lineAlignMarkers = mutableListOf<Pair<Int, String>>()
        val alignSpans = temp.getSpans(0, temp.length, AlignmentSpan::class.java)
        if (alignSpans.isNotEmpty()) {
            val lineStarts = collectLineRanges(temp)
            for ((lineStart, lineEnd) in lineStarts) {
                val lineAlign = alignmentForLine(temp, lineStart, lineEnd)
                if (lineAlign == android.text.Layout.Alignment.ALIGN_CENTER) {
                    lineAlignMarkers.add(lineStart to "center")
                } else if (lineAlign == android.text.Layout.Alignment.ALIGN_OPPOSITE) {
                    lineAlignMarkers.add(lineStart to "end")
                }
            }
            alignSpans.forEach { temp.removeSpan(it) }
            // Insert markers from bottom to top so earlier offsets stay valid
            for ((lineStart, alignKey) in lineAlignMarkers.sortedByDescending { it.first }) {
                temp.insert(lineStart, ALIGN_MARKER_TOKEN)
                temp.setSpan(
                    URLSpan("voicejournal-align:$alignKey"),
                    lineStart,
                    lineStart + ALIGN_MARKER_TOKEN.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        var breakSearchFrom = 0
        while (true) {
            val breakIdx = temp.toString().indexOf(PAGE_BREAK_MARKER, breakSearchFrom)
            if (breakIdx < 0) break
            temp.replace(breakIdx, breakIdx + PAGE_BREAK_MARKER.length, PAGE_BREAK_TOKEN)
            temp.setSpan(
                URLSpan("voicejournal-page-break:1"),
                breakIdx,
                breakIdx + PAGE_BREAK_TOKEN.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            breakSearchFrom = breakIdx + PAGE_BREAK_TOKEN.length
        }

        val sizeSpans = temp.getSpans(0, temp.length, AbsoluteSizeSpan::class.java)
        for (span in sizeSpans) {
            val start = temp.getSpanStart(span)
            val end = temp.getSpanEnd(span)
            if (start in 0..end && end <= temp.length) {
                temp.removeSpan(span)
                temp.setSpan(
                    URLSpan("voicejournal-size:${span.size}"),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        val baseHtml = HtmlCompat.toHtml(temp, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
        val withFontSizes = baseHtml.replace(
            Regex("""<a href="voicejournal-size:(\d+)"[^>]*>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)
        ) { match ->
            val size = match.groupValues[1]
            val content = match.groupValues[2]
            "<span style=\"font-size:${size}px;\" data-font-size=\"$size\">$content</span>"
        }
        val withAlignments = withFontSizes.replace(
            Regex("""<a href="voicejournal-align:(center|end)"[^>]*>.*?</a>""", RegexOption.DOT_MATCHES_ALL)
        ) { match ->
            val align = match.groupValues[1]
            """<span data-voicejournal-align="$align"></span>"""
        }
        val withPageBreaks = withAlignments.replace(
            Regex("""<a href="voicejournal-page-break:1"[^>]*>.*?</a>""", RegexOption.DOT_MATCHES_ALL),
            """<span data-voicejournal-page-break="true"></span>"""
        )
        return withPageBreaks
    }

    fun setText(text: String) {
        isProgrammaticChange = true
        try {
            editable = SpannableStringBuilder(text)
            activeTypingFontSizeSp = null
            pendingParagraphAlignment = null
            currentFontSizeSp = 16
            clearHistory()
            pushToEditor()
            refreshStateSnapshot()
        } finally {
            isProgrammaticChange = false
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        val current = captureSnapshot()
        redoStack.addLast(current)
        restoreSnapshot(previous)
        updateHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeLast()
        val current = captureSnapshot()
        undoStack.addLast(current)
        restoreSnapshot(next)
        updateHistoryFlags()
    }

    fun toggleSpanStyle(style: SpanStyle) {
        val decoration = style.textDecoration
        if (style.fontWeight == FontWeight.Bold) {
            toggleSpan(StyleSpan(Typeface.BOLD), StyleSpan::class.java)
        }
        if (style.fontStyle == FontStyle.Italic) {
            toggleSpan(StyleSpan(Typeface.ITALIC), StyleSpan::class.java)
        }
        if (decoration != null) {
            if (decoration.contains(androidx.compose.ui.text.style.TextDecoration.Underline)) {
                toggleSpan(UnderlineSpan(), UnderlineSpan::class.java)
            }
            if (decoration.contains(androidx.compose.ui.text.style.TextDecoration.LineThrough)) {
                toggleSpan(StrikethroughSpan(), StrikethroughSpan::class.java)
            }
        }
        if (style.color != Color.Unspecified) {
            toggleForegroundColor(style.color)
        }
        refreshStateSnapshot()
    }

    fun toggleParagraphStyle(style: ParagraphStyle) {
        val alignment = when (style.textAlign) {
            TextAlign.Center -> android.text.Layout.Alignment.ALIGN_CENTER
            TextAlign.End, TextAlign.Right -> android.text.Layout.Alignment.ALIGN_OPPOSITE
            else -> android.text.Layout.Alignment.ALIGN_NORMAL
        }
        applyParagraphAlignment(alignment)
        refreshStateSnapshot()
    }

    fun addLink(text: String, url: String) {
        val normalizedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        val content = currentEditable()
        val start = selectionStart.coerceIn(0, content.length)
        val end = selectionEnd.coerceIn(start, content.length)
        if (text.isNotBlank()) {
            content.replace(start, end, text)
            content.setSpan(
                URLSpan(normalizedUrl),
                start,
                start + text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (start != end) {
            content.setSpan(URLSpan(normalizedUrl), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        applyEditableToEditor(content)
        refreshStateSnapshot()
    }

    fun toggleOrderedList() {
        applyListType(ordered = true)
    }

    fun toggleUnorderedList() {
        applyListType(ordered = false)
    }

    fun applyActiveTypingStyle(editable: Editable, start: Int, end: Int) {
        if (start >= end) return
        val size = activeTypingFontSizeSp
        if (size != null && size != 16) {
            val existing = editable.getSpans(start, end, AbsoluteSizeSpan::class.java)
            existing.forEach { editable.removeSpan(it) }
            editable.setSpan(
                AbsoluteSizeSpan(size, true),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val pendingAlign = pendingParagraphAlignment
        if (pendingAlign != null) {
            val (lineStart, lineEnd) = lineBoundsAt(editable, start)
            applyAlignmentToLine(editable, lineStart, lineEnd, pendingAlign)
            if (editable.subSequence(lineStart, lineEnd).isNotEmpty()) {
                pendingParagraphAlignment = null
            }
        }
    }

    fun setFontSize(sizeSp: Int) {
        val target: Spannable = boundEditor?.text ?: editable
        val textLength = target.length

        val selStart = selectionStart.coerceIn(0, textLength)
        val selEnd = selectionEnd.coerceIn(0, textLength)
        val hasHighlight = selStart != selEnd

        if (hasHighlight) {
            // Apply ONLY to the highlighted word, phrase, or sentence
            val start = minOf(selStart, selEnd)
            val end = maxOf(selStart, selEnd)

            val existingSpans = target.getSpans(start, end, AbsoluteSizeSpan::class.java)
            existingSpans.forEach { target.removeSpan(it) }
            target.setSpan(
                AbsoluteSizeSpan(sizeSp, true),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            boundEditor?.setSelection(start, end)
            selectionStart = start
            selectionEnd = end
        }

        // Set active font size for all subsequent typed letters and maintain current size
        currentFontSizeSp = sizeSp
        activeTypingFontSizeSp = sizeSp
        currentHeadingStyle = HeadingStyle.values().firstOrNull { it.sizeSp == sizeSp } ?: HeadingStyle.BODY_1
        editable = SpannableStringBuilder(target)
        refreshStateSnapshot()
    }

    fun setHeadingStyle(heading: HeadingStyle) {
        val target: Spannable = boundEditor?.text ?: editable
        val textLength = target.length
        val selStart = selectionStart.coerceIn(0, textLength)
        val selEnd = selectionEnd.coerceIn(0, textLength)
        val hasHighlight = selStart != selEnd

        if (hasHighlight) {
            val start = minOf(selStart, selEnd)
            val end = maxOf(selStart, selEnd)
            val existingSizes = target.getSpans(start, end, AbsoluteSizeSpan::class.java)
            existingSizes.forEach { target.removeSpan(it) }
            target.setSpan(
                AbsoluteSizeSpan(heading.sizeSp, true),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val existingStyles = target.getSpans(start, end, StyleSpan::class.java)
            existingStyles.filter { it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC }.forEach {
                target.removeSpan(it)
            }
            if (heading.isBold) {
                target.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            boundEditor?.setSelection(start, end)
            selectionStart = start
            selectionEnd = end
        }

        currentHeadingStyle = heading
        currentFontSizeSp = heading.sizeSp
        activeTypingFontSizeSp = heading.sizeSp
        editable = SpannableStringBuilder(target)
        refreshStateSnapshot()
    }

    fun toggleChecklistLine() {
        val content = currentEditable()
        val cursor = selectionEnd.coerceIn(0, content.length)
        val (lineStart, lineEnd) = lineBoundsAt(content, cursor)
        val lineText = content.subSequence(lineStart, lineEnd).toString()
        val newCursor: Int

        if (lineText.startsWith(CHECKLIST_UNCHECKED_PREFIX)) {
            content.replace(lineStart, lineStart + 2, CHECKLIST_CHECKED_PREFIX)
            if (lineEnd > lineStart + 2) {
                content.setSpan(
                    StrikethroughSpan(),
                    lineStart + 2,
                    lineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            newCursor = cursor.coerceIn(lineStart + 2, content.length)
        } else if (lineText.startsWith(CHECKLIST_CHECKED_PREFIX)) {
            content.delete(lineStart, lineStart + 2)
            val spans = content.getSpans(lineStart, (lineEnd - 2).coerceAtLeast(lineStart), StrikethroughSpan::class.java)
            spans.forEach { content.removeSpan(it) }
            newCursor = (cursor - 2).coerceIn(lineStart, content.length)
        } else {
            // Strip any existing ordered/bulleted prefix before applying checklist prefix
            val existingPrefixLen = detectListPrefixLength(lineText)
            if (existingPrefixLen > 0) {
                content.delete(lineStart, lineStart + existingPrefixLen)
            }
            content.getSpans(lineStart, (lineEnd - existingPrefixLen).coerceAtLeast(lineStart), BulletSpan::class.java)
                .forEach { content.removeSpan(it) }
            content.insert(lineStart, CHECKLIST_UNCHECKED_PREFIX)
            val delta = CHECKLIST_UNCHECKED_PREFIX.length - existingPrefixLen
            newCursor = if (cursor <= lineStart + existingPrefixLen) {
                lineStart + CHECKLIST_UNCHECKED_PREFIX.length
            } else {
                (cursor + delta).coerceIn(lineStart + CHECKLIST_UNCHECKED_PREFIX.length, content.length)
            }
        }
        applyEditableToEditor(content, newCursor)
        refreshStateSnapshot()
    }

    /**
     * Handles Enter inside an ordered list, bulleted list, or checklist:
     * - Empty item (prefix only) -> exits list by removing the prefix.
     * - Non-empty item -> inserts newline + next list marker (and renumbers following ordered items).
     * Returns true if the Enter key was handled.
     */
    internal fun handleListEnter(onPageBoundary: Boolean = false): Boolean {
        val content = currentEditable()
        val cursor = selectionEnd.coerceIn(0, content.length)
        val (lineStart, lineEnd) = lineBoundsAt(content, cursor)
        val lineText = content.subSequence(lineStart, lineEnd).toString()

        val orderedMatch = ORDERED_LIST_LINE_REGEX.find(lineText)
        val isBullet = lineText.startsWith(UNORDERED_LIST_PREFIX)
        val isCheck = lineText.startsWith(CHECKLIST_UNCHECKED_PREFIX) || lineText.startsWith(CHECKLIST_CHECKED_PREFIX)
        if (orderedMatch == null && !isBullet && !isCheck) return false

        val prefixLen = when {
            orderedMatch != null -> orderedMatch.value.length
            else -> 2
        }
        val afterPrefix = lineText.substring(prefixLen)

        // Pressing Enter on an empty list item exits list mode cleanly
        if (afterPrefix.isBlank() && cursor >= lineStart + prefixLen) {
            content.delete(lineStart, lineEnd)
            content.getSpans(lineStart, lineStart, StrikethroughSpan::class.java).forEach { content.removeSpan(it) }
            applyEditableToEditor(content, lineStart)
            refreshStateSnapshot()
            return true
        }

        val nextPrefix = when {
            orderedMatch != null -> "${(orderedMatch.groupValues[1].toIntOrNull() ?: 1) + 1}. "
            isBullet -> UNORDERED_LIST_PREFIX
            else -> CHECKLIST_UNCHECKED_PREFIX
        }

        val separator = if (onPageBoundary) PAGE_BREAK_MARKER else "\n"
        val insertion = separator + nextPrefix
        val selStartSafe = minOf(selectionStart, selectionEnd).coerceIn(0, content.length)
        val selEndSafe = maxOf(selectionStart, selectionEnd).coerceIn(selStartSafe, content.length)
        content.replace(selStartSafe, selEndSafe, insertion)
        val newCursor = selStartSafe + insertion.length

        // Ensure new unchecked checklist line does not inherit strikethrough from a checked item
        if (isCheck) {
            val newLineStart = selStartSafe + separator.length
            val newLineEnd = content.toString().indexOf('\n', newLineStart).let { if (it == -1) content.length else it }
            content.getSpans(newLineStart, newLineEnd, StrikethroughSpan::class.java).forEach { content.removeSpan(it) }
        }

        // If inserting in the middle of an ordered list, renumber subsequent consecutive items
        if (orderedMatch != null && !onPageBoundary) {
            val nextNum = (orderedMatch.groupValues[1].toIntOrNull() ?: 1) + 2
            renumberFollowingOrderedItems(content, newCursor, nextNum)
        }

        applyEditableToEditor(content, newCursor)
        refreshStateSnapshot()
        return true
    }

    /**
     * Handles Backspace when the cursor sits directly at the end of a list prefix ("1. |", "• |", "☐ |", "☑ |"):
     * removes the entire list prefix cleanly in one press.
     */
    internal fun handleListBackspace(): Boolean {
        if (selectionStart != selectionEnd) return false
        val content = currentEditable()
        val cursor = selectionEnd.coerceIn(0, content.length)
        val (lineStart, lineEnd) = lineBoundsAt(content, cursor)
        val lineText = content.subSequence(lineStart, lineEnd).toString()
        val prefixLen = detectListPrefixLength(lineText)
        if (prefixLen <= 0 || cursor != lineStart + prefixLen) return false

        content.delete(lineStart, lineStart + prefixLen)
        content.getSpans(lineStart, (lineEnd - prefixLen).coerceAtLeast(lineStart), StrikethroughSpan::class.java)
            .forEach { content.removeSpan(it) }
        applyEditableToEditor(content, lineStart)
        refreshStateSnapshot()
        return true
    }

    fun toggleChecklistAtOffset(offset: Int): Boolean {
        val content = currentEditable()
        if (offset < 0 || offset > content.length) return false
        val lineStart = content.toString().lastIndexOf('\n', offset - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = content.toString().indexOf('\n', lineStart).let { if (it == -1) content.length else it }
        val lineText = content.subSequence(lineStart, lineEnd).toString()

        if (lineText.startsWith(CHECKLIST_UNCHECKED_PREFIX)) {
            content.replace(lineStart, lineStart + 2, CHECKLIST_CHECKED_PREFIX)
            if (lineEnd > lineStart + 2) {
                content.setSpan(
                    StrikethroughSpan(),
                    lineStart + 2,
                    lineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            applyEditableToEditor(content, offset)
            refreshStateSnapshot()
            return true
        } else if (lineText.startsWith(CHECKLIST_CHECKED_PREFIX)) {
            content.replace(lineStart, lineStart + 2, CHECKLIST_UNCHECKED_PREFIX)
            val spans = content.getSpans(lineStart, lineEnd, StrikethroughSpan::class.java)
            spans.forEach { content.removeSpan(it) }
            applyEditableToEditor(content, offset)
            refreshStateSnapshot()
            return true
        }
        return false
    }

    fun focusToEnd() {
        boundEditor?.let { editor ->
            val len = editor.text?.length ?: 0
            editor.setSelection(len)
            editor.requestFocus()
            editor.post {
                val imm = editor.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    fun insertPageBreak() {
        val content = currentEditable()
        val insertAt = selectionEnd.coerceIn(0, content.length)
        content.insert(insertAt, PAGE_BREAK_MARKER)
        applyEditableToEditor(content, insertAt + PAGE_BREAK_MARKER.length)
        refreshStateSnapshot()
    }

    fun clearPageBreaks() {
        val content = currentEditable()
        var start = content.toString().indexOf(PAGE_BREAK_MARKER)
        while (start >= 0) {
            content.delete(start, start + PAGE_BREAK_MARKER.length)
            start = content.toString().indexOf(PAGE_BREAK_MARKER)
        }
        applyEditableToEditor(content)
        refreshStateSnapshot()
    }

    fun removeCurrentPageBreak() {
        val content = currentEditable()
        val cursor = selectionEnd.coerceIn(0, content.length)
        val offsets = pageBreakOffsets()
        val breakIndex = offsets.lastOrNull { it < cursor } ?: offsets.firstOrNull() ?: return
        content.delete(breakIndex, breakIndex + PAGE_BREAK_MARKER.length)
        applyEditableToEditor(content, breakIndex.coerceAtMost(content.length))
        refreshStateSnapshot()
    }

    fun pageBreakCount(): Int {
        return currentEditable().toString().split(PAGE_BREAK_MARKER).size - 1
    }

    fun pageCount(): Int {
        return pageBreakCount() + 1
    }

    fun currentPageIndex(): Int {
        val cursor = selectionEnd.coerceIn(0, currentEditable().length)
        return pageBreakOffsets().count { it < cursor }
    }

    fun pageBreakOffsets(): List<Int> {
        val content = currentEditable().toString()
        val offsets = mutableListOf<Int>()
        var searchFrom = 0
        while (true) {
            val breakIndex = content.indexOf(PAGE_BREAK_MARKER, searchFrom)
            if (breakIndex < 0) break
            offsets.add(breakIndex)
            searchFrom = breakIndex + PAGE_BREAK_MARKER.length
        }
        return offsets
    }

    fun pageRanges(): List<IntRange> {
        val content = currentEditable().toString()
        val ranges = mutableListOf<IntRange>()
        var start = 0
        var searchFrom = 0
        while (true) {
            val breakIndex = content.indexOf(PAGE_BREAK_MARKER, searchFrom)
            if (breakIndex < 0) {
                ranges.add(start..content.length)
                break
            }
            ranges.add(start until breakIndex)
            start = breakIndex + PAGE_BREAK_MARKER.length
            searchFrom = start
        }
        return ranges
    }

    fun normalizePageMarkers() {
        val content = currentEditable()
        val normalized = content.toString()
            .replace("\r\n", "\n")
            .replace(Regex("\n{3,}"), "\n\n")
        if (normalized == content.toString()) return
        editable = SpannableStringBuilder(normalized)
        applyEditableToEditor(editable)
        refreshStateSnapshot()
    }

    fun currentPageRange(): IntRange? {
        val index = currentPageIndex()
        return pageRanges().getOrNull(index)
    }

    fun goToPage(pageIndex: Int) {
        if (pageIndex <= 0) {
            applyEditableToEditor(currentEditable(), 0)
            refreshStateSnapshot()
            return
        }
        val offsets = pageBreakOffsets()
        if (pageIndex > offsets.size) return
        val targetStart = offsets[pageIndex - 1] + PAGE_BREAK_MARKER.length
        applyEditableToEditor(currentEditable(), targetStart)
        refreshStateSnapshot()
    }

    fun goToNextPage() {
        goToPage(currentPageIndex() + 1)
    }

    fun goToPreviousPage() {
        goToPage((currentPageIndex() - 1).coerceAtLeast(0))
    }

    internal fun bindEditor(editor: AppCompatEditText) {
        if (boundEditor === editor) return
        boundEditor = editor
        if (editable.isNotEmpty() || !editor.text.isNullOrEmpty()) {
            applyEditableToEditor(editable)
        }
    }

    internal fun onEditorContentChanged(text: Editable?, start: Int, end: Int) {
        val newContent = SpannableStringBuilder(text ?: "")
        if (!isHistoryMuted) {
            val previous = Snapshot(
                content = SpannableStringBuilder(editable),
                selectionStart = selectionStart,
                selectionEnd = selectionEnd
            )
            val next = Snapshot(
                content = newContent,
                selectionStart = start.coerceAtLeast(0),
                selectionEnd = end.coerceAtLeast(0)
            )
            if (!isContentEquivalent(previous.content, next.content)) {
                undoStack.addLast(previous)
                if (undoStack.size > 100) {
                    undoStack.removeFirst()
                }
                redoStack.clear()
            }
            updateHistoryFlags()
        }
        editable = newContent
        selectionStart = start.coerceAtLeast(0)
        selectionEnd = end.coerceAtLeast(0)
        refreshStateSnapshot()
    }

    private fun applyListType(ordered: Boolean) {
        val content = currentEditable()
        val rawSelStart = minOf(selectionStart, selectionEnd).coerceIn(0, content.length)
        val rawSelEnd = maxOf(selectionStart, selectionEnd).coerceIn(rawSelStart, content.length)
        val hasRange = rawSelStart != rawSelEnd

        val affectedLines = collectSelectedLineRanges(content, rawSelStart, rawSelEnd)
            .filter { (ls, le) -> content.subSequence(ls, le).toString() != PAGE_BREAK_TOKEN }
        if (affectedLines.isEmpty()) return

        val allAlreadyTarget = affectedLines.all { (ls, le) ->
            val lineText = content.subSequence(ls, le).toString()
            if (ordered) ORDERED_LIST_LINE_REGEX.containsMatchIn(lineText)
            else lineText.startsWith(UNORDERED_LIST_PREFIX)
        }

        // Determine starting number for ordered list by inspecting the immediately preceding line
        var nextNumber = 1
        if (ordered && !allAlreadyTarget) {
            val firstLineStart = affectedLines.first().first
            if (firstLineStart > 0) {
                val prevLineEnd = firstLineStart - 1
                val (prevStart, _) = lineBoundsAt(content, prevLineEnd)
                val prevText = content.subSequence(prevStart, prevLineEnd).toString()
                val prevMatch = ORDERED_LIST_LINE_REGEX.find(prevText)
                if (prevMatch != null) {
                    nextNumber = (prevMatch.groupValues[1].toIntOrNull() ?: 0) + 1
                }
            }
        }

        var newSelStart = rawSelStart
        var newSelEnd = rawSelEnd
        var cumulativeDelta = 0

        for ((origLineStart, origLineEnd) in affectedLines) {
            val lineStart = origLineStart + cumulativeDelta
            val lineEnd = origLineEnd + cumulativeDelta
            val lineText = content.subSequence(lineStart, lineEnd).toString()
            val existingPrefixLen = detectListPrefixLength(lineText)

            // Always remove any legacy BulletSpans on this line so we never render double bullets
            content.getSpans(lineStart, lineEnd, BulletSpan::class.java).forEach { content.removeSpan(it) }
            if (lineText.startsWith(CHECKLIST_CHECKED_PREFIX)) {
                content.getSpans(lineStart, lineEnd, StrikethroughSpan::class.java).forEach { content.removeSpan(it) }
            }

            val replacement = when {
                allAlreadyTarget -> ""
                ordered -> "${nextNumber++}. "
                else -> UNORDERED_LIST_PREFIX
            }

            if (existingPrefixLen > 0) {
                content.replace(lineStart, lineStart + existingPrefixLen, replacement)
            } else if (replacement.isNotEmpty()) {
                content.insert(lineStart, replacement)
            }

            val delta = replacement.length - existingPrefixLen
            val afterNewPrefix = lineStart + replacement.length

            if (rawSelStart >= origLineStart) {
                newSelStart = if (rawSelStart <= origLineStart + existingPrefixLen) {
                    afterNewPrefix
                } else {
                    (newSelStart + delta).coerceAtLeast(afterNewPrefix)
                }
            }
            if (rawSelEnd >= origLineStart) {
                newSelEnd = if (rawSelEnd <= origLineStart + existingPrefixLen) {
                    afterNewPrefix
                } else {
                    (newSelEnd + delta).coerceAtLeast(afterNewPrefix)
                }
            }
            cumulativeDelta += delta
        }

        // If we turned on/modified an ordered list, renumber any consecutive ordered lines right below
        if (ordered && !allAlreadyTarget) {
            val lastLineEnd = affectedLines.last().second + cumulativeDelta
            renumberFollowingOrderedItems(content, lastLineEnd, nextNumber)
        }

        val finalStart = newSelStart.coerceIn(0, content.length)
        val finalEnd = if (hasRange) newSelEnd.coerceIn(finalStart, content.length) else finalStart
        applyEditableToEditor(content, finalStart, finalEnd)
        refreshStateSnapshot()
    }

    private fun toggleForegroundColor(color: Color) {
        val target: Spannable = boundEditor?.text ?: editable
        val (start, end) = normalizedSelection(target.length) ?: return
        val spans = target.getSpans(start, end, ForegroundColorSpan::class.java)
        val hasColor = spans.any {
            val spanStart = target.getSpanStart(it)
            val spanEnd = target.getSpanEnd(it)
            spanStart <= start && spanEnd >= end && it.foregroundColor == color.toArgb()
        }
        spans.forEach { target.removeSpan(it) }
        if (!hasColor) {
            target.setSpan(
                ForegroundColorSpan(color.toArgb()),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        boundEditor?.setSelection(start, end)
        selectionStart = start
        selectionEnd = end
        editable = SpannableStringBuilder(target)
        refreshStateSnapshot()
    }

    private fun applyParagraphAlignment(alignment: android.text.Layout.Alignment) {
        val target: Spannable = boundEditor?.text ?: editable
        val rawStart = minOf(selectionStart, selectionEnd).coerceIn(0, target.length)
        val rawEnd = maxOf(selectionStart, selectionEnd).coerceIn(rawStart, target.length)
        val selectedLines = collectSelectedLineRanges(target, rawStart, rawEnd)
        if (selectedLines.isEmpty()) {
            pendingParagraphAlignment = if (alignment == android.text.Layout.Alignment.ALIGN_NORMAL) null else alignment
            currentTextAlign = layoutAlignmentToTextAlign(alignment)
            return
        }

        var hasEmptyLine = false
        for ((lineStart, lineEnd) in selectedLines) {
            if (target.subSequence(lineStart, lineEnd).toString() == PAGE_BREAK_TOKEN) continue
            if (lineStart == lineEnd) {
                hasEmptyLine = true
            }
            applyAlignmentToLine(target, lineStart, lineEnd, alignment)
        }

        pendingParagraphAlignment = when {
            alignment == android.text.Layout.Alignment.ALIGN_NORMAL -> null
            hasEmptyLine -> alignment
            else -> null
        }

        editable = SpannableStringBuilder(target)
        boundEditor?.invalidate()
        boundEditor?.requestLayout()
    }

    private fun applyAlignmentToLine(
        target: Spannable,
        lineStart: Int,
        lineEnd: Int,
        alignment: android.text.Layout.Alignment
    ) {
        val safeStart = lineStart.coerceIn(0, target.length)
        val safeEnd = lineEnd.coerceIn(safeStart, target.length)
        val existingSpans = target.getSpans(safeStart, safeEnd, AlignmentSpan::class.java)
        for (span in existingSpans) {
            val sStart = target.getSpanStart(span)
            val sEnd = target.getSpanEnd(span)
            // Only modify spans that actually intersect this line's character span
            if (sEnd < safeStart || sStart > safeEnd) continue
            if (safeStart < safeEnd && (sEnd == safeStart || sStart == safeEnd)) continue
            val existingAlign = span.alignment
            target.removeSpan(span)
            // Preserve portions of a multi-line span on lines outside [safeStart, safeEnd]
            if (sStart < safeStart) {
                val beforeEnd = (safeStart - 1).coerceAtLeast(sStart)
                if (beforeEnd > sStart || (beforeEnd == sStart && existingAlign != android.text.Layout.Alignment.ALIGN_NORMAL)) {
                    target.setSpan(
                        AlignmentSpan.Standard(existingAlign),
                        sStart,
                        beforeEnd,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
            if (sEnd > safeEnd) {
                val afterStart = (safeEnd + 1).coerceAtMost(sEnd)
                if (sEnd > afterStart || (sEnd == afterStart && existingAlign != android.text.Layout.Alignment.ALIGN_NORMAL)) {
                    target.setSpan(
                        AlignmentSpan.Standard(existingAlign),
                        afterStart,
                        sEnd,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
        }

        if (alignment != android.text.Layout.Alignment.ALIGN_NORMAL) {
            target.setSpan(
                AlignmentSpan.Standard(alignment),
                safeStart,
                safeEnd,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    private fun alignmentForLine(
        content: Spanned,
        lineStart: Int,
        lineEnd: Int
    ): android.text.Layout.Alignment {
        val safeStart = lineStart.coerceIn(0, content.length)
        val safeEnd = lineEnd.coerceIn(safeStart, content.length)
        val spans = content.getSpans(safeStart, safeEnd, AlignmentSpan::class.java)
        val matching = spans.lastOrNull { span ->
            val sStart = content.getSpanStart(span)
            val sEnd = content.getSpanEnd(span)
            if (safeStart == safeEnd) {
                sStart <= safeStart && sEnd >= safeStart
            } else {
                sStart < safeEnd && sEnd > safeStart
            }
        }
        return matching?.alignment ?: android.text.Layout.Alignment.ALIGN_NORMAL
    }

    private fun layoutAlignmentToTextAlign(alignment: android.text.Layout.Alignment): TextAlign {
        return when (alignment) {
            android.text.Layout.Alignment.ALIGN_CENTER -> TextAlign.Center
            android.text.Layout.Alignment.ALIGN_OPPOSITE -> TextAlign.End
            else -> TextAlign.Start
        }
    }

    private fun lineBoundsAt(content: CharSequence, offset: Int): Pair<Int, Int> {
        val safeOffset = offset.coerceIn(0, content.length)
        val str = content.toString()
        val lineStart = if (safeOffset == 0) 0 else str.lastIndexOf('\n', safeOffset - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = str.indexOf('\n', safeOffset).let { if (it == -1) content.length else it }
        return lineStart to lineEnd
    }

    private fun collectLineRanges(content: CharSequence): List<Pair<Int, Int>> {
        val str = content.toString()
        val ranges = mutableListOf<Pair<Int, Int>>()
        var start = 0
        while (start <= str.length) {
            val nl = str.indexOf('\n', start)
            if (nl == -1) {
                ranges.add(start to str.length)
                break
            } else {
                ranges.add(start to nl)
                start = nl + 1
            }
        }
        return ranges
    }

    private fun collectSelectedLineRanges(
        content: CharSequence,
        selStart: Int,
        selEnd: Int
    ): List<Pair<Int, Int>> {
        val safeStart = selStart.coerceIn(0, content.length)
        val safeEnd = selEnd.coerceIn(safeStart, content.length)
        val str = content.toString()
        val firstLineStart = if (safeStart == 0) 0 else str.lastIndexOf('\n', safeStart - 1).let { if (it == -1) 0 else it + 1 }
        // If a range selection ends right at the start of the next line (immediately after '\n'), don't include that trailing line
        val effectiveEnd = if (safeEnd > safeStart && str[safeEnd - 1] == '\n') safeEnd - 1 else safeEnd
        val lastLineEnd = str.indexOf('\n', effectiveEnd).let { if (it == -1) content.length else it }

        val ranges = mutableListOf<Pair<Int, Int>>()
        var pos = firstLineStart
        while (pos <= lastLineEnd) {
            val nl = str.indexOf('\n', pos)
            if (nl == -1 || nl >= lastLineEnd) {
                ranges.add(pos to lastLineEnd)
                break
            } else {
                ranges.add(pos to nl)
                pos = nl + 1
            }
        }
        return ranges
    }

    private fun detectListPrefixLength(lineText: String): Int {
        ORDERED_LIST_LINE_REGEX.find(lineText)?.let { return it.value.length }
        if (lineText.startsWith(UNORDERED_LIST_PREFIX)) return UNORDERED_LIST_PREFIX.length
        if (lineText.startsWith(CHECKLIST_UNCHECKED_PREFIX)) return CHECKLIST_UNCHECKED_PREFIX.length
        if (lineText.startsWith(CHECKLIST_CHECKED_PREFIX)) return CHECKLIST_CHECKED_PREFIX.length
        return 0
    }

    private fun renumberFollowingOrderedItems(
        content: SpannableStringBuilder,
        fromOffset: Int,
        startNumber: Int
    ) {
        var nextNum = startNumber
        var pos = content.toString().indexOf('\n', fromOffset.coerceIn(0, content.length))
        while (pos != -1 && pos + 1 <= content.length) {
            val lineStart = pos + 1
            val lineEnd = content.toString().indexOf('\n', lineStart).let { if (it == -1) content.length else it }
            val lineText = content.subSequence(lineStart, lineEnd).toString()
            val match = ORDERED_LIST_LINE_REGEX.find(lineText) ?: break
            val desired = "${nextNum++}. "
            if (match.value != desired) {
                content.replace(lineStart, lineStart + match.value.length, desired)
                pos = content.toString().indexOf('\n', lineStart + desired.length)
            } else {
                pos = content.toString().indexOf('\n', lineEnd)
            }
        }
    }

    private fun <T> toggleSpan(span: T, clazz: Class<T>) where T : Any {
        val target: Spannable = boundEditor?.text ?: editable
        val (start, end) = normalizedSelection(target.length) ?: return
        val spans = target.getSpans(start, end, clazz)
        val hasSpan = spans.any {
            val spanStart = target.getSpanStart(it)
            val spanEnd = target.getSpanEnd(it)
            spanStart <= start && spanEnd >= end && sameStyle(it, span)
        }
        spans.filter { sameStyle(it, span) }.forEach { target.removeSpan(it) }
        if (!hasSpan) {
            target.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        boundEditor?.setSelection(start, end)
        selectionStart = start
        selectionEnd = end
        editable = SpannableStringBuilder(target)
        refreshStateSnapshot()
    }

    private fun sameStyle(existing: Any, target: Any): Boolean {
        return when {
            existing is StyleSpan && target is StyleSpan -> existing.style == target.style
            existing::class == target::class -> true
            else -> false
        }
    }

    private fun normalizedSelection(max: Int): Pair<Int, Int>? {
        if (max <= 0) return null
        val start = selectionStart.coerceIn(0, max)
        val end = selectionEnd.coerceIn(0, max)
        return if (start == end) {
            if (start < max) start to (start + 1) else (start - 1).coerceAtLeast(0) to start
        } else {
            minOf(start, end) to maxOf(start, end)
        }
    }

    private fun pushToEditor() {
        applyEditableToEditor(editable)
    }

    private fun applyEditableToEditor(
        content: SpannableStringBuilder,
        cursorPosition: Int
    ) {
        applyEditableToEditor(content, cursorPosition, cursorPosition)
    }

    private fun applyEditableToEditor(
        content: SpannableStringBuilder,
        selectionStartPos: Int = selectionStart,
        selectionEndPos: Int = selectionEnd
    ) {
        isProgrammaticChange = true
        try {
            boundEditor?.let { editor ->
                val current = editor.text
                if (current != null && current !== content) {
                    if (current.toString() != content.toString()) {
                        editor.setText(content, TextView.BufferType.EDITABLE)
                    } else {
                        editor.text?.replace(0, editor.text!!.length, content)
                    }
                }
                val textLen = editor.text?.length ?: 0
                val safeStart = selectionStartPos.coerceIn(0, textLen)
                val safeEnd = selectionEndPos.coerceIn(0, textLen)
                val minPos = minOf(safeStart, safeEnd)
                val maxPos = maxOf(safeStart, safeEnd)
                editor.setSelection(minPos, maxPos)
                selectionStart = minPos
                selectionEnd = maxPos
                return
            }
            editable = SpannableStringBuilder(content)
            val textLen = editable.length
            val safeStart = selectionStartPos.coerceIn(0, textLen)
            val safeEnd = selectionEndPos.coerceIn(0, textLen)
            selectionStart = minOf(safeStart, safeEnd)
            selectionEnd = maxOf(safeStart, safeEnd)
        } finally {
            isProgrammaticChange = false
        }
    }

    private fun refreshStateSnapshot() {
        val content = currentEditable()
        annotatedString = AnnotatedString(content.toString())
        currentSpanStyle = extractCurrentSpanStyle(content)

        // Sync list-type and paragraph alignment flags with the actual line under the cursor
        val cursor = selectionEnd.coerceIn(0, content.length)
        val (lineStart, lineEnd) = lineBoundsAt(content, cursor)
        val lineText = content.subSequence(lineStart, lineEnd).toString()
        isOrderedList = ORDERED_LIST_LINE_REGEX.containsMatchIn(lineText)
        isUnorderedList = lineText.startsWith(UNORDERED_LIST_PREFIX)
        isChecklist = lineText.startsWith(CHECKLIST_UNCHECKED_PREFIX) || lineText.startsWith(CHECKLIST_CHECKED_PREFIX)

        val lineAlign = alignmentForLine(content, lineStart, lineEnd)
        currentTextAlign = if (lineAlign != android.text.Layout.Alignment.ALIGN_NORMAL) {
            layoutAlignmentToTextAlign(lineAlign)
        } else if (lineStart == lineEnd && pendingParagraphAlignment != null) {
            layoutAlignmentToTextAlign(pendingParagraphAlignment!!)
        } else {
            TextAlign.Start
        }
    }

    private fun extractCurrentSpanStyle(content: SpannableStringBuilder): SpanStyle {
        if (content.isEmpty()) return SpanStyle()
        val index = (selectionEnd - 1).coerceIn(0, content.length - 1)
        val styleSpans = content.getSpans(index, index + 1, StyleSpan::class.java)
        val underline = content.getSpans(index, index + 1, UnderlineSpan::class.java).isNotEmpty()
        val strike = content.getSpans(index, index + 1, StrikethroughSpan::class.java).isNotEmpty()
        val colorSpan = content.getSpans(index, index + 1, ForegroundColorSpan::class.java).lastOrNull()
        val hasHighlight = selectionStart != selectionEnd
        if (hasHighlight) {
            val selStart = minOf(selectionStart, selectionEnd).coerceIn(0, content.length)
            val selEnd = maxOf(selectionStart, selectionEnd).coerceIn(0, content.length)
            val sizeSpans = content.getSpans(selStart, selEnd, AbsoluteSizeSpan::class.java)
            if (sizeSpans.isNotEmpty()) {
                val size = sizeSpans.last().size
                currentFontSizeSp = size
                currentHeadingStyle = HeadingStyle.values().firstOrNull { h -> h.sizeSp == size } ?: HeadingStyle.BODY_1
            } else {
                currentFontSizeSp = 16
                currentHeadingStyle = HeadingStyle.BODY_1
            }
        } else {
            if (activeTypingFontSizeSp != null) {
                currentFontSizeSp = activeTypingFontSizeSp!!
                currentHeadingStyle = HeadingStyle.values().firstOrNull { h -> h.sizeSp == currentFontSizeSp } ?: HeadingStyle.BODY_1
            } else {
                val sizeSpans = content.getSpans(index, (index + 1).coerceAtMost(content.length), AbsoluteSizeSpan::class.java)
                if (sizeSpans.isNotEmpty()) {
                    val size = sizeSpans.last().size
                    currentFontSizeSp = size
                    currentHeadingStyle = HeadingStyle.values().firstOrNull { h -> h.sizeSp == size } ?: HeadingStyle.BODY_1
                } else {
                    currentFontSizeSp = 16
                    currentHeadingStyle = HeadingStyle.BODY_1
                }
            }
        }
        val bold = styleSpans.any { it.style == Typeface.BOLD || it.style == Typeface.BOLD_ITALIC }
        val italic = styleSpans.any { it.style == Typeface.ITALIC || it.style == Typeface.BOLD_ITALIC }
        val textDecoration = when {
            underline && strike -> androidx.compose.ui.text.style.TextDecoration.combine(
                listOf(
                    androidx.compose.ui.text.style.TextDecoration.Underline,
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
            )
            underline -> androidx.compose.ui.text.style.TextDecoration.Underline
            strike -> androidx.compose.ui.text.style.TextDecoration.LineThrough
            else -> null
        }
        return SpanStyle(
            fontWeight = if (bold) FontWeight.Bold else null,
            fontStyle = if (italic) FontStyle.Italic else null,
            textDecoration = textDecoration,
            color = colorSpan?.let { Color(it.foregroundColor) } ?: Color.Unspecified
        )
    }

    private fun currentEditable(): SpannableStringBuilder {
        return boundEditor?.text?.let { SpannableStringBuilder(it) } ?: editable
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun captureSnapshot(): Snapshot {
        return Snapshot(
            content = SpannableStringBuilder(currentEditable()),
            selectionStart = selectionStart,
            selectionEnd = selectionEnd
        )
    }

    private fun restoreSnapshot(snapshot: Snapshot) {
        isHistoryMuted = true
        applyEditableToEditor(snapshot.content, snapshot.selectionEnd)
        selectionStart = snapshot.selectionStart
        selectionEnd = snapshot.selectionEnd
        isHistoryMuted = false
        refreshStateSnapshot()
    }

    private fun isContentEquivalent(a: SpannableStringBuilder, b: SpannableStringBuilder): Boolean {
        return HtmlCompat.toHtml(a, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE) ==
                HtmlCompat.toHtml(b, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
    }

    private fun updateHistoryFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }
}

data class RichTextEditorColors(
    val textColor: Color = Color.Unspecified,
    val containerColor: Color = Color.Transparent,
    val focusedIndicatorColor: Color = Color.Transparent,
    val unfocusedIndicatorColor: Color = Color.Transparent
)

object RichTextEditorDefaults {
    fun richTextEditorColors(
        textColor: Color = Color.Unspecified,
        containerColor: Color = Color.Transparent,
        focusedIndicatorColor: Color = Color.Transparent,
        unfocusedIndicatorColor: Color = Color.Transparent
    ): RichTextEditorColors {
        return RichTextEditorColors(
            textColor = textColor,
            containerColor = containerColor,
            focusedIndicatorColor = focusedIndicatorColor,
            unfocusedIndicatorColor = unfocusedIndicatorColor
        )
    }
}

@Composable
fun RichTextEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    colors: RichTextEditorColors = RichTextEditorDefaults.richTextEditorColors(),
    readOnly: Boolean = false,
    drawingBlocks: List<NoteBlock> = emptyList(),
    drawingEnabled: Boolean = false,
    drawingTool: CanvasDrawingTool = CanvasDrawingTool.PEN,
    drawingColorArgb: Long = 0xFF000000,
    drawingStrokeWidthDp: Float = 4f,
    drawingAlpha: Float = 1f,
    onStrokeDrawn: ((pageIndex: Int, normalizedPoints: List<Pair<Float, Float>>, colorArgb: Long, strokeWidthPx: Float, alpha: Float, tool: String) -> Unit)? = null,
    onEraseStroke: ((pageIndex: Int, normalizedX: Float, normalizedY: Float, normalizedRadius: Float) -> Unit)? = null,
    onScrollChanged: ((scrollX: Int, scrollY: Int) -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                RichEditText(context).apply {
                    contentDescription = "Notebook Canvas Editor"
                    movementMethod = ArrowKeyMovementMethod.getInstance()
                    linksClickable = true
                    gravity = Gravity.TOP or Gravity.START
                    setTextColor(
                        if (colors.textColor == Color.Unspecified) currentTextColor else colors.textColor.toArgb()
                    )
                    background = null
                    isFocusable = !readOnly
                    isFocusableInTouchMode = !readOnly
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    onFocusGained = {
                        focusManager.clearFocus()
                    }
                    onChecklistToggleRequested = { offset ->
                        state.toggleChecklistAtOffset(offset)
                    }
                    onScrollListener = onScrollChanged
                    this.drawingBlocks = drawingBlocks
                    this.drawingEnabled = drawingEnabled
                    this.drawingTool = drawingTool
                    this.drawingColorArgb = drawingColorArgb
                    this.drawingStrokeWidthDp = drawingStrokeWidthDp
                    this.drawingAlpha = drawingAlpha
                    this.onStrokeDrawn = onStrokeDrawn
                    this.onEraseStroke = onEraseStroke

                    addTextChangedListener(object : TextWatcher {
                        private var insertStart = 0
                        private var insertCount = 0

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            insertStart = start
                            insertCount = after
                        }
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                        override fun afterTextChanged(s: Editable?) {
                            if (s != null && insertCount > 0 && !state.isProgrammaticChange) {
                                val end = (insertStart + insertCount).coerceAtMost(s.length)
                                state.applyActiveTypingStyle(s, insertStart, end)
                            }
                            state.onEditorContentChanged(s, selectionStart, selectionEnd)
                        }
                    })

                    onSelectionChangedListener = { selStart, selEnd ->
                        state.onEditorContentChanged(text, selStart, selEnd)
                    }
                    state.bindEditor(this)
                    this.richTextState = state
                }
            },
            update = { editText ->
                val desiredFocusable = !readOnly
                if (editText.isFocusable != desiredFocusable) {
                    editText.isFocusable = desiredFocusable
                }
                if (editText.isFocusableInTouchMode != desiredFocusable) {
                    editText.isFocusableInTouchMode = desiredFocusable
                }
                val desiredGravity = Gravity.TOP or Gravity.START
                if (editText.gravity != desiredGravity) {
                    editText.gravity = desiredGravity
                }
                if (colors.textColor != Color.Unspecified) {
                    val desiredColor = colors.textColor.toArgb()
                    if (editText.currentTextColor != desiredColor) {
                        editText.setTextColor(desiredColor)
                    }
                }
                val desiredCursorVisible = !readOnly && !drawingEnabled
                if (editText.isCursorVisible != desiredCursorVisible) {
                    editText.isCursorVisible = desiredCursorVisible
                }
                editText.onChecklistToggleRequested = { offset ->
                    state.toggleChecklistAtOffset(offset)
                }
                editText.onScrollListener = onScrollChanged
                editText.drawingBlocks = drawingBlocks
                editText.drawingEnabled = drawingEnabled
                editText.drawingTool = drawingTool
                editText.drawingColorArgb = drawingColorArgb
                editText.drawingStrokeWidthDp = drawingStrokeWidthDp
                editText.drawingAlpha = drawingAlpha
                editText.onStrokeDrawn = onStrokeDrawn
                editText.onEraseStroke = onEraseStroke
                if (editText.richTextState !== state) {
                    state.bindEditor(editText)
                    editText.richTextState = state
                    editText.requestLayout()
                    editText.invalidate()
                }
            }
        )
        if (placeholder != null && state.annotatedString.text.isBlank()) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                placeholder()
            }
        }
    }
}

@Composable
fun PagedRichTextEditor(
    state: RichTextState,
    modifier: Modifier = Modifier,
    placeholder: (@Composable () -> Unit)? = null,
    colors: RichTextEditorColors = RichTextEditorDefaults.richTextEditorColors(),
    readOnly: Boolean = false,
    pageHeight: Dp = 1000.dp,
    pageGap: Dp = 24.dp
) {
    val density = LocalDensity.current
    Box(modifier = modifier) {
        RichTextEditor(
            state = state,
            modifier = Modifier.fillMaxSize(),
            placeholder = placeholder,
            colors = colors,
            readOnly = readOnly
        )
        Text(
            text = "Page ${state.pageCount()}",
            modifier = Modifier.padding(8.dp),
            color = Color(0x99000000)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pageHeightPx = with(density) { pageHeight.toPx() }
            val gapPx = with(density) { pageGap.toPx() }
            var y = pageHeightPx
            val pageStroke = Stroke(width = 2f)
            while (y < size.height) {
                drawRoundRect(
                    color = Color(0x22000000),
                    topLeft = Offset(0f, y - pageHeightPx),
                    size = Size(size.width, pageHeightPx),
                    style = pageStroke
                )
                y += pageHeightPx + gapPx
            }
        }
    }
}

@Composable
fun RichText(
    state: RichTextState,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                linksClickable = true
                setLineSpacing(0f, 1.1f)
            }
        },
        update = { textView ->
            textView.text = HtmlCompat.fromHtml(state.toHtml(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            style.color.takeIf { it != Color.Unspecified }?.let { textView.setTextColor(it.toArgb()) }
            textView.textSize = style.fontSize.resolveSpOrFallback(16f)
            textView.maxLines = maxLines
            textView.ellipsize = if (overflow == TextOverflow.Ellipsis) android.text.TextUtils.TruncateAt.END else null
            textView.gravity = when (style.textAlign) {
                TextAlign.Center -> Gravity.CENTER_HORIZONTAL
                TextAlign.End, TextAlign.Right -> Gravity.END
                else -> Gravity.START
            }
            textView.typeface = when (style.fontWeight) {
                FontWeight.Bold -> Typeface.DEFAULT_BOLD
                else -> Typeface.DEFAULT
            }
            textView.setPadding(0, 0, 0, 0)
        }
    )
}

private fun TextUnit.resolveSpOrFallback(fallback: Float): Float {
    return if (isSpecified) value else fallback
}

internal class RichEditText(context: Context) : AppCompatEditText(context) {
    companion object {
        const val LINES_PER_PAGE = 15
        const val PAGE_LINE_STRIDE = LINES_PER_PAGE + 2
    }

    var onSelectionChangedListener: ((Int, Int) -> Unit)? = null
    var onChecklistToggleRequested: ((Int) -> Boolean)? = null
    var onFocusGained: (() -> Unit)? = null
    var onScrollListener: ((Int, Int) -> Unit)? = null
    var onPageBreakAutoInserted: (() -> Unit)? = null

    var richTextState: RichTextState? = null

    var drawingBlocks: List<NoteBlock> = emptyList()
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }
    var drawingEnabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            isCursorVisible = !value
            invalidate()
        }
    var drawingTool: CanvasDrawingTool = CanvasDrawingTool.PEN
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }
    var drawingColorArgb: Long = 0xFF000000
    var drawingStrokeWidthDp: Float = 4f
    var drawingAlpha: Float = 1f

    var onStrokeDrawn: ((pageIndex: Int, normalizedPoints: List<Pair<Float, Float>>, colorArgb: Long, strokeWidthPx: Float, alpha: Float, tool: String) -> Unit)? = null
    var onEraseStroke: ((pageIndex: Int, normalizedX: Float, normalizedY: Float, normalizedRadius: Float) -> Unit)? = null

    private val density = context.resources.displayMetrics.density
    val fixedLineHeight: Int = (38f * density).roundToInt()
    private val topPadPx: Int = (20f * density).roundToInt()
    private val bottomPadPx: Int = (20f * density).roundToInt()
    val bottomScrollPaddingPx: Int = (180f * density).roundToInt()
    val floatingOverlayClearancePx: Int = (140f * density).roundToInt()
    private val horizontalPadPx: Int = (16f * density).roundToInt()
    val pageGapPx: Int = (36f * density).roundToInt()

    val pageContentHeight: Int = LINES_PER_PAGE * fixedLineHeight
    val pageHeightPx: Int = pageContentHeight + topPadPx + bottomPadPx

    private val cardInsetPx: Float = 8f * density
    private val cardCornerRadiusPx: Float = 8f * density
    private val scrollbarTouchWidthPx: Float = 24f * density
    private val scrollbarBottomClearancePx: Float = 56f * density
    private val scrollbarTopMarginPx: Float = 8f * density
    private val scrollbarRightMarginPx: Float = 3f * density
    private val scrollbarWidthPx: Float = 5f * density
    private val scrollbarDraggingWidthPx: Float = 7f * density

    private var downX = 0f
    private var downY = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var isScrollbarDragging = false
    private var downTime = 0L
    private var isTwoFingerScrolling = false
    private var lastTwoFingerY = 0f
    private var viewInitialized = false

    fun getMaxScrollY(): Int {
        val pages = getTotalPages()
        val totalH = pages * pageHeightPx + (pages - 1) * pageGapPx + bottomScrollPaddingPx
        return maxOf(0, totalH - height)
    }

    override fun scrollTo(x: Int, y: Int) {
        val clampedY = y.coerceIn(0, getMaxScrollY())
        super.scrollTo(x, clampedY)
    }

    override fun bringPointIntoView(offset: Int): Boolean {
        if (!viewInitialized || drawingEnabled || height <= 0) return false
        val prevScrollY = scrollY
        ensureCursorVisible(offset)
        return scrollY != prevScrollY
    }

    fun ensureCursorVisible(targetOffset: Int = selectionEnd) {
        if (!viewInitialized || drawingEnabled || height <= 0) return
        if (isDragging || isScrollbarDragging) return
        val l = layout ?: return
        val textLen = text?.length ?: 0
        val selEnd = if (targetOffset >= 0) targetOffset else selectionEnd
        if (selEnd < 0 || l.lineCount <= 0) return
        val cursor = selEnd.coerceIn(0, textLen)
        val line = l.getLineForOffset(cursor).coerceIn(0, l.lineCount - 1)
        val lineTop = totalPaddingTop + l.getLineTop(line)
        val lineBottom = totalPaddingTop + maxOf(l.getLineBottom(line), l.getLineTop(line) + fixedLineHeight)

        val effectiveBottomClearance = floatingOverlayClearancePx
            .coerceAtMost((height - fixedLineHeight - topPadPx).coerceAtLeast(0))
        val visibleTop = scrollY + topPadPx
        val visibleBottom = scrollY + height - effectiveBottomClearance

        if (lineTop < visibleTop) {
            val targetY = (lineTop - topPadPx).coerceIn(0, getMaxScrollY())
            if (targetY != scrollY) {
                scrollTo(scrollX, targetY)
            }
        } else if (lineBottom > visibleBottom) {
            val availableBand = (height - effectiveBottomClearance - topPadPx - (lineBottom - lineTop)).coerceAtLeast(0)
            val comfortPx = availableBand.coerceAtMost((12f * density).roundToInt())
            val targetY = (lineBottom + comfortPx - (height - effectiveBottomClearance)).coerceIn(0, getMaxScrollY())
            if (targetY != scrollY) {
                scrollTo(scrollX, targetY)
            }
        }
    }

    private fun scrollFromScrollbarY(touchY: Float) {
        val maxScroll = getMaxScrollY()
        if (maxScroll <= 0 || height <= 0) return
        val trackTop = scrollbarTopMarginPx
        val trackBottom = (height - scrollbarBottomClearancePx).coerceAtLeast(trackTop + 1f)
        val trackHeight = (trackBottom - trackTop).coerceAtLeast(1f)
        val totalContentHeight = (height + maxScroll).toFloat().coerceAtLeast(1f)
        val minThumbHeight = (40f * density).coerceAtMost(trackHeight)
        val thumbHeight = (trackHeight * (height.toFloat() / totalContentHeight)).coerceIn(minThumbHeight, trackHeight)
        val thumbTravel = (trackHeight - thumbHeight).coerceAtLeast(1f)
        val fraction = ((touchY - trackTop - thumbHeight / 2f) / thumbTravel).coerceIn(0f, 1f)
        scrollTo(scrollX, (fraction * maxScroll).roundToInt())
        invalidate()
    }

    private val activeStroke = mutableListOf<Pair<Float, Float>>()
    private var activeStrokePageIndex = 0
    private val cardRect = android.graphics.RectF()
    private val cardPath = android.graphics.Path()
    private val strokePath = android.graphics.Path()
    private val scrollbarTrackRect = android.graphics.RectF()
    private val scrollbarThumbRect = android.graphics.RectF()

    private val pageCardPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }
    private val pageCardBorderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1f * density
    }
    private val ruledLinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1f * density
    }
    private val pageCounterPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * density
        textAlign = android.graphics.Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    private val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }
    private val scrollbarTrackPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }
    private val scrollbarThumbPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
    }

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        gravity = Gravity.TOP or Gravity.START
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setPadding(horizontalPadPx, topPadPx, horizontalPadPx, bottomPadPx)
        val fm = paint.fontMetricsInt
        val fontHeight = fm.descent - fm.ascent
        val extra = (fixedLineHeight - fontHeight).coerceAtLeast(0)
        setLineSpacing(extra.toFloat(), 1.0f)
        viewInitialized = true
    }

    fun getTotalPages(): Int {
        val statePages = richTextState?.pageCount() ?: 1
        val l = layout
        val lPages = if (l != null && l.lineCount > 0) {
            ((l.lineCount - 1) / PAGE_LINE_STRIDE) + 1
        } else 1
        return maxOf(statePages, lPages, 1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val pages = getTotalPages()
        val totalH = pages * pageHeightPx + (pages - 1) * pageGapPx + bottomScrollPaddingPx
        val w = measuredWidth
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)
        val h = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> minOf(maxOf(measuredHeight, totalH), hSize)
            else -> maxOf(measuredHeight, totalH)
        }
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!viewInitialized) return
        if (h > 0 && oldh > 0 && h < oldh && !drawingEnabled) {
            ensureCursorVisible()
            post { ensureCursorVisible() }
        } else if (scrollY > getMaxScrollY()) {
            scrollTo(scrollX, getMaxScrollY())
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (viewInitialized && height > 0 && !drawingEnabled) {
            ensureCursorVisible()
            post { ensureCursorVisible() }
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollListener?.invoke(l, t)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        val prevSelStart = selectionStart
        val prevSelEnd = selectionEnd
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus && selectionStart < 0 && prevSelStart >= 0 && prevSelEnd >= 0 && !text.isNullOrEmpty()) {
            val len = text!!.length
            setSelection(prevSelStart.coerceIn(0, len), prevSelEnd.coerceIn(0, len))
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val baseConnection = super.onCreateInputConnection(outAttrs) ?: return null
        return object : InputConnectionWrapper(baseConnection, true) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                if (text == "\n") {
                    if (handleEnterKey()) return true
                }
                return super.commitText(text, newCursorPosition)
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                if (beforeLength == 1 && afterLength == 0) {
                    if (handleBackspaceKey()) return true
                }
                return super.deleteSurroundingText(beforeLength, afterLength)
            }

            override fun sendKeyEvent(event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN) {
                    if (event.keyCode == KeyEvent.KEYCODE_ENTER && handleEnterKey()) return true
                    if (event.keyCode == KeyEvent.KEYCODE_DEL && handleBackspaceKey()) return true
                }
                return super.sendKeyEvent(event)
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                if (handleEnterKey()) return true
            } else if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                if (handleBackspaceKey()) return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun handleEnterKey(): Boolean {
        val st = richTextState ?: return false
        val currentText = text ?: return false
        val cursor = selectionEnd.coerceIn(0, currentText.length)
        val pageIndex = st.currentPageIndex()
        val pageRanges = st.pageRanges()
        val currentRange = pageRanges.getOrNull(pageIndex) ?: (0..currentText.length)

        val l = layout
        val lineOnPage = if (l != null && cursor in 0..currentText.length) {
            val startLine = l.getLineForOffset(currentRange.first.coerceIn(0, currentText.length))
            val cursorLine = l.getLineForOffset(cursor)
            cursorLine - startLine
        } else {
            val pageSub = currentText.subSequence(currentRange.first.coerceIn(0, currentText.length), cursor).toString()
            pageSub.count { it == '\n' }
        }

        val onPageBoundary = lineOnPage >= LINES_PER_PAGE - 1 // 14 means 15th line (0-indexed)
        val pagesBefore = st.pageCount()
        if (st.handleListEnter(onPageBoundary = onPageBoundary)) {
            requestLayout()
            invalidate()
            if (st.pageCount() > pagesBefore) {
                onPageBreakAutoInserted?.invoke()
                post {
                    val nextPageIndex = pageIndex + 1
                    val nextY = nextPageIndex * (pageHeightPx + pageGapPx)
                    scrollTo(0, nextY)
                    ensureCursorVisible()
                }
            } else {
                ensureCursorVisible()
                post { ensureCursorVisible() }
            }
            return true
        }

        if (onPageBoundary) {
            st.insertPageBreak()
            onPageBreakAutoInserted?.invoke()
            requestLayout()
            invalidate()
            post {
                val nextPageIndex = pageIndex + 1
                val nextY = nextPageIndex * (pageHeightPx + pageGapPx)
                scrollTo(0, nextY)
                ensureCursorVisible()
            }
            return true
        }
        return false
    }

    private fun handleBackspaceKey(): Boolean {
        val st = richTextState ?: return false
        val cursor = selectionEnd
        val marker = PAGE_BREAK_MARKER
        val content = text?.toString() ?: ""
        if (cursor >= marker.length && content.regionMatches(cursor - marker.length, marker, 0, marker.length)) {
            st.removeCurrentPageBreak()
            requestLayout()
            invalidate()
            return true
        }
        if (st.handleListBackspace()) {
            requestLayout()
            invalidate()
            return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.actionMasked
        if (isScrollbarDragging) {
            when (actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    scrollFromScrollbarY(event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isScrollbarDragging = false
                    invalidate()
                    return true
                }
            }
            return true
        }
        if (
            actionMasked == MotionEvent.ACTION_DOWN &&
            event.pointerCount == 1 &&
            getMaxScrollY() > 0 &&
            event.x >= width - scrollbarTouchWidthPx &&
            event.y >= scrollbarTopMarginPx &&
            event.y <= (height - scrollbarBottomClearancePx).coerceAtLeast(scrollbarTopMarginPx)
        ) {
            isScrollbarDragging = true
            isDragging = false
            isPressed = false
            cancelLongPress()
            parent?.requestDisallowInterceptTouchEvent(true)
            scrollFromScrollbarY(event.y)
            return true
        }

        if (drawingEnabled) {
            parent?.requestDisallowInterceptTouchEvent(true)
            val action = event.actionMasked

            // 2-finger vertical scrolling while in drawing mode
            if (event.pointerCount >= 2) {
                val avgY = (event.getY(0) + event.getY(1)) / 2f
                when (action) {
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        activeStroke.clear()
                        isTwoFingerScrolling = true
                        lastTwoFingerY = avgY
                        invalidate()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!isTwoFingerScrolling) {
                            activeStroke.clear()
                            isTwoFingerScrolling = true
                            lastTwoFingerY = avgY
                        } else {
                            val dy = lastTwoFingerY - avgY
                            lastTwoFingerY = avgY
                            val maxScroll = getMaxScrollY()
                            if (maxScroll > 0) {
                                val newScrollY = (scrollY + dy.toInt()).coerceIn(0, maxScroll)
                                scrollTo(scrollX, newScrollY)
                            }
                        }
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        // Keep scrolling state until all fingers lift to prevent stray marks
                        isTwoFingerScrolling = true
                    }
                }
                return true
            }

            if (isTwoFingerScrolling) {
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    isTwoFingerScrolling = false
                }
                return true
            }

            val totalPages = getTotalPages()
            val contentWidth = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
            val padL = paddingLeft.toFloat()

            val canvasX = event.x + scrollX
            val canvasY = event.y + scrollY

            val singlePageTotal = (pageHeightPx + pageGapPx).toFloat().coerceAtLeast(1f)
            val touchedPage = (canvasY / singlePageTotal).toInt().coerceIn(0, totalPages - 1)
            val pageTop = touchedPage * singlePageTotal
            val nx = ((canvasX - padL) / contentWidth).coerceIn(0f, 1f)
            val ny = ((canvasY - pageTop - topPadPx) / pageContentHeight.toFloat()).coerceIn(0f, 1f)

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    if (drawingTool == CanvasDrawingTool.ERASER) {
                        val eraserRadius = (drawingStrokeWidthDp * 2.5f * density / contentWidth).coerceAtLeast(0.015f)
                        onEraseStroke?.invoke(touchedPage, nx, ny, eraserRadius)
                    } else {
                        activeStrokePageIndex = touchedPage
                        activeStroke.clear()
                        activeStroke.add(nx to ny)
                    }
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (drawingTool == CanvasDrawingTool.ERASER) {
                        val eraserRadius = (drawingStrokeWidthDp * 2.5f * density / contentWidth).coerceAtLeast(0.015f)
                        onEraseStroke?.invoke(touchedPage, nx, ny, eraserRadius)
                    } else {
                        // Lock stroke coordinates to activeStrokePageIndex (clamped 0f..1f)
                        val lockedPage = activeStrokePageIndex.coerceIn(0, totalPages - 1)
                        val lockedPageTop = lockedPage * singlePageTotal
                        val lockedNy = ((canvasY - lockedPageTop - topPadPx) / pageContentHeight.toFloat()).coerceIn(0f, 1f)
                        activeStroke.add(nx to lockedNy)
                        invalidate()
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (drawingTool != CanvasDrawingTool.ERASER && activeStroke.size > 1) {
                        onStrokeDrawn?.invoke(
                            activeStrokePageIndex,
                            activeStroke.toList(),
                            drawingColorArgb,
                            drawingStrokeWidthDp * density,
                            drawingAlpha,
                            drawingTool.name
                        )
                    }
                    activeStroke.clear()
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    activeStroke.clear()
                    invalidate()
                    return true
                }
            }
            return true
        }

        if (!isFocusable) {
            val maxScroll = getMaxScrollY()
            if (maxScroll <= 0) {
                // Full-height read-only preview inside a parent scroll container (e.g. PreviewScreen verticalScroll):
                // never block parent scrolling; only intercept a tap directly on a URLSpan.
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        if (linksClickable && findUrlSpanAt(event.x, event.y) != null) {
                            downX = event.x
                            downY = event.y
                            downTime = System.currentTimeMillis()
                            return true
                        }
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        val dx = Math.abs(event.x - downX)
                        val dy = Math.abs(event.y - downY)
                        val dt = System.currentTimeMillis() - downTime
                        if (linksClickable && dx < 24 && dy < 24 && dt < 450) {
                            findUrlSpanAt(event.x, event.y)?.let { span ->
                                span.onClick(this)
                                return true
                            }
                        }
                        return false
                    }
                    else -> return false
                }
            }
            // Bounded read-only viewport: allow vertical drag scrolling + URLSpan taps without opening keyboard
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    lastTouchY = event.y
                    isDragging = false
                    downTime = System.currentTimeMillis()
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = lastTouchY - event.y
                    lastTouchY = event.y
                    if (Math.abs(event.y - downY) > 8f || isDragging) {
                        isDragging = true
                        val newScrollY = (scrollY + dy.toInt()).coerceIn(0, maxScroll)
                        scrollTo(scrollX, newScrollY)
                        return true
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = Math.abs(event.x - downX)
                    val dy = Math.abs(event.y - downY)
                    val dt = System.currentTimeMillis() - downTime
                    if (!isDragging && linksClickable && dx < 24 && dy < 24 && dt < 450) {
                        findUrlSpanAt(event.x, event.y)?.onClick(this)
                    }
                    isDragging = false
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    return true
                }
            }
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastTouchY = event.y
                isDragging = false
                downTime = System.currentTimeMillis()
                if (getMaxScrollY() > 0) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = lastTouchY - event.y
                lastTouchY = event.y
                val maxScroll = getMaxScrollY()
                if (maxScroll > 0 && (Math.abs(event.y - downY) > 8f || isDragging)) {
                    if (!isDragging) {
                        isDragging = true
                        isPressed = false
                        cancelLongPress()
                        val cancelEvent = MotionEvent.obtain(event).apply {
                            action = MotionEvent.ACTION_CANCEL
                        }
                        super.onTouchEvent(cancelEvent)
                        cancelEvent.recycle()
                    }
                    val newScrollY = (scrollY + dy.toInt()).coerceIn(0, maxScroll)
                    scrollTo(scrollX, newScrollY)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    isDragging = false
                    return true
                }
                val dx = Math.abs(event.x - downX)
                val dy = Math.abs(event.y - downY)
                val dt = System.currentTimeMillis() - downTime
                val isTap = dx < 24 && dy < 24 && dt < 450

                if (isTap) {
                    // Cleanly terminate super's pending touch state so Editor never resets our selection
                    isPressed = false
                    cancelLongPress()
                    val cancelEvent = MotionEvent.obtain(event).apply {
                        action = MotionEvent.ACTION_CANCEL
                    }
                    super.onTouchEvent(cancelEvent)
                    cancelEvent.recycle()

                    if (!hasFocus()) {
                        onFocusGained?.invoke()
                        requestFocus()
                    }
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

                    val x = (event.x + scrollX - totalPaddingLeft).coerceAtLeast(0f)
                    val y = (event.y + scrollY - totalPaddingTop).coerceAtLeast(0f)
                    val l = layout

                    // Check if tapped near checklist checkbox (first 3 chars of line) or directly on a URLSpan
                    if (l != null && !text.isNullOrEmpty() && y <= l.height) {
                        val line = l.getLineForVertical(y.toInt()).coerceIn(0, (l.lineCount - 1).coerceAtLeast(0))
                        val lineStart = l.getLineStart(line)
                        val offset = l.getOffsetForHorizontal(line, x).coerceIn(0, text!!.length)
                        if (offset <= lineStart + 3 && onChecklistToggleRequested?.invoke(offset) == true) {
                            return true
                        }
                        if (linksClickable && x <= l.getLineRight(line)) {
                            val spanned = text
                            if (spanned != null) {
                                val urlSpans = spanned.getSpans(offset, offset, URLSpan::class.java)
                                val hitSpan = urlSpans.firstOrNull { span ->
                                    val spanStart = spanned.getSpanStart(span)
                                    val spanEnd = spanned.getSpanEnd(span)
                                    offset in spanStart until spanEnd
                                }
                                if (hitSpan != null) {
                                    hitSpan.onClick(this)
                                    return true
                                }
                            }
                        }
                    }

                    // Tapped on notebook lines
                    val currentText = text ?: SpannableStringBuilder()
                    val lHeight = fixedLineHeight.coerceAtLeast(1)
                    val totalPages = getTotalPages()
                    val totalAllowedLines = (totalPages - 1) * PAGE_LINE_STRIDE + LINES_PER_PAGE

                    val rawTargetLine = (y / lHeight).toInt().coerceAtLeast(0)
                    val targetLine = rawTargetLine.coerceIn(0, totalAllowedLines - 1)

                    if (l != null && !text.isNullOrEmpty() && y <= l.height) {
                        val line = l.getLineForVertical(y.toInt()).coerceIn(0, (l.lineCount - 1).coerceAtLeast(0))
                        val offset = l.getOffsetForHorizontal(line, x).coerceIn(0, text!!.length)
                        setSelection(offset)
                        ensureCursorVisible()
                        post { ensureCursorVisible() }
                        return true
                    }

                    if (l == null || currentText.isEmpty()) {
                        if (targetLine > 0) {
                            val pad = StringBuilder()
                            repeat(targetLine) { pad.append("\n") }
                            currentText.append(pad.toString())
                            if (text !== currentText) {
                                setText(currentText)
                            }
                        }
                        val targetOffset = currentText.length
                        setSelection(targetOffset)
                        ensureCursorVisible()
                        post { ensureCursorVisible() }
                        return true
                    } else if (y > l.height) {
                        val currentLineCount = l.lineCount
                        val linesBelow = targetLine - currentLineCount + 1
                        if (linesBelow > 0) {
                            val pad = StringBuilder()
                            repeat(linesBelow) { pad.append("\n") }
                            currentText.append(pad.toString())
                            if (text !== currentText) {
                                setText(currentText)
                            }
                        }
                        val targetOffset = currentText.length
                        setSelection(targetOffset)
                        ensureCursorVisible()
                        post { ensureCursorVisible() }
                        return true
                    }
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findUrlSpanAt(eventX: Float, eventY: Float): URLSpan? {
        val l = layout ?: return null
        val spanned = text ?: return null
        if (spanned.isEmpty()) return null
        val x = (eventX + scrollX - totalPaddingLeft).coerceAtLeast(0f)
        val y = (eventY + scrollY - totalPaddingTop).coerceAtLeast(0f)
        if (y > l.height) return null
        val line = l.getLineForVertical(y.toInt()).coerceIn(0, (l.lineCount - 1).coerceAtLeast(0))
        if (x > l.getLineRight(line)) return null
        val offset = l.getOffsetForHorizontal(line, x).coerceIn(0, spanned.length)
        val urlSpans = spanned.getSpans(offset, offset, URLSpan::class.java)
        return urlSpans.firstOrNull { span ->
            val spanStart = spanned.getSpanStart(span)
            val spanEnd = spanned.getSpanEnd(span)
            offset in spanStart until spanEnd
        }
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val totalPages = getTotalPages()
        val contentWidth = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
        val padL = paddingLeft.toFloat()
        val padR = (width - paddingRight).toFloat()

        // Theme-aware backdrop & page card colors
        val isDark = ((currentTextColor and 0x00FFFFFF) > 0x888888)
        val backdropColor = if (isDark) 0xFF232323.toInt() else 0xFFEBE8E6.toInt()
        if (isDark) {
            pageCardPaint.color = 0xFF141414.toInt()
            pageCardBorderPaint.color = 0x33FFFFFF
            ruledLinePaint.color = 0x22FFFFFF
            pageCounterPaint.color = 0x66FFFFFF
        } else {
            pageCardPaint.color = 0xFFFFFFFF.toInt()
            pageCardBorderPaint.color = 0x22000000
            ruledLinePaint.color = 0x1A000000
            pageCounterPaint.color = 0x55000000
        }

        // 1. Fill backdrop behind page cards
        canvas.drawColor(backdropColor)

        val cardLeft = cardInsetPx
        val cardRight = (width - cardInsetPx).coerceAtLeast(cardLeft + 1f)

        // 2. Render each page card: fill -> clip -> ruled lines -> paper-layer strokes -> counter -> restore -> border
        for (p in 0 until totalPages) {
            val pageTop = p * (pageHeightPx + pageGapPx).toFloat()
            val pageBottom = pageTop + pageHeightPx.toFloat()

            cardRect.set(cardLeft, pageTop, cardRight, pageBottom)
            cardPath.reset()
            cardPath.addRoundRect(cardRect, cardCornerRadiusPx, cardCornerRadiusPx, android.graphics.Path.Direction.CW)

            canvas.drawPath(cardPath, pageCardPaint)

            canvas.save()
            canvas.clipPath(cardPath)

            // 15 ruled lines for this page
            for (line in 0 until LINES_PER_PAGE) {
                val lineY = pageTop + topPadPx + (line + 1) * fixedLineHeight.toFloat()
                canvas.drawLine(padL, lineY, padR, lineY, ruledLinePaint)
            }

            // Paper-layer drawing strokes behind text (highlighter -> pen -> active stroke)
            drawDrawingStrokesForPage(canvas, p, totalPages, pageTop, padL, contentWidth, isHighlighter = true)
            drawDrawingStrokesForPage(canvas, p, totalPages, pageTop, padL, contentWidth, isHighlighter = false)
            if (activeStroke.size >= 2 && activeStrokePageIndex.coerceIn(0, totalPages - 1) == p) {
                drawActiveStrokeForPage(canvas, pageTop, padL, contentWidth)
            }

            // Subtle bottom-right page counter inside the card
            canvas.drawText(
                "${p + 1}/$totalPages",
                cardRight - (10f * density),
                pageBottom - (6f * density),
                pageCounterPaint
            )

            canvas.restore()

            // 1dp rounded border on top of clipped card
            canvas.drawPath(cardPath, pageCardBorderPaint)
        }

        // 3. Draw text, spans, cursor LAST so text always renders crisp on top of drawings
        super.onDraw(canvas)

        // 4. Draw always-visible, theme-aware vertical scrollbar along the right edge
        drawVerticalScrollbar(canvas, isDark)
    }

    private fun drawVerticalScrollbar(canvas: android.graphics.Canvas, isDark: Boolean) {
        val maxScroll = getMaxScrollY()
        if (maxScroll <= 0 || height <= 0 || width <= 0) return

        val trackTop = scrollY + scrollbarTopMarginPx
        val trackBottom = scrollY + (height - scrollbarBottomClearancePx)
        val trackHeight = trackBottom - trackTop
        if (trackHeight <= 4f * density) return

        val barWidth = if (isScrollbarDragging) scrollbarDraggingWidthPx else scrollbarWidthPx
        val barRight = scrollX + width - scrollbarRightMarginPx
        val barLeft = barRight - barWidth
        val cornerRadius = barWidth / 2f

        val totalContentHeight = (height + maxScroll).toFloat().coerceAtLeast(1f)
        val minThumbHeight = (40f * density).coerceAtMost(trackHeight)
        val thumbHeight = (trackHeight * (height.toFloat() / totalContentHeight)).coerceIn(minThumbHeight, trackHeight)
        val scrollFraction = (scrollY.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
        val thumbTravel = (trackHeight - thumbHeight).coerceAtLeast(0f)
        val thumbTop = trackTop + scrollFraction * thumbTravel
        val thumbBottom = thumbTop + thumbHeight

        if (isDark) {
            scrollbarTrackPaint.color = 0x28FFFFFF
            scrollbarThumbPaint.color = if (isScrollbarDragging) 0xCCFFFFFF.toInt() else 0x88FFFFFF.toInt()
        } else {
            scrollbarTrackPaint.color = 0x1F000000
            scrollbarThumbPaint.color = if (isScrollbarDragging) 0xB3000000.toInt() else 0x73000000.toInt()
        }

        scrollbarTrackRect.set(barLeft, trackTop, barRight, trackBottom)
        canvas.drawRoundRect(scrollbarTrackRect, cornerRadius, cornerRadius, scrollbarTrackPaint)

        scrollbarThumbRect.set(barLeft, thumbTop, barRight, thumbBottom)
        canvas.drawRoundRect(scrollbarThumbRect, cornerRadius, cornerRadius, scrollbarThumbPaint)
    }

    private fun drawDrawingStrokesForPage(
        canvas: android.graphics.Canvas,
        pageIndex: Int,
        totalPages: Int,
        pageTop: Float,
        padL: Float,
        contentWidth: Float,
        isHighlighter: Boolean
    ) {
        if (drawingBlocks.isEmpty()) return

        for (block in drawingBlocks) {
            val tool = block.drawingTool ?: "PEN"
            val isToolHighlighter = tool.equals("HIGHLIGHTER", ignoreCase = true)
            if (isToolHighlighter != isHighlighter) continue

            val p = (block.canvasPageIndex ?: 0).coerceIn(0, totalPages - 1)
            if (p != pageIndex) continue

            val points = decodeDrawingPath(block.drawingPath)
            if (points.size < 2) continue

            val strokeColorInt = block.drawingColor?.toInt() ?: 0xFF000000.toInt()
            val baseAlpha = (block.drawingAlpha ?: 1f).coerceIn(0.05f, 1f)
            val alpha = if (isToolHighlighter) (baseAlpha * 0.45f).coerceIn(0.1f, 0.6f) else baseAlpha
            val colorWithAlpha = (strokeColorInt and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)

            strokePaint.color = colorWithAlpha
            strokePaint.strokeWidth = (block.drawingStrokeWidth ?: (4f * density)).coerceAtLeast(1f)

            drawSmoothStroke(canvas, points, pageTop, padL, contentWidth)
        }
    }

    private fun drawActiveStrokeForPage(
        canvas: android.graphics.Canvas,
        pageTop: Float,
        padL: Float,
        contentWidth: Float
    ) {
        if (activeStroke.size < 2) return

        val isHighlighter = drawingTool == CanvasDrawingTool.HIGHLIGHTER
        val baseAlpha = drawingAlpha.coerceIn(0.05f, 1f)
        val alpha = if (isHighlighter) (baseAlpha * 0.45f).coerceIn(0.1f, 0.6f) else baseAlpha
        val colorInt = drawingColorArgb.toInt()
        val colorWithAlpha = (colorInt and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)

        strokePaint.color = colorWithAlpha
        strokePaint.strokeWidth = (drawingStrokeWidthDp * density).coerceAtLeast(1f)

        drawSmoothStroke(canvas, activeStroke, pageTop, padL, contentWidth)
    }

    private fun drawSmoothStroke(
        canvas: android.graphics.Canvas,
        points: List<Pair<Float, Float>>,
        pageTop: Float,
        padL: Float,
        contentWidth: Float
    ) {
        if (points.size < 2) return
        strokePath.reset()
        val firstX = padL + (points[0].first * contentWidth)
        val firstY = pageTop + topPadPx + (points[0].second * pageContentHeight)
        strokePath.moveTo(firstX, firstY)

        if (points.size == 2) {
            val secondX = padL + (points[1].first * contentWidth)
            val secondY = pageTop + topPadPx + (points[1].second * pageContentHeight)
            strokePath.lineTo(secondX, secondY)
        } else {
            for (i in 1 until points.size - 1) {
                val curX = padL + (points[i].first * contentWidth)
                val curY = pageTop + topPadPx + (points[i].second * pageContentHeight)
                val nextX = padL + (points[i + 1].first * contentWidth)
                val nextY = pageTop + topPadPx + (points[i + 1].second * pageContentHeight)
                val midX = (curX + nextX) / 2f
                val midY = (curY + nextY) / 2f
                strokePath.quadTo(curX, curY, midX, midY)
            }
            val lastX = padL + (points.last().first * contentWidth)
            val lastY = pageTop + topPadPx + (points.last().second * pageContentHeight)
            strokePath.lineTo(lastX, lastY)
        }

        canvas.drawPath(strokePath, strokePaint)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChangedListener?.invoke(selStart, selEnd)
        if (viewInitialized && height > 0 && !drawingEnabled) {
            ensureCursorVisible()
        }
    }
}
