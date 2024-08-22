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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class UserPreference(private val dataStore: DataStore<Preferences>) {
    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[NIP_KEY] = user.nip
            preferences[MESSAGE_KEY] = user.message
            preferences[FULLNAME_KEY] = user.fullname
            preferences[EMAIL_KEY] = user.email
            preferences[POSITION_KEY] = user.position
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true
        }
        Log.d("UserPreference", "Saved user: $user")
    }

    fun getSession(): Flow<UserModel?> {
        return dataStore.data.map { preferences ->
            val isLogin = preferences[IS_LOGIN_KEY] ?: false
            if (isLogin) {
                UserModel(
                    preferences[NIP_KEY] ?: 0,
                    preferences[TOKEN_KEY] ?: "",
                    preferences[FULLNAME_KEY] ?: "",
                    preferences[POSITION_KEY] ?: "",
                    preferences[EMAIL_KEY] ?: "",
                    preferences[IS_LOGIN_KEY] ?: false,
                    preferences[MESSAGE_KEY] ?: ""
                )
            } else {
                null
            }
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val NIP_KEY = intPreferencesKey("nip")
        private val MESSAGE_KEY = stringPreferencesKey("message")
        private val FULLNAME_KEY = stringPreferencesKey("fullname")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val POSITION_KEY = stringPreferencesKey("position")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}