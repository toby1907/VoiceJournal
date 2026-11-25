package com.example.voicejournal.ui.common.richeditor

import android.text.Layout
import android.text.style.AlignmentSpan
import android.text.SpannableStringBuilder
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RichTextStateTest {

    @Test
    fun setHtml_updatesPlainTextSnapshot() {
        val state = RichTextState()
        state.setHtml("<p><b>Hello</b> world</p>")

        assertEquals("Hello world", state.annotatedString.text.trim())
    }

    @Test
    fun toggleBold_appliesFormattingToSelectionAndReflectsInHtml() {
        val (state, _) = createBoundState("hello world")

        state.onEditorContentChanged(currentText(state), 0, 5)
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

        val html = state.toHtml().lowercase()
        assertTrue(html.contains("<b>hello</b>"))
    }

    @Test
    fun toggleUnderline_appliesFormattingToSelectionAndReflectsInHtml() {
        val (state, _) = createBoundState("hello world")

        state.onEditorContentChanged(currentText(state), 6, 11)
        state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))

        val html = state.toHtml().lowercase()
        assertTrue(html.contains("<u>world</u>"))
    }

    @Test
    fun addLink_withoutScheme_normalizesToHttps() {
        val (state, _) = createBoundState("hello")

        state.onEditorContentChanged(currentText(state), 0, 5)
        state.addLink(text = "", url = "example.com")

        val html = state.toHtml().lowercase()
        assertTrue(html.contains("href=\"https://example.com\""))
    }

    @Test
    fun toggleOrderedList_prefixesCurrentLine() {
        val (state, _) = createBoundState("item")

        state.onEditorContentChanged(currentText(state), 0, 0)
        state.toggleOrderedList()

        assertTrue(state.annotatedString.text.startsWith("1. "))
    }

    @Test
    fun toggleParagraphAlignment_appliesCenterSpan() {
        val (state, editor) = createBoundState("center me")

        state.onEditorContentChanged(currentText(state), 0, 8)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

        val spans = editor.text?.getSpans(0, editor.text!!.length, AlignmentSpan.Standard::class.java).orEmpty()
        assertTrue(spans.any { it.alignment == Layout.Alignment.ALIGN_CENTER })
    }

    @Test
    fun fontSize_roundTripsThroughHtml() {
        val (state, _) = createBoundState("hello big world")

        state.onEditorContentChanged(currentText(state), 6, 9)
        state.setFontSize(24)

        val html = state.toHtml()
        assertTrue(html.contains("24"))

        val (reload, reloadEditor) = createBoundState("")
        reload.setHtml(html)

        reload.onEditorContentChanged(reloadEditor.text, 6, 9)
        assertEquals(24, reload.currentFontSizeSp)
    }

    @Test
    fun htmlRoundTrip_preservesTextContent() {
        val state = RichTextState()
        state.setHtml("<p><b>Hello</b> <a href=\"https://example.com\">World</a></p>")

        val roundTrip = state.toHtml()
        val reload = RichTextState()
        reload.setHtml(roundTrip)

        assertEquals("Hello World", reload.annotatedString.text.trim())
    }

    @Test
    fun insertPageBreak_insertsInvisibleMarkerAndRoundTripsThroughHtml() {
        val (state, _) = createBoundState("first page")

        state.onEditorContentChanged(currentText(state), 10, 10)
        state.insertPageBreak()

        assertTrue(!state.annotatedString.text.contains("Page break"))
        val html = state.toHtml()
        assertTrue(html.contains("data-voicejournal-page-break=\"true\""))
        assertEquals(2, state.pageCount())

        val reloaded = RichTextState()
        reloaded.setHtml(html)
        assertEquals(2, reloaded.pageCount())
        assertTrue(!reloaded.annotatedString.text.contains("Page break"))
    }

    @Test
    fun clearPageBreaks_removesSeparatorMarkers() {
        val (state, _) = createBoundState("first page")

        state.onEditorContentChanged(currentText(state), 10, 10)
        state.insertPageBreak()
        state.clearPageBreaks()

        assertEquals(1, state.pageCount())
        assertTrue(state.annotatedString.text.contains("first page"))
        assertTrue(!state.annotatedString.text.contains("Page break"))
    }

    @Test
    fun removeCurrentPageBreak_removesNearestBreak() {
        val (state, editor) = createBoundState("first page")

        editor.setText("first page")
        state.onEditorContentChanged(editor.text, 10, 10)
        state.insertPageBreak()
        editor.setText(state.annotatedString.text)
        state.onEditorContentChanged(editor.text, state.annotatedString.text.length, state.annotatedString.text.length)
        state.removeCurrentPageBreak()

        assertEquals(1, state.pageCount())
        assertTrue(!state.annotatedString.text.contains("Page break"))
    }

    @Test
    fun goToNextAndPreviousPage_movesCaretBetweenPages() {
        val (state, editor) = createBoundState("first page")

        editor.setText("first page")
        state.onEditorContentChanged(editor.text, 10, 10)
        state.insertPageBreak()
        editor.setText(state.annotatedString.text)
        state.onEditorContentChanged(editor.text, 10, 10)

        assertEquals(1, state.pageBreakOffsets().size)
        state.goToPage(1)
        assertEquals(1, state.currentPageIndex())
        state.goToNextPage()
        assertTrue(state.currentPageIndex() == 1)

        state.goToPreviousPage()
        assertTrue(state.currentPageIndex() == 0)
    }

    @Test
    fun pageRanges_tracksEachPageContentWindow() {
        val (state, editor) = createBoundState("page one")

        editor.setText("page one")
        state.onEditorContentChanged(editor.text, 8, 8)
        state.insertPageBreak()
        editor.setText(state.annotatedString.text)
        state.onEditorContentChanged(editor.text, state.annotatedString.text.length, state.annotatedString.text.length)
        state.insertPageBreak()

        val ranges = state.pageRanges()

        assertEquals(3, ranges.size)
        assertTrue(ranges[0].first == 0)
        assertTrue(ranges[1].first > ranges[0].last)
        assertTrue(ranges[2].first > ranges[1].last)
    }

    @Test
    fun normalizePageMarkers_collapsesExtraBlankLines() {
        val state = RichTextState()
        state.setText("page one\n\n\npage two")

        state.normalizePageMarkers()

        assertTrue(!state.annotatedString.text.contains("\n\n\n"))
    }

    @Test
    fun undoRedo_restoresAndReappliesTextChanges() {
        val (state, editor) = createBoundState("hello")

        editor.setText("hello world")
        state.onEditorContentChanged(editor.text, 11, 11)
        assertEquals("hello world", state.annotatedString.text)
        assertTrue(state.canUndo)

        state.undo()
        assertEquals("hello", state.annotatedString.text)
        assertTrue(state.canRedo)

        state.redo()
        assertEquals("hello world", state.annotatedString.text)
    }

    @Test
    fun alignment_switchesCleanlyBetweenLeftCenterRight_withoutTogglingOff() {
        val (state, editor) = createBoundState("Line 1\nLine 2\nLine 3")

        // Place cursor on Line 2 ("Line 2" starts at offset 7)
        state.onEditorContentChanged(editor.text, 7, 7)
        assertEquals(TextAlign.Start, state.currentTextAlign)

        // Apply Center to Line 2
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
        assertEquals(TextAlign.Center, state.currentTextAlign)

        // Switch directly from Center -> Right (must apply ALIGN_OPPOSITE, not drop back to Left)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End))
        assertEquals(TextAlign.End, state.currentTextAlign)
        val line2Spans = editor.text!!.getSpans(7, 13, AlignmentSpan::class.java)
        assertTrue(line2Spans.any { it.alignment == Layout.Alignment.ALIGN_OPPOSITE })

        // Verify Line 1 and Line 3 remained Left-aligned
        state.onEditorContentChanged(editor.text, 0, 0)
        assertEquals(TextAlign.Start, state.currentTextAlign)
        state.onEditorContentChanged(editor.text, 14, 14)
        assertEquals(TextAlign.Start, state.currentTextAlign)

        // Move back to Line 2 -> reports Right, then reset to Left
        state.onEditorContentChanged(editor.text, 9, 9)
        assertEquals(TextAlign.End, state.currentTextAlign)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))
        assertEquals(TextAlign.Start, state.currentTextAlign)
        assertTrue(editor.text!!.getSpans(7, 13, AlignmentSpan::class.java).isEmpty())
    }

    @Test
    fun alignment_onEmptyLine_appliesWhenTypingStarts() {
        val (state, editor) = createBoundState("")
        state.onEditorContentChanged(editor.text, 0, 0)

        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
        assertEquals(TextAlign.Center, state.currentTextAlign)

        editor.text!!.insert(0, "Centered")
        state.applyActiveTypingStyle(editor.text!!, 0, 8)
        state.onEditorContentChanged(editor.text, 8, 8)

        assertEquals(TextAlign.Center, state.currentTextAlign)
        val spans = editor.text!!.getSpans(0, 8, AlignmentSpan::class.java)
        assertTrue(spans.any { it.alignment == Layout.Alignment.ALIGN_CENTER })
    }

    @Test
    fun alignment_roundTripsThroughHtml_perLineWithoutDrift() {
        val (state, editor) = createBoundState("Left line\nCentered line\nRight line\nBottom left")
        // Center Line 2
        val line2Start = "Left line\n".length
        state.onEditorContentChanged(editor.text, line2Start, line2Start)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

        // Right-align Line 3
        val line3Start = "Left line\nCentered line\n".length
        state.onEditorContentChanged(editor.text, line3Start, line3Start)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End))

        val html = state.toHtml()
        assertTrue(html.contains("data-voicejournal-align=\"center\""))
        assertTrue(html.contains("data-voicejournal-align=\"end\""))

        val (reloaded, reloadedEditor) = createBoundState("")
        reloaded.setHtml(html)

        val text = reloadedEditor.text!!.toString()
        val lines = text.trimEnd('\n').split("\n")
        assertEquals(listOf("Left line", "Centered line", "Right line", "Bottom left"), lines)

        val l1 = 0
        val l2 = text.indexOf("Centered line")
        val l3 = text.indexOf("Right line")
        val l4 = text.indexOf("Bottom left")

        reloaded.onEditorContentChanged(reloadedEditor.text, l1, l1)
        assertEquals(TextAlign.Start, reloaded.currentTextAlign)
        reloaded.onEditorContentChanged(reloadedEditor.text, l2, l2)
        assertEquals(TextAlign.Center, reloaded.currentTextAlign)
        reloaded.onEditorContentChanged(reloadedEditor.text, l3, l3)
        assertEquals(TextAlign.End, reloaded.currentTextAlign)
        reloaded.onEditorContentChanged(reloadedEditor.text, l4, l4)
        assertEquals(TextAlign.Start, reloaded.currentTextAlign)
    }

    @Test
    fun lists_toggleOnOffAndSwitchCleanly_withoutDoubleBulletsOrStackedPrefixes() {
        val (state, editor) = createBoundState("Apple")
        state.onEditorContentChanged(editor.text, 0, 0)

        // Toggle ordered ON
        state.toggleOrderedList()
        assertEquals("1. Apple", editor.text.toString())
        assertEquals(3, editor.selectionEnd)
        assertTrue(state.isOrderedList)
        assertTrue(!state.isUnorderedList)

        // Switch ordered -> unordered (replaces prefix, zero BulletSpan)
        state.toggleUnorderedList()
        assertEquals("• Apple", editor.text.toString())
        assertEquals(2, editor.selectionEnd)
        assertTrue(state.isUnorderedList)
        assertTrue(!state.isOrderedList)
        assertTrue(editor.text!!.getSpans(0, editor.text!!.length, android.text.style.BulletSpan::class.java).isEmpty())

        // Toggle unordered OFF
        state.toggleUnorderedList()
        assertEquals("Apple", editor.text.toString())
        assertEquals(0, editor.selectionEnd)
        assertTrue(!state.isUnorderedList)
        assertTrue(!state.isOrderedList)
    }

    @Test
    fun multiLineList_numbersAllSelectedLinesAndSwitchesCleanly() {
        val (state, editor) = createBoundState("First\nSecond\nThird")
        state.onEditorContentChanged(editor.text, 0, editor.text!!.length)

        state.toggleOrderedList()
        assertEquals("1. First\n2. Second\n3. Third", editor.text.toString())

        state.onEditorContentChanged(editor.text, 0, editor.text!!.length)
        state.toggleUnorderedList()
        assertEquals("• First\n• Second\n• Third", editor.text.toString())

        state.onEditorContentChanged(editor.text, 0, editor.text!!.length)
        state.toggleUnorderedList()
        assertEquals("First\nSecond\nThird", editor.text.toString())
    }

    @Test
    fun listEnterAndBackspace_continueRenumberAndExitCleanly() {
        // 1. Ordered list continuation + exit on empty item
        val (state, editor) = createBoundState("1. First")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        assertTrue(state.handleListEnter())
        assertEquals("1. First\n2. ", editor.text.toString())
        assertTrue(state.isOrderedList)

        // Enter on empty "2. " exits list
        assertTrue(state.handleListEnter())
        assertEquals("1. First\n", editor.text.toString())
        assertTrue(!state.isOrderedList)

        // 2. Insert in middle of ordered list renumbers subsequent items
        state.setText("1. One\n2. Two")
        val endOfOne = "1. One".length
        state.onEditorContentChanged(editor.text, endOfOne, endOfOne)
        assertTrue(state.handleListEnter())
        assertEquals("1. One\n2. \n3. Two", editor.text.toString())

        // 3. Bulleted list continuation + backspace prefix removal
        state.setText("• Alpha")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        assertTrue(state.handleListEnter())
        assertEquals("• Alpha\n• ", editor.text.toString())
        assertTrue(state.handleListBackspace())
        assertEquals("• Alpha\n", editor.text.toString())
        assertTrue(!state.isUnorderedList)

        // 4. Checklist continuation + exit on empty
        state.setText("☐ Task")
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        assertTrue(state.handleListEnter())
        assertEquals("☐ Task\n☐ ", editor.text.toString())
        assertTrue(state.isChecklist)
        assertTrue(state.handleListEnter())
        assertEquals("☐ Task\n", editor.text.toString())
        assertTrue(!state.isChecklist)
    }

    private fun createBoundState(initialText: String): Pair<RichTextState, AppCompatEditText> {
        val state = RichTextState()
        val editor = AppCompatEditText(ApplicationProvider.getApplicationContext())
        state.bindEditor(editor)
        state.setText(initialText)
        return state to editor
    }

    private fun currentText(state: RichTextState) = SpannableStringBuilder(state.annotatedString.text)
}
