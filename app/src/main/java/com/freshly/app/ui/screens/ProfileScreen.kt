package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.data.model.Achievement
import com.freshly.app.ui.components.*
import com.freshly.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ImageAvatar(
                        imageUrl = user.avatarUrl,
                        name = user.name,
                        size = 96.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Level ${user.level}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.freshly.app.ui.theme.Primary500
                            )
                            Text(
                                text = "${user.xp} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${user.streak} 🔥",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Day Streak",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // XP Progress
            item {
                XPProgressBar(
                    currentXP = user.xp % 1000,
                    maxXP = 1000,
                    level = user.level
                )
            }
            
            // Achievements Section
            item {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(user.achievements) { achievement ->
                        AchievementBadge(achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementBadge(achievement: Achievement) {
    FreshlyCard(
        backgroundColor = if (achievement.isUnlocked)
            com.freshly.app.ui.theme.Primary500.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = achievement.iconName,
                style = MaterialTheme.typography.displaySmall,
                color = if (achievement.isUnlocked) Color.Unspecified else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            if (achievement.isUnlocked) {
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = com.freshly.app.ui.theme.Primary500
                )
            }
        }
    }
}
