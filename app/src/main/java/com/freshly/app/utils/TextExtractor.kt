package com.freshly.app.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ExtractedItemInfo(
    val name: String? = null,
    val expiryDate: String? = null,
    val quantity: String? = null,
    val unit: String? = null,
    val category: String? = null,
    val confidence: Float = 0f
)

class TextExtractor(private val context: Context) {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun extractTextFromImage(imageUri: Uri): ExtractedItemInfo {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            
            val extractedText = result.text
            val lines = result.textBlocks.flatMap { it.lines }.map { it.text }
            
            return parseExtractedText(extractedText, lines)
        } catch (e: Exception) {
            e.printStackTrace()
            return ExtractedItemInfo(confidence = 0f)
        }
    }
    
    private fun parseExtractedText(fullText: String, lines: List<String>): ExtractedItemInfo {
        val lowerText = fullText.lowercase()
        val productName = extractProductName(lines)
        val expiryDate = extractExpiryDate(fullText, lines)
        val quantity = extractQuantity(fullText)
        val unit = extractUnit(fullText, quantity)
        val category = detectCategory(lowerText)
        
        // Calculate confidence based on how many fields we successfully extracted
        var confidence = 0f
        if (productName != null) confidence += 0.3f
        if (expiryDate != null) confidence += 0.4f
        if (quantity != null) confidence += 0.15f
        if (category != null) confidence += 0.15f
        
        return ExtractedItemInfo(
            name = productName,
            expiryDate = expiryDate,
            quantity = quantity,
            unit = unit,
            category = category,
            confidence = confidence
        )
    }
    
    private fun extractProductName(lines: List<String>): String? {
        // Look for the product name in the first few prominent lines
        // Typically product names are in the first 1-3 lines and have certain characteristics
        
        for (i in 0 until minOf(5, lines.size)) {
            val line = lines[i].trim()
            
            // Skip lines that are likely not product names
            if (line.length < 3) continue
            if (line.matches(Regex("^[0-9.]+$"))) continue // Pure numbers
            if (line.matches(Regex(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}.*"))) continue // Date lines
            if (line.lowercase().startsWith("exp")) continue
            if (line.lowercase().startsWith("best before")) continue
            if (line.lowercase().startsWith("use by")) continue
            if (line.lowercase().startsWith("mfg")) continue
            if (line.lowercase().startsWith("batch")) continue
            if (line.lowercase().startsWith("lot")) continue
            
            // Valid product name found
            if (line.length >= 3 && line.length <= 50) {
                return line
            }
        }
        
        return null
    }
    
    private fun extractExpiryDate(fullText: String, lines: List<String>): String? {
        val datePatterns = listOf(
            // MM/DD/YYYY or DD/MM/YYYY
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{4})"""),
            Regex("""(\d{1,2})[/-](\d{1,2})[/-](\d{2})"""),
            // YYYY-MM-DD
            Regex("""(\d{4})[/-](\d{1,2})[/-](\d{1,2})"""),
            // DD MMM YYYY or DD-MMM-YYYY
            Regex("""(\d{1,2})\s*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s*(\d{2,4})""", RegexOption.IGNORE_CASE),
            // MMM DD, YYYY
            Regex("""(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s*(\d{1,2}),?\s*(\d{4})""", RegexOption.IGNORE_CASE),
            // MM/YYYY or MM-YYYY
            Regex("""(\d{1,2})[/-](\d{4})""")
        )
        
        val expiryKeywords = listOf("exp", "expiry", "expiration", "best before", "use by", "bb", "use before")
        
        // Look for dates near expiry keywords
        lines.forEach { line ->
            val lowerLine = line.lowercase()
            val hasExpiryKeyword = expiryKeywords.any { lowerLine.contains(it) }
            
            if (hasExpiryKeyword) {
                // Try to extract date from this line or nearby
                for (pattern in datePatterns) {
                    val match = pattern.find(line)
                    if (match != null) {
                        return formatDate(match.value)
                    }
                }
            }
        }
        
        // If no keyword found, look for any date that's in the future
        for (pattern in datePatterns) {
            val matches = pattern.findAll(fullText)
            for (match in matches) {
                val formattedDate = formatDate(match.value)
                if (formattedDate != null && isFutureDate(formattedDate)) {
                    return formattedDate
                }
            }
        }
        
        return null
    }
    
    private fun formatDate(dateStr: String): String? {
        try {
            val inputFormats = listOf(
                SimpleDateFormat("MM/dd/yyyy", Locale.US),
                SimpleDateFormat("dd/MM/yyyy", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US),
                SimpleDateFormat("MM-dd-yyyy", Locale.US),
                SimpleDateFormat("dd-MM-yyyy", Locale.US),
                SimpleDateFormat("MM/dd/yy", Locale.US),
                SimpleDateFormat("dd/MM/yy", Locale.US),
                SimpleDateFormat("dd MMM yyyy", Locale.US),
                SimpleDateFormat("MMM dd yyyy", Locale.US),
                SimpleDateFormat("MM/yyyy", Locale.US),
                SimpleDateFormat("MM-yyyy", Locale.US)
            )
            
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            
            for (format in inputFormats) {
                format.isLenient = false
                try {
                    val date = format.parse(dateStr)
                    if (date != null) {
                        return outputFormat.format(date)
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    private fun isFutureDate(dateStr: String): Boolean {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = format.parse(dateStr)
            val today = Calendar.getInstance().time
            return date != null && date.after(today)
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun extractQuantity(text: String): String? {
        // Look for quantity patterns like "500g", "1L", "250ml", "2 kg", etc.
        val quantityPatterns = listOf(
            Regex("""(\d+\.?\d*)\s*(kg|g|l|ml|oz|lb|lbs|liters?|grams?|kilograms?)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+\.?\d*)\s*x\s*(\d+\.?\d*)""", RegexOption.IGNORE_CASE), // Pack quantities like "12 x 250ml"
            Regex("""(\d+)\s*(pack|pcs|pieces|count|units?)""", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in quantityPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return null
    }
    
    private fun extractUnit(text: String, quantity: String?): String? {
        if (quantity == null) return null
        
        val unitPattern = Regex("""$quantity\s*(kg|g|l|ml|oz|lb|lbs|liters?|grams?|kilograms?|pack|pcs|pieces|count|units?)""", RegexOption.IGNORE_CASE)
        val match = unitPattern.find(text)
        
        return match?.groupValues?.get(1)?.lowercase()?.let { unit ->
            when {
                unit.startsWith("kg") || unit.startsWith("kilogram") -> "kg"
                unit.startsWith("g") || unit.startsWith("gram") -> "g"
                unit.startsWith("l") || unit.startsWith("liter") -> "liters"
                unit.startsWith("ml") -> "ml"
                unit.contains("pack") || unit.contains("pcs") || unit.contains("piece") || unit.contains("count") || unit.contains("unit") -> "items"
                else -> "items"
            }
        }
    }
    
    private fun detectCategory(text: String): String? {
        // Define category keywords
        val categoryKeywords = mapOf(
            "Fridge" to listOf(
                "milk", "cheese", "yogurt", "butter", "cream", "eggs", "egg",
                "meat", "chicken", "beef", "pork", "fish", "salmon", "tuna",
                "vegetable", "lettuce", "carrot", "tomato", "cucumber", "salad",
                "juice", "fresh", "refrigerate", "keep refrigerated", "cold storage"
            ),
            "Freezer" to listOf(
                "frozen", "ice cream", "freeze", "keep frozen", "pizza",
                "frozen vegetables", "frozen food", "frost"
            ),
            "Pantry" to listOf(
                "canned", "can", "pasta", "rice", "flour", "sugar", "oil",
                "cereal", "bread", "biscuit", "cookie", "chips", "snack",
                "dry", "ambient", "shelf stable", "room temperature"
            )
        )
        
        // Count matches for each category
        val categoryScores = mutableMapOf<String, Int>()
        
        for ((category, keywords) in categoryKeywords) {
            val score = keywords.count { text.contains(it) }
            if (score > 0) {
                categoryScores[category] = score
            }
        }
        
        // Return category with highest score
        return categoryScores.maxByOrNull { it.value }?.key
    }
}
