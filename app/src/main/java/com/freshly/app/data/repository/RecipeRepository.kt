package com.freshly.app.data.repository

import com.freshly.app.data.model.Ingredient
import com.freshly.app.data.model.Recipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecipeRepository {
    
    suspend fun generateRecipes(selectedIngredients: List<String>): Flow<List<Recipe>> = flow {
        // Simulate API call with 800ms delay (matching animation)
        delay(800)
        emit(getSampleRecipes(selectedIngredients))
    }
    
    fun getRecipeById(id: String): Recipe? {
        return getSampleRecipes(emptyList()).find { it.id == id }
    }
    
    fun getDailyRecipe(): Recipe {
        return getSampleRecipes(emptyList()).first()
    }
    
    private fun getSampleRecipes(matchedIngredients: List<String>): List<Recipe> {
        return listOf(
            Recipe(
                id = "1",
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
                id = "2",
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
                id = "3",
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
