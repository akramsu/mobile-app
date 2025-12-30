package com.freshly.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.tasks.await


object FirebaseManager {
    

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
    
    val userId: String
        get() = auth.currentUser?.uid ?: ""
    

    val isAuthenticated: Boolean
        get() = auth.currentUser != null
    
    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign up with email and password
     */
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                
                
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to create account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(displayName: String? = null, photoUrl: String? = null): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
            }.build()
            
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Collection references
     */
    object Collections {
        const val USERS = "users"
        const val PANTRY_ITEMS = "pantryItems"
        const val RECIPES = "recipes"
        const val ACHIEVEMENTS = "achievements"
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
