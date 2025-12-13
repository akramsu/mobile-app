package com.freshly.app.data.repository

import android.util.Log
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

class RecipeRepository {
    
    /**
     * Generate recipes based on selected ingredients
     * In real implementation, this would call an AI API
     */
    suspend fun generateRecipes(selectedIngredients: List<String>): Flow<List<Recipe>> = flow {
        // Simulate API call with 800ms delay (matching animation)
        delay(800)
        emit(getSampleRecipes(selectedIngredients))
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
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return null
        
        return try {
            val snapshot = FirebaseManager.getRecipesCollection(userId)
                .document(id)
                .get()
                .await()
            
            snapshot.data?.let { Recipe.fromMap(it) }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipe by ID", e)
            null
        }
    }
    
    /**
     * Get daily recipe suggestion (uses local sample data)
     */
    fun getDailyRecipe(): Recipe {
        return getSampleRecipes(emptyList()).first()
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
