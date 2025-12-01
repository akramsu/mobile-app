package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freshly.app.data.model.Category
import com.freshly.app.ui.components.*
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.viewmodel.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    onAddItem: () -> Unit = {},
    viewModel: PantryViewModel = viewModel()
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var deletedItemName by remember { mutableStateOf("") }
    
    val allItems by viewModel.repository.items.collectAsState(initial = emptyList())
    val filteredItems = if (selectedCategory == null) {
        allItems
    } else {
        allItems.filter { it.category == selectedCategory }
    }
    
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = Primary500,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item"
                )
            }
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
                    text = "All",
                    emoji = "📋",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
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
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    val categoryName = selectedCategory?.name?.lowercase() ?: "pantry"
                    Text(
                        text = if (selectedCategory == null) "No items in pantry" else "No items in $categoryName",
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
                        items = filteredItems.filter {
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
