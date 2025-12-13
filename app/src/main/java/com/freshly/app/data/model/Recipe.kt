package com.freshly.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val cookTime: Int,
    val servings: Int,
    val difficulty: String = "Medium",
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val matchedIngredients: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) : Parcelable {
    
    /**
     * Convert Recipe to Firestore-compatible Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "cookTime" to cookTime,
            "servings" to servings,
            "difficulty" to difficulty,
            "ingredients" to ingredients.map { it.toMap() },
            "steps" to steps,
            "matchedIngredients" to matchedIngredients,
            "tags" to tags
        )
    }
    
    companion object {
        /**
         * Create Recipe from Firestore document
         */
        fun fromMap(map: Map<String, Any?>): Recipe {
            return Recipe(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String ?: "",
                cookTime = (map["cookTime"] as? Long)?.toInt() ?: 30,
                servings = (map["servings"] as? Long)?.toInt() ?: 2,
                difficulty = map["difficulty"] as? String ?: "Medium",
                ingredients = (map["ingredients"] as? List<*>)?.mapNotNull { 
                    (it as? Map<*, *>)?.let { ingredientMap ->
                        Ingredient.fromMap(ingredientMap as Map<String, Any?>)
                    }
                } ?: emptyList(),
                steps = (map["steps"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                matchedIngredients = (map["matchedIngredients"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                tags = (map["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
}

@Parcelize
data class Ingredient(
    val name: String,
    val amount: String,
    val isMatched: Boolean = false
) : Parcelable {
    
    /**
     * Convert Ingredient to Firestore-compatible Map
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "amount" to amount,
            "isMatched" to isMatched
        )
    }
    
    companion object {
        /**
         * Create Ingredient from Firestore document
         */
        fun fromMap(map: Map<String, Any?>): Ingredient {
            return Ingredient(
                name = map["name"] as? String ?: "",
                amount = map["amount"] as? String ?: "",
                isMatched = map["isMatched"] as? Boolean ?: false
            )
        }
    }
}
