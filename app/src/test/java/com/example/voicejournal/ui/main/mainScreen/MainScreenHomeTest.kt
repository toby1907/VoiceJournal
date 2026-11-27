package com.example.voicejournal.ui.main.mainScreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.voicejournal.Data.model.VoiceJournal
import com.example.voicejournal.Data.model.hashNotePin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w411dp-h891dp-xxhdpi")
class MainScreenHomeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val utcZone: ZoneId = ZoneId.of("UTC")

    @After
    fun tearDown() {
        AudioDurationCache.clear()
    }

    private fun sampleNote(
        id: Int,
        date: LocalDate,
        hour: Int = 9,
        heading: String? = "Note $id",
        richBodyHtml: String = "<p>Details for note $id</p>",
        audioPath: String = "",
        imageUris: List<String>? = null,
        favourite: Boolean = false,
        folderName: String? = null,
        isLocked: Boolean = false
    ): VoiceJournal {
        return VoiceJournal(
            id = id,
            title = richBodyHtml,
            content = heading,
            created = date.atTime(hour, 15).atZone(utcZone).toInstant().toEpochMilli(),
            fileName = audioPath,
            color = -1,
            imageUris = imageUris,
            tags = emptyList(),
            favourite = favourite,
            folderName = folderName,
            isLocked = isLocked,
            lockPinHash = if (isLocked) hashNotePin("1234") else null
        )
    }

    @Test
    fun formatAudioDurationLabel_formatsSecondsMinutesAndHours() {
        assertEquals("", formatAudioDurationLabel(-100L))
        assertEquals("", formatAudioDurationLabel(0L))
        assertEquals("0:01", formatAudioDurationLabel(400L))
        assertEquals("0:08", formatAudioDurationLabel(8_000L))
        assertEquals("1:24", formatAudioDurationLabel(84_000L))
        assertEquals("1:02:05", formatAudioDurationLabel(3_725_000L))
    }

    @Test
    fun homeFiltersAndDateHeaders_workAccuratelyAndRespectLocks() {
        val today = LocalDate.of(2026, 11, 27)
        val yesterday = LocalDate.of(2026, 11, 26)
        val older = LocalDate.of(2026, 11, 10)

        assertEquals("Today · Nov 27", formatHomeDateGroupHeader(today, today, Locale.US))
        assertEquals("Yesterday · Nov 26", formatHomeDateGroupHeader(yesterday, today, Locale.US))
        assertEquals("November 10, 2026 · Tue", formatHomeDateGroupHeader(older, today, Locale.US))

        val voiceNote = sampleNote(id = 1, date = today, audioPath = "/tmp/a.mp3")
        val photoNote = sampleNote(id = 2, date = yesterday, imageUris = listOf("content://img/1"), favourite = true)
        val lockedVoice = sampleNote(id = 3, date = older, audioPath = "/tmp/secret.mp3", isLocked = true)

        val all = listOf(voiceNote, photoNote, lockedVoice)
        assertEquals(listOf(1), filterNotesByQuickFilter(all, HomeQuickFilter.VOICE).map { it.id })
        assertEquals(listOf(2), filterNotesByQuickFilter(all, HomeQuickFilter.PHOTOS).map { it.id })
        assertEquals(listOf(2), filterNotesByQuickFilter(all, HomeQuickFilter.FAVORITES).map { it.id })
    }

    @Test
    fun newJournalItem_withAudioFile_displaysAudioIconAndDuration() {
        val today = LocalDate.of(2026, 11, 27)
        val audioPath = "/tmp/cached_voice_memo.mp3"
        AudioDurationCache.put(audioPath, 84_000L)

        val audioNote = sampleNote(
            id = 5,
            date = today,
            heading = "Morning Voice Memo",
            audioPath = audioPath
        )

        composeRule.setContent {
            MaterialTheme {
                NewJournalItem(voiceJournal = audioNote)
            }
        }

        composeRule.onNodeWithText("Morning Voice Memo").assertIsDisplayed()
        composeRule.onNodeWithTag("note_audio_duration_badge").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Audio file").assertIsDisplayed()
        composeRule.onNodeWithText("1:24").assertIsDisplayed()
    }

    @Test
    fun newJournalItem_lockedNote_hidesPrivateContentAndAudioBadge() {
        val today = LocalDate.of(2026, 11, 27)
        val lockedAudioNote = sampleNote(
            id = 6,
            date = today,
            heading = "Secret Plan",
            audioPath = "/tmp/secret.mp3",
            isLocked = true
        )

        composeRule.setContent {
            MaterialTheme {
                NewJournalItem(voiceJournal = lockedAudioNote, audioDurationMsOverride = 45_000L)
            }
        }

        composeRule.onNodeWithText("Locked note").assertIsDisplayed()
        composeRule.onNodeWithText("Secret Plan").assertDoesNotExist()
        composeRule.onNodeWithTag("note_audio_duration_badge").assertDoesNotExist()
    }

    @Test
    fun noteListContent_supportsQuickFiltersFolderChipAndEmptyState() {
        val today = LocalDate.of(2026, 11, 27)
        val audioPath = "/tmp/memo_45s.mp3"
        AudioDurationCache.put(audioPath, 45_000L)

        val voiceNote = sampleNote(id = 1, date = today, hour = 10, heading = "Voice Summary", audioPath = audioPath, folderName = "Work")
        val textNote = sampleNote(id = 2, date = today, hour = 8, heading = "Quick Idea", folderName = "Work")

        var opened: VoiceJournal? = null
        var clearedFolder = false

        composeRule.setContent {
            MaterialTheme {
                NoteListContent(
                    journals = listOf(voiceNote, textNote),
                    selectedFolder = "Work",
                    onClearFolder = { clearedFolder = true },
                    onOpenJournal = { opened = it },
                    today = today,
                    zoneId = utcZone
                )
            }
        }

        composeRule.onNodeWithText("Voice Summary").assertIsDisplayed()
        composeRule.onNodeWithText("0:45").assertIsDisplayed()
        composeRule.onNodeWithText("Quick Idea").assertIsDisplayed()
        composeRule.onNodeWithTag("home_active_folder_chip").assertIsDisplayed()

        // Filter to Voice-only notes
        composeRule.onNodeWithTag("home_filter_voice").performClick()
        composeRule.onNodeWithText("Voice Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Quick Idea").assertDoesNotExist()

        // Filter to Photos (none exist -> empty state card with "Show all entries")
        composeRule.onNodeWithTag("home_filter_photos").performClick()
        composeRule.onNodeWithTag("home_empty_state_card").assertIsDisplayed()
        composeRule.onNodeWithText("Show all entries").performClick()
        assertTrue(clearedFolder)
        composeRule.onNodeWithText("Quick Idea").assertIsDisplayed()

        // Clicking a note invokes onOpenJournal
        composeRule.onNodeWithTag("home_note_item_1").performClick()
        assertEquals(1, opened?.id)
    }

    @Test
    fun noteListContent_whenJournalIsEmpty_showsCreateFirstEntryCta() {
        var createClicked = false

        composeRule.setContent {
            MaterialTheme {
                NoteListContent(
                    journals = emptyList(),
                    onOpenJournal = {},
                    onCreateNote = { createClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Your journal awaits").assertIsDisplayed()
        composeRule.onNodeWithText("Create first entry").performClick()
        assertTrue(createClicked)
    }
}
