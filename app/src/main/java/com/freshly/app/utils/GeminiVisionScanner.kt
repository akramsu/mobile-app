package com.freshly.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.freshly.app.utils.GeminiApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream

/**
 * Enhanced product scanner using Gemini Vision API
 * 
 * This scanner uses Google's Gemini multimodal AI to analyze product images
 * and extract structured information including product name, expiry date,
 * quantity, and category - even when they appear in different locations.
 * 
 * Key Features:
 * - Text Extraction: Reads labels for packaged products (MFG vs EXP dates)
 * - Visual Recognition: Identifies fresh produce, fruits, vegetables without labels
 * - Freshness Analysis: Predicts shelf life based on visual appearance and ripeness
 * - Smart Detection: Automatically determines if item is labeled or fresh
 * - Multi-language support: Works with different languages
 * - Structured output: Returns JSON with confidence scores
 * 
 * Examples:
 * - Packaged snack → Reads label for name, expiry date
 * - Apple → Identifies "Apple", predicts 7-10 days shelf life based on appearance
 * - Tomato → Identifies "Tomato", estimates 3-5 days based on ripeness
 */
class GeminiVisionScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "GeminiVisionScanner"
        private const val MAX_IMAGE_SIZE = 1024 // Max dimension in pixels
    }
    
    init {
        GeminiApiKeyManager.initialize(context)
    }
    
    private fun createVisionModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = GeminiApiKeyManager.getCurrentApiKey(),
            generationConfig = generationConfig {
                temperature = 0.2f // Lower temperature for more consistent results
                topK = 32
                topP = 0.8f
                maxOutputTokens = 1024
            }
        )
    }
    
    /**
     * Scan product image and extract structured information
     * 
     * @param imageUri The URI of the captured product image
     * @return ExtractedItemInfo with all detected product details
     */
    suspend fun scanProduct(imageUri: Uri): ExtractedItemInfo = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting Gemini Vision scan for image: $imageUri")
            
            // Load and optimize image
            val bitmap = loadAndOptimizeBitmap(imageUri)
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to load bitmap from URI")
                return@withContext ExtractedItemInfo(confidence = 0f)
            }
            
            // Create the prompt for Gemini
            val prompt = createProductScanPrompt()
            
            // Call Gemini Vision API with retry logic
            val response = try {
                val content = content {
                    image(bitmap)
                    text(prompt)
                }
                
                createVisionModel().generateContent(content)
            } catch (e: Exception) {
                // Check if it's a rate limit error and rotate key
                if (GeminiApiKeyManager.isRateLimitError(e)) {
                    Log.w(TAG, "Rate limit hit, rotating API key and retrying...")
                    GeminiApiKeyManager.rotateToNextKey(context)
                    
                    // Retry with new key
                    val content = content {
                        image(bitmap)
                        text(prompt)
                    }
                    
                    createVisionModel().generateContent(content)
                } else {
                    Log.e(TAG, "Gemini API call failed", e)
                    return@withContext ExtractedItemInfo(confidence = 0f)
                }
            }
            
            // Parse the response
            val responseText = response.text ?: ""
            Log.d(TAG, "Gemini response: $responseText")
            
            parseGeminiResponse(responseText)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during product scan", e)
            ExtractedItemInfo(confidence = 0f)
        }
    }
    
    /**
     * Create an intelligent prompt for Gemini to analyze both labeled and unlabeled items
     */
    private fun createProductScanPrompt(): String {
        val today = java.time.LocalDate.now().toString()
        return """
Analyze this image intelligently and extract food/product information. Follow these steps:

STEP 1: DETERMINE ITEM TYPE
- Is this a PACKAGED product with visible labels/text?
- OR is this FRESH produce/food without packaging (fruit, vegetable, meat, etc.)?

STEP 2A: FOR PACKAGED PRODUCTS (with labels):
1. Product Name: Extract brand and product name from label
2. Expiry Date: Find "EXP", "Best Before", "Use By", "BB" (NOT manufacturing date)
3. Quantity: Net weight/volume from label
4. Category: Fridge/Freezer/Pantry based on storage instructions

STEP 2B: FOR FRESH PRODUCE (no label):
1. Product Name: Identify the item (e.g., "Banana", "Tomato", "Chicken Breast")
2. Visual Analysis: Assess ripeness, freshness, color, spots, bruising
3. Expiry Prediction: Based on visual condition, predict shelf life:
   - Calculate expiry as: TODAY ($today) + predicted shelf life days
   - Examples:
     * Green banana → 5-7 days
     * Ripe banana (yellow) → 2-3 days
     * Overripe banana (brown spots) → 1 day
     * Fresh tomato (firm, red) → 5-7 days
     * Soft tomato → 2-3 days
     * Fresh leafy greens → 3-5 days
     * Fresh meat (good color) → 2-3 days
     * Fresh fish → 1-2 days
4. Quantity: Estimate count (e.g., "3" for 3 apples)
5. Category: Always "Fridge" for fresh produce/meat

CRITICAL RULES:
- For dates: Return YYYY-MM-DD format
- For packaged items: Prioritize printed expiry over predictions
- For fresh items: Be conservative with shelf life predictions
- If both MFG and EXP dates exist, return only EXP
- Confidence should reflect certainty of ALL fields

Return ONLY a valid JSON object (no markdown):
{
  "productName": "string or null",
  "expiryDate": "YYYY-MM-DD or null",
  "quantity": "number or null",
  "unit": "g/kg/ml/l/items or null",
  "category": "Fridge/Freezer/Pantry or null",
  "confidence": 0.0-1.0
}

Examples:
Packaged: {"productName":"Lay's Chips","expiryDate":"2024-03-15","quantity":"500","unit":"g","category":"Pantry","confidence":0.9}
Fresh: {"productName":"Banana","expiryDate":"2025-01-05","quantity":"3","unit":"items","category":"Fridge","confidence":0.75}
""".trimIndent()
    }
    
    /**
     * Parse Gemini's JSON response into ExtractedItemInfo
     */
    private fun parseGeminiResponse(responseText: String): ExtractedItemInfo {
        return try {
            // Clean the response - remove markdown code blocks if present
            val cleanedJson = responseText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            
            Log.d(TAG, "Parsing JSON: $cleanedJson")
            
            val json = JSONObject(cleanedJson)
            
            ExtractedItemInfo(
                name = json.optString("productName").takeIf { it.isNotEmpty() && it != "null" },
                expiryDate = json.optString("expiryDate").takeIf { it.isNotEmpty() && it != "null" },
                quantity = json.optString("quantity").takeIf { it.isNotEmpty() && it != "null" },
                unit = json.optString("unit").takeIf { it.isNotEmpty() && it != "null" },
                category = json.optString("category").takeIf { it.isNotEmpty() && it != "null" },
                confidence = json.optDouble("confidence", 0.0).toFloat()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response", e)
            
            // Fallback: Try to extract data using string parsing
            extractDataFromPlainText(responseText)
        }
    }
    
    /**
     * Fallback method to extract data if JSON parsing fails
     */
    private fun extractDataFromPlainText(text: String): ExtractedItemInfo {
        var name: String? = null
        var expiryDate: String? = null
        var quantity: String? = null
        var unit: String? = null
        var category: String? = null
        var confidence = 0.3f // Lower confidence for fallback method
        
        val lines = text.lines()
        
        for (line in lines) {
            val lower = line.lowercase()
            when {
                "product" in lower && "name" in lower -> {
                    name = line.substringAfter(":").trim().removeSurrounding("\"")
                }
                "expiry" in lower && "date" in lower -> {
                    expiryDate = line.substringAfter(":").trim().removeSurrounding("\"")
                }
                "quantity" in lower -> {
                    quantity = line.substringAfter(":").trim().removeSurrounding("\"")
                }
                "unit" in lower -> {
                    unit = line.substringAfter(":").trim().removeSurrounding("\"")
                }
                "category" in lower -> {
                    category = line.substringAfter(":").trim().removeSurrounding("\"")
                }
                "confidence" in lower -> {
                    confidence = line.substringAfter(":").trim()
                        .removeSurrounding("\"")
                        .toFloatOrNull() ?: 0.3f
                }
            }
        }
        
        return ExtractedItemInfo(
            name = name,
            expiryDate = expiryDate,
            quantity = quantity,
            unit = unit,
            category = category,
            confidence = confidence
        )
    }
    
    /**
     * Load bitmap from URI and optimize it for Gemini Vision API
     * Reduces size while maintaining quality for better API performance
     */
    private fun loadAndOptimizeBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) return null
            
            // Calculate scaling factor to reduce image size if needed
            val maxDimension = maxOf(originalBitmap.width, originalBitmap.height)
            
            if (maxDimension > MAX_IMAGE_SIZE) {
                val scale = MAX_IMAGE_SIZE.toFloat() / maxDimension
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                
                Log.d(TAG, "Scaling image from ${originalBitmap.width}x${originalBitmap.height} to ${newWidth}x${newHeight}")
                
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true).also {
                    if (it != originalBitmap) {
                        originalBitmap.recycle()
                    }
                }
            } else {
                originalBitmap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap", e)
            null
        }
    }
    
    /**
     * Scan product with retry logic for better reliability
     * 
     * @param imageUri The URI of the captured product image
     * @param maxRetries Maximum number of retry attempts
     * @return ExtractedItemInfo with all detected product details
     */
    suspend fun scanProductWithRetry(
        imageUri: Uri,
        maxRetries: Int = 2
    ): ExtractedItemInfo {
        var lastResult = ExtractedItemInfo(confidence = 0f)
        
        repeat(maxRetries) { attempt ->
            Log.d(TAG, "Scan attempt ${attempt + 1}/$maxRetries")
            
            val result = scanProduct(imageUri)
            
            // If we got a good result, return it
            if (result.confidence >= 0.6f) {
                Log.d(TAG, "Successful scan with confidence: ${result.confidence}")
                return result
            }
            
            // Keep the best result so far
            if (result.confidence > lastResult.confidence) {
                lastResult = result
            }
        }
        
        Log.d(TAG, "Returning best result with confidence: ${lastResult.confidence}")
        return lastResult
    }
}
