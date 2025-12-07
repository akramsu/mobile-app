package com.freshly.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.navigation.MainTab
import com.freshly.app.ui.theme.Primary500

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun BottomNav(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem(MainTab.Pantry.route, "Pantry", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
        NavItem(MainTab.AIChef.route, "Chef", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
        NavItem(MainTab.Home.route, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem(MainTab.AIAssistant.route, "AI", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
        NavItem(MainTab.Profile.route, "Profile", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
    )
    
    Box(
        modifier = modifier.wrapContentWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Main navigation pill
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(50.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(50.dp),
            color = Color.White,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isHomeButton = item.route == MainTab.Home.route
                    val isSelected = selectedTab == item.route
                    
                    if (isHomeButton) {
                        // Special center home button with circular design
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Surface(
                                onClick = { onTabSelected(item.route) },
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        clip = false
                                    ),
                                shape = CircleShape,
                                color = if (isSelected) Primary500 else Color(0xFF1E293B)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(26.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Regular navigation items
                        Surface(
                            onClick = { onTabSelected(item.route) },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp),
                            shape = RoundedCornerShape(50.dp),
                            color = Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = if (isSelected) Color.Black else Color(0xFF9CA3AF)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else Color(0xFF9CA3AF),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
