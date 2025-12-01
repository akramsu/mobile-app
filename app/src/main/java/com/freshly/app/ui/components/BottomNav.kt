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
        NavItem(MainTab.Home.route, "Home", Icons.Outlined.Home, Icons.Filled.Home),
        NavItem(MainTab.Pantry.route, "Pantry", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart),
        NavItem(MainTab.AIChef.route, "Chef", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
        NavItem(MainTab.Analytics.route, "Analytics", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        NavItem(MainTab.Profile.route, "Profile", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
    )
    
    // Rounded pill navigation bar
    Surface(
        modifier = modifier
            .wrapContentWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(50.dp),
                clip = false
            ),
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.route
                
                Surface(
                    onClick = { onTabSelected(item.route) },
                    modifier = Modifier
                        .defaultMinSize(minWidth = 50.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) Color.Black else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.Black else Color(0xFF6B7280),
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}
