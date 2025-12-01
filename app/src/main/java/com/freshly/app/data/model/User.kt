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
    val dietaryRestrictions: List<String> = emptyList(),
    val region: String = "US"
) : Parcelable {
    
    fun getXpForNextLevel(): Int {
        return level * 1000
    }
    
    fun getXpProgress(): Float {
        return (xp % 1000) / 1000f
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
    val category: AchievementCategory
) : Parcelable

enum class AchievementCategory {
    TRACKING, SAVING, COOKING, STREAK, SPECIAL
}
