package com.freshly.app.data.repository

import android.util.Log
import com.freshly.app.data.api.GeminiApiService
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.model.Ingredient
import com.freshly.app.data.model.Recipe
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*

class RecipeRepository private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: RecipeRepository? = null
        
        fun getInstance(): RecipeRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecipeRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val geminiService = GeminiApiService()
    private val recipeCache = mutableMapOf<String, Pair<List<Recipe>, Long>>()
    private val cacheExpiryMs = 24 * 60 * 60 * 1000L // 24 hours
    
    // Cache for AI-generated recipes by ID
    private val generatedRecipeCache = mutableMapOf<String, Recipe>()
    
    /**
     * Generate recipes using Gemini AI based on selected ingredients
     * Includes caching and retry logic
     */
    suspend fun generateRecipes(
        selectedIngredients: List<String>,
        dietaryPreferences: List<String> = emptyList(),
        skillLevel: String = "Medium"
    ): Flow<List<Recipe>> = flow {
        // Check cache first
        val cacheKey = buildCacheKey(selectedIngredients, dietaryPreferences, skillLevel)
        val cached = getCachedRecipes(cacheKey)
        if (cached != null) {
            Log.d("RecipeRepository", "Using cached recipes for: $cacheKey")
            emit(cached)
            return@flow
        }
        
        // Emit empty list to show loading state
        emit(emptyList())
        
        // Try to generate recipes with retry logic
        var attempts = 0
        var lastError: Exception? = null
        
        while (attempts < 3) {
            attempts++
            
            try {
                Log.d("RecipeRepository", "Generating recipes (attempt $attempts/3)...")
                
                val result = geminiService.generateRecipes(
                    selectedIngredients,
                    dietaryPreferences,
                    skillLevel
                )
                
                if (result.isSuccess) {
                    val recipes = result.getOrNull() ?: emptyList()
                    
                    // Cache the results
                    cacheRecipes(cacheKey, recipes)
                    
                    // Cache each recipe by ID for navigation
                    recipes.forEach { recipe ->
                        generatedRecipeCache[recipe.id] = recipe
                        Log.d("RecipeRepository", "Cached generated recipe: ${recipe.id} - ${recipe.title}")
                    }
                    
                    emit(recipes)
                    return@flow
                } else {
                    lastError = result.exceptionOrNull() as? Exception 
                        ?: Exception("Unknown error")
                    
                    // Don't retry on rate limit errors
                    if (lastError?.message?.contains("rate limit", ignoreCase = true) == true ||
                        lastError?.message?.contains("quota", ignoreCase = true) == true) {
                        break
                    }
                }
                
            } catch (e: Exception) {
                Log.e("RecipeRepository", "Attempt $attempts failed", e)
                lastError = e
            }
            
            // Wait before retry (exponential backoff)
            if (attempts < 3) {
                delay(1000L * attempts)
            }
        }
        
        // All retries failed
        throw lastError ?: Exception("Failed to generate recipes after $attempts attempts")
    }
    
    /**
     * Build cache key from parameters
     */
    private fun buildCacheKey(
        ingredients: List<String>,
        dietary: List<String>,
        skill: String
    ): String {
        return (ingredients.sorted() + dietary.sorted() + skill).joinToString("|")
    }
    
    /**
     * Get cached recipes if not expired
     */
    private fun getCachedRecipes(key: String): List<Recipe>? {
        val cached = recipeCache[key] ?: return null
        val (recipes, timestamp) = cached
        
        return if (System.currentTimeMillis() - timestamp < cacheExpiryMs) {
            recipes
        } else {
            recipeCache.remove(key)
            null
        }
    }
    
    /**
     * Cache recipes with timestamp
     */
    private fun cacheRecipes(key: String, recipes: List<Recipe>) {
        recipeCache[key] = Pair(recipes, System.currentTimeMillis())
        Log.d("RecipeRepository", "Cached ${recipes.size} recipes")
    }
    
    /**
     * Clear recipe cache
     */
    fun clearCache() {
        recipeCache.clear()
        Log.d("RecipeRepository", "Recipe cache cleared")
    }
    
    /**
     * Get all saved recipes from Firestore
     */
    val savedRecipes: Flow<List<Recipe>> = callbackFlow {
        val userId = FirebaseManager.userId
        
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = FirebaseManager.getRecipesCollection(userId)
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("RecipeRepository", "Error listening to recipes", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val recipes = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { Recipe.fromMap(it) }
                    }
                    trySend(recipes)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Save recipe to Firestore
     */
    suspend fun saveRecipe(recipe: Recipe) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getRecipesCollection(userId)
                .document(recipe.id)
                .set(recipe.toMap())
                .await()
            
            Log.d("RecipeRepository", "Recipe saved: ${recipe.title}")
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe", e)
        }
    }
    
    /**
     * Delete recipe from Firestore
     */
    suspend fun deleteRecipe(recipeId: String) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getRecipesCollection(userId)
                .document(recipeId)
                .delete()
                .await()
            
            Log.d("RecipeRepository", "Recipe deleted: $recipeId")
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error deleting recipe", e)
        }
    }
    
    /**
     * Get recipe by ID from Firestore
     */
    suspend fun getRecipeById(id: String): Recipe? {
        // First check AI-generated recipe cache
        generatedRecipeCache[id]?.let { 
            Log.d("RecipeRepository", "Found recipe in generated cache: $id")
            return it 
        }
        
        // Then check static fallback recipes
        val staticRecipe = getRecommendedRecipesFallback().find { it.id == id }
        if (staticRecipe != null) {
            Log.d("RecipeRepository", "Found recipe in static list: $id")
            return staticRecipe
        }
        
        // Finally try to get from Firebase
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) {
            Log.w("RecipeRepository", "No user ID, cannot fetch from Firebase")
            return null
        }
        
        return try {
            val snapshot = FirebaseManager.getRecipesCollection(userId)
                .document(id)
                .get()
                .await()
            
            snapshot.data?.let { 
                Log.d("RecipeRepository", "Found recipe in Firebase: $id")
                Recipe.fromMap(it) 
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipe by ID: $id", e)
            null
        }
    }
    
    /**
     * Clear the generated recipe cache (useful when user logs out or wants fresh recipes)
     */
    fun clearGeneratedRecipeCache() {
        generatedRecipeCache.clear()
        Log.d("RecipeRepository", "Cleared generated recipe cache")
    }
    
    /**
     * Get recommended recipes from user's saved recipes, fallback to static
     * Fetches once without listener to avoid crashes
     */
    fun getRecommendedRecipesFlow(): Flow<List<Recipe>> = flow {
        val userId = FirebaseManager.userId
        
        if (userId.isEmpty()) {
            Log.d("RecipeRepository", "No user logged in, using fallback recipes")
            emit(getRecommendedRecipesFallback())
            return@flow
        }
        
        try {
            Log.d("RecipeRepository", "Fetching saved recipes from Firebase...")
            
            // Fetch saved recipes once (no listener)
            val snapshot = FirebaseManager.getRecipesCollection(userId)
                .limit(4)
                .get()
                .await()
            
            val savedRecipes = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data?.let { Recipe.fromMap(it) }
                } catch (e: Exception) {
                    Log.e("RecipeRepository", "Error parsing recipe", e)
                    null
                }
            }
            
            if (savedRecipes.isNotEmpty()) {
                Log.d("RecipeRepository", "Found ${savedRecipes.size} saved recipes")
                emit(savedRecipes)
            } else {
                Log.d("RecipeRepository", "No saved recipes found, using fallback")
                emit(getRecommendedRecipesFallback())
            }
            
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error fetching saved recipes", e)
            emit(getRecommendedRecipesFallback())
        }
    }
    
    /**
     * Get static fallback recommended recipes for home screen
     */
    private fun getRecommendedRecipesFallback(): List<Recipe> {
        return listOf(
            Recipe(
                id = "pasta-primavera",
                title = "Pasta Primavera",
                description = "Perfect for using up your vegetables. Fresh seasonal veggies with pasta in a light garlic sauce.",
                imageUrl = "🍝",
                cookTime = 25,
                servings = 4,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Pasta", "400g", true),
                    Ingredient("Cherry tomatoes", "200g", true),
                    Ingredient("Bell peppers", "2", true),
                    Ingredient("Zucchini", "1", true),
                    Ingredient("Garlic", "3 cloves", false),
                    Ingredient("Olive oil", "3 tbsp", false),
                    Ingredient("Parmesan cheese", "50g", false),
                    Ingredient("Fresh basil", "handful", false)
                ),
                steps = listOf(
                    "Bring a large pot of salted water to boil and cook pasta according to package directions",
                    "While pasta cooks, dice bell peppers and zucchini into bite-sized pieces",
                    "Heat olive oil in a large pan over medium heat and sauté minced garlic for 1 minute",
                    "Add bell peppers and zucchini, cook for 5-6 minutes until tender",
                    "Add halved cherry tomatoes and cook for 2 more minutes",
                    "Drain pasta and add to the vegetable mixture, toss well",
                    "Season with salt and pepper, top with grated Parmesan and fresh basil",
                    "Serve immediately while hot"
                ),
                tags = listOf("Italian", "Vegetarian", "Quick")
            ),
            Recipe(
                id = "chicken-teriyaki-bowl",
                title = "Chicken Teriyaki Bowl",
                description = "Sweet and savory Japanese-inspired rice bowl with tender chicken and vegetables.",
                imageUrl = "🍗",
                cookTime = 30,
                servings = 3,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Chicken breast", "500g", true),
                    Ingredient("Rice", "2 cups", true),
                    Ingredient("Broccoli", "200g", true),
                    Ingredient("Carrots", "2", true),
                    Ingredient("Soy sauce", "4 tbsp", false),
                    Ingredient("Honey", "2 tbsp", false),
                    Ingredient("Ginger", "1 inch", false),
                    Ingredient("Sesame seeds", "1 tbsp", false)
                ),
                steps = listOf(
                    "Cook rice according to package directions and keep warm",
                    "Cut chicken into bite-sized pieces and season with salt",
                    "Mix soy sauce, honey, and grated ginger to make teriyaki sauce",
                    "Heat oil in a large pan and cook chicken until golden brown",
                    "Add broccoli florets and sliced carrots, stir-fry for 5 minutes",
                    "Pour teriyaki sauce over chicken and vegetables, cook for 2 minutes",
                    "Serve chicken and vegetables over rice, garnish with sesame seeds"
                ),
                tags = listOf("Asian", "Healthy", "Dinner")
            ),
            Recipe(
                id = "greek-salad-wrap",
                title = "Greek Salad Wrap",
                description = "Fresh Mediterranean flavors wrapped in a soft tortilla. Light, healthy, and delicious.",
                imageUrl = "🥙",
                cookTime = 15,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Tortilla wraps", "2 large", false),
                    Ingredient("Romaine lettuce", "2 cups", true),
                    Ingredient("Cucumber", "1", true),
                    Ingredient("Tomatoes", "2", true),
                    Ingredient("Red onion", "1/4", true),
                    Ingredient("Feta cheese", "100g", false),
                    Ingredient("Olives", "1/4 cup", false),
                    Ingredient("Greek yogurt", "3 tbsp", false),
                    Ingredient("Lemon juice", "1 tbsp", false)
                ),
                steps = listOf(
                    "Chop lettuce, dice cucumber and tomatoes, thinly slice red onion",
                    "Mix Greek yogurt with lemon juice to make the sauce",
                    "Warm tortillas in a dry pan for 30 seconds each side",
                    "Spread yogurt sauce on each tortilla",
                    "Layer lettuce, cucumber, tomatoes, onion, crumbled feta, and olives",
                    "Roll up tightly, tucking in the sides as you go",
                    "Cut in half diagonally and serve immediately"
                ),
                tags = listOf("Mediterranean", "Vegetarian", "Lunch")
            ),
            Recipe(
                id = "berry-smoothie-bowl",
                title = "Berry Smoothie Bowl",
                description = "Nutritious and colorful breakfast bowl packed with antioxidants and fresh fruits.",
                imageUrl = "🫐",
                cookTime = 10,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Frozen berries", "2 cups", true),
                    Ingredient("Banana", "1", true),
                    Ingredient("Greek yogurt", "1 cup", true),
                    Ingredient("Honey", "1 tbsp", false),
                    Ingredient("Granola", "1/4 cup", false),
                    Ingredient("Fresh berries", "handful", false),
                    Ingredient("Chia seeds", "1 tbsp", false),
                    Ingredient("Almond slices", "2 tbsp", false)
                ),
                steps = listOf(
                    "Add frozen berries, banana, yogurt, and honey to a blender",
                    "Blend until smooth and thick (add a splash of milk if too thick)",
                    "Pour into two bowls",
                    "Top with granola, fresh berries, chia seeds, and almond slices",
                    "Arrange toppings in sections for a beautiful presentation",
                    "Serve immediately while cold"
                ),
                tags = listOf("Breakfast", "Healthy", "Vegan")
            )
        )
    }
    
    private fun getSampleRecipes(matchedIngredients: List<String>): List<Recipe> {
        return listOf(
            Recipe(
                id = UUID.randomUUID().toString(),
                title = "Creamy Pasta Carbonara",
                description = "Classic Italian pasta with eggs, cheese, and pancetta",
                imageUrl = "",
                cookTime = 20,
                servings = 4,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Pasta", "400g", matchedIngredients.contains("Pasta")),
                    Ingredient("Eggs", "4", matchedIngredients.contains("Eggs")),
                    Ingredient("Parmesan cheese", "100g", matchedIngredients.contains("Cheese")),
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
                matchedIngredients = matchedIngredients,
                tags = listOf("Italian", "Quick", "Dinner")
            ),
            Recipe(
                id = UUID.randomUUID().toString(),
                title = "Chicken Stir Fry",
                description = "Quick and healthy Asian-inspired dish",
                imageUrl = "",
                cookTime = 15,
                servings = 2,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Chicken breast", "300g", matchedIngredients.contains("Chicken")),
                    Ingredient("Mixed vegetables", "200g", matchedIngredients.contains("Vegetables")),
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
                matchedIngredients = matchedIngredients,
                tags = listOf("Asian", "Healthy", "Quick")
            ),
            Recipe(
                id = UUID.randomUUID().toString(),
                title = "Veggie Omelette",
                description = "Fluffy eggs with fresh vegetables",
                imageUrl = "",
                cookTime = 10,
                servings = 1,
                difficulty = "Easy",
                ingredients = listOf(
                    Ingredient("Eggs", "3", matchedIngredients.contains("Eggs")),
                    Ingredient("Bell peppers", "1/2 cup", matchedIngredients.contains("Peppers")),
                    Ingredient("Onions", "1/4 cup", false),
                    Ingredient("Cheese", "50g", matchedIngredients.contains("Cheese")),
                    Ingredient("Salt & pepper", "to taste", false)
                ),
                steps = listOf(
                    "Beat eggs in a bowl",
                    "Chop vegetables finely",
                    "Heat pan with butter",
                    "Pour eggs and add vegetables",
                    "Fold and serve hot"
                ),
                matchedIngredients = matchedIngredients,
                tags = listOf("Breakfast", "Vegetarian", "Quick")
            )
        )
    }
}
