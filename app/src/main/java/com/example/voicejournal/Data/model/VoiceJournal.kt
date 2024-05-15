package com.example.voicejournal.Data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.HtmlCompat
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
    var id: Int? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "created") val created: Long,
    @ColumnInfo(name = "fileName") var fileName: String,
    @ColumnInfo(name = "color") var color: Int,
    @ColumnInfo(name = "imageUris") var imageUris: List<String>?,
    @ColumnInfo(name = "tags") var tags: List<Tag>?,
    @ColumnInfo(name = "favourite") var favourite:Boolean?,
)
{
    companion object {
        val noteColors = listOf(RedOrange, LightGreen, Violet, BabyBlue, RedPink)
    }
    fun doesMatchSearchQuery(query: String): Boolean {
        // State to hold the parsed AnnotatedString


        fun parseHtml(htmlString: String) :String{
            var annotatedString by mutableStateOf<AnnotatedString?>(null)
            val spanned = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)
            val annotated = buildAnnotatedString {
                append(spanned)
            }
            annotatedString = annotated
            return annotatedString!!.text
        }
        val matchingCombinations = listOf(
            parseHtml(title),
            "${parseHtml(title).first()}",
            "${content?.first()}",
            "${ tags?.get(0)?.name }"
        )

        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
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
    fun toTagList(tagString: String?): List<Tag> {
        if (tagString.isNullOrEmpty()) {
            // Handle empty input (return an empty list or handle differently)
            return emptyList()
        }

        val delimiter = ","
        val tagPairs = tagString.split(delimiter)

        return tagPairs.mapNotNull { pair ->
            val (name, checked) = pair.split(":")
            try {
                Tag(name, checked.toBoolean())
            } catch (e: Exception) {
                // Handle invalid pair (e.g., missing checked state)
                null
            }
        }
    }

}
