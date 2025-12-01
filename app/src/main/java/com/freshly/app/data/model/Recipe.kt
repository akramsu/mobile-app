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
) : Parcelable

@Parcelize
data class Ingredient(
    val name: String,
    val amount: String,
    val isMatched: Boolean = false
) : Parcelable
