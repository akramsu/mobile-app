package com.freshly.app.data.api

import android.util.Log
import com.freshly.app.BuildConfig
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.model.Recipe
import com.freshly.app.utils.GeminiRateLimiter
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Service for interacting with Gemini AI API
 * Uses gemini-1.5-flash model for FREE tier (1,500 requests/day)
 */
class GeminiApiService {
    
    companion object {
        private const val TAG = "GeminiApiService"
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val REQUEST_TIMEOUT_MS = 30000L
    }
    
    private val rateLimiter = GeminiRateLimiter()
    
    // Recipe generation model - balanced for creative but accurate results
    private val recipeModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f      // Less creative = faster
            topK = 20               // Reduced for speed
            topP = 0.9f
            maxOutputTokens = 2048  // Reduced for 2 shorter recipes
        }
    )
    
    // Chat model - more conversational
    private val chatModel = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f      // More natural conversations
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024  // Shorter chat responses
        }
    )
    
    /**
     * Generate recipes based on pantry items
     * @return Result with list of recipes or error
     */
    suspend fun generateRecipes(
        ingredients: List<String>,
        dietaryPreferences: List<String> = emptyList(),
        skillLevel: String = "Medium"
    ): Result<List<Recipe>> {
        return try {
            // Check rate limits
            if (!rateLimiter.canMakeRequest()) {
                val (dailyRemaining, minuteRemaining) = rateLimiter.getRemainingRequests()
                val message = if (minuteRemaining == 0) {
                    "Please wait a minute before trying again."
                } else {
                    "Daily limit reached ($dailyRemaining remaining). Resets in 24 hours."
                }
                Log.w(TAG, "Rate limit exceeded: $message")
                return Result.failure(RateLimitException(message))
            }
            
            // Build prompt
            val prompt = GeminiPromptBuilder.buildRecipePrompt(
                ingredients,
                dietaryPreferences,
                skillLevel
            )
            
            Log.d(TAG, "Generating recipes for ${ingredients.size} ingredients...")
            
            // Record request before making it
            rateLimiter.recordRequest()
            
            // Call API
            val response = recipeModel.generateContent(prompt)
            val responseText = response.text ?: ""
            
            Log.d(TAG, "Received response: ${responseText.take(200)}...")
            
            // Parse response
            val recipes = GeminiResponseParser.parseRecipes(responseText)
            
            if (recipes.isEmpty()) {
                Log.w(TAG, "No recipes parsed from response")
                return Result.failure(Exception("Failed to generate recipes. Please try again."))
            }
            
            Log.d(TAG, "Successfully generated ${recipes.size} recipes")
            Result.success(recipes)
            
        } catch (e: Exception) {
            Log.e(TAG, "Recipe generation failed", e)
            
            // Handle specific errors
            val errorMessage = when {
                e is RateLimitException -> e.message
                e.message?.contains("429", ignoreCase = true) == true -> 
                    "Rate limit exceeded. Please wait a minute."
                e.message?.contains("quota", ignoreCase = true) == true -> 
                    "Daily quota reached (1,500 requests). Resets in 24 hours."
                e.message?.contains("API key", ignoreCase = true) == true -> 
                    "Invalid API key. Get a new one at aistudio.google.com/app/apikey"
                e.message?.contains("not found", ignoreCase = true) == true ||
                e.message?.contains("not supported", ignoreCase = true) == true -> 
                    "Model not available. Please regenerate your API key at aistudio.google.com/app/apikey and ensure Gemini API is enabled."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Check your internet connection."
                else -> "Failed to generate recipes: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Chat with streaming responses
     * @return Flow of text chunks as they arrive
     */
    fun chatStream(
        userMessage: String,
        pantryItems: List<PantryItem>
    ): Flow<String> = flow {
        // Check rate limits
        if (!rateLimiter.canMakeRequest()) {
            val (_, minuteRemaining) = rateLimiter.getRemainingRequests()
            if (minuteRemaining == 0) {
                emit("⚠️ Please wait a minute before sending more messages.")
            } else {
                emit("⚠️ Daily message limit reached. Resets in 24 hours.")
            }
            return@flow
        }
        
        // Build context
        val pantryContext = GeminiPromptBuilder.buildPantryContext(pantryItems)
        val prompt = GeminiPromptBuilder.buildChatPrompt(userMessage, pantryContext)
        
        Log.d(TAG, "Starting chat stream for message: ${userMessage.take(50)}...")
        
        rateLimiter.recordRequest()
        
        // Stream response
        chatModel.generateContentStream(prompt)
            .collect { chunk ->
                chunk.text?.let { text ->
                    emit(text)
                }
            }
            
        Log.d(TAG, "Chat stream completed")
        
    }.catch { e ->
        Log.e(TAG, "Chat stream failed", e)
        
        val errorMessage = when {
            e.message?.contains("429", ignoreCase = true) == true -> 
                "⚠️ Too many requests. Please wait a minute."
            e.message?.contains("quota", ignoreCase = true) == true -> 
                "⚠️ Daily quota reached. Try again tomorrow."
            e.message?.contains("network", ignoreCase = true) == true -> 
                "⚠️ Network error. Check your connection."
            else -> "Sorry, I encountered an error. Please try again."
        }
        
        emit(errorMessage)
    }
    
    /**
     * Generate AI insights about user's pantry
     * @return Result with insights map or error
     */
    suspend fun generateInsights(
        pantryItems: List<PantryItem>,
        userName: String,
        dietaryRestrictions: List<String>
    ): Result<Map<String, String>> {
        return try {
            // Check rate limits
            if (!rateLimiter.canMakeRequest()) {
                val (dailyRemaining, _) = rateLimiter.getRemainingRequests()
                return Result.failure(Exception("Daily limit reached ($dailyRemaining remaining)"))
            }
            
            // Build pantry data
            val expiringSoon = pantryItems.count { it.getDaysUntilExpiry() <= 3 }
            val expiringItemsList = pantryItems
                .filter { it.getDaysUntilExpiry() in 0..3 }
                .map { "${it.name} (${it.getDaysUntilExpiry()} days)" }
            
            val pantryData = mapOf(
                "totalItems" to pantryItems.size,
                "expiringSoon" to expiringSoon,
                "expiredThisMonth" to 0, // TODO: Track from analytics
                "topCategories" to pantryItems
                    .groupBy { it.category }
                    .entries
                    .sortedByDescending { it.value.size }
                    .take(3)
                    .map { it.key },
                "expiringItemsList" to expiringItemsList
            )
            
            val userProfile = mapOf(
                "dietaryRestrictions" to dietaryRestrictions,
                "userName" to userName
            )
            
            // Build prompt
            val prompt = GeminiPromptBuilder.buildInsightsPrompt(pantryData, userProfile)
            
            Log.d(TAG, "Generating insights...")
            
            rateLimiter.recordRequest()
            
            // Call API
            val response = chatModel.generateContent(prompt)
            val responseText = response.text ?: ""
            
            Log.d(TAG, "Received insights: ${responseText.take(100)}...")
            
            // Parse response
            val insights = GeminiResponseParser.parseInsights(responseText)
            
            if (insights.isEmpty()) {
                return Result.failure(Exception("Failed to generate insights"))
            }
            
            Log.d(TAG, "Successfully generated ${insights.size} insights")
            Result.success(insights)
            
        } catch (e: Exception) {
            Log.e(TAG, "Insights generation failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get remaining API quota
     */
    fun getRemainingQuota(): Pair<Int, Int> {
        return rateLimiter.getRemainingRequests()
    }
    
    /**
     * Get daily usage percentage
     */
    fun getDailyUsagePercentage(): Int {
        return rateLimiter.getDailyUsagePercentage()
    }
}

/**
 * Custom exception for rate limiting
 */
class RateLimitException(message: String) : Exception(message)
