package com.example.voicejournal.ui.common.richeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.text.style.AbsoluteSizeSpan
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.encodeDrawingPath
import com.example.voicejournal.ui.main.AddVoiceNote.components.CanvasDrawingTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RichEditTextCanvasTest {

    private lateinit var context: Context
    private lateinit var state: RichTextState
    private lateinit var editor: RichEditText

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        state = RichTextState()
        editor = RichEditText(context).apply {
            layoutParams = ViewGroup.LayoutParams(1080, 900)
            richTextState = state
            onSelectionChangedListener = { selStart, selEnd ->
                state.onEditorContentChanged(text, selStart, selEnd)
            }
        }
        state.bindEditor(editor)
    }

    private fun layoutEditor(width: Int = 1080, height: Int = 900) {
        editor.layoutParams = ViewGroup.LayoutParams(width, height)
        val wSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val hSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        editor.measure(wSpec, hSpec)
        editor.layout(0, 0, editor.measuredWidth, editor.measuredHeight)
    }

    @Test
    fun lineHeightAndPageMetrics_match38dpAnd15LinesPerPage() {
        val density = context.resources.displayMetrics.density
        val expectedLineHeight = (38f * density).roundToInt()
        val expectedTopBottomPad = (20f * density).roundToInt()

        assertEquals(15, RichEditText.LINES_PER_PAGE)
        assertEquals(expectedLineHeight, editor.fixedLineHeight)
        assertEquals(15 * expectedLineHeight, editor.pageContentHeight)
        assertEquals(editor.pageContentHeight + expectedTopBottomPad * 2, editor.pageHeightPx)
    }

    @Test
    fun enterOn15thLine_autoInsertsPageBreak_andBackspaceRemovesIt() {
        val fifteenLines = (1..15).joinToString("\n") { "Line $it" }
        state.setText(fifteenLines)
        editor.setSelection(editor.text!!.length)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        layoutEditor()

        assertEquals(1, state.pageCount())

        var autoBreakTriggered = false
        editor.onPageBreakAutoInserted = { autoBreakTriggered = true }

        val enterEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
        val handledEnter = editor.dispatchKeyEvent(enterEvent)
        ShadowLooper.idleMainLooper()

        assertTrue("Enter on 15th line should be intercepted for page break", handledEnter)
        assertTrue("onPageBreakAutoInserted callback should fire", autoBreakTriggered)
        assertEquals(2, state.pageCount())
        assertEquals(2, editor.getTotalPages())
        assertEquals(1, state.currentPageIndex())

        // Now Backspace right after the page break marker should remove the page break
        val delEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)
        val handledDel = editor.dispatchKeyEvent(delEvent)

        assertTrue("Backspace immediately after page break should be intercepted", handledDel)
        assertEquals(1, state.pageCount())
        assertFalse(state.annotatedString.text.contains("Page break"))
    }

    @Test
    fun verticalDragScroll_scrollsWhileFocused() {
        val page1 = (1..15).joinToString("\n") { "Page 1 line $it" }
        state.setText(page1)
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        state.insertPageBreak()
        layoutEditor(width = 1080, height = 500)

        editor.requestFocus()
        assertTrue("Multi-page editor must have positive maxScrollY", editor.getMaxScrollY() > 0)

        var lastReportedScrollY = 0
        editor.onScrollListener = { _, y -> lastReportedScrollY = y }

        val downTime = SystemClock.uptimeMillis()
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 200f, 450f, 0))
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime + 20, MotionEvent.ACTION_MOVE, 200f, 350f, 0))
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime + 40, MotionEvent.ACTION_MOVE, 200f, 150f, 0))
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime + 60, MotionEvent.ACTION_UP, 200f, 150f, 0))

        assertTrue("Vertical drag should scroll editor down", editor.scrollY > 0)
        assertTrue("onScrollListener should receive positive scrollY", lastReportedScrollY > 0)
    }

    @Test
    fun penHighlighterEraser_normalizedStrokesAndOnDraw() {
        state.setText("Annotated notebook line")
        layoutEditor(width = 1080, height = 900)

        val capturedStrokes = mutableListOf<NoteBlock>()
        editor.drawingEnabled = true
        editor.onStrokeDrawn = { pageIndex, points, colorArgb, strokeWidthPx, alpha, tool ->
            capturedStrokes.add(
                NoteBlock(
                    id = "stroke-${capturedStrokes.size}",
                    order = capturedStrokes.size,
                    type = NoteBlockType.DRAWING.name,
                    canvasPageIndex = pageIndex,
                    drawingPath = encodeDrawingPath(points),
                    drawingColor = colorArgb,
                    drawingStrokeWidth = strokeWidthPx,
                    drawingAlpha = alpha,
                    drawingTool = tool
                )
            )
        }

        // Draw a PEN stroke
        editor.drawingTool = CanvasDrawingTool.PEN
        editor.drawingColorArgb = 0xFF1A73E8L
        editor.drawingStrokeWidthDp = 4f
        editor.drawingAlpha = 1f
        val t1 = SystemClock.uptimeMillis()
        editor.onTouchEvent(MotionEvent.obtain(t1, t1, MotionEvent.ACTION_DOWN, 120f, 140f, 0))
        editor.onTouchEvent(MotionEvent.obtain(t1, t1 + 16, MotionEvent.ACTION_MOVE, 320f, 180f, 0))
        editor.onTouchEvent(MotionEvent.obtain(t1, t1 + 32, MotionEvent.ACTION_UP, 520f, 220f, 0))

        assertEquals(1, capturedStrokes.size)
        assertEquals(CanvasDrawingTool.PEN.name, capturedStrokes[0].drawingTool)
        assertEquals(0, capturedStrokes[0].canvasPageIndex)

        // Draw a HIGHLIGHTER stroke
        editor.drawingTool = CanvasDrawingTool.HIGHLIGHTER
        editor.drawingColorArgb = 0xFFFFD54FL
        editor.drawingStrokeWidthDp = 14f
        editor.drawingAlpha = 0.5f
        val t2 = t1 + 100
        editor.onTouchEvent(MotionEvent.obtain(t2, t2, MotionEvent.ACTION_DOWN, 100f, 90f, 0))
        editor.onTouchEvent(MotionEvent.obtain(t2, t2 + 16, MotionEvent.ACTION_MOVE, 400f, 90f, 0))
        editor.onTouchEvent(MotionEvent.obtain(t2, t2 + 32, MotionEvent.ACTION_UP, 700f, 90f, 0))

        assertEquals(2, capturedStrokes.size)
        assertEquals(CanvasDrawingTool.HIGHLIGHTER.name, capturedStrokes[1].drawingTool)

        // Render ruled lines, highlighter under text, text, and pen over text via onDraw
        editor.drawingBlocks = capturedStrokes.toList()
        val bitmap = Bitmap.createBitmap(1080, 900, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        editor.draw(canvas)

        // Eraser hit test
        var erasedPage = -1
        var erasedX = -1f
        var erasedY = -1f
        var erasedRadius = 0f
        editor.drawingTool = CanvasDrawingTool.ERASER
        editor.onEraseStroke = { pageIndex, nx, ny, radius ->
            erasedPage = pageIndex
            erasedX = nx
            erasedY = ny
            erasedRadius = radius
        }
        val t3 = t2 + 100
        editor.onTouchEvent(MotionEvent.obtain(t3, t3, MotionEvent.ACTION_DOWN, 320f, 180f, 0))
        editor.onTouchEvent(MotionEvent.obtain(t3, t3 + 16, MotionEvent.ACTION_UP, 320f, 180f, 0))

        assertEquals(0, erasedPage)
        assertTrue("Normalized X must be in [0, 1]", erasedX in 0f..1f)
        assertTrue("Normalized Y must be in [0, 1]", erasedY in 0f..1f)
        assertTrue("Eraser radius must be positive", erasedRadius > 0f)
    }

    @Test
    fun tapOnEmptyCanvas_placesCursorOnTargetLine() {
        state.setText("")
        layoutEditor(width = 1080, height = 900)

        val density = context.resources.displayMetrics.density
        val topPad = (20f * density).roundToInt()
        val tapY = topPad + (4.5f * editor.fixedLineHeight)

        val downTime = SystemClock.uptimeMillis()
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, 100f, tapY, 0))
        editor.onTouchEvent(MotionEvent.obtain(downTime, downTime + 40, MotionEvent.ACTION_UP, 102f, tapY + 1f, 0))

        val newlineCount = editor.text.toString().count { it == '\n' }
        assertEquals("Tapping line index 4 should insert 4 newlines", 4, newlineCount)
        assertEquals(editor.text!!.length, editor.selectionEnd)
    }

    @Test
    fun multiSizeHtml_roundTripsFontSizes() {
        state.setText("Title Body Caption")
        // Apply 24sp to "Title" (0..5)
        state.onEditorContentChanged(editor.text, 0, 5)
        state.setFontSize(24)
        // Apply 12sp to "Caption" (11..18)
        state.onEditorContentChanged(editor.text, 11, 18)
        state.setFontSize(12)

        val html = state.toHtml()
        assertTrue("HTML should preserve 24sp data-font-size", html.contains("data-font-size=\"24\""))
        assertTrue("HTML should preserve 12sp data-font-size", html.contains("data-font-size=\"12\""))

        val reloadedState = RichTextState()
        val reloadedEditor = RichEditText(context)
        reloadedState.bindEditor(reloadedEditor)
        reloadedState.setHtml(html)

        val spans = reloadedEditor.text!!.getSpans(0, reloadedEditor.text!!.length, AbsoluteSizeSpan::class.java)
        val sizes = spans.map { it.size }.toSet()
        assertTrue("Reloaded spans should contain 24sp", sizes.contains(24))
        assertTrue("Reloaded spans should contain 12sp", sizes.contains(12))
    }

    @Test
    fun onMeasure_respectsExactlyAndAtMostViewportBounds() {
        state.setText((1..15).joinToString("\n") { "Line $it" })
        state.onEditorContentChanged(editor.text, editor.text!!.length, editor.text!!.length)
        state.insertPageBreak()

        val wSpec = View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY)
        val exactH = 600
        editor.measure(wSpec, View.MeasureSpec.makeMeasureSpec(exactH, View.MeasureSpec.EXACTLY))
        assertEquals("EXACTLY height must not expand past viewport", exactH, editor.measuredHeight)

        editor.layout(0, 0, editor.measuredWidth, editor.measuredHeight)
        assertTrue("Bounded multi-page viewport must be scrollable", editor.getMaxScrollY() > 0)

        val atMostH = 700
        editor.measure(wSpec, View.MeasureSpec.makeMeasureSpec(atMostH, View.MeasureSpec.AT_MOST))
        assertTrue("AT_MOST height must be <= hSize", editor.measuredHeight <= atMostH)
    }

    @Test
    fun bottomScrollClearance_provides180dpWithoutPaddingBottomClipping() {
        val density = context.resources.displayMetrics.density
        val expectedBottomPad = (20f * density).roundToInt()
        val expectedScrollClearance = (180f * density).roundToInt()

        assertEquals(
            "paddingBottom must stay 20dp so TextView does not clip lines 12-15",
            expectedBottomPad,
            editor.paddingBottom
        )
        assertEquals(expectedScrollClearance, editor.bottomScrollPaddingPx)

        layoutEditor(width = 1080, height = 500)
        val pages = editor.getTotalPages()
        val expectedTotalH = pages * editor.pageHeightPx + (pages - 1) * editor.pageGapPx + expectedScrollClearance
        assertEquals(maxOf(0, expectedTotalH - 500), editor.getMaxScrollY())
    }
}
