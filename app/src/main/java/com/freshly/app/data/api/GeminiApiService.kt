package com.freshly.app.data.api

import android.content.Context
import android.util.Log
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.model.Recipe
import com.freshly.app.utils.GeminiApiKeyManager
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
 * Uses gemini-2.5-flash model with automatic API key rotation
 */
class GeminiApiService(private val context: Context) {
    
    companion object {
        private const val TAG = "GeminiApiService"
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val REQUEST_TIMEOUT_MS = 30000L
    }
    
    private val rateLimiter = GeminiRateLimiter()
    
    init {
        Log.d(TAG, "GeminiApiService initialized with model: $MODEL_NAME")
        Log.d(TAG, "Available API keys: ${GeminiApiKeyManager.getKeyCount()}")
    }
    
    // Create models with current API key
    private fun createRecipeModel() = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = GeminiApiKeyManager.getCurrentApiKey(),
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 20
            topP = 0.9f
            maxOutputTokens = 2048
        }
    )
    
    private fun createChatModel() = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = GeminiApiKeyManager.getCurrentApiKey(),
        generationConfig = generationConfig {
            temperature = 0.9f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )
    
    private fun createInsightsModel() = GenerativeModel(
        modelName = MODEL_NAME,
        apiKey = GeminiApiKeyManager.getCurrentApiKey(),
        generationConfig = generationConfig {
            temperature = 0.8f
            topK = 30
            topP = 0.92f
            maxOutputTokens = 512
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
            
            // Call API with automatic key rotation on failure
            val response = try {
                createRecipeModel().generateContent(prompt)
            } catch (e: Exception) {
                // Check if rate limit error and try rotating key
                if (GeminiApiKeyManager.isRateLimitError(e) && GeminiApiKeyManager.getKeyCount() > 1) {
                    Log.w(TAG, "Rate limit hit, rotating to next API key...")
                    GeminiApiKeyManager.rotateToNextKey(context)
                    // Retry with new key
                    createRecipeModel().generateContent(prompt)
                } else {
                    throw e
                }
            }
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
        createChatModel().generateContentStream(prompt)
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
                val (dailyRemaining, minuteRemaining) = rateLimiter.getRemainingRequests()
                val message = if (minuteRemaining == 0) {
                    "Please wait a minute before trying again."
                } else {
                    "Daily limit reached. Try again tomorrow."
                }
                Log.w(TAG, "Rate limit reached for insights generation")
                return Result.failure(RateLimitException(message))
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
            
            Log.d(TAG, "Generating insights for $userName with ${pantryItems.size} items...")
            
            rateLimiter.recordRequest()
            
            // Call API using chat model (1024 tokens)
            try {
                val response = createChatModel().generateContent(prompt)
                val responseText = response.text ?: ""
                
                Log.d(TAG, "Received insights response (${responseText.length} chars)")
                Log.d(TAG, "Insights response preview: ${responseText.take(200)}...")
                
                // Parse response
                val insights = GeminiResponseParser.parseInsights(responseText)
                
                if (insights.isEmpty()) {
                    Log.e(TAG, "No insights parsed from response. Full response: $responseText")
                    return Result.failure(Exception("Failed to generate insights. The AI returned an unexpected format. Please try again."))
                }
                
                return Result.success(insights)
            } catch (e: Exception) {
                // Check if it's a rate limit error and rotate key
                if (GeminiApiKeyManager.isRateLimitError(e)) {
                    Log.w(TAG, "Rate limit hit, rotating API key and retrying...")
                    GeminiApiKeyManager.rotateToNextKey(context)
                    
                    // Retry with new key
                    val response = createChatModel().generateContent(prompt)
                    val responseText = response.text ?: ""
                    
                    // Parse response
                    val insights = GeminiResponseParser.parseInsights(responseText)
                    
                    if (insights.isEmpty()) {
                        Log.e(TAG, "No insights parsed from response after retry. Full response: $responseText")
                        return Result.failure(Exception("Failed to generate insights. The AI returned an unexpected format. Please try again."))
                    }
                    
                    return Result.success(insights)
                } else {
                    throw e
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Insights generation failed: ${e.message}", e)
            
            // Handle specific errors
            val errorMessage = when {
                e is RateLimitException -> e.message ?: "Rate limit exceeded"
                e.message?.contains("429", ignoreCase = true) == true -> 
                    "Too many requests. Please wait a minute."
                e.message?.contains("quota", ignoreCase = true) == true -> 
                    "Daily quota reached. Try again tomorrow."
                e.message?.contains("API key", ignoreCase = true) == true -> 
                    "Invalid API key. Please check your configuration."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Check your connection."
                else -> "Failed to generate insights: ${e.message}"
            }
            
            Result.failure(Exception(errorMessage))
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
    
    /**
     * Generate detailed recipe content
     */
    suspend fun generateDetailedRecipe(
        title: String,
        ingredients: List<String>
    ): Result<String> {
        return try {
            if (!rateLimiter.canMakeRequest()) {
                val (dailyRemaining, minuteRemaining) = rateLimiter.getRemainingRequests()
                val message = if (minuteRemaining == 0) {
                    "Please wait a minute before trying again."
                } else {
                    "Daily limit reached. Try again tomorrow."
                }
                return Result.failure(RateLimitException(message))
            }
            
            val prompt = GeminiPromptBuilder.buildDetailedRecipePrompt(title, ingredients)
            Log.d(TAG, "Generating detailed recipe for: $title")
            
            rateLimiter.recordRequest()
            
            try {
                val response = createRecipeModel().generateContent(prompt)
                val text = response.text ?: ""
                
                Log.d(TAG, "Detailed recipe generated successfully")
                Result.success(text)
            } catch (e: Exception) {
                // Check if it's a rate limit error and rotate key
                if (GeminiApiKeyManager.isRateLimitError(e)) {
                    Log.w(TAG, "Rate limit hit, rotating API key and retrying...")
                    GeminiApiKeyManager.rotateToNextKey(context)
                    
                    // Retry with new key
                    val response = createRecipeModel().generateContent(prompt)
                    val text = response.text ?: ""
                    
                    Log.d(TAG, "Detailed recipe generated successfully after key rotation")
                    return Result.success(text)
                } else {
                    throw e
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating detailed recipe", e)
            Result.failure(e)
        }
    }
}

/**
 * Custom exception for rate limiting
 */
class RateLimitException(message: String) : Exception(message)
