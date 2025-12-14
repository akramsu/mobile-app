package com.freshly.app.utils

import android.util.Log

/**
 * Rate limiter for Gemini API Free Tier
 * Limits: 15 requests/minute, 1,500 requests/day
 */
class GeminiRateLimiter {
    
    companion object {
        private const val DAILY_LIMIT = 1500
        private const val MINUTE_LIMIT = 15
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
        private const val ONE_MINUTE_MS = 60 * 1000L
        private const val TAG = "GeminiRateLimiter"
    }
    
    private val dailyTimestamps = mutableListOf<Long>()
    private val minuteTimestamps = mutableListOf<Long>()
    
    /**
     * Check if we can make a new request without exceeding rate limits
     */
    fun canMakeRequest(): Boolean {
        val now = System.currentTimeMillis()
        
        // Clean old timestamps
        dailyTimestamps.removeAll { now - it > ONE_DAY_MS }
        minuteTimestamps.removeAll { now - it > ONE_MINUTE_MS }
        
        val canProceed = dailyTimestamps.size < DAILY_LIMIT && 
                        minuteTimestamps.size < MINUTE_LIMIT
        
        if (!canProceed) {
            Log.w(TAG, "Rate limit reached - Daily: ${dailyTimestamps.size}/$DAILY_LIMIT, " +
                      "Minute: ${minuteTimestamps.size}/$MINUTE_LIMIT")
        }
        
        return canProceed
    }
    
    /**
     * Record a successful API request
     */
    fun recordRequest() {
        val now = System.currentTimeMillis()
        dailyTimestamps.add(now)
        minuteTimestamps.add(now)
        
        Log.d(TAG, "Request recorded - Daily: ${dailyTimestamps.size}/$DAILY_LIMIT, " +
                  "Minute: ${minuteTimestamps.size}/$MINUTE_LIMIT")
    }
    
    /**
     * Get remaining requests for both limits
     * @return Pair<DailyRemaining, MinuteRemaining>
     */
    fun getRemainingRequests(): Pair<Int, Int> {
        val now = System.currentTimeMillis()
        
        // Clean old timestamps
        dailyTimestamps.removeAll { now - it > ONE_DAY_MS }
        minuteTimestamps.removeAll { now - it > ONE_MINUTE_MS }
        
        return Pair(
            DAILY_LIMIT - dailyTimestamps.size,
            MINUTE_LIMIT - minuteTimestamps.size
        )
    }
    
    /**
     * Reset all counters (for testing)
     */
    fun reset() {
        dailyTimestamps.clear()
        minuteTimestamps.clear()
        Log.d(TAG, "Rate limiter reset")
    }
    
    /**
     * Get percentage of daily quota used
     */
    fun getDailyUsagePercentage(): Int {
        val now = System.currentTimeMillis()
        dailyTimestamps.removeAll { now - it > ONE_DAY_MS }
        return ((dailyTimestamps.size.toFloat() / DAILY_LIMIT) * 100).toInt()
    }
}
