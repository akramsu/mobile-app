package com.freshly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.ui.components.*
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.ui.theme.Warning500
import com.freshly.app.ui.theme.AI500
import com.freshly.app.utils.DateUtils
import com.freshly.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onAddItem: () -> Unit = {},
    onNotifications: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val expiringItems by viewModel.expiringItems.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Greeting Section
        item {
            Column {
                Text(
                    text = "${DateUtils.getGreeting()}, ${user.name} 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You have ${expiringItems.size} items expiring today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Combined XP & Streak Card (Green gradient)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Primary500, Color(0xFF2D8A5E))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    // Level and Streak Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Level
                        Column {
                            Text(
                                text = "Level",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${user.level}",
                                fontSize = 36.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Day Streak
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🔥", fontSize = 24.sp)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Day Streak",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "${user.streak}",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // XP Progress
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "XP Progress",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${user.xp % 1000} / 1,000",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((user.xp % 1000) / 1000f)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }
        
        // Expiring Soon Section
        item {
            Text(
                text = "Expiring Soon",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expiringItems) { item ->
                    val daysUntilExpiry = item.getDaysUntilExpiry()
                    val isExpiringSoon = daysUntilExpiry <= 2
                    val borderColor = if (isExpiringSoon) Warning500 else Primary500
                    
                    Box(
                        modifier = Modifier
                            .width(144.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        // Left colored border
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(borderColor)
                                .align(Alignment.CenterStart)
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Item emoji/icon
                            Text(
                                text = when (item.category.name) {
                                    "DAIRY" -> "🥛"
                                    "FRUIT" -> "🫐"
                                    "VEGETABLE" -> "🥬"
                                    "MEAT" -> "🥩"
                                    "GRAIN" -> "🍞"
                                    else -> "🍽️"
                                },
                                fontSize = 36.sp
                            )
                            
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            // Expiry chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isExpiringSoon) 
                                    Warning500.copy(alpha = 0.1f) 
                                else 
                                    Primary500.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "$daysUntilExpiry day${if (daysUntilExpiry != 1) "s" else ""}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isExpiringSoon) Warning500 else Primary500
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Quick Actions Section
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.Search,
                    label = "Search",
                    isPrimary = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Item",
                    isPrimary = false,
                    onClick = onAddItem,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Notifications,
                    label = "Notifications",
                    isPrimary = false,
                    onClick = onNotifications,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Recipe Card (AI-styled)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, AI500, RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Recipe image area with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AI500, Color(0xFF7C3AED))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🍝", fontSize = 60.sp)
                    }
                    
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pasta Primavera",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Perfect for using up your vegetables",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip(text = "Tomatoes", variant = "ai")
                            Chip(text = "Pasta", variant = "fresh")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f),
        colors = if (isPrimary) {
            ButtonDefaults.buttonColors(
                containerColor = Primary500,
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        },
        border = if (!isPrimary) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        } else null,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun Chip(text: String, variant: String) {
    val backgroundColor = when (variant) {
        "ai" -> AI500.copy(alpha = 0.1f)
        "fresh" -> Primary500.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (variant) {
        "ai" -> AI500
        "fresh" -> Primary500
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}
