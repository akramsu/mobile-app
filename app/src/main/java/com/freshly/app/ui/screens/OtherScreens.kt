package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.data.model.Category
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.components.PrimaryButton
import com.freshly.app.ui.components.SecondaryButton
import com.freshly.app.ui.theme.AI500
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.ui.theme.Warning500
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onItemAdded: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fridge") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    var purchaseDate by remember { mutableStateOf(today) }
    var expiryDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("items") }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add Item",
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Photo Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AI500.copy(alpha = 0.05f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = AI500,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "📸", fontSize = 40.sp)
                            Text(
                                text = "AI Detect Photo",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AI500
                            )
                            Text(
                                text = "Take a photo to auto-fill details",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { /* TODO: Camera */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary500
                                )
                            ) {
                                Text("Take Photo")
                            }
                        }
                    }
                }
            }
            
            // Item Name
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Item Name *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Organic Milk") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Primary500
                        )
                    )
                }
            }
            
            // Category
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Primary500
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Fridge", "Freezer", "Pantry").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        category = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Purchase Date
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Purchase Date",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("mm/dd/yyyy") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Primary500
                        )
                    )
                }
            }
            
            // Expiry Date
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Expiry Date *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("mm/dd/yyyy") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Primary500
                        )
                    )
                }
            }
            
            // Quantity and Unit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quantity
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Quantity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Primary500
                            )
                        )
                    }
                    
                    // Unit
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Unit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        var unitExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded }
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Primary500
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                listOf("items", "kg", "liters", "g").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            unit = option
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "Save Item",
                        onClick = {
                            if (name.isNotBlank() && expiryDate.isNotBlank()) {
                                onItemAdded()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
    val recipe = RecipeDetail(
        name = "Creamy Blueberry Pancakes",
        time = 20,
        difficulty = "Easy",
        servings = 2,
        rating = 4.8f,
        reviews = 124,
        image = "🥞",
        ingredients = listOf(
            Ingredient("Flour", 1, "cup", true),
            Ingredient("Milk", 1, "cup", true),
            Ingredient("Blueberries", 1, "cup", true),
            Ingredient("Eggs", 2, "whole", true),
            Ingredient("Vanilla", 1, "tsp", false)
        ),
        steps = listOf(
            RecipeStep(1, "Combine 1 cup flour, 2 tbsp sugar, 2 tsp baking powder"),
            RecipeStep(2, "Mix 1 cup milk with 2 eggs and 1 tsp vanilla"),
            RecipeStep(3, "Fold wet ingredients into dry, do not overmix"),
            RecipeStep(4, "Gently fold in 1 cup fresh blueberries"),
            RecipeStep(5, "Cook on buttered griddle until golden brown")
        ),
        tips = "Use fresh blueberries for best flavor. Cook on medium heat to prevent burning."
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // App Top Bar
        AppTopBar(
            title = "Recipe",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Recipe Image Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF9D7DF2),
                                    Color(0xFF7c3aed)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = recipe.image,
                        fontSize = 72.sp
                    )
                }
            }

            // Content Section
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title & Meta
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = recipe.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "⏱️ ${recipe.time} min",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "📊 ${recipe.difficulty}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "👥 ${recipe.servings} servings",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "★ ${recipe.rating}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB547)
                        )
                        Text(
                            text = "(${recipe.reviews} reviews)",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // Ingredients Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Ingredients",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recipe.ingredients.forEach { ingredient ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (ingredient.available) 
                                        Primary500.copy(alpha = 0.1f) 
                                    else 
                                        Color(0xFFF5F5F5)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (ingredient.available) "✓" else "○",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ingredient.available) Primary500 else Color.Gray
                                        )
                                        Text(
                                            text = "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Instructions Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Instructions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            recipe.steps.forEach { step ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Primary500, shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = step.number.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = step.instruction,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chef's Tips
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(60.dp)
                                    .background(AI500, shape = RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "💡 CHEF'S TIPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AI500,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = recipe.tips,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrimaryButton(
                            text = "👨‍🍳 Cook Now",
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "💾 Save",
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

data class RecipeDetail(
    val name: String,
    val time: Int,
    val difficulty: String,
    val servings: Int,
    val rating: Float,
    val reviews: Int,
    val image: String,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val tips: String
)

data class Ingredient(
    val name: String,
    val quantity: Int,
    val unit: String,
    val available: Boolean
)

data class RecipeStep(
    val number: Int,
    val instruction: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                NotificationItem(
                    id = "1",
                    title = "Item Expiring Soon",
                    message = "Your milk expires in 2 days. Consider using it in a recipe!",
                    type = NotificationType.EXPIRING,
                    timestamp = "2 hours ago",
                    icon = "🥛",
                    read = false
                ),
                NotificationItem(
                    id = "2",
                    title = "Recipe Saved",
                    message = "You saved 'Tomato Pasta' to your favorites",
                    type = NotificationType.SAVED,
                    timestamp = "5 hours ago",
                    icon = "🍝",
                    read = false
                ),
                NotificationItem(
                    id = "3",
                    title = "New Recipe Available",
                    message = "AI Chef recommends 'Berry Smoothie Bowl' based on your pantry",
                    type = NotificationType.RECIPE,
                    timestamp = "1 day ago",
                    icon = "🥤",
                    read = true
                ),
                NotificationItem(
                    id = "4",
                    title = "Streak Milestone",
                    message = "Congratulations! You've reached a 15-day streak!",
                    type = NotificationType.STREAK,
                    timestamp = "2 days ago",
                    icon = "🔥",
                    read = true
                ),
                NotificationItem(
                    id = "5",
                    title = "Multiple Items Expiring",
                    message = "Berries and yogurt expire tomorrow. Create a recipe now!",
                    type = NotificationType.EXPIRING,
                    timestamp = "3 days ago",
                    icon = "🫐",
                    read = true
                )
            )
        )
    }

    val unreadCount = notifications.count { !it.read }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Top Bar with subtitle and actions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Text(text = "←", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount new",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "All caught up",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                if (unreadCount > 0) {
                    TextButton(
                        onClick = {
                            notifications = notifications.map { it.copy(read = true) }
                        }
                    ) {
                        Text(
                            text = "Mark all",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary500
                        )
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🔔", fontSize = 48.sp, color = Color.Gray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notifications yet",
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "You'll get notifications about expiring items and new recipes",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications.size) { index ->
                    val notification = notifications[index]
                    NotificationCard(
                        notification = notification,
                        onMarkAsRead = {
                            notifications = notifications.map {
                                if (it.id == notification.id) it.copy(read = true) else it
                            }
                        },
                        onDelete = {
                            notifications = notifications.filter { it.id != notification.id }
                        }
                    )
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

enum class NotificationType {
    EXPIRING, SAVED, RECIPE, STREAK
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val icon: String,
    val read: Boolean
)

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor: Color
    val borderColor: Color
    
    when (notification.type) {
        NotificationType.EXPIRING -> {
            backgroundColor = Warning500.copy(alpha = 0.05f)
            borderColor = Warning500
        }
        NotificationType.RECIPE -> {
            backgroundColor = AI500.copy(alpha = 0.05f)
            borderColor = AI500
        }
        NotificationType.STREAK -> {
            backgroundColor = Primary500.copy(alpha = 0.05f)
            borderColor = Primary500
        }
        NotificationType.SAVED -> {
            backgroundColor = Primary500.copy(alpha = 0.05f)
            borderColor = Primary500
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (!notification.read) Color(0xFFF9FAFB) else backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Box {
            // Left colored border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
                    .align(Alignment.CenterStart)
            )
            
            // Unread indicator dot
            if (!notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Primary500, shape = CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 12.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon
                Text(
                    text = notification.icon,
                    fontSize = 32.sp
                )
                
                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = notification.timestamp,
                        fontSize = 12.sp,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
                
                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!notification.read) {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text(text = "✓", fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(text = "🗑️", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationsScreen(
    onBack: () -> Unit,
    onSelectRecipe: (String) -> Unit
) {
    val recommendations = listOf(
        RecipeRecommendation(
            id = "1",
            name = "Creamy Blueberry Pancakes",
            time = 20,
            difficulty = "Easy",
            rating = 4.8f,
            image = "🥞",
            description = "Perfect way to use up those blueberries before they spoil",
            matchedItems = 4,
            totalItems = 5
        ),
        RecipeRecommendation(
            id = "2",
            name = "Vegetable Stir Fry",
            time = 15,
            difficulty = "Easy",
            rating = 4.6f,
            image = "🥘",
            description = "Great for using mixed vegetables in your pantry",
            matchedItems = 7,
            totalItems = 8
        ),
        RecipeRecommendation(
            id = "3",
            name = "Pasta Carbonara",
            time = 25,
            difficulty = "Medium",
            rating = 4.9f,
            image = "🍝",
            description = "Classic Italian dish with items you already have",
            matchedItems = 6,
            totalItems = 7
        ),
        RecipeRecommendation(
            id = "4",
            name = "Tomato Soup",
            time = 30,
            difficulty = "Easy",
            rating = 4.5f,
            image = "🍲",
            description = "Comfort food to use up expiring tomatoes",
            matchedItems = 5,
            totalItems = 6
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "AI Recommendations",
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Text(
                    text = "Based on your pantry, here are personalized recipes to help you reduce food waste and enjoy your ingredients.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }

            // Recipe Cards
            items(recommendations.size) { index ->
                val recipe = recommendations[index]
                RecipeRecommendationCard(
                    recipe = recipe,
                    onClick = { onSelectRecipe(recipe.id) }
                )
            }

            // Smart Tip
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(60.dp)
                                .background(
                                    AI500,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "💡 SMART TIP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AI500,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Recipes with higher match percentages use more of your available ingredients, helping reduce waste.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

data class RecipeRecommendation(
    val id: String,
    val name: String,
    val time: Int,
    val difficulty: String,
    val rating: Float,
    val image: String,
    val description: String,
    val matchedItems: Int,
    val totalItems: Int
)

@Composable
fun RecipeRecommendationCard(
    recipe: RecipeRecommendation,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe Image/Emoji
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Primary500.copy(alpha = 0.2f),
                                Primary500.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recipe.image,
                    fontSize = 32.sp
                )
            }

            // Recipe Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Name
                Text(
                    text = recipe.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Description
                Text(
                    text = recipe.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                // Time, Difficulty, Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ ${recipe.time} min",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "📊 ${recipe.difficulty}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "★ ${recipe.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFB547)
                    )
                }

                // Match Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                Color(0xFFE5E5E5),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(recipe.matchedItems.toFloat() / recipe.totalItems.toFloat())
                                .background(
                                    Primary500,
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    Text(
                        text = "${recipe.matchedItems}/${recipe.totalItems}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = onBack
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { com.freshly.app.utils.PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    
    var expiryReminders by remember { mutableStateOf(true) }
    var soundEffects by remember { mutableStateOf(true) }
    val darkModeFlow by preferencesManager.darkMode.collectAsState(initial = false)
    var darkMode by remember { mutableStateOf(darkModeFlow) }
    
    LaunchedEffect(darkModeFlow) {
        darkMode = darkModeFlow
    }
    
    var dietaryRestrictions by remember { mutableStateOf(setOf("vegetarian")) }
    var region by remember { mutableStateOf("US") }
    var privateProfile by remember { mutableStateOf(false) }
    var twoFactor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings",
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Notifications Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Notifications",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Expiry Reminders
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Expiry Reminders",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Get alerts when items are expiring",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = expiryReminders,
                                onCheckedChange = { expiryReminders = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary500
                                )
                            )
                        }
                    }
                    
                    // Sound Effects
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sound Effects",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Play notification sounds",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = soundEffects,
                                onCheckedChange = { soundEffects = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary500
                                )
                            )
                        }
                    }
                }
            }

            // Display Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Brightness6,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Display",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dark Mode",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Enable dark theme",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Switch(
                                checked = darkMode,
                                onCheckedChange = { 
                                    scope.launch {
                                        preferencesManager.setDarkMode(it)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary500
                                )
                            )
                        }
                    }
                }
            }

            // About & Help Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "About",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // App Version Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "App Version",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "1.2.0",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Built with",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Kotlin & Jetpack Compose",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Surface(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF5F5),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5757)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFF5757)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            color = Color(0xFFFF5757),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Bottom spacing for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
