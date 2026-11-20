package com.example.voicejournal.Data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VoiceJournalLockTest {

    @Test
    fun verifyPin_acceptsMatchingPinAndRejectsNonMatchingPin() {
        val journal = VoiceJournal(
            id = 1,
            title = "<p>Secret</p>",
            content = "Body",
            created = 1L,
            fileName = "",
            color = 0,
            imageUris = null,
            tags = null,
            favourite = false,
            noteBlocks = null,
            isLocked = true,
            lockPinHash = hashNotePin("1234")
        )

        assertTrue(journal.verifyPin("1234"))
        assertFalse(journal.verifyPin("9999"))
    }

    @Test
    fun primaryPreviews_maskLockedContentWhenRevealDisabled() {
        val journal = VoiceJournal(
            id = 1,
            title = "<p>Secret</p>",
            content = "Private body",
            created = 1L,
            fileName = "/tmp/audio.3gp",
            color = 0,
            imageUris = listOf("content://image/1"),
            tags = null,
            favourite = false,
            noteBlocks = null,
            isLocked = true,
            lockPinHash = hashNotePin("1234")
        )

        assertEquals("<p>Locked note</p>", journal.primaryRichTitleHtml(revealLockedContent = false))
        assertEquals("Unlock this note to view its content.", journal.primaryBodyPreviewText(revealLockedContent = false))
        assertEquals("", journal.primaryAudioPath(revealLockedContent = false))
        assertTrue(journal.imageUrisFromBlocks(revealLockedContent = false).isEmpty())
    }

    @Test
    fun matchesVisibleMetadata_onlyMatchesPublicLockedText() {
        val journal = VoiceJournal(
            id = 1,
            title = "<p>Secret</p>",
            content = "Hidden phrase",
            created = 1L,
            fileName = "",
            color = 0,
            imageUris = null,
            tags = null,
            favourite = false,
            noteBlocks = null,
            folderName = "Work",
            isLocked = true,
            lockPinHash = hashNotePin("1234")
        )

        assertTrue(journal.matchesVisibleMetadata("locked"))
        assertTrue(journal.matchesVisibleMetadata("work"))
        assertFalse(journal.matchesVisibleMetadata("hidden"))
    }

    @Test
    fun shareText_masksLockedContentWhenRevealDisabled() {
        val journal = VoiceJournal(
            id = 1,
            title = "<p>Secret title</p>",
            content = "Hidden phrase",
            created = 1L,
            fileName = "",
            color = 0,
            imageUris = null,
            tags = null,
            favourite = false,
            noteBlocks = null,
            isLocked = true,
            lockPinHash = hashNotePin("1234")
        )

        val shared = journal.shareText(revealLockedContent = false)

        assertTrue(shared.contains("Locked note"))
        assertFalse(shared.contains("Hidden phrase"))
    }
}
