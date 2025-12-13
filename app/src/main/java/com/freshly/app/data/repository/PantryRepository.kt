package com.freshly.app.data.repository

import android.util.Log
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.model.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.util.*

class PantryRepository {
    
    private var itemsListener: ListenerRegistration? = null
    
    /**
     * Get all pantry items as Flow with real-time updates
     */
    val items: Flow<List<PantryItem>> = callbackFlow {
        val userId = FirebaseManager.userId
        
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        itemsListener = FirebaseManager.getPantryItemsCollection(userId)
            .orderBy("expiryDate", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PantryRepository", "Error listening to items", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { PantryItem.fromMap(it) }
                    }
                    trySend(items)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { itemsListener?.remove() }
    }
    
    /**
     * Get item by ID
     */
    suspend fun getItemById(id: String): PantryItem? {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return null
        
        return try {
            val snapshot = FirebaseManager.getPantryItemsCollection(userId)
                .document(id)
                .get()
                .await()
            
            snapshot.data?.let { PantryItem.fromMap(it) }
        } catch (e: Exception) {
            Log.e("PantryRepository", "Error getting item by ID", e)
            null
        }
    }
    
    /**
     * Get items by category
     */
    fun getItemsByCategory(category: Category): Flow<List<PantryItem>> {
        return items.map { list -> list.filter { it.category == category } }
    }
    
    /**
     * Get items expiring soon
     */
    fun getExpiringItems(daysThreshold: Int = 3): Flow<List<PantryItem>> {
        return items.map { list ->
            list.filter { it.getDaysUntilExpiry() <= daysThreshold && it.getDaysUntilExpiry() >= 0 }
                .sortedBy { it.expiryDate }
        }
    }
    
    /**
     * Add new item to Firestore
     */
    suspend fun addItem(item: PantryItem) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getPantryItemsCollection(userId)
                .document(item.id)
                .set(item.toMap())
                .await()
            
            Log.d("PantryRepository", "Item added: ${item.name}")
        } catch (e: Exception) {
            Log.e("PantryRepository", "Error adding item", e)
        }
    }
    
    /**
     * Update existing item in Firestore
     */
    suspend fun updateItem(item: PantryItem) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getPantryItemsCollection(userId)
                .document(item.id)
                .set(item.toMap())
                .await()
            
            Log.d("PantryRepository", "Item updated: ${item.name}")
        } catch (e: Exception) {
            Log.e("PantryRepository", "Error updating item", e)
        }
    }
    
    /**
     * Delete item from Firestore
     */
    suspend fun deleteItem(id: String) {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            FirebaseManager.getPantryItemsCollection(userId)
                .document(id)
                .delete()
                .await()
            
            Log.d("PantryRepository", "Item deleted: $id")
        } catch (e: Exception) {
            Log.e("PantryRepository", "Error deleting item", e)
        }
    }
    
    /**
     * Search items by name
     */
    fun searchItems(query: String): Flow<List<PantryItem>> {
        return items.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
    
    /**
     * Initialize sample items for new users
     */
    suspend fun initializeSampleItems() {
        val userId = FirebaseManager.userId
        if (userId.isEmpty()) return
        
        try {
            // Check if user already has items
            val snapshot = FirebaseManager.getPantryItemsCollection(userId).get().await()
            if (!snapshot.isEmpty) {
                Log.d("PantryRepository", "User already has items, skipping initialization")
                return
            }
            
            // Add sample items
            val sampleItems = getSampleItems()
            sampleItems.forEach { item ->
                addItem(item)
            }
            
            Log.d("PantryRepository", "Sample items initialized")
        } catch (e: Exception) {
            Log.e("PantryRepository", "Error initializing sample items", e)
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
            ),
            PantryItem(
                id = UUID.randomUUID().toString(),
                name = "Blueberries",
                category = Category.FRIDGE,
                quantity = 1,
                unit = "pint",
                addedDate = today.minus(3, DateTimeUnit.DAY).toString(),
                expiryDate = today.plus(1, DateTimeUnit.DAY).toString()
            )
        )
    }
}
