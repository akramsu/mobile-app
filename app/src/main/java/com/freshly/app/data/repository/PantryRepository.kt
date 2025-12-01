package com.freshly.app.data.repository

import com.freshly.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.util.*

class PantryRepository {
    
    private val _items = MutableStateFlow<List<PantryItem>>(getSampleItems())
    val items: Flow<List<PantryItem>> = _items
    
    fun getItemById(id: String): PantryItem? {
        return _items.value.find { it.id == id }
    }
    
    fun getItemsByCategory(category: Category): Flow<List<PantryItem>> {
        return items.map { list -> list.filter { it.category == category } }
    }
    
    fun getExpiringItems(daysThreshold: Int = 3): Flow<List<PantryItem>> {
        return items.map { list ->
            list.filter { it.getDaysUntilExpiry() <= daysThreshold && it.getDaysUntilExpiry() >= 0 }
                .sortedBy { it.expiryDate }
        }
    }
    
    suspend fun addItem(item: PantryItem) {
        _items.value = _items.value + item
    }
    
    suspend fun updateItem(item: PantryItem) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
    }
    
    suspend fun deleteItem(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }
    
    fun searchItems(query: String): Flow<List<PantryItem>> {
        return items.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    
    private fun getSampleItems(): List<PantryItem> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return listOf(
            PantryItem(
                id = UUID.randomUUID().toString(),
                name = "Milk",
                category = Category.FRIDGE,
                quantity = 1,
                unit = "gallon",
                addedDate = today.minus(2, DateTimeUnit.DAY).toString(),
                expiryDate = today.plus(2, DateTimeUnit.DAY).toString()
            ),
            PantryItem(
                id = UUID.randomUUID().toString(),
                name = "Eggs",
                category = Category.FRIDGE,
                quantity = 12,
                unit = "count",
                addedDate = today.minus(5, DateTimeUnit.DAY).toString(),
                expiryDate = today.plus(8, DateTimeUnit.DAY).toString()
            ),
            PantryItem(
                id = UUID.randomUUID().toString(),
                name = "Chicken Breast",
                category = Category.FREEZER,
                quantity = 2,
                unit = "lbs",
                addedDate = today.minus(10, DateTimeUnit.DAY).toString(),
                expiryDate = today.plus(30, DateTimeUnit.DAY).toString()
            ),
            PantryItem(
                id = UUID.randomUUID().toString(),
                name = "Rice",
                category = Category.PANTRY,
                quantity = 5,
                unit = "lbs",
                addedDate = today.minus(20, DateTimeUnit.DAY).toString(),
                expiryDate = today.plus(365, DateTimeUnit.DAY).toString()
            )
        )
    }
}
