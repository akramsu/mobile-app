package com.freshly.app.data.api

import android.util.Log
import com.freshly.app.data.model.Ingredient
import com.freshly.app.data.model.Recipe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * Parser for Gemini API responses
 */
object GeminiResponseParser {
    
    private const val TAG = "GeminiResponseParser"
    
    /**
     * Parse recipes from Gemini JSON response
     * Handles markdown wrapping and malformed JSON gracefully
     */
    fun parseRecipes(jsonResponse: String): List<Recipe> {
        if (jsonResponse.isBlank()) {
            Log.e(TAG, "Empty response received")
            return emptyList()
        }
        
        return try {
            // Extract JSON array from response (handle markdown wrapping)
            val cleanJson = jsonResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()
                .let {
                    // Find first [ and last ]
                    val start = it.indexOf('[')
                    val end = it.lastIndexOf(']')
                    if (start >= 0 && end > start) {
                        it.substring(start, end + 1)
                    } else {
                        it
                    }
                }
            
            Log.d(TAG, "Parsing JSON: ${cleanJson.take(200)}...")
            
            val jsonArray = JSONArray(cleanJson)
            val recipes = mutableListOf<Recipe>()
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val recipeJson = jsonArray.getJSONObject(i)
                    val recipe = parseRecipeObject(recipeJson)
                    recipes.add(recipe)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing recipe at index $i", e)
                    // Continue with other recipes
                }
            }
            
            Log.d(TAG, "Successfully parsed ${recipes.size} recipes")
            recipes
            
        } catch (e: JSONException) {
            Log.e(TAG, "Failed to parse recipes JSON", e)
            Log.e(TAG, "Response was: $jsonResponse")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing recipes", e)
            emptyList()
        }
    }
    
    /**
     * Parse a single recipe JSON object
     */
    private fun parseRecipeObject(recipeJson: JSONObject): Recipe {
        // Parse ingredients
        val ingredientsArray = recipeJson.getJSONArray("ingredients")
        val ingredients = mutableListOf<Ingredient>()
        val matchedIngredients = mutableListOf<String>()
        
        for (j in 0 until ingredientsArray.length()) {
            val ingJson = ingredientsArray.getJSONObject(j)
            val name = ingJson.getString("name")
            val amount = ingJson.getString("amount")
            val isMatched = ingJson.optBoolean("isMatched", false)
            
            ingredients.add(Ingredient(name, amount, isMatched))
            if (isMatched) matchedIngredients.add(name)
        }
        
        // Parse steps
        val stepsArray = recipeJson.getJSONArray("steps")
        val steps = mutableListOf<String>()
        for (j in 0 until stepsArray.length()) {
            steps.add(stepsArray.getString(j))
        }
        
        // Parse tags
        val tagsArray = recipeJson.optJSONArray("tags")
        val tags = mutableListOf<String>()
        if (tagsArray != null) {
            for (j in 0 until tagsArray.length()) {
                tags.add(tagsArray.getString(j))
            }
        }
        
        // Create Recipe object
        return Recipe(
            id = UUID.randomUUID().toString(),
            title = recipeJson.getString("title"),
            description = recipeJson.getString("description"),
            imageUrl = "", // Will be populated later if needed
            cookTime = recipeJson.optInt("cookTime", 30),
            servings = recipeJson.optInt("servings", 4),
            difficulty = recipeJson.optString("difficulty", "Medium"),
            ingredients = ingredients,
            steps = steps,
            matchedIngredients = matchedIngredients,
            tags = tags,
            youtubeVideoLink = recipeJson.optString("youtubeVideoLink").takeIf { it.isNotBlank() }
        )
    }
    
    /**
     * Parse AI insights from response
     * Returns map with keys: achievement, tip, savings, environmental, urgent
     */
    fun parseInsights(response: String): Map<String, String> {
        val insights = mutableMapOf<String, String>()
        
        if (response.isBlank()) {
            Log.w(TAG, "Empty insights response")
            return insights
        }
        
        try {
            Log.d(TAG, "Parsing insights from response (${response.length} chars)")
            
            // Extract JSON from response (handle markdown wrapping)
            val cleanJson = response
                .replace("```json", "")
                .replace("```", "")
                .trim()
                .let {
                    // Find first { and last }
                    val start = it.indexOf('{')
                    val end = it.lastIndexOf('}')
                    if (start >= 0 && end > start) {
                        it.substring(start, end + 1)
                    } else {
                        it
                    }
                }
            
            Log.d(TAG, "Cleaned JSON: ${cleanJson.take(300)}...")
            
            // Parse as JSON object
            val jsonObject = JSONObject(cleanJson)
            
            // Extract each insight field
            if (jsonObject.has("achievement")) {
                insights["achievement"] = jsonObject.getString("achievement")
                Log.d(TAG, "Found achievement insight")
            }
            if (jsonObject.has("tip")) {
                insights["tip"] = jsonObject.getString("tip")
                Log.d(TAG, "Found tip insight")
            }
            if (jsonObject.has("savings")) {
                insights["savings"] = jsonObject.getString("savings")
                Log.d(TAG, "Found savings insight")
            }
            if (jsonObject.has("environmental")) {
                insights["environmental"] = jsonObject.getString("environmental")
                Log.d(TAG, "Found environmental insight")
            }
            if (jsonObject.has("urgent")) {
                insights["urgent"] = jsonObject.getString("urgent")
                Log.d(TAG, "Found urgent insight")
            }
            
            Log.d(TAG, "Parsed ${insights.size} insights: ${insights.keys.joinToString()}")
            
            if (insights.isEmpty()) {
                Log.w(TAG, "No insights found in JSON. Full response: $response")
            }
            
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing insights as JSON, trying emoji-based fallback", e)
            
            // Fallback to emoji-based parsing for backwards compatibility
            try {
                val lines = response.split("\n").filter { it.isNotBlank() }
                Log.d(TAG, "Trying emoji-based parsing with ${lines.size} lines")
                
                for ((index, line) in lines.withIndex()) {
                    val trimmedLine = line.trim()
                    
                    when {
                        trimmedLine.startsWith("🎉") -> {
                            insights["achievement"] = trimmedLine
                            Log.d(TAG, "Found achievement insight (emoji-based)")
                        }
                        trimmedLine.startsWith("💡") -> {
                            insights["tip"] = trimmedLine
                            Log.d(TAG, "Found tip insight (emoji-based)")
                        }
                        trimmedLine.startsWith("💰") -> {
                            insights["savings"] = trimmedLine
                            Log.d(TAG, "Found savings insight (emoji-based)")
                        }
                        trimmedLine.startsWith("🌱") || trimmedLine.startsWith("🌍") -> {
                            insights["environmental"] = trimmedLine
                            Log.d(TAG, "Found environmental insight (emoji-based)")
                        }
                        trimmedLine.startsWith("⚡") || trimmedLine.startsWith("✅") -> {
                            insights["urgent"] = trimmedLine
                            Log.d(TAG, "Found urgent insight (emoji-based)")
                        }
                    }
                }
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Fallback emoji-based parsing also failed", fallbackException)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error parsing insights", e)
        }
        
        return insights
    }
    
    /**
     * Validate if a string is valid JSON
     */
    fun isValidJson(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e1: JSONException) {
            try {
                JSONArray(jsonString)
                true
            } catch (e2: JSONException) {
                false
            }
        }
    }
    
    /**
     * Extract error message from API error response
     */
    fun extractErrorMessage(errorResponse: String): String {
        return try {
            val json = JSONObject(errorResponse)
            json.optString("error", errorResponse)
        } catch (e: JSONException) {
            // If not JSON, return the raw message
            when {
                errorResponse.contains("429", ignoreCase = true) -> 
                    "Rate limit exceeded. Please wait a minute."
                errorResponse.contains("quota", ignoreCase = true) -> 
                    "Daily quota reached. Resets in 24 hours."
                errorResponse.contains("network", ignoreCase = true) -> 
                    "Network error. Check your connection."
                errorResponse.contains("timeout", ignoreCase = true) -> 
                    "Request timed out. Please try again."
                else -> "An error occurred. Please try again."
            }
        }
    }
}
