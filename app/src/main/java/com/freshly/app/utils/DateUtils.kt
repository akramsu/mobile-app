package com.freshly.app.utils

import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    fun getTodayDate(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    
    fun formatDate(date: LocalDate, pattern: String = "MMM dd, yyyy"): String {
        val javaDate = Date(date.toEpochDays() * 86400000L)
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(javaDate)
    }
    
    fun getGreeting(): String {
        val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    
    fun getDaysUntilExpiry(expiryDate: LocalDate): Int {
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
            else -> formatDate(date, "MMM dd")
        }
    }
    
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
