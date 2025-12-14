package com.freshly.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.model.Recipe
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.data.repository.RecipeRepository
import com.freshly.app.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class AIChefViewModel : ViewModel() {
    
    private val pantryRepository = PantryRepository()
    private val recipeRepository = RecipeRepository()
    private val userRepository = UserRepository()
    
    val pantryItems: StateFlow<List<PantryItem>> = pantryRepository.items
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems: StateFlow<Set<String>> = _selectedItems.asStateFlow()
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    companion object {
        private const val GENERATION_TIMEOUT_MS = 45000L // 45 seconds
        private const val TAG = "AIChefViewModel"
    }
    
    fun toggleItem(itemName: String) {
        _selectedItems.value = if (_selectedItems.value.contains(itemName)) {
            _selectedItems.value - itemName
        } else {
            _selectedItems.value + itemName
        }
    }
    
    /**
     * Generate recipes using Gemini AI
     */
    fun generateRecipes() {
        if (_selectedItems.value.isEmpty()) {
            _errorMessage.value = "Please select at least one ingredient"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _recipes.value = emptyList()
            
            try {
                // Add timeout to prevent hanging
                withTimeout(GENERATION_TIMEOUT_MS) {
                    // Get user dietary preferences
                    val user = userRepository.user.first()
                    val dietaryPreferences = user.dietaryRestrictions
                    
                    recipeRepository.generateRecipes(
                        selectedIngredients = _selectedItems.value.toList(),
                        dietaryPreferences = dietaryPreferences,
                        skillLevel = "Medium" // Could make this user-configurable
                    ).collect { recipes ->
                        if (recipes.isNotEmpty()) {
                            _recipes.value = recipes
                            Log.d(TAG, "Generated ${recipes.size} recipes successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("timeout", ignoreCase = true) == true ->
                        "Request timed out. Please try again."
                    e.message?.contains("rate limit", ignoreCase = true) == true ->
                        e.message ?: "Rate limit exceeded"
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        e.message ?: "Daily quota reached"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error. Check your connection."
                    else -> e.message ?: "Failed to generate recipes"
                }
                
                _errorMessage.value = errorMsg
                Log.e(TAG, "Recipe generation failed: $errorMsg", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Retry recipe generation
     */
    fun retryGeneration() {
        generateRecipes()
    }
    
    /**
     * Dismiss error message
     */
    fun dismissError() {
        _errorMessage.value = null
    }
    
    /**
     * Reset selection and recipes
     */
    fun reset() {
        _selectedItems.value = emptySet()
        _recipes.value = emptyList()
        _errorMessage.value = null
    }
}
