package com.freshly.app.utils

import android.content.Context
import android.util.Log
import com.freshly.app.BuildConfig

/**
 * Smart API Key Manager for Gemini API
 * 
 * Handles multiple API keys with automatic rotation when rate limits are hit.
 * Stores the current working key in SharedPreferences for persistence.
 * 
 * Features:
 * - Automatic failover when API call fails with rate limit error
 * - Persistent storage of current key index
 * - Round-robin rotation through all available keys
 * - Automatic retry with next key on 429 errors
 */
object GeminiApiKeyManager {
    
    private const val TAG = "GeminiApiKeyManager"
    private const val PREFS_NAME = "gemini_api_prefs"
    private const val KEY_CURRENT_INDEX = "current_key_index"
    
    private val apiKeys = BuildConfig.GEMINI_API_KEYS
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    
    private var currentKeyIndex = 0
    
    /**
     * Initialize the key manager with stored preferences
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentKeyIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        
        // Validate index
        if (currentKeyIndex >= apiKeys.size) {
            currentKeyIndex = 0
        }
        
        Log.d(TAG, "Initialized with ${apiKeys.size} API keys, starting at index $currentKeyIndex")
    }
    
    /**
     * Get the current active API key
     */
    fun getCurrentApiKey(): String {
        if (apiKeys.isEmpty()) {
            Log.e(TAG, "No API keys configured!")
            return ""
        }
        
        val key = apiKeys[currentKeyIndex]
        Log.d(TAG, "Using API key #${currentKeyIndex + 1} (${key.take(10)}...)")
        return key
    }
    
    /**
     * Rotate to the next API key
     * Call this when the current key hits rate limit
     */
    fun rotateToNextKey(context: Context): String {
        if (apiKeys.isEmpty()) {
            Log.e(TAG, "No API keys configured!")
            return ""
        }
        
        // Move to next key
        val previousIndex = currentKeyIndex
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        
        // Save to preferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CURRENT_INDEX, currentKeyIndex)
            .apply()
        
        Log.w(TAG, "Rotated from key #${previousIndex + 1} to key #${currentKeyIndex + 1}")
        
        return getCurrentApiKey()
    }
    
    /**
     * Check if an exception is due to rate limiting
     */
    fun isRateLimitError(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("quota") ||
               message.contains("rate limit") ||
               message.contains("429") ||
               message.contains("resource_exhausted") ||
               message.contains("too many requests")
    }
    
    /**
     * Get total number of available API keys
     */
    fun getKeyCount(): Int = apiKeys.size
    
    /**
     * Get current key index (0-based)
     */
    fun getCurrentKeyIndex(): Int = currentKeyIndex
    
    /**
     * Reset to first key (useful for testing or manual reset)
     */
    fun resetToFirstKey(context: Context) {
        currentKeyIndex = 0
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CURRENT_INDEX, 0)
            .apply()
        
        Log.d(TAG, "Reset to first API key")
    }
}
