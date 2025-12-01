package com.freshly.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.LocalDate

@Parcelize
data class PantryItem(
    val id: String,
    val name: String,
    val category: Category,
    val quantity: Int,
    val unit: String,
    val addedDate: String, // LocalDate as String for Parcelize
    val expiryDate: String, // LocalDate as String for Parcelize
    val imageUrl: String? = null,
    val notes: String? = null
) : Parcelable {
    
    fun getDaysUntilExpiry(): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val expiry = LocalDate.parse(expiryDate)
        return (expiry.toEpochDays() - today.toEpochDays())
    }
    
    fun getExpiryStatus(): ExpiryStatus {
        val daysUntilExpiry = getDaysUntilExpiry()
        return when {
            daysUntilExpiry < 0 -> ExpiryStatus.EXPIRED
            daysUntilExpiry <= 3 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.FRESH
        }
    }
}

enum class Category {
    FRIDGE, FREEZER, PANTRY
}

enum class ExpiryStatus {
    FRESH, EXPIRING_SOON, EXPIRED
}
