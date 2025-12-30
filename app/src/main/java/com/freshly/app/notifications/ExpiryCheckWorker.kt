package com.freshly.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.utils.PreferencesManager
import kotlinx.coroutines.flow.first

class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val repository = PantryRepository()
    private val preferencesManager = PreferencesManager(context)
    
    override suspend fun doWork(): Result {
        return try {
            Log.d("ExpiryCheckWorker", "Starting expiry check...")
            
            // Check if notifications are enabled in user preferences
            val notificationsEnabled = preferencesManager.notificationsEnabled.first()
            if (!notificationsEnabled) {
                Log.d("ExpiryCheckWorker", "Notifications disabled by user, skipping")
                return Result.success()
            }
            
            // Get all items from repository
            val allItems = repository.items.first()
            
            // Filter items that need notification (expiring in 7 days or less)
            val expiringItems = allItems.filter { item ->
                val daysUntilExpiry = item.getDaysUntilExpiry()
                daysUntilExpiry in 0..7
            }
            
            Log.d("ExpiryCheckWorker", "Found ${expiringItems.size} items expiring soon")
            
            // Send notifications if there are items
            if (expiringItems.isNotEmpty()) {
                NotificationHelper.sendExpiryNotifications(applicationContext, expiringItems)
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e("ExpiryCheckWorker", "Error checking expiry dates", e)
            // Retry on failure
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "expiry_check_work"
    }
}
