package com.freshly.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.R
import com.freshly.app.data.model.Achievement
import com.freshly.app.data.model.AchievementCategory
import com.freshly.app.data.model.AchievementRarity
import com.freshly.app.ui.components.*
import com.freshly.app.ui.theme.AI500
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.viewmodel.ProfileViewModel

// Helper function to map achievement icons to Material Icons
fun getAchievementIcon(iconName: String, category: AchievementCategory, rarity: AchievementRarity): ImageVector {
    return when {
        // Map based on category and rarity
        rarity == AchievementRarity.LEGENDARY -> Icons.Filled.WorkspacePremium
        rarity == AchievementRarity.EPIC -> Icons.Filled.Stars
        category == AchievementCategory.STREAK && iconName.contains("🔥") -> Icons.Filled.LocalFireDepartment
        category == AchievementCategory.SPECIAL || iconName.contains("⭐") -> Icons.Filled.Star
        iconName.contains("🏆") || iconName.contains("👑") -> Icons.Filled.EmojiEvents
        iconName.contains("💎") -> Icons.Filled.Stars
        // Default mapping by category
        category == AchievementCategory.TRACKING -> Icons.Filled.MilitaryTech
        category == AchievementCategory.SAVING -> Icons.Filled.Star
        category == AchievementCategory.COOKING -> Icons.Filled.EmojiEvents
        category == AchievementCategory.STREAK -> Icons.Filled.LocalFireDepartment
        else -> Icons.Filled.MilitaryTech
    }
}

// Helper function to get achievement icon color
fun getAchievementIconColor(rarity: AchievementRarity): Color {
    return when (rarity) {
        AchievementRarity.COMMON -> Color(0xFF94A3B8)
        AchievementRarity.RARE -> Color(0xFF3B82F6)
        AchievementRarity.EPIC -> Color(0xFF9333EA)
        AchievementRarity.LEGENDARY -> Color(0xFFFBBF24)
    }
}

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSavedRecipesClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val unlockedCount = user.achievements.count { it.isUnlocked }
    val totalCount = user.achievements.size
    
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Profile",
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Gradient Header with User Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Primary500.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with border
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .border(
                                    width = 4.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Primary500, Primary500.copy(alpha = 0.6f))
                                    ),
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                        ) {
                            ImageAvatar(
                                imageUrl = user.avatarUrl,
                                name = user.name,
                                size = 100.dp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Level Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Primary500,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Level ${user.level}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Edit Profile Button
                            Surface(
                                onClick = onEditProfileClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Primary500.copy(alpha = 0.3f), Primary500.copy(alpha = 0.5f))
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Primary500.copy(alpha = 0.15f), Primary500.copy(alpha = 0.25f))
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = null,
                                            tint = Primary500,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "Edit Profile",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            // My Saved Recipes Button
                            Surface(
                                onClick = onSavedRecipesClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 2.dp,
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(AI500.copy(alpha = 0.3f), AI500.copy(alpha = 0.5f))
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(AI500.copy(alpha = 0.15f), AI500.copy(alpha = 0.25f))
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = AI500,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "My Recipes",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // XP Card
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "XP",
                        value = "${user.xp}",
                        label = "Total XP",
                        color = Primary500
                    )
                    
                    // Streak Card
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "${user.streak}",
                        value = "Days",
                        label = "Day Streak",
                        color = Color(0xFFFF6B6B)
                    )
                    
                    // Achievements Card
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "$unlockedCount",
                        value = "/$totalCount",
                        label = "Badges",
                        color = Color(0xFFFFB547)
                    )
                }
            }
            
            // XP Progress Section
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${user.xp % 1000} / 1000 XP",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    XPProgressBar(
                        currentXP = user.xp % 1000,
                        maxXP = 1000,
                        level = user.level
                    )
                }
            }
            
            // Recently Unlocked Section
            item {
                val recentlyUnlocked = user.achievements
                    .filter { it.isUnlocked }
                    .sortedByDescending { it.unlockedDate }
                    .take(3)
                
                if (recentlyUnlocked.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Recently Unlocked",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentlyUnlocked) { achievement ->
                                RecentAchievementCard(
                                    achievement = achievement,
                                    onClick = { selectedAchievement = achievement }
                                )
                            }
                        }
                    }
                }
            }
            
            // Achievements Section
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary500.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "$unlockedCount unlocked",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary500
                            )
                        }
                    }
                }
            }
            
            // Category Filters
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") }
                        )
                    }
                    items(AchievementCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { 
                                Text(category.name.lowercase().capitalize())
                            }
                        )
                    }
                }
            }
            
            item {
                val filteredAchievements = if (selectedCategory != null) {
                    user.achievements.filter { it.category == selectedCategory }
                } else {
                    user.achievements
                }
                
                val gridHeight = ((filteredAchievements.size / 3 + 1) * 140).dp
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredAchievements) { achievement ->
                        AchievementBadge(
                            achievement = achievement,
                            onClick = { selectedAchievement = achievement }
                        )
                    }
                }
            }
            
            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Achievement Detail Modal
        selectedAchievement?.let { achievement ->
            AchievementDetailModal(
                achievement = achievement,
                onDismiss = { selectedAchievement = null }
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = icon,
                fontSize = 28.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 20.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AchievementBadge(
    achievement: Achievement,
    onClick: () -> Unit = {}
) {
    // Shimmer animation for unlocked badges
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    
    val (backgroundColor, borderColor, borderWidth) = if (achievement.isUnlocked) {
        val baseColor = when (achievement.category) {
            AchievementCategory.TRACKING -> Primary500
            AchievementCategory.SAVING -> Color(0xFF10B981)
            AchievementCategory.COOKING -> Color(0xFFFFB547)
            AchievementCategory.STREAK -> Color(0xFFFF6B6B)
            AchievementCategory.SPECIAL -> Color(0xFF9D7DF2)
        }
        val rarityMultiplier = when (achievement.rarity) {
            AchievementRarity.COMMON -> 1f
            AchievementRarity.RARE -> 1.2f
            AchievementRarity.EPIC -> 1.5f
            AchievementRarity.LEGENDARY -> 2f
        }
        Triple(
            baseColor.copy(alpha = 0.12f * rarityMultiplier),
            baseColor.copy(alpha = 0.4f * shimmerAlpha),
            when (achievement.rarity) {
                AchievementRarity.COMMON -> 1.5.dp
                AchievementRarity.RARE -> 2.dp
                AchievementRarity.EPIC -> 2.5.dp
                AchievementRarity.LEGENDARY -> 3.dp
            }
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            Color.Transparent,
            0.dp
        )
    }
    
    val scale by animateFloatAsState(
        targetValue = when (achievement.rarity) {
            AchievementRarity.COMMON -> 1f
            AchievementRarity.RARE -> 1.05f
            AchievementRarity.EPIC -> 1.08f
            AchievementRarity.LEGENDARY -> 1.12f
        },
        label = "scale"
    )
    
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(if (achievement.isUnlocked) scale else 1f)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (achievement.isUnlocked) 2.dp else 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Achievement icon
                Icon(
                    imageVector = getAchievementIcon(achievement.iconName, achievement.category, achievement.rarity),
                    contentDescription = achievement.title,
                    modifier = Modifier
                        .size(when (achievement.rarity) {
                            AchievementRarity.COMMON -> 32.dp
                            AchievementRarity.RARE -> 36.dp
                            AchievementRarity.EPIC -> 38.dp
                            AchievementRarity.LEGENDARY -> 42.dp
                        })
                        .padding(bottom = 4.dp),
                    tint = if (!achievement.isUnlocked) 
                        Color.Gray.copy(alpha = 0.4f)
                    else
                        getAchievementIconColor(achievement.rarity)
                )
                
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (achievement.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 10.sp,
                    color = if (achievement.isUnlocked)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    maxLines = 2
                )
                
                if (achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${achievement.xpReward}",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary500
                        )
                    }
                } else if (achievement.progress > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = achievement.progress.toFloat() / achievement.target,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = borderColor.copy(alpha = 0.6f),
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentAchievementCard(
    achievement: Achievement,
    onClick: () -> Unit
) {
    val categoryColor = when (achievement.category) {
        AchievementCategory.TRACKING -> Primary500
        AchievementCategory.SAVING -> Color(0xFF10B981)
        AchievementCategory.COOKING -> Color(0xFFFFB547)
        AchievementCategory.STREAK -> Color(0xFFFF6B6B)
        AchievementCategory.SPECIAL -> Color(0xFF9D7DF2)
    }
    
    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = categoryColor.copy(alpha = 0.1f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getAchievementIcon(achievement.iconName, achievement.category, achievement.rarity),
                contentDescription = achievement.title,
                modifier = Modifier.size(36.dp),
                tint = getAchievementIconColor(achievement.rarity)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Primary500.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary500
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementDetailModal(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    val categoryColor = when (achievement.category) {
        AchievementCategory.TRACKING -> Primary500
        AchievementCategory.SAVING -> Color(0xFF10B981)
        AchievementCategory.COOKING -> Color(0xFFFFB547)
        AchievementCategory.STREAK -> Color(0xFFFF6B6B)
        AchievementCategory.SPECIAL -> Color(0xFF9D7DF2)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                // Large icon
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = if (achievement.isUnlocked)
                        getAchievementIconColor(achievement.rarity).copy(alpha = 0.15f)
                    else
                        Color.Gray.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = getAchievementIcon(achievement.iconName, achievement.category, achievement.rarity),
                            contentDescription = achievement.title,
                            modifier = Modifier.size(60.dp),
                            tint = if (!achievement.isUnlocked) 
                                Color.Gray.copy(alpha = 0.5f)
                            else
                                getAchievementIconColor(achievement.rarity)
                        )
                    }
                }
                
                // Title
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                HorizontalDivider()
                
                // Progress or unlock status
                if (achievement.isUnlocked) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "✅", fontSize = 18.sp)
                                Text(
                                    text = "Unlocked",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                        achievement.unlockedDate?.let {
                            Text(
                                text = "Unlocked on: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "How to unlock:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = achievement.unlockCondition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        if (achievement.progress > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Progress",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${achievement.progress}/${achievement.target}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = achievement.progress.toFloat() / achievement.target,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = categoryColor,
                                    trackColor = categoryColor.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
                
                HorizontalDivider()
                
                // XP Reward
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = achievement.category.name.lowercase().capitalize(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = categoryColor
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reward",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Primary500.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "+${achievement.xpReward} XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary500
                                )
                            }
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Rarity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        val rarityColor = when (achievement.rarity) {
                            AchievementRarity.COMMON -> Color.Gray
                            AchievementRarity.RARE -> Color(0xFF3B82F6)
                            AchievementRarity.EPIC -> Color(0xFF9D7DF2)
                            AchievementRarity.LEGENDARY -> Color(0xFFFFB547)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = rarityColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = achievement.rarity.name.lowercase().capitalize(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = rarityColor
                            )
                        }
                    }
                }
            }
        }
    }
}
