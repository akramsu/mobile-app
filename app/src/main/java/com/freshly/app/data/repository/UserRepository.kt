package com.freshly.app.data.repository

import com.freshly.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class UserRepository {
    
    private val _user = MutableStateFlow(getSampleUser())
    val user: Flow<User> = _user
    
    suspend fun updateUser(user: User) {
        _user.value = user
    }
    
    suspend fun addXP(amount: Int) {
        val currentUser = _user.value
        val newXP = currentUser.xp + amount
        val newLevel = (newXP / 1000) + 1
        _user.value = currentUser.copy(xp = newXP, level = newLevel)
    }
    
    suspend fun incrementStreak() {
        _user.value = _user.value.copy(streak = _user.value.streak + 1)
    }
    
    suspend fun unlockAchievement(achievementId: String) {
        val currentUser = _user.value
        val updatedAchievements = currentUser.achievements.map { achievement ->
            if (achievement.id == achievementId) {
                achievement.copy(isUnlocked = true, unlockedDate = kotlinx.datetime.Clock.System.now().toString())
            } else {
                achievement
            }
        }
        _user.value = currentUser.copy(achievements = updatedAchievements)
    }
    
    private fun getSampleUser(): User {
        return User(
            name = "Akram",
            email = "akram@example.com",
            xp = 750,
            level = 3,
            streak = 7,
            avatarUrl = null,
            achievements = getSampleAchievements(),
            dietaryRestrictions = emptyList(),
            region = "US"
        )
    }
    
    private fun getSampleAchievements(): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_item",
                title = "First Step",
                description = "Add your first pantry item",
                iconName = "🎯",
                xpReward = 50,
                isUnlocked = true,
                unlockedDate = "2025-11-24",
                category = AchievementCategory.TRACKING
            ),
            Achievement(
                id = "week_streak",
                title = "Week Warrior",
                description = "Maintain a 7-day streak",
                iconName = "🔥",
                xpReward = 100,
                isUnlocked = true,
                unlockedDate = "2025-11-28",
                category = AchievementCategory.STREAK
            ),
            Achievement(
                id = "zero_waste",
                title = "Zero Waste Hero",
                description = "Go a week without wasting food",
                iconName = "♻️",
                xpReward = 200,
                isUnlocked = false,
                category = AchievementCategory.SAVING
            ),
            Achievement(
                id = "chef_beginner",
                title = "Home Chef",
                description = "Cook 10 recipes from the app",
                iconName = "👨‍🍳",
                xpReward = 150,
                isUnlocked = false,
                category = AchievementCategory.COOKING
            ),
            Achievement(
                id = "month_streak",
                title = "Monthly Master",
                description = "Maintain a 30-day streak",
                iconName = "⭐",
                xpReward = 500,
                isUnlocked = false,
                category = AchievementCategory.STREAK
            ),
            Achievement(
                id = "scanner_pro",
                title = "Scanner Pro",
                description = "Add 50 items using photo scan",
                iconName = "📸",
                xpReward = 250,
                isUnlocked = false,
                category = AchievementCategory.TRACKING
            )
        )
    }
}
