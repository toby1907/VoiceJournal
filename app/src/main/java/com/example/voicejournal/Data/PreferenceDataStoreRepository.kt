package com.example.voicejournal.Data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val SELECTED_URIS_KEY = stringSetPreferencesKey("selected_uris")
    }

    suspend fun saveSelectedUris(selectedUris: List<String>) {
        dataStore.edit { preferences ->
            // Read the current value of SELECTED_URIS_KEY
            val currentUris = preferences[SELECTED_URIS_KEY] ?: emptySet()
            // Create a new set that contains the current and new uris
            val newUris = currentUris.plus(selectedUris)
            // Return a new preferences object with the updated value
            preferences[SELECTED_URIS_KEY] = newUris
        }
    }
    suspend fun removeSelectedUris() {
        dataStore.edit { preferences ->
            preferences.remove(SELECTED_URIS_KEY)
        }
    }

    fun getSelectedUris(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[SELECTED_URIS_KEY] ?: emptySet()
        }
    }
}