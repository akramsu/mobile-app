package com.freshly.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.freshly.app.BuildConfig
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
 * Advantages over traditional OCR:
 * - Context-aware: Understands the difference between MFG and EXP dates
 * - Handles various layouts: Product info can be anywhere on the package
 * - Multi-language support: Works with different languages
 * - Image quality tolerant: Works even with blurry or partial images
 * - Structured output: Returns JSON with confidence scores
 */
class GeminiVisionScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "GeminiVisionScanner"
        private const val MAX_IMAGE_SIZE = 1024 // Max dimension in pixels
    }
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.2f // Lower temperature for more consistent results
            topK = 32
            topP = 0.8f
            maxOutputTokens = 1024
        }
    )
    
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
            
            // Call Gemini Vision API
            val response = try {
                val content = content {
                    image(bitmap)
                    text(prompt)
                }
                
                generativeModel.generateContent(content)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API call failed", e)
                return@withContext ExtractedItemInfo(confidence = 0f)
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
     * Create a detailed prompt for Gemini to analyze product images
     */
    private fun createProductScanPrompt(): String {
        return """
Analyze this product image and extract the following information. Look carefully at the entire package.

IMPORTANT INSTRUCTIONS:
1. Product Name: The brand name and product description (e.g., "Lay's Classic Potato Chips")
2. Expiry Date: Look for "EXP", "Best Before", "Use By", "BB", or similar labels. DO NOT confuse with manufacturing date (MFG).
3. Quantity: The net weight or volume (e.g., "500g", "1L", "250ml")
4. Category: Classify as one of: Fridge, Freezer, or Pantry

CRITICAL: 
- The expiry date and product name may be in DIFFERENT locations on the package
- Prioritize EXPIRY date over manufacturing date
- If you see both MFG and EXP dates, only return the EXP date
- For dates, convert to YYYY-MM-DD format
- For quantity, extract just the number (e.g., from "500g" return "500")
- For unit, extract just the unit (e.g., from "500g" return "g")

Return ONLY a JSON object in this exact format (no markdown, no code blocks):
{
  "productName": "string or null",
  "expiryDate": "YYYY-MM-DD or null",
  "quantity": "number string or null",
  "unit": "g/kg/ml/l/items or null",
  "category": "Fridge/Freezer/Pantry or null",
  "confidence": 0.0-1.0
}

If you cannot detect a field with confidence, set it to null.
Confidence score should reflect how certain you are about ALL extracted fields combined.

Example response:
{"productName":"Lay's Classic Potato Chips","expiryDate":"2024-03-15","quantity":"500","unit":"g","category":"Pantry","confidence":0.85}
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
