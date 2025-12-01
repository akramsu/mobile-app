package com.freshly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freshly.app.data.model.ExpiryStatus
import com.freshly.app.data.model.PantryItem
import com.freshly.app.ui.theme.Danger
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.ui.theme.Warning
import com.freshly.app.utils.DateUtils
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun PantryItemRow(
    item: PantryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (item.getExpiryStatus()) {
        ExpiryStatus.FRESH -> Primary500
        ExpiryStatus.EXPIRING_SOON -> Warning
        ExpiryStatus.EXPIRED -> Danger
    }
    
    val deleteAction = SwipeAction(
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White
            )
        },
        background = Danger,
        onSwipe = onDelete
    )
    
    SwipeableActionsBox(
        endActions = listOf(deleteAction),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator stripe
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item image
            ItemImage(
                imageUrl = item.imageUrl,
                name = item.name,
                size = 48.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = DateUtils.getRelativeTimeString(kotlinx.datetime.LocalDate.parse(item.expiryDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }
        }
    }
}
