package com.freshly.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.Recipe
import com.freshly.app.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SavedRecipesViewModel(application: Application) : AndroidViewModel(application) {
    
    private val recipeRepository = RecipeRepository.getInstance(application.applicationContext)
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()
    
    private var currentPage = 0
    private val pageSize = 10
    
    init {
        loadRecipes()
    }
    
    private fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Collect all saved recipes from the repository
                recipeRepository.savedRecipes.collect { allRecipes ->
                    Log.d("SavedRecipesViewModel", "Loaded ${allRecipes.size} total recipes")
                    
                    // For initial load, show first page
                    val recipesToShow = allRecipes.take((currentPage + 1) * pageSize)
                    _recipes.value = recipesToShow
                    
                    // Check if there's more data to load
                    _hasMoreData.value = allRecipes.size > recipesToShow.size
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("SavedRecipesViewModel", "Error loading recipes", e)
                _isLoading.value = false
                _hasMoreData.value = false
            }
        }
    }
    
    fun loadMoreRecipes() {
        if (_isLoading.value || !_hasMoreData.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            currentPage++
            
            try {
                // Get current recipes list and add more
                recipeRepository.savedRecipes.collect { allRecipes ->
                    val newRecipes = allRecipes.take((currentPage + 1) * pageSize)
                    _recipes.value = newRecipes
                    _hasMoreData.value = allRecipes.size > newRecipes.size
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("SavedRecipesViewModel", "Error loading more recipes", e)
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        currentPage = 0
        _recipes.value = emptyList()
        _hasMoreData.value = true
        loadRecipes()
    }
}
