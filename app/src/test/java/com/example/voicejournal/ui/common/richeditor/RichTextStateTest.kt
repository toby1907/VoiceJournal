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
    fun insertPageBreak_insertsVisibleSeparator() {
        val (state, _) = createBoundState("first page")

        state.onEditorContentChanged(currentText(state), 10, 10)
        state.insertPageBreak()

        assertTrue(state.annotatedString.text.contains("Page break"))
        assertTrue(state.toHtml().contains("Page break"))
        assertEquals(2, state.pageCount())
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

    private fun createBoundState(initialText: String): Pair<RichTextState, AppCompatEditText> {
        val state = RichTextState()
        val editor = AppCompatEditText(ApplicationProvider.getApplicationContext())
        state.bindEditor(editor)
        state.setText(initialText)
        return state to editor
    }

    private fun currentText(state: RichTextState) = SpannableStringBuilder(state.annotatedString.text)
}
