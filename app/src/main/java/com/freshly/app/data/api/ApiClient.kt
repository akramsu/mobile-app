package com.freshly.app.data.api

import com.freshly.app.data.model.Ingredient
import com.freshly.app.data.model.Recipe
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private const val BASE_URL = "https://api.freshly.app/" // Replace with actual API URL
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: FreshlyApiService = retrofit.create(FreshlyApiService::class.java)
}

// Mock API Implementation for development
object MockApiService {
    
    fun generateRecipes(ingredients: List<String>): RecipeResponse {
        val mockRecipes = listOf(
            Recipe(
                id = "1",
                title = "Creamy Pasta Carbonara",
                description = "Classic Italian pasta with eggs, cheese, and pancetta",
                imageUrl = "",
                cookTime = 20,
                servings = 4,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Pasta", "400g", ingredients.any { it.contains("pasta", true) }),
                    Ingredient("Eggs", "4", ingredients.any { it.contains("egg", true) }),
                    Ingredient("Parmesan cheese", "100g", ingredients.any { it.contains("cheese", true) }),
                    Ingredient("Pancetta", "150g", false),
                    Ingredient("Black pepper", "to taste", false)
                ),
                steps = listOf(
                    "Cook pasta according to package directions",
                    "Fry pancetta until crispy",
                    "Beat eggs with parmesan",
                    "Drain pasta and mix with egg mixture",
                    "Add pancetta and season with pepper"
                ),
                matchedIngredients = ingredients,
                tags = listOf("Italian", "Quick", "Dinner")
            ),
            Recipe(
                id = "2",
                title = "Chicken Stir Fry",
                description = "Quick and healthy Asian-inspired dish",
                imageUrl = "",
                cookTime = 15,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Chicken breast", "300g", ingredients.any { it.contains("chicken", true) }),
                    Ingredient("Mixed vegetables", "200g", ingredients.any { it.contains("veg", true) }),
                    Ingredient("Soy sauce", "3 tbsp", false),
                    Ingredient("Garlic", "2 cloves", false),
                    Ingredient("Ginger", "1 inch", false)
                ),
                steps = listOf(
                    "Cut chicken into bite-sized pieces",
                    "Heat oil in wok or large pan",
                    "Cook chicken until golden",
                    "Add vegetables and stir fry",
                    "Season with soy sauce, garlic, and ginger"
                ),
                matchedIngredients = ingredients,
                tags = listOf("Asian", "Healthy", "Quick")
            )
        )
        
        return RecipeResponse(
            recipes = mockRecipes,
            totalResults = mockRecipes.size
        )
    }
    
    fun getAIInsights(): InsightsResponse {
        return InsightsResponse(
            insights = listOf(
                AIInsight(
                    id = "1",
                    title = "Great job!",
                    description = "You've reduced waste by 85% this month. Keep buying what you need!",
                    priority = "HIGH"
                ),
                AIInsight(
                    id = "2",
                    title = "Expiring Soon",
                    description = "3 items are expiring in the next 2 days. Check your pantry!",
                    priority = "MEDIUM"
                )
            ),
            recommendations = listOf(
                "Try meal prepping on Sundays",
                "Freeze extra portions for later",
                "Use expiring items first"
            )
        )
    }
}
