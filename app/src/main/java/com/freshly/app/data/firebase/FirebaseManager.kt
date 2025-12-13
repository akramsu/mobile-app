package com.freshly.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/**
 * Singleton manager for Firebase services
 * Handles Firestore and Authentication initialization
 */
object FirebaseManager {
    
    // Firestore instance
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .build()
        }
    }
    
    // Authentication instance
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    
    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser
    
    // User ID (generates anonymous user if not logged in)
    val userId: String
        get() = auth.currentUser?.uid ?: ""
    
    // Check if user is authenticated
    val isAuthenticated: Boolean
        get() = auth.currentUser != null
    
    /**
     * Sign in anonymously for first-time users
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to sign in anonymously"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
    
    /**
     * Collection references
     */
    object Collections {
        const val USERS = "users"
        const val PANTRY_ITEMS = "pantryItems"
        const val RECIPES = "recipes"
        const val ACHIEVEMENTS = "achievements"
        const val ANALYTICS = "analytics"
    }
    
    /**
     * Get user document reference
     */
    fun getUserDocument(userId: String = this.userId) =
        firestore.collection(Collections.USERS).document(userId)
    
    /**
     * Get pantry items collection for a user
     */
    fun getPantryItemsCollection(userId: String = this.userId) =
        getUserDocument(userId).collection(Collections.PANTRY_ITEMS)
    
    /**
     * Get recipes collection for a user
     */
    fun getRecipesCollection(userId: String = this.userId) =
        getUserDocument(userId).collection(Collections.RECIPES)
    
    /**
     * Get achievements collection for a user
     */
    fun getAchievementsCollection(userId: String = this.userId) =
        getUserDocument(userId).collection(Collections.ACHIEVEMENTS)
}
