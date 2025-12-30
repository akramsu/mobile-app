package com.freshly.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String,
    val email: String? = null,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val avatarUrl: String? = null,
    val achievements: List<Achievement> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList()
) : Parcelable {
    
    fun getXpForNextLevel(): Int {
        return level * 1000
    }
    
    fun getXpProgress(): Float {
        return (xp % 1000) / 1000f
    }
    
    /**
     * Convert User to Firestore-compatible Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "xp" to xp,
            "level" to level,
            "streak" to streak,
            "avatarUrl" to avatarUrl,
            "dietaryRestrictions" to dietaryRestrictions
        )
    }
    
    companion object {
        /**
         * Create User from Firestore document
         */
        fun fromMap(map: Map<String, Any?>): User {
            return User(
                name = map["name"] as? String ?: "User",
                email = map["email"] as? String,
                xp = (map["xp"] as? Long)?.toInt() ?: 0,
                level = (map["level"] as? Long)?.toInt() ?: 1,
                streak = (map["streak"] as? Long)?.toInt() ?: 0,
                avatarUrl = map["avatarUrl"] as? String,
                achievements = emptyList(), // Load separately from subcollection
                dietaryRestrictions = (map["dietaryRestrictions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
}

@Parcelize
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: String? = null,
    val category: AchievementCategory,
    val progress: Int = 0,
    val target: Int = 1,
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val unlockCondition: String = ""
) : Parcelable {
    
    /**
     * Convert Achievement to Firestore-compatible Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "iconName" to iconName,
            "xpReward" to xpReward,
            "isUnlocked" to isUnlocked,
            "unlockedDate" to unlockedDate,
            "category" to category.name,
            "progress" to progress,
            "target" to target,
            "rarity" to rarity.name,
            "unlockCondition" to unlockCondition
        )
    }
    
    companion object {
        /**
         * Create Achievement from Firestore document
         */
        fun fromMap(map: Map<String, Any?>): Achievement {
            return Achievement(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                iconName = map["iconName"] as? String ?: "🎯",
                xpReward = (map["xpReward"] as? Long)?.toInt() ?: 0,
                isUnlocked = map["isUnlocked"] as? Boolean ?: false,
                unlockedDate = map["unlockedDate"] as? String,
                category = try {
                    AchievementCategory.valueOf(map["category"] as? String ?: "SPECIAL")
                } catch (e: Exception) {
                    AchievementCategory.SPECIAL
                },
                progress = (map["progress"] as? Long)?.toInt() ?: 0,
                target = (map["target"] as? Long)?.toInt() ?: 1,
                rarity = try {
                    AchievementRarity.valueOf(map["rarity"] as? String ?: "COMMON")
                } catch (e: Exception) {
                    AchievementRarity.COMMON
                },
                unlockCondition = map["unlockCondition"] as? String ?: ""
            )
        }
    }
}

enum class AchievementCategory {
    TRACKING, SAVING, COOKING, STREAK, SPECIAL
}

enum class AchievementRarity {
    COMMON,    // 50-100 XP
    RARE,      // 150-250 XP
    EPIC,      // 300-500 XP
    LEGENDARY  // 500+ XP
}
