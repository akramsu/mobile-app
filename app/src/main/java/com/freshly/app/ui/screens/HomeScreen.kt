package com.freshly.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.freshly.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onAddItem: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRecipeClick: (String) -> Unit = {},
    onSavedRecipesClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val expiringItems by viewModel.expiringItems.collectAsState()
    val recommendedRecipes by viewModel.recommendedRecipes.collectAsState()
    var selectedAction by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Greeting Section with Profile and Notifications
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Profile Image
                    ImageAvatar(
                        imageUrl = user.avatarUrl,
                        name = user.name ?: "User",
                        size = 48.dp
                    )
                    
                    // Greeting Text
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Hi, ${user.name}!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You have ${expiringItems.size} items expiring today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Notifications Icon
                IconButton(
                    onClick = onNotifications,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
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
                            // Item image with smart emoji
                            ItemImage(
                                imageUrl = item.imageUrl,
                                name = item.name,
                                category = item.category.name,
                                size = 60.dp
                            )
                            
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Search,
                    label = "Search",
                    isSelected = selectedAction == "Search",
                    onClick = { 
                        selectedAction = "Search"
                        onSearchClick()
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Item",
                    isSelected = selectedAction == "Add Item",
                    onClick = { 
                        selectedAction = "Add Item"
                        onAddItem()
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.Notifications,
                    label = "Alerts",
                    isSelected = selectedAction == "Notifications",
                    onClick = { 
                        selectedAction = "Notifications"
                        onNotifications()
                    }
                )
            }
        }
        
        // Recent Recipes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Recipes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (recommendedRecipes.isNotEmpty()) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary500,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onSavedRecipesClick() }
                    )
                }
            }
        }
        
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                recommendedRecipes.forEach { recipe ->
                    Surface(
                        onClick = { onRecipeClick(recipe.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Recipe emoji icon with gradient background
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(AI500.copy(alpha = 0.7f), Primary500.copy(alpha = 0.7f))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipe.imageUrl.ifEmpty { "🍽️" },
                                    fontSize = 40.sp
                                )
                            }
                            
                            // Recipe details
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Title
                                Text(
                                    text = recipe.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                
                                // Description
                                Text(
                                    text = recipe.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                
                                // Meta info row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Cook time
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Primary500
                                        )
                                        Text(
                                            text = "${recipe.cookTime}m",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    
                                    // Servings
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Primary500
                                        )
                                        Text(
                                            text = "${recipe.servings}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    
                                    // Difficulty badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (recipe.difficulty.lowercase()) {
                                            "easy" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                            "medium" -> Color(0xFFFFC107).copy(alpha = 0.15f)
                                            "hard" -> Color(0xFFFF5722).copy(alpha = 0.15f)
                                            else -> Primary500.copy(alpha = 0.15f)
                                        }
                                    ) {
                                        Text(
                                            text = recipe.difficulty,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = when (recipe.difficulty.lowercase()) {
                                                "easy" -> Color(0xFF4CAF50)
                                                "medium" -> Color(0xFFFFC107)
                                                "hard" -> Color(0xFFFF5722)
                                                else -> Primary500
                                            }
                                        )
                                    }
                                }
                            }
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
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Circular button with gradient border
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 6.dp,
                    shape = CircleShape,
                    clip = false
                ),
            shape = CircleShape,
            color = if (isSelected) Primary500 else Color.White,
            border = if (!isSelected) {
                BorderStroke(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Primary500.copy(alpha = 0.6f),
                            Primary500.copy(alpha = 0.3f)
                        )
                    )
                )
            } else null
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = if (isSelected) Color.White else Color(0xFF6B7280)
                )
            }
        }
        
        // Label
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.Black else Color(0xFF6B7280)
        )
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
