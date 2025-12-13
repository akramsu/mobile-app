package com.freshly.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.freshly.app.MainActivity
import com.freshly.app.R
import com.freshly.app.data.model.PantryItem

object NotificationHelper {
    
    private const val CHANNEL_ID_CRITICAL = "expiry_critical"
    private const val CHANNEL_ID_WARNING = "expiry_warning"
    private const val CHANNEL_ID_INFO = "expiry_info"
    
    private const val GROUP_KEY = "com.freshly.app.EXPIRY_NOTIFICATIONS"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Critical channel - Expires today/tomorrow
            val criticalChannel = NotificationChannel(
                CHANNEL_ID_CRITICAL,
                "Critical Expiry Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Items expiring today or tomorrow"
                enableVibration(true)
                enableLights(true)
            }
            
            // Warning channel - Expires in 2-3 days
            val warningChannel = NotificationChannel(
                CHANNEL_ID_WARNING,
                "Expiry Warnings",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Items expiring soon (2-3 days)"
                enableVibration(false)
            }
            
            // Info channel - Expires in 4+ days
            val infoChannel = NotificationChannel(
                CHANNEL_ID_INFO,
                "Expiry Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Gentle reminders about upcoming expiries"
                setShowBadge(false)
            }
            
            notificationManager.createNotificationChannel(criticalChannel)
            notificationManager.createNotificationChannel(warningChannel)
            notificationManager.createNotificationChannel(infoChannel)
        }
    }
    
    fun sendExpiryNotifications(context: Context, items: List<PantryItem>) {
        if (items.isEmpty()) return
        
        // Group items by urgency
        val criticalItems = items.filter { it.getDaysUntilExpiry() <= 1 }
        val warningItems = items.filter { it.getDaysUntilExpiry() in 2..3 }
        val infoItems = items.filter { it.getDaysUntilExpiry() in 4..7 }
        
        // Send notifications (grouped to avoid spam)
        if (criticalItems.isNotEmpty()) {
            sendGroupedNotification(context, criticalItems, CHANNEL_ID_CRITICAL, 1)
        }
        
        if (warningItems.isNotEmpty()) {
            sendGroupedNotification(context, warningItems, CHANNEL_ID_WARNING, 2)
        }
        
        if (infoItems.isNotEmpty()) {
            sendGroupedNotification(context, infoItems, CHANNEL_ID_INFO, 3)
        }
    }
    
    private fun sendGroupedNotification(
        context: Context,
        items: List<PantryItem>,
        channelId: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Determine emoji and message based on channel
        val (emoji, title, priority) = when (channelId) {
            CHANNEL_ID_CRITICAL -> Triple("🚨", "Items Expiring Soon!", NotificationCompat.PRIORITY_HIGH)
            CHANNEL_ID_WARNING -> Triple("⚠️", "Pantry Reminder", NotificationCompat.PRIORITY_DEFAULT)
            else -> Triple("📋", "Upcoming Expiries", NotificationCompat.PRIORITY_LOW)
        }
        
        // Build notification content
        val contentText = if (items.size == 1) {
            val item = items.first()
            val days = item.getDaysUntilExpiry()
            when {
                days == 0 -> "${item.name} expires TODAY"
                days == 1 -> "${item.name} expires tomorrow"
                else -> "${item.name} expires in $days days"
            }
        } else {
            "${items.size} items need your attention"
        }
        
        // Create inbox style for multiple items
        val inboxStyle = NotificationCompat.InboxStyle()
        items.take(5).forEach { item ->
            val days = item.getDaysUntilExpiry()
            val daysText = when {
                days == 0 -> "today"
                days == 1 -> "tomorrow"
                else -> "in $days days"
            }
            inboxStyle.addLine("${getItemEmoji(item)} ${item.name} - expires $daysText")
        }
        
        if (items.size > 5) {
            inboxStyle.addLine("+ ${items.size - 5} more items")
        }
        
        inboxStyle.setBigContentTitle("$emoji $title")
        inboxStyle.setSummaryText("Tap to view pantry")
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with your app icon
            .setContentTitle("$emoji $title")
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(GROUP_KEY)
            .build()
        
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
    
    private fun getItemEmoji(item: PantryItem): String {
        return when (item.category.name.lowercase()) {
            "fridge" -> "🧊"
            "freezer" -> "❄️"
            "pantry" -> "📦"
            else -> "🍎"
        }
    }
    
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
