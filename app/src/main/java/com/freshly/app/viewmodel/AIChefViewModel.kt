package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.model.Recipe
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AIChefViewModel : ViewModel() {
    
    private val pantryRepository = PantryRepository()
    private val recipeRepository = RecipeRepository()
    
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
    
    fun toggleItem(itemName: String) {
        _selectedItems.value = if (_selectedItems.value.contains(itemName)) {
            _selectedItems.value - itemName
        } else {
            _selectedItems.value + itemName
        }
    }
    
    suspend fun generateRecipes() {
        _isLoading.value = true
        recipeRepository.generateRecipes(_selectedItems.value.toList())
            .collect { recipes ->
                _recipes.value = recipes
                _isLoading.value = false
            }
    }
    
    fun reset() {
        _selectedItems.value = emptySet()
        _recipes.value = emptyList()
    }
}
