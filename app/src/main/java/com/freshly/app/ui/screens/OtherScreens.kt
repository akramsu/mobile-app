package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.components.PrimaryButton

@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onItemAdded: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Item",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Add Item Screen - Implement form with camera, date pickers, etc.")
            
            PrimaryButton(
                text = "Save Item",
                onClick = onItemAdded
            )
        }
    }
}

@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Item Details",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Item Details Screen for item: $itemId")
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Recipe",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Recipe Details Screen for recipe: $recipeId")
        }
    }
}

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notifications",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Notifications Screen - List of expiry alerts")
        }
    }
}

@Composable
fun RecommendationsScreen(
    onBack: () -> Unit,
    onSelectRecipe: (String) -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Recommendations",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "AI Recommendations Screen")
        }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = "• Notifications")
            Text(text = "• Display Options")
            Text(text = "• Dietary Restrictions")
            Text(text = "• Region Selection")
            Text(text = "• About")
        }
    }
}
