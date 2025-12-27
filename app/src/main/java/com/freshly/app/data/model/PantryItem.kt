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
    val imageUrl: String? = null
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
    
    /**
     * Convert PantryItem to Firestore-compatible Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category.name,
            "quantity" to quantity,
            "unit" to unit,
            "addedDate" to addedDate,
            "expiryDate" to expiryDate,
            "imageUrl" to imageUrl
        )
    }
    
    companion object {
        /**
         * Create PantryItem from Firestore document
         */
        fun fromMap(map: Map<String, Any?>): PantryItem {
            return PantryItem(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                category = try {
                    Category.valueOf(map["category"] as? String ?: "PANTRY")
                } catch (e: Exception) {
                    Category.PANTRY
                },
                quantity = (map["quantity"] as? Long)?.toInt() ?: 1,
                unit = map["unit"] as? String ?: "unit",
                addedDate = map["addedDate"] as? String ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                expiryDate = map["expiryDate"] as? String ?: Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                imageUrl = map["imageUrl"] as? String
            )
        }
    }
}

enum class Category {
    FRIDGE, FREEZER, PANTRY
}

enum class ExpiryStatus {
    FRESH, EXPIRING_SOON, EXPIRED
}
