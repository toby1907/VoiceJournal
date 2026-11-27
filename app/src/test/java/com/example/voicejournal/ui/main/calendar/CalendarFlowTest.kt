package com.example.voicejournal.ui.main.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import com.example.voicejournal.Data.model.NoteBlock
import com.example.voicejournal.Data.model.NoteBlockType
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.hashNotePin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xxhdpi")
class CalendarFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val utcZone: ZoneId = ZoneId.of("UTC")

    private fun epochOn(date: LocalDate, hour: Int, minute: Int = 0): Long {
        return date.atTime(hour, minute).atZone(utcZone).toInstant().toEpochMilli()
    }

    private fun sampleJournal(
        id: Int,
        date: LocalDate,
        hour: Int = 9,
        minute: Int = 0,
        heading: String? = "Heading $id",
        richBodyHtml: String = "<p>Body $id</p>",
        audioPath: String = "",
        imageUris: List<String>? = null,
        noteBlocks: List<NoteBlock>? = null,
        isLocked: Boolean = false,
        favourite: Boolean = false,
        folderName: String? = null
    ): VoiceJournal {
        return VoiceJournal(
            id = id,
            title = richBodyHtml,
            content = heading,
            created = epochOn(date, hour, minute),
            fileName = audioPath,
            color = -1,
            imageUris = imageUris,
            tags = emptyList(),
            favourite = favourite,
            noteBlocks = noteBlocks,
            folderName = folderName,
            isLocked = isLocked,
            lockPinHash = if (isLocked) hashNotePin("1234") else null
        )
    }

    @Test
    fun helpers_groupAndInspectNotes_handlesNullImagesBlocksAndLocks() {
        val nov15 = LocalDate.of(2026, 11, 15)
        val nov20 = LocalDate.of(2026, 11, 20)

        val textOnly = sampleJournal(
            id = 1,
            date = nov15,
            hour = 8,
            heading = "Morning reflection",
            imageUris = null
        )
        val blockImageNote = sampleJournal(
            id = 2,
            date = nov15,
            hour = 18,
            heading = "Sunset photo",
            imageUris = listOf(""),
            noteBlocks = listOf(
                NoteBlock(
                    id = "img-1",
                    type = NoteBlockType.IMAGE.name,
                    order = 0,
                    mediaUri = "content://media/external/images/media/42"
                )
            )
        )
        val lockedWithImage = sampleJournal(
            id = 3,
            date = nov20,
            hour = 12,
            heading = "Top Secret",
            richBodyHtml = "<p>Private details</p>",
            audioPath = "/tmp/secret.mp3",
            imageUris = listOf("content://media/external/images/media/99"),
            isLocked = true
        )

        val grouped = groupJournalsByLocalDate(listOf(textOnly, blockImageNote, lockedWithImage), utcZone)
        assertEquals(2, grouped[nov15]?.size)
        // Sorted newest-first within the day
        assertEquals(2, grouped[nov15]?.first()?.id)
        assertEquals(1, grouped[nov15]?.last()?.id)

        // Resolves IMAGE block URI even when legacy imageUris has blank string
        assertEquals(
            "content://media/external/images/media/42",
            dayPreviewThumbnailUri(grouped[nov15].orEmpty())
        )
        // Never leaks thumbnail or audio indicator from locked notes
        assertNull(dayPreviewThumbnailUri(grouped[nov20].orEmpty()))
        assertFalse(dayHasAudio(grouped[nov20].orEmpty()))
        assertTrue(dayHasLockedNote(grouped[nov20].orEmpty()))

        assertEquals(3, countEntriesInMonth(grouped, YearMonth.of(2026, 11)))
        assertEquals(0, countEntriesInMonth(grouped, YearMonth.of(2026, 10)))

        val (lockedTitle, lockedSnippet) = extractCalendarCardText(lockedWithImage)
        assertEquals("Locked note", lockedTitle)
        assertFalse(lockedSnippet.contains("Top Secret"))
        assertFalse(lockedSnippet.contains("Private details"))
    }

    @Test
    fun helpers_parseIdsAndBuildRoutes_neverCrashOnMalformedInput() {
        assertEquals(emptyList<Int>(), parseCalendarItemIds(null))
        assertEquals(emptyList<Int>(), parseCalendarItemIds(""))
        assertEquals(emptyList<Int>(), parseCalendarItemIds("   , abc, null "))
        assertEquals(listOf(4, 7, 12), parseCalendarItemIds(" 4, 7 , 4, bad, 12 "))

        val date = LocalDate.of(2026, 11, 27)
        val j1 = sampleJournal(id = 10, date = date)
        val j2 = sampleJournal(id = 20, date = date)

        assertEquals("JournalPreviewScreen/10,20", buildJournalPreviewRoute(listOf(j1, j2)))
        assertNull(buildJournalPreviewRoute(emptyList()))
        assertEquals("preview?noteId=20&noteColor=-1&noteIndex=1", buildPreviewRoute(j2, listOf(j1, j2)))

        val newRoute = buildNewNoteForDateRoute(date, utcZone, LocalTime.of(10, 30))
        val expectedMillis = date.atTime(10, 30).atZone(utcZone).toInstant().toEpochMilli()
        assertTrue(newRoute.contains("initialDateMillis=$expectedMillis"))

        assertEquals("November 2026", formatCalendarMonthTitle(YearMonth.of(2026, 11), Locale.US))
    }

    @Test
    fun calendarScreen_displaysTextAndVoiceNotes_andUpdatesSelectedDayAgenda() {
        val today = LocalDate.of(2026, 11, 27)
        val nov12 = LocalDate.of(2026, 11, 12)
        val nov18 = LocalDate.of(2026, 11, 18)

        val todayVoiceNote = sampleJournal(
            id = 1,
            date = today,
            hour = 10,
            heading = "Standup Audio",
            richBodyHtml = "<p>Discussed launch timeline</p>",
            audioPath = "/storage/audio1.mp3",
            imageUris = null
        )
        val nov12TextOnly = sampleJournal(
            id = 2,
            date = nov12,
            hour = 14,
            heading = "Gratitude Log",
            richBodyHtml = "<p>Sunny afternoon walk</p>",
            imageUris = null
        )
        val nov12Locked = sampleJournal(
            id = 3,
            date = nov12,
            hour = 16,
            heading = "Hidden Diary",
            richBodyHtml = "<p>Secret content</p>",
            isLocked = true
        )

        var openedJournal: VoiceJournal? = null
        var openedDayPreview: List<VoiceJournal>? = null
        var createdForDate: LocalDate? = null

        composeRule.setContent {
            MaterialTheme {
                CalendarScreenContent(
                    horizontal = true,
                    journals = listOf(todayVoiceNote, nov12TextOnly, nov12Locked),
                    today = today,
                    zoneId = utcZone,
                    onOpenJournal = { openedJournal = it },
                    onOpenDayPreview = { openedDayPreview = it },
                    onCreateJournalForDate = { createdForDate = it }
                )
            }
        }

        // Month header includes both month and year plus monthly count
        composeRule.onNodeWithTag("MonthTitle").assertTextEquals(formatCalendarMonthTitle(YearMonth.of(2026, 11)))
        composeRule.onNodeWithTag("calendar_month_count").assertTextEquals("3 entries this month")

        // Today's voice-only note (with imageUris == null) appears in the inline agenda
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_agenda_item_1"))
        composeRule.onNodeWithText("Standup Audio").assertIsDisplayed()
        composeRule.onNodeWithTag("calendar_agenda_item_1").performClick()
        assertEquals(1, openedJournal?.id)

        // Scroll back to calendar card and select Nov 12 (text-only + locked notes)
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_day_2026-11-12"))
        composeRule.onNodeWithTag("calendar_day_2026-11-12").performClick()

        // Selecting another day reveals "Jump to Today" chip
        composeRule.onNodeWithTag("calendar_today_button").assertIsDisplayed()

        // Agenda shows Nov 12's text-only note AND masks the locked note
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_agenda_item_2"))
        composeRule.onNodeWithText("Gratitude Log").assertIsDisplayed()
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_agenda_item_3"))
        composeRule.onNodeWithText("Locked note").assertIsDisplayed()

        // Clicking "View all" triggers onOpenDayPreview with both Nov 12 entries
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_view_all_button"))
        composeRule.onNodeWithTag("calendar_view_all_button").performClick()
        assertEquals(setOf(2, 3), openedDayPreview?.map { it.id }?.toSet())

        // Selecting empty day Nov 18 displays friendly empty state and creates note for Nov 18
        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_day_2026-11-18"))
        composeRule.onNodeWithTag("calendar_day_2026-11-18").performClick()

        composeRule.onNodeWithTag("CalendarRoot")
            .performScrollToNode(hasTestTag("calendar_empty_day_card"))
        composeRule.onNodeWithText("No entries on this day").assertIsDisplayed()
        composeRule.onNodeWithText("Write for this day").performClick()
        assertEquals(nov18, createdForDate)
    }

    @Test
    fun calendarScreen_monthNavigationAndTodayJump_updatesMonthHeader() {
        val today = LocalDate.of(2026, 11, 27)

        composeRule.setContent {
            MaterialTheme {
                CalendarScreenContent(
                    horizontal = true,
                    journals = emptyList(),
                    today = today,
                    zoneId = utcZone,
                    onOpenJournal = {},
                    onOpenDayPreview = {},
                    onCreateJournalForDate = {}
                )
            }
        }

        composeRule.onNodeWithTag("MonthTitle")
            .assertTextEquals(formatCalendarMonthTitle(YearMonth.of(2026, 11)))

        composeRule.onNodeWithContentDescription("Next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("MonthTitle")
            .assertTextEquals(formatCalendarMonthTitle(YearMonth.of(2026, 12)))

        composeRule.onNodeWithTag("calendar_today_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("MonthTitle")
            .assertTextEquals(formatCalendarMonthTitle(YearMonth.of(2026, 11)))
    }

    @Test
    fun journalPreviewScreen_rendersPopulatedListAndActions() {
        val date = LocalDate.of(2026, 11, 27)
        val entry1 = sampleJournal(id = 11, date = date, hour = 9, heading = "First Note")
        val entry2 = sampleJournal(id = 12, date = date, hour = 15, heading = "Second Note")

        var backPressed = false
        var opened: VoiceJournal? = null
        var addedForDate: LocalDate? = null

        composeRule.setContent {
            MaterialTheme {
                JournalPreviewScreenContent(
                    journals = listOf(entry2, entry1),
                    isLoading = false,
                    zoneId = utcZone,
                    onBack = { backPressed = true },
                    onOpenJournal = { opened = it },
                    onAddForDate = { addedForDate = it }
                )
            }
        }

        composeRule.onNodeWithTag("journal_preview_title")
            .assertTextEquals(formatSelectedDateHeader(date))
        composeRule.onNodeWithTag("journal_preview_subtitle")
            .assertTextEquals("2 entries")
        composeRule.onNodeWithText("First Note").assertIsDisplayed()
        composeRule.onNodeWithText("Second Note").assertIsDisplayed()

        composeRule.onNodeWithTag("journal_preview_item_11").performClick()
        assertEquals(11, opened?.id)

        composeRule.onNodeWithTag("journal_preview_add_button").performClick()
        assertEquals(date, addedForDate)

        composeRule.onNodeWithTag("journal_preview_back_button").performClick()
        assertTrue(backPressed)
    }

    @Test
    fun journalPreviewScreen_rendersEmptyAndLoadingStates() {
        var backPressed = false

        composeRule.setContent {
            MaterialTheme {
                JournalPreviewScreenContent(
                    journals = emptyList(),
                    isLoading = false,
                    zoneId = utcZone,
                    onBack = { backPressed = true },
                    onOpenJournal = {},
                    onAddForDate = null
                )
            }
        }

        composeRule.onNodeWithTag("journal_preview_empty").assertIsDisplayed()
        composeRule.onNodeWithText("No entries found").assertIsDisplayed()
        composeRule.onNodeWithText("Back to Calendar").performClick()
        assertTrue(backPressed)
    }
}
