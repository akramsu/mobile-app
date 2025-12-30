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
        // App-specific settings only (not user data)
        val ONBOARDED_KEY = booleanPreferencesKey("onboarded")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }
    
    // Onboarding status
    val hasOnboarded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ONBOARDED_KEY] ?: false
        }
    
    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDED_KEY] = value
        }
    }
    
    // Notification preferences
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
