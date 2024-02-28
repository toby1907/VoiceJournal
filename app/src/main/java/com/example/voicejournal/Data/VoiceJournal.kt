package com.example.voicejournal.Data

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.voicejournal.ui.main.AddVoiceNote.components.Tag
import com.example.voicejournal.ui.theme.*
@TypeConverters(UriListConverter::class, TagListConverter::class)
@Entity(tableName = "voice.db")
data class VoiceJournal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "voice_id" )
    val id: Int? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "created") val created: Long,
    @ColumnInfo(name = "fileName") var fileName: String,
    @ColumnInfo(name = "color") var color: Int,
    @ColumnInfo(name = "imageUris") var imageUris: List<String>?,
    @ColumnInfo(name = "tags") var tags: List<Tag>? // The new property for the list of tags
)
{
    companion object {
        val noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }

}
class InvalidNoteException(message: String): Exception(message)
// Define a converter class that can convert a list of strings to a single string and back
class UriListConverter {
    @TypeConverter
    fun fromUriList(uriList: List<String>?): String? {
        return uriList?.joinToString(",")
    }

    @TypeConverter
    fun toUriList(uriString: String?): List<String>? {
        return uriString?.split(",")
    }
}
class TagListConverter {
    // A function that converts a list of tags to a string
    @TypeConverter
    fun fromTagList(tags: List<Tag>?): String? {
        // Use a delimiter to separate the tag names and the checked states
        val delimiter = ","
        // Join the tag names and the checked states into a single string
        return tags?.joinToString(delimiter) { tag -> "${tag.name}:${tag.isChecked}" }
    }

    // A function that converts a string to a list of tags
    @TypeConverter
    fun toTagList(tagString: String?): List<Tag>? {
        // Use the same delimiter to split the string
        val delimiter = ","
        // Split the string into a list of tag names and checked states
        val tagPairs = tagString?.split(delimiter)
        // Map each pair to a Tag object
        return tagPairs?.map { pair ->
            // Split the pair into a name and a checked state
            val (name, checked) = pair.split(":")
            // Create a Tag object with the name and the checked state
            Tag(name, checked.toBoolean())
        }
    }
}
