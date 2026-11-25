package com.example.voicejournal.ui.common.richeditor

import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.SpannableStringBuilder
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        // Highlight Line 2 ("Line 2" is 7..13)
        state.onEditorContentChanged(editor.text, 7, 13)
        assertEquals(TextAlign.Start, state.currentTextAlign)

        // Apply Center to highlighted Line 2
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

        // Highlight Line 2 again -> reports Right, then reset to Left
        state.onEditorContentChanged(editor.text, 7, 13)
        assertEquals(TextAlign.End, state.currentTextAlign)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))
        assertEquals(TextAlign.Start, state.currentTextAlign)
        assertTrue(editor.text!!.getSpans(7, 13, AlignmentSpan::class.java).isEmpty())
    }

    @Test
    fun alignment_onlyAppliesToHighlightedWordsInMiddleOfLine() {
        val (state, editor) = createBoundState("Hello world today")

        // Highlight only "world" (6..11) and apply Center
        state.onEditorContentChanged(editor.text, 6, 11)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

        val lines = editor.text.toString().split("\n")
        assertEquals(listOf("Hello ", "world", " today"), lines)

        val worldStart = editor.text.toString().indexOf("world")
        val worldEnd = worldStart + "world".length
        // Only "world" is centered; "Hello " and " today" remain Left
        assertTrue(editor.text!!.getSpans(0, 6, AlignmentSpan::class.java).isEmpty())
        val worldSpans = editor.text!!.getSpans(worldStart, worldEnd, AlignmentSpan::class.java)
        assertTrue(worldSpans.any { it.alignment == Layout.Alignment.ALIGN_CENTER })
        val todayStart = editor.text.toString().indexOf(" today")
        assertTrue(editor.text!!.getSpans(todayStart, editor.text!!.length, AlignmentSpan::class.java).isEmpty())

        // Switching the still-highlighted "world" from Center -> Right must not add extra newlines
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End))
        assertEquals(listOf("Hello ", "world", " today"), editor.text.toString().split("\n"))
        val updatedSpans = editor.text!!.getSpans(worldStart, worldEnd, AlignmentSpan::class.java)
        assertTrue(updatedSpans.any { it.alignment == Layout.Alignment.ALIGN_OPPOSITE })
    }

    @Test
    fun alignment_noHighlight_doesNotAlignExistingText_onlySubsequentTypedWords() {
        val (state, editor) = createBoundState("First line")

        // Collapsed cursor at end of "First line"
        state.onEditorContentChanged(editor.text, 10, 10)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

        // Toolbar reflects armed Center alignment, but existing "First line" stays Left (no spans)
        assertEquals(TextAlign.Center, state.currentTextAlign)
        assertEquals("First line", editor.text.toString())
        assertTrue(editor.text!!.getSpans(0, editor.text!!.length, AlignmentSpan::class.java).isEmpty())

        // Type 'N' then "ext" as subsequent typed words
        val insertPos1 = editor.text!!.length
        editor.text!!.insert(insertPos1, "N")
        state.applyActiveTypingStyle(editor.text!!, insertPos1, insertPos1 + 1)
        state.onEditorContentChanged(editor.text, editor.selectionStart, editor.selectionEnd)

        val insertPos2 = editor.text!!.length
        editor.text!!.insert(insertPos2, "ext")
        state.applyActiveTypingStyle(editor.text!!, insertPos2, insertPos2 + 3)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)

        // "First line" remains Left on line 1; "Next" is Centered on line 2
        assertEquals("First line\nNext", editor.text.toString())
        assertTrue(editor.text!!.getSpans(0, "First line".length, AlignmentSpan::class.java).isEmpty())
        val nextStart = editor.text.toString().indexOf("Next")
        val nextSpans = editor.text!!.getSpans(nextStart, nextStart + 4, AlignmentSpan::class.java)
        assertTrue(nextSpans.any { it.alignment == Layout.Alignment.ALIGN_CENTER })

        // Switch alignment to Left without highlight and type "Back" -> "Next" stays Centered, "Back" is Left
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start))
        assertEquals(TextAlign.Start, state.currentTextAlign)
        val insertPos3 = editor.text!!.length
        editor.text!!.insert(insertPos3, "Back")
        state.applyActiveTypingStyle(editor.text!!, insertPos3, insertPos3 + 4)
        state.onEditorContentChanged(editor.text, editor.selectionStart, editor.selectionEnd)

        assertEquals("First line\nNext\nBack", editor.text.toString())
        val nextSpansAfter = editor.text!!.getSpans(nextStart, nextStart + 4, AlignmentSpan::class.java)
        assertTrue(nextSpansAfter.any { it.alignment == Layout.Alignment.ALIGN_CENTER })
        val backStart = editor.text.toString().indexOf("Back")
        assertTrue(editor.text!!.getSpans(backStart, backStart + 4, AlignmentSpan::class.java).isEmpty())

        // Tapping back onto "First line" disarms any pending typing alignment and reports Start
        state.onEditorContentChanged(editor.text, 2, 2)
        assertEquals(TextAlign.Start, state.currentTextAlign)
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
        // Highlight & Center Line 2
        val line2Start = "Left line\n".length
        state.onEditorContentChanged(editor.text, line2Start, line2Start + "Centered line".length)
        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))

        // Highlight & Right-align Line 3
        val line3Start = "Left line\nCentered line\n".length
        state.onEditorContentChanged(editor.text, line3Start, line3Start + "Right line".length)
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

    @Test
    fun bold_onEmptyNote_armsToolbarAndBoldsTypedText() {
        val (state, editor) = createBoundState("")
        state.onEditorContentChanged(editor.text, 0, 0)

        // Toggle Bold ON on an empty note -> toolbar lights up immediately
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)

        // Type 'H' then 'i' -> merges into one clean bold span [0, 2]
        editor.text!!.insert(0, "H")
        state.applyActiveTypingStyle(editor.text!!, 0, 1)
        state.onEditorContentChanged(editor.text, 1, 1)

        editor.text!!.insert(1, "i")
        state.applyActiveTypingStyle(editor.text!!, 1, 2)
        state.onEditorContentChanged(editor.text, 2, 2)

        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        val boldSpans = editor.text!!.getSpans(0, 2, StyleSpan::class.java)
            .filter { it.style == Typeface.BOLD }
        assertEquals(1, boldSpans.size)
        assertEquals(0, editor.text!!.getSpanStart(boldSpans[0]))
        assertEquals(2, editor.text!!.getSpanEnd(boldSpans[0]))

        // Toggle Bold OFF -> toolbar turns off, "Hi" remains bold, subsequent text is normal
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertNull(state.currentSpanStyle.fontWeight)

        editor.text!!.insert(2, " there")
        state.applyActiveTypingStyle(editor.text!!, 2, 8)
        state.onEditorContentChanged(editor.text, 8, 8)

        assertNull(state.currentSpanStyle.fontWeight)
        val html = state.toHtml().lowercase()
        assertTrue("Expected <b>hi</b> there in HTML, got: $html", html.contains("<b>hi</b> there"))
    }

    @Test
    fun bold_withCollapsedCursorOnExistingText_doesNotAlterOrSelectExistingChars() {
        val (state, editor) = createBoundState("Hello")
        editor.setSelection(5)
        state.onEditorContentChanged(editor.text, 5, 5)

        // Toggle Bold ON with collapsed cursor at end of "Hello"
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

        // Must NOT highlight or bold 'o'; cursor stays collapsed at 5..5
        assertEquals(5, editor.selectionStart)
        assertEquals(5, editor.selectionEnd)
        assertTrue(editor.text!!.getSpans(0, 5, StyleSpan::class.java).isEmpty())
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)

        // Subsequent typed text is bold while "Hello" remains plain
        editor.text!!.insert(5, " world")
        state.applyActiveTypingStyle(editor.text!!, 5, 11)
        state.onEditorContentChanged(editor.text, 11, 11)

        assertTrue(editor.text!!.getSpans(0, 5, StyleSpan::class.java).isEmpty())
        val worldSpans = editor.text!!.getSpans(5, 11, StyleSpan::class.java)
        assertTrue(worldSpans.any { it.style == Typeface.BOLD })
        assertEquals(5, editor.text!!.getSpanStart(worldSpans.first()))
        assertEquals(11, editor.text!!.getSpanEnd(worldSpans.first()))
    }

    @Test
    fun bold_unboldingSubRange_preservesSurroundingBoldAndItalic() {
        val (state, editor) = createBoundState("Hello world today")
        state.onEditorContentChanged(editor.text, 0, 17)
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

        // Highlight only "world" (6..11) and toggle Bold OFF
        state.onEditorContentChanged(editor.text, 6, 11)
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertNull(state.currentSpanStyle.fontWeight)

        // "Hello " (0..6) and " today" (11..17) stay bold; "world" (6..11) is plain
        val beforeSpans = editor.text!!.getSpans(0, 6, StyleSpan::class.java)
        assertTrue(beforeSpans.any { it.style == Typeface.BOLD && editor.text!!.getSpanStart(it) == 0 && editor.text!!.getSpanEnd(it) == 6 })
        val midSpans = editor.text!!.getSpans(6, 11, StyleSpan::class.java)
            .filter { editor.text!!.getSpanStart(it) < 11 && editor.text!!.getSpanEnd(it) > 6 }
        assertTrue("Expected no bold spans on unbolded 'world'", midSpans.isEmpty())
        val afterSpans = editor.text!!.getSpans(11, 17, StyleSpan::class.java)
        assertTrue(afterSpans.any { it.style == Typeface.BOLD && editor.text!!.getSpanStart(it) == 11 && editor.text!!.getSpanEnd(it) == 17 })

        // Verify BOLD_ITALIC span splits cleanly when unbolding a sub-range
        state.setText("Hello")
        editor.text!!.setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        state.onEditorContentChanged(editor.text, 1, 4)
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        assertEquals(FontStyle.Italic, state.currentSpanStyle.fontStyle)

        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertNull(state.currentSpanStyle.fontWeight)
        assertEquals(FontStyle.Italic, state.currentSpanStyle.fontStyle)

        // 'H' (0..1) and 'o' (4..5) remain BOLD_ITALIC; "ell" (1..4) is ITALIC only
        state.onEditorContentChanged(editor.text, 0, 1)
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        assertEquals(FontStyle.Italic, state.currentSpanStyle.fontStyle)
        state.onEditorContentChanged(editor.text, 4, 5)
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        assertEquals(FontStyle.Italic, state.currentSpanStyle.fontStyle)
    }

    @Test
    fun bold_partialHighlight_boldsEntireSelectionThenTogglesOff() {
        val (state, editor) = createBoundState("Hello world")
        state.onEditorContentChanged(editor.text, 0, 5)
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))

        // Highlight "lo wo" (3..8) which is partially bold, partially plain
        state.onEditorContentChanged(editor.text, 3, 8)
        assertNull(state.currentSpanStyle.fontWeight)

        // First tap bolds all of 3..8 (merging with 0..5 -> 0..8)
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)
        val merged = editor.text!!.getSpans(0, 8, StyleSpan::class.java)
            .filter { it.style == Typeface.BOLD }
        assertEquals(1, merged.size)
        assertEquals(0, editor.text!!.getSpanStart(merged[0]))
        assertEquals(8, editor.text!!.getSpanEnd(merged[0]))

        // Second tap on still-highlighted 3..8 unbolds 3..8 while keeping "Hel" (0..3) bold
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertNull(state.currentSpanStyle.fontWeight)
        val remaining = editor.text!!.getSpans(0, 11, StyleSpan::class.java)
            .filter { it.style == Typeface.BOLD }
        assertEquals(1, remaining.size)
        assertEquals(0, editor.text!!.getSpanStart(remaining[0]))
        assertEquals(3, editor.text!!.getSpanEnd(remaining[0]))
    }

    @Test
    fun bold_combinedWithCenterAlignment_appliesBothToSubsequentTypedWords() {
        val (state, editor) = createBoundState("Intro")
        state.onEditorContentChanged(editor.text, 5, 5)

        state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
        state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
        assertEquals(TextAlign.Center, state.currentTextAlign)
        assertEquals(FontWeight.Bold, state.currentSpanStyle.fontWeight)

        editor.text!!.insert(5, "Heading")
        state.applyActiveTypingStyle(editor.text!!, 5, 12)
        state.onEditorContentChanged(editor.text, editor.selectionStart, editor.selectionEnd)

        assertEquals("Intro\nHeading", editor.text.toString())
        // "Intro" stays Left + plain
        assertTrue(editor.text!!.getSpans(0, 5, AlignmentSpan::class.java).isEmpty())
        assertTrue(editor.text!!.getSpans(0, 5, StyleSpan::class.java).isEmpty())
        // "Heading" is Centered + Bold
        val headingStart = editor.text.toString().indexOf("Heading")
        val headingEnd = headingStart + "Heading".length
        assertTrue(editor.text!!.getSpans(headingStart, headingEnd, AlignmentSpan::class.java).any { it.alignment == Layout.Alignment.ALIGN_CENTER })
        assertTrue(editor.text!!.getSpans(headingStart, headingEnd, StyleSpan::class.java).any { it.style == Typeface.BOLD })
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
