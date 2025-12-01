package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.ui.components.*
import com.freshly.app.ui.theme.AI
import com.freshly.app.viewmodel.AIChefViewModel
import kotlinx.coroutines.launch

@Composable
fun AIChefScreen(
    onRecipeClick: (String) -> Unit = {},
    viewModel: AIChefViewModel = viewModel()
) {
    val pantryItems by viewModel.pantryItems.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            AppTopBar(title = "AI Chef")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (recipes.isEmpty()) {
                // Item selection screen
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Select Ingredients",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose items you want to use",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    items(pantryItems) { item ->
                        FreshlyCard(
                            modifier = Modifier.fillMaxWidth(),
                            border = if (selectedItems.contains(item.name))
                                androidx.compose.foundation.BorderStroke(2.dp, AI)
                            else null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedItems.contains(item.name),
                                    onCheckedChange = { viewModel.toggleItem(item.name) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                ItemImage(
                                    imageUrl = item.imageUrl,
                                    name = item.name,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (item.getDaysUntilExpiry() <= 3) {
                                        Text(
                                            text = "⚡ Expiring soon",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Generate button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = "Generate Recipes",
                        onClick = {
                            scope.launch {
                                viewModel.generateRecipes()
                            }
                        },
                        enabled = selectedItems.isNotEmpty(),
                        loading = isLoading
                    )
                }
            } else {
                // Recipe results
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recipe Suggestions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            SecondaryButton(
                                text = "New Search",
                                onClick = { viewModel.reset() },
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }
                    
                    items(recipes) { recipe ->
                        FreshlyCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onRecipeClick(recipe.id) }
                        ) {
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = recipe.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "⏱️ ${recipe.cookTime} min",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "🍽️ ${recipe.servings} servings",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "✅ ${recipe.matchedIngredients.size} matched",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AI
                                )
                            }
                        }
                    }
                }
            }
            
            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = AI)
                        Text(
                            text = "Generating recipes...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AI
                        )
                    }
                }
            }
        }
    }
}
