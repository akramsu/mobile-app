package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.model.Category
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.repository.PantryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PantryViewModel : ViewModel() {
    
    private val repository = PantryRepository()
    
    private val _deletedItem = MutableStateFlow<PantryItem?>(null)
    
    fun getItemsByCategory(category: Category): Flow<List<PantryItem>> {
        return repository.getItemsByCategory(category)
    }
    
    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            _deletedItem.value = item
            repository.deleteItem(itemId)
        }
    }
    
    fun undoDelete() {
        viewModelScope.launch {
            _deletedItem.value?.let { item ->
                repository.addItem(item)
                _deletedItem.value = null
            }
        }
    }
}
