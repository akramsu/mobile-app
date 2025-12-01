package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.data.model.Category
import com.freshly.app.ui.components.*
import com.freshly.app.viewmodel.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf(Category.FRIDGE) }
    var searchQuery by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var deletedItemName by remember { mutableStateOf("") }
    
    val items by viewModel.getItemsByCategory(selectedCategory).collectAsState(initial = emptyList())
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            val result = snackbarHostState.showSnackbar(
                message = "$deletedItemName deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                // Handle undo
                viewModel.undoDelete()
            }
            showSnackbar = false
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Pantry",
                onSettingsClick = { /* Navigate to settings */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip(
                    text = "Fridge",
                    emoji = "🧊",
                    selected = selectedCategory == Category.FRIDGE,
                    onClick = { selectedCategory = Category.FRIDGE }
                )
                CategoryChip(
                    text = "Freezer",
                    emoji = "❄️",
                    selected = selectedCategory == Category.FREEZER,
                    onClick = { selectedCategory = Category.FREEZER }
                )
                CategoryChip(
                    text = "Pantry",
                    emoji = "📦",
                    selected = selectedCategory == Category.PANTRY,
                    onClick = { selectedCategory = Category.PANTRY }
                )
            }
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search items...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "No items in ${selectedCategory.name.lowercase()}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = items.filter {
                            searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.id }
                    ) { item ->
                        PantryItemRow(
                            item = item,
                            onClick = { /* Navigate to item details */ },
                            onDelete = {
                                deletedItemName = item.name
                                viewModel.deleteItem(item.id)
                                showSnackbar = true
                            }
                        )
                    }
                }
            }
        }
    }
}
