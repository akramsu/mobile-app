package com.freshly.app.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "freshly_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val ONBOARDED_KEY = booleanPreferencesKey("onboarded")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val USER_XP_KEY = intPreferencesKey("user_xp")
        val USER_LEVEL_KEY = intPreferencesKey("user_level")
        val USER_STREAK_KEY = intPreferencesKey("user_streak")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }
    
    val hasOnboarded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDED_KEY] ?: false
        }
    
    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDED_KEY] = value
        }
    }
    
    suspend fun saveUserData(name: String, xp: Int, level: Int, streak: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_XP_KEY] = xp
            preferences[USER_LEVEL_KEY] = level
            preferences[USER_STREAK_KEY] = streak
        }
    }
    
    val userData: Flow<Map<String, Any>> = context.dataStore.data
        .map { preferences ->
            mapOf(
                "name" to (preferences[USER_NAME_KEY] ?: "User"),
                "xp" to (preferences[USER_XP_KEY] ?: 0),
                "level" to (preferences[USER_LEVEL_KEY] ?: 1),
                "streak" to (preferences[USER_STREAK_KEY] ?: 0)
            )
        }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
        }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    val darkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }
}
