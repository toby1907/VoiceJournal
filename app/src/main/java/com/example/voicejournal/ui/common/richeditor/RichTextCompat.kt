package com.example.voicejournal.ui.common.richeditor

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
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

private const val PAGE_BREAK_LABEL = "────────── Page break ──────────"
private const val PAGE_BREAK_MARKER = "\n\n$PAGE_BREAK_LABEL\n\n"

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

            val spanned = HtmlCompat.fromHtml(preprocessed, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val parsed = SpannableStringBuilder(spanned)

            val urlSpans = parsed.getSpans(0, parsed.length, URLSpan::class.java)
            for (span in urlSpans) {
                val url = span.url
                if (url != null && url.startsWith("voicejournal-size:")) {
                    val size = url.removePrefix("voicejournal-size:").toIntOrNull()
                    if (size != null) {
                        val start = parsed.getSpanStart(span)
                        val end = parsed.getSpanEnd(span)
                        parsed.removeSpan(span)
                        parsed.setSpan(
                            AbsoluteSizeSpan(size, true),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            editable = parsed
            activeTypingFontSizeSp = null
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
        return withFontSizes
    }

    fun setText(text: String) {
        isProgrammaticChange = true
        try {
            editable = SpannableStringBuilder(text)
            activeTypingFontSizeSp = null
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
        toggleParagraphSpan(AlignmentSpan.Standard(alignment), AlignmentSpan.Standard::class.java)
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
        isOrderedList = !isOrderedList
        if (isOrderedList) {
            isUnorderedList = false
        }
        applyListMarker()
    }

    fun toggleUnorderedList() {
        isUnorderedList = !isUnorderedList
        if (isUnorderedList) {
            isOrderedList = false
        }
        applyListMarker()
    }

    fun applyActiveTypingStyle(editable: Editable, start: Int, end: Int) {
        if (start >= end) return
        val size = activeTypingFontSizeSp ?: return
        if (size == 16) return
        val existing = editable.getSpans(start, end, AbsoluteSizeSpan::class.java)
        existing.forEach { editable.removeSpan(it) }
        editable.setSpan(
            AbsoluteSizeSpan(size, true),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
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
        val lineStart = content.toString().lastIndexOf('\n', cursor - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = content.toString().indexOf('\n', cursor).let { if (it == -1) content.length else it }
        val lineText = content.subSequence(lineStart, lineEnd).toString()

        if (lineText.startsWith("☐ ")) {
            content.replace(lineStart, lineStart + 2, "☑ ")
            content.setSpan(
                StrikethroughSpan(),
                lineStart + 2,
                lineEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (lineText.startsWith("☑ ")) {
            content.delete(lineStart, lineStart + 2)
            val spans = content.getSpans(lineStart, (lineEnd - 2).coerceAtLeast(lineStart), StrikethroughSpan::class.java)
            spans.forEach { content.removeSpan(it) }
        } else {
            content.insert(lineStart, "☐ ")
        }
        applyEditableToEditor(content, (cursor + 2).coerceAtMost(content.length))
        refreshStateSnapshot()
    }

    fun toggleChecklistAtOffset(offset: Int): Boolean {
        val content = currentEditable()
        if (offset < 0 || offset > content.length) return false
        val lineStart = content.toString().lastIndexOf('\n', offset - 1).let { if (it == -1) 0 else it + 1 }
        val lineEnd = content.toString().indexOf('\n', lineStart).let { if (it == -1) content.length else it }
        val lineText = content.subSequence(lineStart, lineEnd).toString()

        if (lineText.startsWith("☐ ")) {
            content.replace(lineStart, lineStart + 2, "☑ ")
            content.setSpan(
                StrikethroughSpan(),
                lineStart + 2,
                lineEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            applyEditableToEditor(content, offset)
            refreshStateSnapshot()
            return true
        } else if (lineText.startsWith("☑ ")) {
            content.replace(lineStart, lineStart + 2, "☐ ")
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
        val labelStart = insertAt + 2
        val labelEnd = labelStart + PAGE_BREAK_LABEL.length
        content.setSpan(
            AlignmentSpan.Standard(android.text.Layout.Alignment.ALIGN_CENTER),
            labelStart,
            labelEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        content.setSpan(
            StyleSpan(Typeface.ITALIC),
            labelStart,
            labelEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        content.setSpan(
            ForegroundColorSpan(0xFF8A8A8A.toInt()),
            labelStart,
            labelEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
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
        if (editor.text.isNullOrEmpty() && editable.isNotEmpty()) {
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

    private fun applyListMarker() {
        val content = currentEditable()
        val start = selectionStart.coerceIn(0, content.length)
        val lineStart = content.toString().lastIndexOf('\n', start - 1).let { if (it == -1) 0 else it + 1 }
        if (isOrderedList) {
            if (!content.toString().substring(lineStart).startsWith("1. ")) {
                content.insert(lineStart, "1. ")
            }
        } else if (isUnorderedList) {
            if (!content.toString().substring(lineStart).startsWith("• ")) {
                content.insert(lineStart, "• ")
                content.setSpan(BulletSpan(12), lineStart, content.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        applyEditableToEditor(content)
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

    private fun <T> toggleParagraphSpan(span: T, clazz: Class<T>) where T : Any {
        val target: Spannable = boundEditor?.text ?: editable
        val (start, end) = normalizedParagraphSelection(if (target is SpannableStringBuilder) target else SpannableStringBuilder(target)) ?: return
        val spans = target.getSpans(start, end, clazz)
        val hasSpan = spans.any {
            val spanStart = target.getSpanStart(it)
            val spanEnd = target.getSpanEnd(it)
            spanStart <= start && spanEnd >= end
        }
        spans.forEach { target.removeSpan(it) }
        if (!hasSpan) {
            target.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        editable = SpannableStringBuilder(target)
        refreshStateSnapshot()
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

    private fun normalizedParagraphSelection(content: SpannableStringBuilder): Pair<Int, Int>? {
        if (content.isEmpty()) return null
        val rawStart = selectionStart.coerceIn(0, content.length)
        val rawEnd = selectionEnd.coerceIn(rawStart, content.length)
        val start = content.toString().lastIndexOf('\n', rawStart - 1).let { if (it == -1) 0 else it + 1 }
        val end = content.toString().indexOf('\n', rawEnd).let { if (it == -1) content.length else it }
        return start to end
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
                    movementMethod = LinkMovementMethod.getInstance()
                    linksClickable = true
                    autoLinkMask = Linkify.WEB_URLS
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
                editText.isFocusable = !readOnly
                editText.isFocusableInTouchMode = !readOnly
                editText.gravity = Gravity.TOP or Gravity.START
                if (colors.textColor != Color.Unspecified) {
                    editText.setTextColor(colors.textColor.toArgb())
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
                editText.richTextState = state
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

private class RichEditText(context: Context) : AppCompatEditText(context) {
    companion object {
        const val LINES_PER_PAGE = 15
    }

    var onSelectionChangedListener: ((Int, Int) -> Unit)? = null
    var onChecklistToggleRequested: ((Int) -> Boolean)? = null
    var onFocusGained: (() -> Unit)? = null
    var onScrollListener: ((Int, Int) -> Unit)? = null
    var onPageBreakAutoInserted: (() -> Unit)? = null

    var richTextState: RichTextState? = null

    var drawingBlocks: List<NoteBlock> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    var drawingEnabled: Boolean = false
        set(value) {
            field = value
            isCursorVisible = !value
            invalidate()
        }
    var drawingTool: CanvasDrawingTool = CanvasDrawingTool.PEN
        set(value) {
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
    private val horizontalPadPx: Int = (16f * density).roundToInt()
    val pageGapPx: Int = (32f * density).roundToInt()

    val pageContentHeight: Int = LINES_PER_PAGE * fixedLineHeight
    val pageHeightPx: Int = pageContentHeight + topPadPx + bottomPadPx

    private var downX = 0f
    private var downY = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private var downTime = 0L

    fun getMaxScrollY(): Int {
        val pages = getTotalPages()
        val totalH = pages * pageHeightPx + (pages - 1) * pageGapPx + bottomPadPx
        return maxOf(0, totalH - height)
    }

    override fun scrollTo(x: Int, y: Int) {
        val clampedY = y.coerceIn(0, getMaxScrollY())
        super.scrollTo(x, clampedY)
    }

    private val activeStroke = mutableListOf<Pair<Float, Float>>()
    private var activeStrokePageIndex = 0

    private val ruledLinePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1f * density
    }
    private val pageDividerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 1.5f * density
        pathEffect = DashPathEffect(floatArrayOf(10f * density, 6f * density), 0f)
    }
    private val pageLabelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f * density
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    init {
        gravity = Gravity.TOP or Gravity.START
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setPadding(horizontalPadPx, topPadPx, horizontalPadPx, bottomPadPx)
        val fm = paint.fontMetricsInt
        val fontHeight = fm.descent - fm.ascent
        val extra = (fixedLineHeight - fontHeight).coerceAtLeast(0)
        setLineSpacing(extra.toFloat(), 1.0f)
    }

    fun getTotalPages(): Int {
        val statePages = richTextState?.pageCount() ?: 1
        val l = layout
        val lPages = if (l != null && l.lineCount > 0) {
            ((l.lineCount - 1) / LINES_PER_PAGE) + 1
        } else 1
        return maxOf(statePages, lPages, 1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val pages = getTotalPages()
        val totalH = pages * pageHeightPx + (pages - 1) * pageGapPx + bottomPadPx
        val w = measuredWidth
        val h = maxOf(measuredHeight, totalH)
        setMeasuredDimension(w, h)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollListener?.invoke(l, t)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        val prevSelStart = selectionStart
        val prevSelEnd = selectionEnd
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        if (gainFocus) {
            onFocusGained?.invoke()
            if (prevSelStart >= 0 && prevSelEnd >= 0 && !text.isNullOrEmpty()) {
                val len = text!!.length
                setSelection(prevSelStart.coerceIn(0, len), prevSelEnd.coerceIn(0, len))
            }
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

            override fun sendKeyEvent(event: KeyEvent?): Boolean {
                if (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (handleEnterKey()) return true
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

        if (lineOnPage >= LINES_PER_PAGE - 1) { // 14 means 15th line (0-indexed)
            st.insertPageBreak()
            onPageBreakAutoInserted?.invoke()
            requestLayout()
            invalidate()
            post {
                val nextPageIndex = pageIndex + 1
                val nextY = nextPageIndex * (pageHeightPx + pageGapPx)
                scrollTo(0, nextY)
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
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawingEnabled) {
            parent?.requestDisallowInterceptTouchEvent(true)
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

            when (event.action) {
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
                        activeStroke.add(nx to ny)
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

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                lastTouchY = event.y
                isDragging = false
                downTime = System.currentTimeMillis()
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = lastTouchY - event.y
                lastTouchY = event.y
                if (Math.abs(event.y - downY) > 8f || isDragging) {
                    isDragging = true
                    val maxScroll = getMaxScrollY()
                    if (maxScroll > 0) {
                        val newScrollY = (scrollY + dy.toInt()).coerceIn(0, maxScroll)
                        scrollTo(scrollX, newScrollY)
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                val dx = Math.abs(event.x - downX)
                val dy = Math.abs(event.y - downY)
                val dt = System.currentTimeMillis() - downTime
                val isTap = dx < 24 && dy < 24 && dt < 450 && !isDragging

                if (isTap) {
                    onFocusGained?.invoke()
                    requestFocus()
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

                    val x = (event.x + scrollX - totalPaddingLeft).coerceAtLeast(0f)
                    val y = (event.y + scrollY - totalPaddingTop).coerceAtLeast(0f)
                    val l = layout

                    // Check if tapped near checklist checkbox (first 3 chars of line)
                    if (l != null && !text.isNullOrEmpty() && y <= l.height) {
                        val line = l.getLineForVertical(y.toInt()).coerceIn(0, (l.lineCount - 1).coerceAtLeast(0))
                        val lineStart = l.getLineStart(line)
                        val offset = l.getOffsetForHorizontal(line, x).coerceIn(0, text!!.length)
                        if (offset <= lineStart + 3 && onChecklistToggleRequested?.invoke(offset) == true) {
                            return true
                        }
                    }

                    // Tapped on notebook lines
                    val currentText = text ?: SpannableStringBuilder()
                    val lHeight = fixedLineHeight.coerceAtLeast(1)
                    val totalPages = getTotalPages()
                    val totalAllowedLines = totalPages * LINES_PER_PAGE

                    val rawTargetLine = (y / lHeight).toInt().coerceAtLeast(0)
                    val targetLine = rawTargetLine.coerceIn(0, totalAllowedLines - 1)

                    if (l != null && !text.isNullOrEmpty() && y <= l.height) {
                        val line = l.getLineForVertical(y.toInt()).coerceIn(0, (l.lineCount - 1).coerceAtLeast(0))
                        val offset = l.getOffsetForHorizontal(line, x).coerceIn(0, text!!.length)
                        setSelection(offset)
                        onSelectionChangedListener?.invoke(offset, offset)
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
                        onSelectionChangedListener?.invoke(targetOffset, targetOffset)
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
                        onSelectionChangedListener?.invoke(targetOffset, targetOffset)
                        return true
                    }
                }
                isDragging = false
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        val totalPages = getTotalPages()
        val contentWidth = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
        val padL = paddingLeft.toFloat()
        val padR = (width - paddingRight).toFloat()

        // Theme-aware paint colors
        val isDark = ((currentTextColor and 0x00FFFFFF) > 0x888888)
        if (isDark) {
            ruledLinePaint.color = 0x22FFFFFF
            pageDividerPaint.color = 0x44FFFFFF
            pageLabelPaint.color = 0x88FFFFFF.toInt()
        } else {
            ruledLinePaint.color = 0x1A000000
            pageDividerPaint.color = 0x33000000
            pageLabelPaint.color = 0x66000000
        }

        // 1. Draw ruled notebook lines and page dividers
        for (p in 0 until totalPages) {
            val pageTop = p * (pageHeightPx + pageGapPx).toFloat()

            // 15 ruled lines for this page
            for (line in 0 until LINES_PER_PAGE) {
                val lineY = pageTop + topPadPx + (line + 1) * fixedLineHeight.toFloat()
                canvas.drawLine(padL, lineY, padR, lineY, ruledLinePaint)
            }

            // Divider between pages
            if (p < totalPages - 1) {
                val dividerY = pageTop + pageHeightPx + (pageGapPx / 2f)
                canvas.drawLine(padL, dividerY, padR, dividerY, pageDividerPaint)
                val label = "── Page ${p + 2} ──"
                canvas.drawText(label, width / 2f, dividerY - (4f * density), pageLabelPaint)
            }
        }

        // 2. Draw highlighter drawing strokes (UNDER text)
        drawDrawingStrokes(canvas, totalPages, isHighlighter = true)

        // 3. Draw text, spans, cursor
        super.onDraw(canvas)

        // 4. Draw pen drawing strokes (OVER text)
        drawDrawingStrokes(canvas, totalPages, isHighlighter = false)

        // 5. Draw active stroke in progress
        drawActiveStroke(canvas)
    }

    private fun drawDrawingStrokes(canvas: android.graphics.Canvas, totalPages: Int, isHighlighter: Boolean) {
        if (drawingBlocks.isEmpty()) return
        val contentWidth = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
        val padL = paddingLeft.toFloat()

        for (block in drawingBlocks) {
            val tool = block.drawingTool ?: "PEN"
            val isToolHighlighter = tool.equals("HIGHLIGHTER", ignoreCase = true)
            if (isToolHighlighter != isHighlighter) continue

            val p = (block.canvasPageIndex ?: 0).coerceIn(0, totalPages - 1)
            val points = decodeDrawingPath(block.drawingPath)
            if (points.size < 2) continue

            val pageTop = p * (pageHeightPx + pageGapPx).toFloat()
            val strokeColorInt = block.drawingColor?.toInt() ?: 0xFF000000.toInt()
            val baseAlpha = (block.drawingAlpha ?: 1f).coerceIn(0.05f, 1f)
            val alpha = if (isToolHighlighter) (baseAlpha * 0.45f).coerceIn(0.1f, 0.6f) else baseAlpha
            val colorWithAlpha = (strokeColorInt and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)

            strokePaint.color = colorWithAlpha
            strokePaint.strokeWidth = (block.drawingStrokeWidth ?: (4f * density)).coerceAtLeast(1f)

            for (i in 1 until points.size) {
                val start = points[i - 1]
                val end = points[i]
                val x1 = padL + (start.first * contentWidth)
                val y1 = pageTop + topPadPx + (start.second * pageContentHeight)
                val x2 = padL + (end.first * contentWidth)
                val y2 = pageTop + topPadPx + (end.second * pageContentHeight)
                canvas.drawLine(x1, y1, x2, y2, strokePaint)
            }
        }
    }

    private fun drawActiveStroke(canvas: android.graphics.Canvas) {
        if (activeStroke.size < 2) return
        val totalPages = getTotalPages()
        val p = activeStrokePageIndex.coerceIn(0, totalPages - 1)
        val pageTop = p * (pageHeightPx + pageGapPx).toFloat()
        val contentWidth = (width - paddingLeft - paddingRight).toFloat().coerceAtLeast(1f)
        val padL = paddingLeft.toFloat()

        val isHighlighter = drawingTool == CanvasDrawingTool.HIGHLIGHTER
        val baseAlpha = drawingAlpha.coerceIn(0.05f, 1f)
        val alpha = if (isHighlighter) (baseAlpha * 0.45f).coerceIn(0.1f, 0.6f) else baseAlpha
        val colorInt = drawingColorArgb.toInt()
        val colorWithAlpha = (colorInt and 0x00FFFFFF) or ((alpha * 255).toInt() shl 24)

        strokePaint.color = colorWithAlpha
        strokePaint.strokeWidth = (drawingStrokeWidthDp * density).coerceAtLeast(1f)

        for (i in 1 until activeStroke.size) {
            val start = activeStroke[i - 1]
            val end = activeStroke[i]
            val x1 = padL + (start.first * contentWidth)
            val y1 = pageTop + topPadPx + (start.second * pageContentHeight)
            val x2 = padL + (end.first * contentWidth)
            val y2 = pageTop + topPadPx + (end.second * pageContentHeight)
            canvas.drawLine(x1, y1, x2, y2, strokePaint)
        }
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChangedListener?.invoke(selStart, selEnd)
    }
}
