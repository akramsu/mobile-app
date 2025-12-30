package com.freshly.app.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freshly.app.ui.screens.*
import com.freshly.app.ui.screens.auth.SignInScreen
import com.freshly.app.ui.screens.auth.SignUpScreen
import com.freshly.app.viewmodel.PantryViewModel

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
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.popBackStack()
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
            val pantryViewModel: PantryViewModel = viewModel()
            AddItemScreen(
                viewModel = pantryViewModel,
                onBack = { navController.popBackStack() },
                onItemAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType },
                navArgument("generateAI") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val generateAI = backStackEntry.arguments?.getBoolean("generateAI") ?: false
            RecipeDetailScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                generateAIContent = generateAI
            )
        }
        
        composable(Screen.Notifications.route) {
            val pantryViewModel: PantryViewModel = viewModel()
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                viewModel = pantryViewModel
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    // Sign out from Firebase
                    com.freshly.app.data.firebase.FirebaseManager.signOut()
                    // Navigate to splash which will redirect to sign in
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.SavedRecipes.route) {
            SavedRecipesScreen(
                onBack = { navController.popBackStack() },
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                }
            )
        }
        
        composable("quiz") {
            QuizScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
