package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.data.model.Achievement
import com.freshly.app.ui.components.*
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val unlockedCount = user.achievements.count { it.isUnlocked }
    val totalCount = user.achievements.size
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Profile",
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
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
                                    text = "⭐",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Level ${user.level}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
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
                        icon = "✨",
                        value = "${user.xp}",
                        label = "Total XP",
                        color = Primary500
                    )
                    
                    // Streak Card
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🔥",
                        value = "${user.streak}",
                        label = "Day Streak",
                        color = Color(0xFFFF6B6B)
                    )
                    
                    // Achievements Card
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = "🏆",
                        value = "$unlockedCount/$totalCount",
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
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(user.achievements) { achievement ->
                        AchievementBadge(achievement)
                    }
                }
            }
            
            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
fun AchievementBadge(achievement: Achievement) {
    val backgroundColor = if (achievement.isUnlocked) {
        when (achievement.category) {
            com.freshly.app.data.model.AchievementCategory.TRACKING -> Primary500.copy(alpha = 0.12f)
            com.freshly.app.data.model.AchievementCategory.SAVING -> Color(0xFF10B981).copy(alpha = 0.12f)
            com.freshly.app.data.model.AchievementCategory.COOKING -> Color(0xFFFFB547).copy(alpha = 0.12f)
            com.freshly.app.data.model.AchievementCategory.STREAK -> Color(0xFFFF6B6B).copy(alpha = 0.12f)
            com.freshly.app.data.model.AchievementCategory.SPECIAL -> Color(0xFF9D7DF2).copy(alpha = 0.12f)
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val borderColor = if (achievement.isUnlocked) {
        when (achievement.category) {
            com.freshly.app.data.model.AchievementCategory.TRACKING -> Primary500.copy(alpha = 0.3f)
            com.freshly.app.data.model.AchievementCategory.SAVING -> Color(0xFF10B981).copy(alpha = 0.3f)
            com.freshly.app.data.model.AchievementCategory.COOKING -> Color(0xFFFFB547).copy(alpha = 0.3f)
            com.freshly.app.data.model.AchievementCategory.STREAK -> Color(0xFFFF6B6B).copy(alpha = 0.3f)
            com.freshly.app.data.model.AchievementCategory.SPECIAL -> Color(0xFF9D7DF2).copy(alpha = 0.3f)
        }
    } else {
        Color.Transparent
    }
    
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (achievement.isUnlocked) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        tonalElevation = if (achievement.isUnlocked) 2.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievement.iconName,
                fontSize = 36.sp,
                color = if (achievement.isUnlocked) Color.Unspecified else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (achievement.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary500
                    )
                }
            }
        }
    }
}
