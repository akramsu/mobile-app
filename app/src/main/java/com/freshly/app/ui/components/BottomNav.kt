package com.freshly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.navigation.MainTab
import com.freshly.app.ui.theme.Primary500

data class NavItem(
    val route: String,
    val label: String,
    val icon: Any // Can be ImageVector or String (emoji)
)

@Composable
fun BottomNav(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(MainTab.Home.route, "Home", Icons.Default.Home),
        NavItem(MainTab.Pantry.route, "Pantry", "📦"),
        NavItem(MainTab.AIChef.route, "Chef", "👨‍🍳"),
        NavItem(MainTab.Analytics.route, "Analytics", "📊"),
        NavItem(MainTab.Profile.route, "Profile", Icons.Default.Person)
    )
    
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.route,
                onClick = { onTabSelected(item.route) },
                icon = {
                    when (val icon = item.icon) {
                        is ImageVector -> {
                            Icon(
                                imageVector = icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is String -> {
                            Text(
                                text = icon,
                                fontSize = 24.sp
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary500,
                    selectedTextColor = Primary500,
                    indicatorColor = Primary500.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
