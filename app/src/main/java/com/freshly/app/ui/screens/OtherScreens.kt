package com.freshly.app.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.freshly.app.data.model.Category
import com.freshly.app.data.model.PantryItem
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.components.ImageAvatar
import com.freshly.app.ui.components.PrimaryButton
import com.freshly.app.ui.components.SecondaryButton
import com.freshly.app.ui.theme.AI500
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.ui.theme.Warning500
import com.freshly.app.utils.TextExtractor
import com.freshly.app.viewmodel.PantryViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddItemScreen(
    viewModel: PantryViewModel,
    onBack: () -> Unit,
    onItemAdded: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fridge") }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    var purchaseDate by remember { mutableStateOf(today) }
    var expiryDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("items") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isProcessingImage by remember { mutableStateOf(false) }
    var extractionConfidence by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val textExtractor = remember { TextExtractor(context) }
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Create image URI for camera
    val photoUri = remember {
        val photoFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }
    
    // Camera launcher with OCR processing
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = photoUri
            isProcessingImage = true
            
            // Process image with ML Kit OCR
            scope.launch {
                try {
                    val extractedInfo = textExtractor.extractTextFromImage(photoUri)
                    
                    // Auto-fill form fields with extracted data
                    extractedInfo.name?.let { name = it }
                    extractedInfo.expiryDate?.let { expiryDate = it }
                    extractedInfo.quantity?.let { quantity = it }
                    extractedInfo.unit?.let { unit = it }
                    extractedInfo.category?.let { category = it }
                    extractionConfidence = extractedInfo.confidence
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    extractionConfidence = 0f
                } finally {
                    isProcessingImage = false
                }
            }
        }
    }
    
    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    
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
            // Photo Capture Section with Smart Text Extraction
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Scan Product Label 📸",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (imageUri != null) {
                        // Show captured image with processing overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.LightGray)
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Captured product label",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Processing overlay
                            if (isProcessingImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.7f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = Primary500,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "Scanning label...",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            // Re-scan button
                            IconButton(
                                onClick = {
                                    if (cameraPermissionState.status.isGranted) {
                                        cameraLauncher.launch(photoUri)
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Re-scan label",
                                    tint = Primary500
                                )
                            }
                            
                            // Confidence indicator
                            if (!isProcessingImage && extractionConfidence > 0f) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        extractionConfidence >= 0.7f -> Color(0xFF4CAF50).copy(alpha = 0.9f)
                                        extractionConfidence >= 0.4f -> Color(0xFFFFC107).copy(alpha = 0.9f)
                                        else -> Color(0xFFFF9800).copy(alpha = 0.9f)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                extractionConfidence >= 0.7f -> Icons.Outlined.CheckCircle
                                                extractionConfidence >= 0.4f -> Icons.Outlined.Info
                                                else -> Icons.Outlined.Warning
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = when {
                                                extractionConfidence >= 0.7f -> "High confidence"
                                                extractionConfidence >= 0.4f -> "Medium - verify info"
                                                else -> "Low - check details"
                                            },
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Show camera capture button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clickable {
                                    if (cameraPermissionState.status.isGranted) {
                                        cameraLauncher.launch(photoUri)
                                    } else {
                                        cameraPermissionState.launchPermissionRequest()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = AI500.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = AI500.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = "Scan product",
                                    modifier = Modifier.size(48.dp),
                                    tint = AI500
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Scan Product Label",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AI500
                                )
                                Text(
                                    text = "Auto-fill details from packaging",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    // Info helper text
                    if (imageUri == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = AI500.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Capture the product label to auto-fill name, expiry date, and more",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp
                            )
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
            
            // Expiry Date with Date Picker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Expiry Date *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        placeholder = { Text("Select expiry date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = Primary500
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Primary500,
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = false
                    )
                }
                
                // Date Picker Dialog
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = Date(millis)
                                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        expiryDate = formatter.format(date)
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
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
                                scope.launch {
                                    val newItem = PantryItem(
                                        id = UUID.randomUUID().toString(),
                                        name = name,
                                        category = when(category) {
                                            "Fridge" -> Category.FRIDGE
                                            "Freezer" -> Category.FREEZER
                                            "Pantry" -> Category.PANTRY
                                            else -> Category.PANTRY
                                        },
                                        quantity = quantity.toIntOrNull() ?: 1,
                                        unit = unit,
                                        addedDate = purchaseDate,
                                        expiryDate = expiryDate,
                                        imageUrl = imageUri?.toString(),
                                        notes = null
                                    )
                                    viewModel.repository.addItem(newItem)
                                    onItemAdded()
                                }
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
private fun AIRecipeContentCard(aiContent: String) {
    val parsedData = remember(aiContent) {
        try {
            val jsonContent = aiContent
                .replace("```json", "")
                .replace("```", "")
                .trim()
                .let {
                    val start = it.indexOf('{')
                    val end = it.lastIndexOf('}')
                    if (start >= 0 && end > start) it.substring(start, end + 1) else it
                }
            
            val json = org.json.JSONObject(jsonContent)
            Triple(
                json.optString("description", ""),
                json.optJSONArray("steps"),
                json.optJSONArray("tips")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    if (parsedData != null) {
        val (description, stepsArray, tipsArray) = parsedData
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = AI500.copy(alpha = 0.05f),
            border = androidx.compose.foundation.BorderStroke(1.dp, AI500.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "AI Recipe Guide",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AI500
                    )
                }
                
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
                
                if (stepsArray != null && stepsArray.length() > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Quick Steps:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        for (i in 0 until stepsArray.length()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${i + 1}.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AI500
                                )
                                Text(
                                    text = stepsArray.getString(i),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                if (tipsArray != null && tipsArray.length() > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "💡 Tips:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        for (i in 0 until tipsArray.length()) {
                            Text(
                                text = "• ${tipsArray.getString(i)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Fallback if JSON parsing fails
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = AI500.copy(alpha = 0.05f)
        ) {
            Text(
                text = aiContent,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    generateAIContent: Boolean = false
) {
    val recipeRepository = remember { com.freshly.app.data.repository.RecipeRepository.getInstance() }
    val geminiService = remember { com.freshly.app.data.api.GeminiApiService() }
    var recipe by remember { mutableStateOf<com.freshly.app.data.model.Recipe?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var aiGeneratedContent by remember { mutableStateOf<String?>(null) }
    var isGeneratingAI by remember { mutableStateOf(false) }
    
    LaunchedEffect(recipeId) {
        isLoading = true
        recipe = recipeRepository.getRecipeById(recipeId)
        isLoading = false
        
        // If generateAIContent is true and recipe is found, generate AI content
        if (generateAIContent && recipe != null) {
            isGeneratingAI = true
            val ingredients = recipe!!.matchedIngredients.ifEmpty { 
                recipe!!.ingredients.map { it.name }
            }
            val result = geminiService.generateDetailedRecipe(
                recipe!!.title,
                ingredients
            )
            if (result.isSuccess) {
                aiGeneratedContent = result.getOrNull()
            }
            isGeneratingAI = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Recipe not found")
                PrimaryButton(
                    text = "Go Back",
                    onClick = onBack
                )
            }
        }
        return
    }
    
    val currentRecipe = recipe!!

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
                        text = currentRecipe.imageUrl.ifEmpty { "🍽️" },
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
                            text = currentRecipe.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentRecipe.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "⏱️ ${currentRecipe.cookTime} min",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "📊 ${currentRecipe.difficulty}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "👥 ${currentRecipe.servings} servings",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    // Tags
                    if (currentRecipe.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            currentRecipe.tags.take(3).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AI500.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        color = AI500,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    
                    // AI-Generated Content Section
                    if (generateAIContent) {
                        if (isGeneratingAI) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = AI500.copy(alpha = 0.05f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AI500,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "✨ AI is creating your recipe details...",
                                        fontSize = 14.sp,
                                        color = AI500,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else if (aiGeneratedContent != null) {
                            AIRecipeContentCard(aiGeneratedContent!!)
                        }
                    }

                    // Ingredients Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Ingredients",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentRecipe.ingredients.forEach { ingredient ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (ingredient.isMatched) 
                                        Primary500.copy(alpha = 0.1f) 
                                    else 
                                        Color(0xFFF5F5F5)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (ingredient.isMatched) "✓" else "○",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ingredient.isMatched) Primary500 else Color.Gray
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ingredient.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = ingredient.amount,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
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
                            currentRecipe.steps.forEachIndexed { index, step ->
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
                                                .size(28.dp)
                                                .background(Primary500, shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (index + 1).toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = step,
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
                    
                    // Save Recipe Button
                    PrimaryButton(
                        text = "Save Recipe",
                        onClick = { /* TODO: Implement save functionality */ },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Chef's Tips (if available)
                    currentRecipe.description.takeIf { it.isNotEmpty() }?.let { tips ->
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
                                        text = "💡 ABOUT THIS RECIPE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AI500,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = tips,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
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
    onBack: () -> Unit,
    viewModel: PantryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val allItems by viewModel.repository.items.collectAsState(initial = emptyList())
    
    // Generate notifications from expiring items
    val notifications = remember(allItems) {
        val expiringItems = allItems.filter { item ->
            val daysUntilExpiry = item.getDaysUntilExpiry()
            daysUntilExpiry in 0..7
        }.sortedBy { it.getDaysUntilExpiry() }
        
        expiringItems.mapIndexed { index, item ->
            val days = item.getDaysUntilExpiry()
            val (title, message, type) = when {
                days == 0 -> Triple(
                    "Item Expires TODAY!",
                    "${item.name} expires today. Use it now or it will spoil!",
                    NotificationType.CRITICAL
                )
                days == 1 -> Triple(
                    "Item Expires Tomorrow",
                    "${item.name} expires tomorrow. Plan to use it soon!",
                    NotificationType.EXPIRING
                )
                days in 2..3 -> Triple(
                    "Item Expiring Soon",
                    "${item.name} expires in $days days. Consider using it in a recipe!",
                    NotificationType.EXPIRING
                )
                else -> Triple(
                    "Upcoming Expiry",
                    "${item.name} expires in $days days.",
                    NotificationType.INFO
                )
            }
            
            val icon = when (item.category.name.lowercase()) {
                "fridge" -> "🧊"
                "freezer" -> "❄️"
                "pantry" -> "📦"
                else -> "🍎"
            }
            
            NotificationItem(
                id = item.id,
                title = title,
                message = message,
                type = type,
                timestamp = when {
                    days == 0 -> "Today"
                    days == 1 -> "Tomorrow"
                    else -> "In $days days"
                },
                icon = icon,
                itemName = item.name,
                daysUntilExpiry = days,
                read = false
            )
        }
    }

    val unreadCount = notifications.size

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
                                text = "$unreadCount items expiring",
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
                        notification = notification
                    )
                }
                
                // Info message at bottom
                item {
                    Text(
                        text = "💡 Tip: Items are automatically removed when they expire or are used",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
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
    CRITICAL, EXPIRING, INFO, SAVED, RECIPE, STREAK
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val icon: String,
    val itemName: String = "",
    val daysUntilExpiry: Int = 0,
    val read: Boolean
)

@Composable
fun NotificationCard(
    notification: NotificationItem
) {
    val (backgroundColor, borderColor) = when (notification.type) {
        NotificationType.CRITICAL -> Pair(Color(0xFFFFEBEE), Color(0xFFEF5350))
        NotificationType.EXPIRING -> Pair(Warning500.copy(alpha = 0.05f), Warning500)
        NotificationType.INFO -> Pair(Color(0xFFE3F2FD), Color(0xFF42A5F5))
        NotificationType.RECIPE -> Pair(AI500.copy(alpha = 0.05f), AI500)
        NotificationType.STREAK -> Pair(Primary500.copy(alpha = 0.05f), Primary500)
        NotificationType.SAVED -> Pair(Primary500.copy(alpha = 0.05f), Primary500)
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
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Urgency badge
                        if (notification.daysUntilExpiry == 0) {
                            Surface(
                                color = Color(0xFFEF5350),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "TODAY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.timestamp,
                            fontSize = 12.sp,
                            color = Color.Gray.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Qty: ${notification.itemName}",
                            fontSize = 11.sp,
                            color = Primary500,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Logout Button
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
            }
            
            // Bottom spacing for navigation bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: com.freshly.app.viewmodel.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val user by viewModel.user.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            uploadError = null
            scope.launch {
                try {
                    // Upload to Cloudinary
                    val cloudinaryUrl = com.freshly.app.utils.CloudinaryManager.uploadImage(
                        context = context,
                        imageUri = it,
                        folder = "avatars",
                        maxSize = 800
                    )
                    avatarUrl = cloudinaryUrl
                    isUploading = false
                } catch (e: Exception) {
                    uploadError = e.message ?: "Upload failed"
                    isUploading = false
                }
            }
        }
    }
    
    LaunchedEffect(user) {
        name = user.name ?: ""
        email = user.email ?: ""
        avatarUrl = user.avatarUrl ?: ""
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Profile",
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
            // Avatar Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profile Picture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(
                                width = 3.dp,
                                color = Primary500,
                                shape = CircleShape
                            )
                            .padding(4.dp)
                            .clickable(enabled = !isUploading) { 
                                imagePickerLauncher.launch("image/*")
                            }
                    ) {
                        ImageAvatar(
                            imageUrl = avatarUrl,
                            name = name.ifEmpty { "User" },
                            size = 112.dp
                        )
                        
                        // Edit icon overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp),
                            shape = CircleShape,
                            color = Primary500
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "✏️",
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    SecondaryButton(
                        text = if (isUploading) "Uploading..." else "Upload Photo",
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        enabled = !isUploading
                    )
                    
                    if (uploadError != null) {
                        Text(
                            text = uploadError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = "Click to upload your photo from gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Name Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your name") },
                        singleLine = true
                    )
                }
            }
            
            // Email Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email") },
                        singleLine = true,
                        enabled = false, // Email typically shouldn't be editable
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Email cannot be changed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Save Button
            item {
                PrimaryButton(
                    text = if (isSaving) "Saving..." else "Save Changes",
                    onClick = {
                        if (!isSaving && !isUploading) {
                            isSaving = true
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                viewModel.updateProfile(name, avatarUrl)
                                kotlinx.coroutines.delay(500)
                                isSaving = false
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && !isUploading && name.isNotBlank()
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
