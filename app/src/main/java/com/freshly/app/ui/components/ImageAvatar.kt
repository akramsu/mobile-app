package com.freshly.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.freshly.app.ui.theme.Primary500

@Composable
fun ImageAvatar(
    imageUrl: String?,
    name: String,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    // Check if imageUrl is a Cloudinary URL or valid URL (not emoji)
    val isValidUrl = !imageUrl.isNullOrEmpty() && 
                     (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Primary500.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        if (isValidUrl) {
            // Load image from Cloudinary URL
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show first letter of username as fallback
            val initial = name.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                fontSize = (size.value * 0.4).sp,
                color = Primary500,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ItemImage(
    imageUrl: String?,
    name: String,
    size: Dp = 60.dp,
    modifier: Modifier = Modifier,
    category: String? = null
) {
    val (emoji, gradientColors) = getCategoryVisuals(name, category)
    
    Box(
        modifier = modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
            .background(
                if (imageUrl.isNullOrEmpty()) {
                    androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Smart placeholder based on category or name
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = (size.value * 0.5).sp
            )
        }
    }
}

/**
 * Get category-specific emoji and gradient colors based on item name or category
 */
private fun getCategoryVisuals(name: String, category: String?): Pair<String, List<Color>> {
    val lowerName = name.lowercase()
    
    // Match based on keywords in name
    return when {
        // Fruits
        lowerName.contains("apple") -> "🍎" to listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
        lowerName.contains("banana") -> "🍌" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
        lowerName.contains("orange") -> "🍊" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("grape") -> "🍇" to listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8))
        lowerName.contains("strawberr") -> "🍓" to listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))
        lowerName.contains("watermelon") -> "🍉" to listOf(Color(0xFFC8E6C9), Color(0xFFA5D6A7))
        lowerName.contains("lemon") || lowerName.contains("lime") -> "🍋" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        lowerName.contains("peach") -> "🍑" to listOf(Color(0xFFFFCCBC), Color(0xFFFFAB91))
        lowerName.contains("cherry") || lowerName.contains("cherries") -> "🍒" to listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))
        lowerName.contains("pineapple") -> "🍍" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
        lowerName.contains("mango") -> "🥭" to listOf(Color(0xFFFFE082), Color(0xFFFFD54F))
        
        // Vegetables
        lowerName.contains("tomato") -> "🍅" to listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        lowerName.contains("carrot") -> "🥕" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("broccoli") -> "🥦" to listOf(Color(0xFFC8E6C9), Color(0xFFA5D6A7))
        lowerName.contains("lettuce") || lowerName.contains("salad") -> "🥬" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        lowerName.contains("potato") -> "🥔" to listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4))
        lowerName.contains("onion") -> "🧅" to listOf(Color(0xFFFFE0B2), Color(0xFFFFD180))
        lowerName.contains("pepper") || lowerName.contains("bell pepper") -> "🫑" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        lowerName.contains("cucumber") -> "🥒" to listOf(Color(0xFFC8E6C9), Color(0xFFA5D6A7))
        lowerName.contains("corn") -> "🌽" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        lowerName.contains("mushroom") -> "🍄" to listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4))
        lowerName.contains("avocado") -> "🥑" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        
        // Proteins
        lowerName.contains("chicken") || lowerName.contains("poultry") -> "🍗" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("beef") || lowerName.contains("steak") -> "🥩" to listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        lowerName.contains("fish") || lowerName.contains("salmon") -> "🐟" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        lowerName.contains("egg") -> "🥚" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
        lowerName.contains("bacon") || lowerName.contains("pork") -> "🥓" to listOf(Color(0xFFFFCCBC), Color(0xFFFFAB91))
        lowerName.contains("shrimp") || lowerName.contains("prawn") -> "🦐" to listOf(Color(0xFFFFCDD2), Color(0xFFF8BBD0))
        
        // Dairy
        lowerName.contains("milk") -> "🥛" to listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5))
        lowerName.contains("cheese") -> "🧀" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        lowerName.contains("yogurt") || lowerName.contains("yoghurt") -> "🥛" to listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))
        lowerName.contains("butter") -> "🧈" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        lowerName.contains("cream") -> "🥛" to listOf(Color(0xFFFFFFFF), Color(0xFFFFF9C4))
        
        // Bakery & Grains
        lowerName.contains("bread") -> "🍞" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("pasta") || lowerName.contains("noodle") -> "🍝" to listOf(Color(0xFFFFF9C4), Color(0xFFFFE082))
        lowerName.contains("rice") -> "🍚" to listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F5))
        lowerName.contains("cereal") -> "🥣" to listOf(Color(0xFFFFE0B2), Color(0xFFFFD180))
        lowerName.contains("pizza") -> "🍕" to listOf(Color(0xFFFFCCBC), Color(0xFFFFAB91))
        lowerName.contains("burger") -> "🍔" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Beverages
        lowerName.contains("coffee") -> "☕" to listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4))
        lowerName.contains("tea") -> "🍵" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        lowerName.contains("juice") -> "🧃" to listOf(Color(0xFFFFE082), Color(0xFFFFD54F))
        lowerName.contains("water") || lowerName.contains("bottle") -> "💧" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        lowerName.contains("soda") || lowerName.contains("cola") -> "🥤" to listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        lowerName.contains("wine") -> "🍷" to listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8))
        lowerName.contains("beer") -> "🍺" to listOf(Color(0xFFFFE082), Color(0xFFFFD54F))
        
        // Sweets & Snacks
        lowerName.contains("chocolate") || lowerName.contains("candy") -> "🍫" to listOf(Color(0xFFD7CCC8), Color(0xFFBCAAA4))
        lowerName.contains("cookie") || lowerName.contains("biscuit") -> "🍪" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("cake") -> "🍰" to listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))
        lowerName.contains("ice cream") || lowerName.contains("icecream") -> "🍦" to listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8))
        lowerName.contains("donut") || lowerName.contains("doughnut") -> "🍩" to listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))
        
        // Condiments & Others
        lowerName.contains("sauce") || lowerName.contains("ketchup") -> "🥫" to listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        lowerName.contains("oil") || lowerName.contains("olive") -> "🫒" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        lowerName.contains("honey") -> "🍯" to listOf(Color(0xFFFFE082), Color(0xFFFFD54F))
        lowerName.contains("soup") -> "🍲" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("salad") -> "🥗" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        
        // Frozen foods
        lowerName.contains("frozen") || category?.lowercase() == "freezer" -> "❄️" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        
        // Category fallbacks
        category?.lowercase() == "fridge" -> "🧊" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        category?.lowercase() == "pantry" -> "📦" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Default
        else -> "🍽️" to listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }
}

/**
 * Get recipe-specific emoji and gradient based on recipe tags or name
 */
@Composable
fun RecipeImage(
    imageUrl: String?,
    name: String,
    tags: List<String> = emptyList(),
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val (emoji, gradientColors) = getRecipeVisuals(name, tags)
    
    Box(
        modifier = modifier
            .size(width = size, height = size * 0.75f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (imageUrl.isNullOrEmpty()) {
                    androidx.compose.ui.graphics.Brush.linearGradient(gradientColors)
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium,
                fontSize = (size.value * 0.4).sp
            )
        }
    }
}

/**
 * Get recipe-specific visuals based on recipe type
 */
private fun getRecipeVisuals(name: String, tags: List<String>): Pair<String, List<Color>> {
    val lowerName = name.lowercase()
    val lowerTags = tags.map { it.lowercase() }
    
    return when {
        // Cuisine types
        lowerTags.contains("italian") || lowerName.contains("pasta") || lowerName.contains("pizza") -> 
            "🍝" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerTags.contains("mexican") || lowerName.contains("taco") || lowerName.contains("burrito") -> 
            "🌮" to listOf(Color(0xFFFFCCBC), Color(0xFFFFAB91))
        lowerTags.contains("japanese") || lowerName.contains("sushi") || lowerName.contains("ramen") -> 
            "🍜" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        lowerTags.contains("chinese") || lowerName.contains("fried rice") || lowerName.contains("noodles") -> 
            "🥡" to listOf(Color(0xFFFFE082), Color(0xFFFFD54F))
        lowerTags.contains("indian") || lowerName.contains("curry") || lowerName.contains("biryani") -> 
            "🍛" to listOf(Color(0xFFFFE082), Color(0xFFFFB74D))
        lowerTags.contains("thai") || lowerName.contains("pad thai") -> 
            "🍲" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Meal types
        lowerTags.contains("breakfast") || lowerName.contains("pancake") || lowerName.contains("waffle") || lowerName.contains("breakfast") -> 
            "🥞" to listOf(Color(0xFFFFF9C4), Color(0xFFFFF176))
        lowerTags.contains("salad") || lowerName.contains("salad") -> 
            "🥗" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        lowerTags.contains("soup") || lowerName.contains("soup") -> 
            "🍲" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerTags.contains("sandwich") || lowerName.contains("sandwich") || lowerName.contains("burger") -> 
            "🥪" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Protein-based
        lowerName.contains("chicken") || lowerTags.contains("chicken") -> 
            "🍗" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        lowerName.contains("beef") || lowerName.contains("steak") -> 
            "🥩" to listOf(Color(0xFFFFCDD2), Color(0xFFEF9A9A))
        lowerName.contains("fish") || lowerName.contains("salmon") || lowerTags.contains("seafood") -> 
            "🐟" to listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
        lowerTags.contains("vegetarian") || lowerTags.contains("vegan") -> 
            "🥗" to listOf(Color(0xFFDCEDC8), Color(0xFFC5E1A5))
        
        // Desserts
        lowerTags.contains("dessert") || lowerName.contains("cake") || lowerName.contains("cookie") -> 
            "🍰" to listOf(Color(0xFFF8BBD0), Color(0xFFF48FB1))
        lowerName.contains("ice cream") -> 
            "🍦" to listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8))
        lowerName.contains("pie") || lowerName.contains("tart") -> 
            "🥧" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Baking
        lowerTags.contains("baking") || lowerName.contains("bread") || lowerName.contains("muffin") -> 
            "🍞" to listOf(Color(0xFFFFE0B2), Color(0xFFFFCC80))
        
        // Default
        else -> "👨‍🍳" to listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }
}
