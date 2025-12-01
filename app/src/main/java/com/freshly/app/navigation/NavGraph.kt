package com.freshly.app.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freshly.app.ui.screens.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onOnboardingComplete: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onComplete = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    onOnboardingComplete()
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainAppScreen(navController)
        }
        
        composable(Screen.AddItem.route) {
            AddItemScreen(
                onBack = { navController.popBackStack() },
                onItemAdded = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ItemDetails.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onEdit = { /* Handle edit */ }
            )
        }
        
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Recommendations.route) {
            RecommendationsScreen(
                onBack = { navController.popBackStack() },
                onSelectRecipe = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
