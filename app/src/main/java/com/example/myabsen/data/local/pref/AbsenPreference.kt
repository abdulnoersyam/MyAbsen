package com.example.myabsen.data.local.pref

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AbsenPreference(private val dataStore: DataStore<Preferences>) {
    suspend fun saveAbsenSession(absen: String) {
        dataStore.edit { preferences ->
            preferences[ID_ABSEN_KEY] = absen
        }
        Log.d("AbsenPreference", "Saved absen: $absen")
    }

    // Function to get the absen session
    fun getAbsenSession(): Flow<String?> {
        return dataStore.data
            .map { preferences ->
                preferences[ID_ABSEN_KEY]
            }
    }

    // function hapus session
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AbsenPreference? = null

        private val ID_ABSEN_KEY = stringPreferencesKey("id_absen")

        fun getInstance(dataStore: DataStore<Preferences>): AbsenPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = AbsenPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}