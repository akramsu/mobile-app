package com.freshly.app.navigation

sealed class Screen(val route: String) {
    object AuthLoading : Screen("auth_loading")
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Home : Screen("home")
    object Pantry : Screen("pantry")
    object AIChef : Screen("ai_chef")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object AddItem : Screen("add_item")
    object ItemDetails : Screen("item_details/{itemId}") {
        fun createRoute(itemId: String) = "item_details/$itemId"
    }
    object RecipeDetail : Screen("recipe_detail/{recipeId}") {
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    object Notifications : Screen("notifications")
    object Recommendations : Screen("recommendations")
    object Settings : Screen("settings")
    object FirebaseDebug : Screen("firebase_debug")
}

sealed class MainTab(val route: String) {
    object Home : MainTab("home")
    object Pantry : MainTab("pantry")
    object AIChef : MainTab("ai_chef")
    object AIAssistant : MainTab("ai_assistant")
    object Profile : MainTab("profile")
}
