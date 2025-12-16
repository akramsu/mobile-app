package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.model.User
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel : ViewModel() {
    
    private val userRepository = UserRepository()
    private val pantryRepository = PantryRepository()
    private val recipeRepository = com.freshly.app.data.repository.RecipeRepository.getInstance()
    
    val user: StateFlow<User> = userRepository.user
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = User(name = "", xp = 0, level = 1, streak = 0)
        )
    
    val expiringItems: StateFlow<List<PantryItem>> = pantryRepository.getExpiringItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val recommendedRecipes: StateFlow<List<com.freshly.app.data.model.Recipe>> = recipeRepository.getRecommendedRecipesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
