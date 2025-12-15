package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.freshly.app.navigation.Screen
import androidx.navigation.compose.rememberNavController
import com.freshly.app.navigation.MainTab
import com.freshly.app.ui.components.BottomNav

@Composable
fun MainAppScreen(
    mainNavController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(MainTab.Home.route) }
    val innerNavController = rememberNavController()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                MainTab.Home.route -> HomeScreen(
                    onAddItem = { mainNavController.navigate("add_item") },
                    onNotifications = { mainNavController.navigate("notifications") },
                    onSearchClick = { selectedTab = MainTab.Pantry.route },
                    onRecipeClick = { recipeId -> mainNavController.navigate("recipe_detail/$recipeId") }
                )
                MainTab.Pantry.route -> PantryScreen(
                    onAddItem = { mainNavController.navigate("add_item") }
                )
                MainTab.AIChef.route -> AIChefScreen(
                    onRecipeClick = { recipeId, generateAI -> 
                        mainNavController.navigate(Screen.RecipeDetail.createRoute(recipeId, generateAI))
                    }
                )
                MainTab.AIAssistant.route -> AIAssistantScreen()
                MainTab.Profile.route -> ProfileScreen(
                    onSettingsClick = { mainNavController.navigate("settings") },
                    onEditProfileClick = { mainNavController.navigate("edit_profile") }
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    }
}
