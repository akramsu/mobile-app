package com.freshly.app.data.repository

import android.util.Log
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.model.*
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    
    private val firestore = FirebaseManager.firestore
    private var userListener: ListenerRegistration? = null
    
    /**
     * Get user data as Flow with real-time updates
     */
    val user: Flow<User> = callbackFlow {
        val userId = FirebaseManager.userId
        
        if (userId.isEmpty()) {
            // If not authenticated, send default user
            trySend(getDefaultUser())
            close()
            return@callbackFlow
        }
        
        val userDoc = FirebaseManager.getUserDocument(userId)
        
        userListener = userDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("UserRepository", "Error listening to user", error)
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                val userData = snapshot.data
                if (userData != null) {
                    val user = User.fromMap(userData)
                    // Load achievements
                    loadAchievements(userId) { achievements ->
                        trySend(user.copy(achievements = achievements))
                    }
                } else {
                    trySend(getDefaultUser())
                }
            } else {
                // User doesn't exist, create default user
                trySend(getDefaultUser())
            }
        }
        
        awaitClose { userListener?.remove() }
    }
    
    /**
     * Get user once (no real-time updates)
     */
    suspend fun getUserOnce(): User {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return getDefaultUser()
        
        return try {
            val snapshot = FirebaseManager.getUserDocument(userId).get().await()
            if (snapshot.exists()) {
                val userData = snapshot.data
                if (userData != null) {
                    val user = User.fromMap(userData)
                    val achievements = getAchievements()
                    user.copy(achievements = achievements)
                } else {
                    getDefaultUser()
                }
            } else {
                createDefaultUser(userId)
                getDefaultUser()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user", e)
            getDefaultUser()
        }
    }
    
    /**
     * Update user in Firestore
     */
    suspend fun updateUser(user: User) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getUserDocument(userId).set(user.toMap()).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user", e)
        }
    }
    
    /**
     * Update user profile (name and avatar)
     */
    suspend fun updateUserProfile(name: String, avatarUrl: String) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            val updates = mapOf(
                "name" to name,
                "avatarUrl" to avatarUrl
            )
            FirebaseManager.getUserDocument(userId).update(updates).await()
            Log.d("UserRepository", "Profile updated successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile", e)
        }
    }
    
    /**
     * Update dietary restrictions
     */
    suspend fun updateDietaryRestrictions(restrictions: List<String>) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getUserDocument(userId)
                .update("dietaryRestrictions", restrictions)
                .await()
            Log.d("UserRepository", "Dietary restrictions updated successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating dietary restrictions", e)
        }
    }
    
    /**
     * Add XP to user
     */
    suspend fun addXP(amount: Int) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            val currentUser = getUserOnce()
            val newXP = currentUser.xp + amount
            val newLevel = (newXP / 1000) + 1
            
            val updates = mapOf(
                "xp" to newXP,
                "level" to newLevel
            )
            
            FirebaseManager.getUserDocument(userId).update(updates).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding XP", e)
        }
    }
    
    /**
     * Increment user streak
     */
    suspend fun incrementStreak() {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            val currentUser = getUserOnce()
            val newStreak = currentUser.streak + 1
            
            FirebaseManager.getUserDocument(userId)
                .update("streak", newStreak)
                .await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error incrementing streak", e)
        }
    }
    
    /**
     * Check and update daily streak
     * Returns true if streak was updated
     */
    suspend fun checkAndUpdateDailyStreak(): Boolean {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return false
        
        try {
            val prefs = FirebaseManager.firestore
                .collection("users")
                .document(userId)
                .collection("preferences")
                .document("streak")
                .get()
                .await()
            
            val lastCheckDate = prefs.getString("lastCheckDate")
            val today = java.time.LocalDate.now().toString()
            
            if (lastCheckDate != today) {
                // Check if streak should continue or reset
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                
                if (lastCheckDate == yesterday) {
                    // Continue streak
                    incrementStreak()
                } else if (lastCheckDate != null) {
                    // Reset streak (missed a day)
                    FirebaseManager.getUserDocument(userId)
                        .update("streak", 0)
                        .await()
                }
                
                // Update last check date
                FirebaseManager.firestore
                    .collection("users")
                    .document(userId)
                    .collection("preferences")
                    .document("streak")
                    .set(mapOf("lastCheckDate" to today))
                    .await()
                
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking daily streak", e)
            return false
        }
    }
    
    /**
     * Unlock achievement
     */
    suspend fun unlockAchievement(achievementId: String) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            val achievementDoc = FirebaseManager.getAchievementsCollection(userId)
                .document(achievementId)
            
            val updates = mapOf(
                "isUnlocked" to true,
                "unlockedDate" to kotlinx.datetime.Clock.System.now().toString()
            )
            
            achievementDoc.update(updates).await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error unlocking achievement", e)
        }
    }
    
    /**
     * Get achievements for user
     */
    private suspend fun getAchievements(): List<Achievement> {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return getSampleAchievements()
        
        return try {
            val snapshot = FirebaseManager.getAchievementsCollection(userId).get().await()
            if (snapshot.isEmpty) {
                // Initialize achievements
                val achievements = getSampleAchievements()
                achievements.forEach { achievement ->
                    FirebaseManager.getAchievementsCollection(userId)
                        .document(achievement.id)
                        .set(achievement.toMap())
                        .await()
                }
                achievements
            } else {
                snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { Achievement.fromMap(it) }
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting achievements", e)
            getSampleAchievements()
        }
    }
    
    /**
     * Load achievements with callback
     */
    private fun loadAchievements(userId: String, callback: (List<Achievement>) -> Unit) {
        FirebaseManager.getAchievementsCollection(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    callback(getSampleAchievements())
                } else {
                    val achievements = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { Achievement.fromMap(it) }
                    }
                    callback(achievements)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Error loading achievements", e)
                callback(getSampleAchievements())
            }
    }
    
    /**
     * Create default user in Firestore
     */
    private suspend fun createDefaultUser(userId: String) {
        try {
            val defaultUser = getDefaultUser()
            FirebaseManager.getUserDocument(userId).set(defaultUser.toMap()).await()
            
            // Initialize achievements
            val achievements = getSampleAchievements()
            achievements.forEach { achievement ->
                FirebaseManager.getAchievementsCollection(userId)
                    .document(achievement.id)
                    .set(achievement.toMap())
                    .await()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating default user", e)
        }
    }
    
    /**
     * Create new user document in Firestore after signup
     */
    suspend fun createNewUser(name: String, email: String) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            val newUser = User(
                name = name,
                email = email,
                xp = 0,
                level = 1,
                streak = 0,
                avatarUrl = null,
                achievements = emptyList(),
                dietaryRestrictions = emptyList()
            )
            
            FirebaseManager.getUserDocument(userId).set(newUser.toMap()).await()
            
            // Initialize achievements
            val achievements = getSampleAchievements()
            achievements.forEach { achievement ->
                FirebaseManager.getAchievementsCollection(userId)
                    .document(achievement.id)
                    .set(achievement.toMap())
                    .await()
            }
            
            Log.d("UserRepository", "New user created successfully: $name")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating new user", e)
        }
    }
    
    private fun getDefaultUser(): User {
        return User(
            name = "Fresh Foodie",
            email = null,
            xp = 0,
            level = 1,
            streak = 0,
            avatarUrl = null,
            achievements = getSampleAchievements(),
            dietaryRestrictions = emptyList()
        )
    }
    
    private fun getSampleAchievements(): List<Achievement> {
        return listOf(
            // TRACKING Achievements
            Achievement(
                id = "first_item",
                title = "First Step",
                description = "Add your first pantry item",
                iconName = "🎯",
                xpReward = 50,
                isUnlocked = false,
                unlockedDate = null,
                category = AchievementCategory.TRACKING,
                progress = 0,
                target = 1,
                rarity = com.freshly.app.data.model.AchievementRarity.COMMON,
                unlockCondition = "Add 1 item to your pantry"
            ),
            Achievement(
                id = "pantry_manager",
                title = "Pantry Manager",
                description = "Track 25 different items",
                iconName = "📦",
                xpReward = 150,
                isUnlocked = false,
                category = AchievementCategory.TRACKING,
                progress = 0,
                target = 25,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Add 25 items to your pantry"
            ),
            Achievement(
                id = "scanner_pro",
                title = "Photo Scanner Pro",
                description = "Add 50 items using photo scan",
                iconName = "📸",
                xpReward = 200,
                isUnlocked = false,
                category = AchievementCategory.TRACKING,
                progress = 0,
                target = 50,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Use camera to add 50 items"
            ),
            
            // STREAK Achievements
            Achievement(
                id = "week_streak",
                title = "Week Warrior",
                description = "Maintain a 7-day streak",
                iconName = "🔥",
                xpReward = 100,
                isUnlocked = false,
                category = AchievementCategory.STREAK,
                progress = 0,
                target = 7,
                rarity = com.freshly.app.data.model.AchievementRarity.COMMON,
                unlockCondition = "Check the app for 7 days in a row"
            ),
            Achievement(
                id = "month_streak",
                title = "Monthly Champion",
                description = "Maintain a 30-day streak",
                iconName = "💎",
                xpReward = 300,
                isUnlocked = false,
                category = AchievementCategory.STREAK,
                progress = 0,
                target = 30,
                rarity = com.freshly.app.data.model.AchievementRarity.EPIC,
                unlockCondition = "Check the app for 30 days in a row"
            ),
            Achievement(
                id = "year_streak",
                title = "Legendary Keeper",
                description = "Maintain a 365-day streak",
                iconName = "👑",
                xpReward = 1000,
                isUnlocked = false,
                category = AchievementCategory.STREAK,
                progress = 0,
                target = 365,
                rarity = com.freshly.app.data.model.AchievementRarity.LEGENDARY,
                unlockCondition = "Check the app for 1 year straight"
            ),
            
            // SAVING Achievements
            Achievement(
                id = "zero_waste",
                title = "Zero Waste Hero",
                description = "Go a week without wasting food",
                iconName = "♻️",
                xpReward = 200,
                isUnlocked = false,
                category = AchievementCategory.SAVING,
                progress = 0,
                target = 7,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Don't let any items expire for 7 days"
            ),
            Achievement(
                id = "money_saver",
                title = "Money Saver",
                description = "Prevent $100 worth of food waste",
                iconName = "💰",
                xpReward = 250,
                isUnlocked = false,
                category = AchievementCategory.SAVING,
                progress = 0,
                target = 100,
                rarity = com.freshly.app.data.model.AchievementRarity.EPIC,
                unlockCondition = "Save $100 by preventing food waste"
            ),
            Achievement(
                id = "eco_warrior",
                title = "Eco Warrior",
                description = "Use all items before expiry 50 times",
                iconName = "🌱",
                xpReward = 300,
                isUnlocked = false,
                category = AchievementCategory.SAVING,
                progress = 0,
                target = 50,
                rarity = com.freshly.app.data.model.AchievementRarity.EPIC,
                unlockCondition = "Use 50 items before they expire"
            ),
            
            // COOKING Achievements
            Achievement(
                id = "chef_beginner",
                title = "Home Chef",
                description = "Cook 10 recipes from the app",
                iconName = "👨‍🍳",
                xpReward = 150,
                isUnlocked = false,
                category = AchievementCategory.COOKING,
                progress = 0,
                target = 10,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Cook 10 recipes"
            ),
            Achievement(
                id = "master_chef",
                title = "Master Chef",
                description = "Cook 50 different recipes",
                iconName = "⭐",
                xpReward = 400,
                isUnlocked = false,
                category = AchievementCategory.COOKING,
                progress = 0,
                target = 50,
                rarity = com.freshly.app.data.model.AchievementRarity.EPIC,
                unlockCondition = "Cook 50 different recipes"
            ),
            Achievement(
                id = "recipe_explorer",
                title = "Recipe Explorer",
                description = "Try recipes from 5 different cuisines",
                iconName = "🌍",
                xpReward = 200,
                isUnlocked = false,
                category = AchievementCategory.COOKING,
                progress = 0,
                target = 5,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Cook recipes from 5 cuisines"
            ),
            
            // SPECIAL Achievements
            Achievement(
                id = "early_adopter",
                title = "Early Adopter",
                description = "Join during the first month",
                iconName = "🚀",
                xpReward = 500,
                isUnlocked = false,
                category = AchievementCategory.SPECIAL,
                progress = 0,
                target = 1,
                rarity = com.freshly.app.data.model.AchievementRarity.LEGENDARY,
                unlockCondition = "Be among the first users"
            ),
            Achievement(
                id = "notification_master",
                title = "Never Miss a Beat",
                description = "Act on 20 expiry notifications",
                iconName = "🔔",
                xpReward = 150,
                isUnlocked = false,
                category = AchievementCategory.SPECIAL,
                progress = 0,
                target = 20,
                rarity = com.freshly.app.data.model.AchievementRarity.RARE,
                unlockCondition = "Respond to 20 notifications"
            )
        )
    }
}
