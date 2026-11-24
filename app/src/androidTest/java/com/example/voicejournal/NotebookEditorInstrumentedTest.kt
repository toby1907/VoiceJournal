package com.example.voicejournal

import android.Manifest
import android.os.Build
import android.os.Environment
import android.view.KeyEvent
import android.widget.EditText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.hamcrest.Matcher
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class NotebookEditorInstrumentedTest {

    @get:Rule(order = 0)
    val permissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        GrantPermissionRule.grant(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val device: UiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private fun takeScreenshot(name: String) {
        device.executeShellCommand("mkdir -p /data/local/tmp/test_screenshots")
        device.executeShellCommand("screencap -p /data/local/tmp/test_screenshots/$name.png")
    }

    private fun setEditorLinesAndFocus(linesText: String): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<android.view.View> = isAssignableFrom(EditText::class.java)
            override fun getDescription(): String = "Set 15 lines of text and move cursor to end"
            override fun perform(uiController: UiController, view: android.view.View) {
                val editText = view as EditText
                editText.requestFocus()
                editText.setText(linesText)
                editText.setSelection(editText.text.length)
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    @Test
    fun endToEnd_newNote_title_multiPage_scroll_drawing_andSaveToHome() {
        val noteTitle = "Emulator E2E Note"

        // 1. Wait for Home screen Add Note FAB and tap it
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithContentDescription("Add Note").fetchSemanticsNodes().isNotEmpty()
        }
        takeScreenshot("01_home_screen")
        composeRule.onNodeWithContentDescription("Add Note").performClick()

        // 2. Edit TopBar title
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("note_title_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("note_title_input").performClick()
        composeRule.onNodeWithTag("note_title_input").performTextInput(noteTitle)
        composeRule.waitForIdle()
        takeScreenshot("02_editor_title_entered")

        // 3. Enter 15 lines in Notebook Canvas Editor, capture Line 15 + scrollbar screenshot, then press Enter to trigger auto page break (2 / 2)
        val fifteenLines = (1..15).joinToString("\n") { "Notebook line $it" }
        onView(withContentDescription("Notebook Canvas Editor"))
            .perform(setEditorLinesAndFocus(fifteenLines))
        composeRule.waitForIdle()
        takeScreenshot("02b_line15_keyboard_open_scrollbar")

        onView(withContentDescription("Notebook Canvas Editor"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("2 / 2").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("2 / 2").assertIsDisplayed()
        takeScreenshot("03_multipage_break")

        // 4. Vertical drag scroll on the Notebook Canvas Editor
        val canvasObj = device.findObject(UiSelector().description("Notebook Canvas Editor"))
        assertTrue("Notebook Canvas Editor should be visible", canvasObj.waitForExists(5_000))
        val bounds = canvasObj.visibleBounds
        val centerX = bounds.centerX()
        device.swipe(centerX, bounds.top + (bounds.height() * 0.3f).toInt(), centerX, bounds.top + (bounds.height() * 0.75f).toInt(), 20)
        composeRule.waitForIdle()
        device.swipe(centerX, bounds.top + (bounds.height() * 0.75f).toInt(), centerX, bounds.top + (bounds.height() * 0.25f).toInt(), 20)
        composeRule.waitForIdle()
        takeScreenshot("04_vertical_scrolled")

        // 5. Enter Drawing Mode, draw a stroke, tap pinned Done
        composeRule.onNodeWithContentDescription("Drawing Mode")
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Done").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Done").assertIsDisplayed()

        val drawY = bounds.top + (bounds.height() * 0.45f).toInt()
        device.swipe(
            bounds.left + (bounds.width() * 0.2f).toInt(),
            drawY,
            bounds.left + (bounds.width() * 0.8f).toInt(),
            drawY + 80,
            25
        )
        composeRule.waitForIdle()
        takeScreenshot("05_drawing_mode")

        composeRule.onNodeWithText("Done").performClick()
        composeRule.waitForIdle()

        // 6. Tap Save Note checkmark
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithContentDescription("Save Note").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Save Note").performClick()

        // 7. Verify returned to Home screen and saved note appears
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText(noteTitle, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(noteTitle, substring = true).assertIsDisplayed()
        takeScreenshot("06_saved_note_on_home")
    }
}
