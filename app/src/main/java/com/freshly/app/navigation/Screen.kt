package com.freshly.app.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object SignUp : Screen("sign_up")
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object Home : Screen("home")
    object Pantry : Screen("pantry")
    object AIChef : Screen("ai_chef")
    object Profile : Screen("profile")
    object AddItem : Screen("add_item")
    object RecipeDetail : Screen("recipe_detail/{recipeId}?generateAI={generateAI}") {
        fun createRoute(recipeId: String, generateAI: Boolean = false) = "recipe_detail/$recipeId?generateAI=$generateAI"
    }
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object SavedRecipes : Screen("saved_recipes")
}

sealed class MainTab(val route: String) {
    object Home : MainTab("home")
    object Pantry : MainTab("pantry")
    object AIChef : MainTab("ai_chef")
    object AIAssistant : MainTab("ai_assistant")
    object Profile : MainTab("profile")
}
