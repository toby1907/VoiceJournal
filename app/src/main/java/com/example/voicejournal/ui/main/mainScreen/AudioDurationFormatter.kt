package com.example.voicejournal.ui.main.mainScreen

import android.media.MediaMetadataRetriever
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Formats an audio duration in milliseconds into a concise label such as "0:42", "3:15", or "1:04:20".
 * Returns an empty string when [durationMs] is non-positive.
 */
fun formatAudioDurationLabel(durationMs: Long): String {
    if (durationMs <= 0L) return ""
    val totalSeconds = ((durationMs + 500L) / 1000L).coerceAtLeast(1L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

object AudioDurationCache {
    private val cache = ConcurrentHashMap<String, Long>()

    fun getCached(path: String): Long? = cache[path]

    fun put(path: String, durationMs: Long) {
        if (path.isNotBlank() && durationMs > 0L) {
            cache[path] = durationMs
        }
    }

    fun clear() {
        cache.clear()
    }

    suspend fun getOrLoad(path: String): Long {
        val normalized = path.trim()
        if (normalized.isBlank()) return 0L
        cache[normalized]?.let { return it }
        val loaded = withContext(Dispatchers.IO) {
            extractDurationMs(normalized)
        }
        if (loaded > 0L) {
            cache[normalized] = loaded
        }
        return loaded
    }

    private fun extractDurationMs(path: String): Long {
        val file = File(path)
        if (!file.exists() || !file.isFile || file.length() <= 0L) return 0L
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.coerceAtLeast(0L)
                ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
                // Ignore release errors
            }
        }
    }
}

@Composable
fun rememberAudioDurationMs(audioPath: String): Long {
    val normalized = audioPath.trim()
    val cached = if (normalized.isNotBlank()) AudioDurationCache.getCached(normalized) else null
    val duration by produceState(initialValue = cached ?: 0L, key1 = normalized) {
        if (normalized.isBlank()) {
            value = 0L
        } else if (cached != null) {
            value = cached
        } else {
            value = AudioDurationCache.getOrLoad(normalized)
        }
    }
    return duration
}
