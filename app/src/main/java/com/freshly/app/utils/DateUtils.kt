package com.freshly.app.utils

import kotlinx.datetime.*

object DateUtils {
    
    private fun getTodayDate(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    
    private fun getDaysUntilExpiry(expiryDate: LocalDate): Int {
        val today = getTodayDate()
        return (expiryDate.toEpochDays() - today.toEpochDays())
    }
    
    fun getRelativeTimeString(date: LocalDate): String {
        val days = getDaysUntilExpiry(date)
        return when {
            days < 0 -> "Expired ${-days} day${if (-days != 1) "s" else ""} ago"
            days == 0 -> "Expires today"
            days == 1 -> "Expires tomorrow"
            days <= 7 -> "Expires in $days days"
            else -> "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
        }
    }
}
