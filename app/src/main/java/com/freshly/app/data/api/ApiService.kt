package com.freshly.app.data.api

import com.freshly.app.data.model.Recipe
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RecipeRequest(
    val ingredients: List<String>,
    val preferences: Map<String, Any>? = null
)

data class RecipeResponse(
    val recipes: List<Recipe>,
    val totalResults: Int
)

data class InsightsResponse(
    val insights: List<AIInsight>,
    val recommendations: List<String>
)

data class AIInsight(
    val id: String,
    val title: String,
    val description: String,
    val priority: String
)

interface FreshlyApiService {
    
    @POST("api/generate-recipes")
    suspend fun generateRecipes(@Body request: RecipeRequest): RecipeResponse
    
    @GET("api/ai-insights")
    suspend fun getAIInsights(): InsightsResponse
}
