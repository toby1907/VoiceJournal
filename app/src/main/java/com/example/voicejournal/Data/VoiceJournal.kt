package com.example.voicejournal.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.voicejournal.ui.theme.*

@Entity(tableName = "voice.db")
data class VoiceJournal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "voice_id" )
    val id: Int? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "created") val created: Long,
    @ColumnInfo(name = "fileName") var fileName: String,
    @ColumnInfo(name = "color") var color: Int
)
{
    companion object {
        val noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }

}
class InvalidNoteException(message: String): Exception(message)