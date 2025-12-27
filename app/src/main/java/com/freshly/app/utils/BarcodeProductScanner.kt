package com.freshly.app.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Barcode scanner that looks up product information from Open Food Facts database
 * 
 * This is a complementary tool to GeminiVisionScanner. Use this when:
 * 1. Products have clear, visible barcodes
 * 2. You want faster scanning with pre-existing product data
 * 3. You want to reduce API costs (Open Food Facts is free)
 * 
 * Falls back to GeminiVisionScanner if barcode not found or no barcode detected.
 */
class BarcodeProductScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "BarcodeProductScanner"
        private const val OPEN_FOOD_FACTS_API = "https://world.openfoodfacts.org/api/v0/product"
    }
    
    private val barcodeScanner = BarcodeScanning.getClient()
    
    /**
     * Scan for barcode and lookup product information
     * 
     * @param imageUri The URI of the captured product image
     * @return ExtractedItemInfo if barcode found and product exists, null otherwise
     */
    suspend fun scanBarcode(imageUri: Uri): ExtractedItemInfo? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting barcode scan")
            
            // Detect barcode in image
            val image = InputImage.fromFilePath(context, imageUri)
            val barcodes = barcodeScanner.process(image).await()
            
            if (barcodes.isEmpty()) {
                Log.d(TAG, "No barcode detected")
                return@withContext null
            }
            
            // Use first detected barcode
            val barcode = barcodes.first()
            val barcodeValue = barcode.rawValue ?: return@withContext null
            
            Log.d(TAG, "Barcode detected: $barcodeValue (${getBarcodeFormat(barcode.format)})")
            
            // Look up product in Open Food Facts
            lookupProduct(barcodeValue)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning barcode", e)
            null
        }
    }
    
    /**
     * Look up product information from Open Food Facts API
     */
    private suspend fun lookupProduct(barcode: String): ExtractedItemInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$OPEN_FOOD_FACTS_API/$barcode.json")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "FreshlyApp/1.0")
                connectTimeout = 5000
                readTimeout = 5000
            }
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Product not found in database")
                return@withContext null
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            parseProductResponse(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up product", e)
            null
        }
    }
    
    /**
     * Parse Open Food Facts API response
     */
    private fun parseProductResponse(jsonResponse: String): ExtractedItemInfo? {
        try {
            val json = JSONObject(jsonResponse)
            
            // Check if product exists
            val status = json.optInt("status", 0)
            if (status == 0) {
                Log.d(TAG, "Product not found in Open Food Facts")
                return null
            }
            
            val product = json.getJSONObject("product")
            
            // Extract product name
            val productName = product.optString("product_name")
                .takeIf { it.isNotEmpty() }
                ?: product.optString("product_name_en")
                ?: product.optString("generic_name")
            
            // Extract quantity
            val quantity = product.optString("quantity")
                .takeIf { it.isNotEmpty() }
                ?.let { parseQuantityValue(it) }
            
            val unit = product.optString("quantity")
                .takeIf { it.isNotEmpty() }
                ?.let { parseQuantityUnit(it) }
            
            // Determine category based on product categories
            val categories = product.optString("categories_tags")
            val category = detectCategoryFromTags(categories)
            
            Log.d(TAG, "Product found: $productName")
            
            return ExtractedItemInfo(
                name = productName,
                expiryDate = null, // Barcode databases don't include expiry dates
                quantity = quantity,
                unit = unit,
                category = category,
                confidence = 0.95f // High confidence from verified database
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing product response", e)
            return null
        }
    }
    
    /**
     * Parse quantity value from text like "500g", "1.5L", etc.
     */
    private fun parseQuantityValue(text: String): String? {
        val pattern = Regex("""(\d+(?:\.\d+)?)\s*(?:kg|g|l|ml|oz|lb)""", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.get(1)
    }
    
    /**
     * Parse unit from text like "500g", "1.5L", etc.
     */
    private fun parseQuantityUnit(text: String): String? {
        val pattern = Regex("""\d+(?:\.\d+)?\s*(kg|g|l|ml|oz|lb)""", RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.get(1)?.lowercase()?.let { unit ->
            when {
                unit.startsWith("kg") -> "kg"
                unit.startsWith("g") -> "g"
                unit.startsWith("l") -> "liters"
                unit.startsWith("ml") -> "ml"
                else -> "items"
            }
        }
    }
    
    /**
     * Detect storage category from Open Food Facts category tags
     */
    private fun detectCategoryFromTags(tags: String): String? {
        val lowerTags = tags.lowercase()
        
        return when {
            // Freezer items
            lowerTags.contains("frozen") || 
            lowerTags.contains("ice-cream") ||
            lowerTags.contains("ice-creams") -> "Freezer"
            
            // Fridge items
            lowerTags.contains("dairy") ||
            lowerTags.contains("milk") ||
            lowerTags.contains("cheese") ||
            lowerTags.contains("yogurt") ||
            lowerTags.contains("fresh") ||
            lowerTags.contains("meat") ||
            lowerTags.contains("fish") -> "Fridge"
            
            // Pantry items
            lowerTags.contains("canned") ||
            lowerTags.contains("pasta") ||
            lowerTags.contains("rice") ||
            lowerTags.contains("cereals") ||
            lowerTags.contains("cookies") ||
            lowerTags.contains("snacks") ||
            lowerTags.contains("beverages") -> "Pantry"
            
            else -> null
        }
    }
    
    /**
     * Get human-readable barcode format name
     */
    private fun getBarcodeFormat(format: Int): String {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> "EAN-13"
            Barcode.FORMAT_EAN_8 -> "EAN-8"
            Barcode.FORMAT_UPC_A -> "UPC-A"
            Barcode.FORMAT_UPC_E -> "UPC-E"
            Barcode.FORMAT_CODE_128 -> "Code 128"
            Barcode.FORMAT_QR_CODE -> "QR Code"
            else -> "Unknown"
        }
    }
}

/**
 * Smart scanner that tries barcode first, then falls back to Gemini Vision
 * 
 * This provides the best user experience:
 * 1. Fast barcode lookup (< 1 second) if available
 * 2. Comprehensive AI scanning if no barcode or not found
 */
class SmartProductScanner(private val context: Context) {
    
    private val barcodeScanner = BarcodeProductScanner(context)
    private val visionScanner = GeminiVisionScanner(context)
    
    companion object {
        private const val TAG = "SmartProductScanner"
    }
    
    /**
     * Scan product using the best available method
     * 
     * Strategy:
     * 1. Try barcode scanning first (fast, free)
     * 2. If barcode found and product exists, use that
     * 3. Otherwise, use Gemini Vision for comprehensive scanning
     */
    suspend fun scan(imageUri: Uri): ExtractedItemInfo {
        Log.d(TAG, "Starting smart scan")
        
        // Try barcode first
        val barcodeResult = barcodeScanner.scanBarcode(imageUri)
        
        if (barcodeResult != null && barcodeResult.name != null) {
            Log.d(TAG, "Barcode scan successful, product found in database")
            
            // Barcode scan successful, but we still need expiry date
            // Run Gemini scan but only ask for expiry date to save tokens
            val expiryInfo = visionScanner.scanForExpiryOnly(imageUri)
            
            return barcodeResult.copy(
                expiryDate = expiryInfo.expiryDate,
                confidence = (barcodeResult.confidence + expiryInfo.confidence) / 2
            )
        }
        
        // Barcode not found or no barcode detected, use full Gemini scan
        Log.d(TAG, "Barcode scan failed, using Gemini Vision")
        return visionScanner.scanProductWithRetry(imageUri)
    }
}

/**
 * Extension function for GeminiVisionScanner to scan only expiry date
 * This is more efficient when we already have product info from barcode
 */
private suspend fun GeminiVisionScanner.scanForExpiryOnly(imageUri: Uri): ExtractedItemInfo {
    // Use full scan for now - could be optimized with custom prompt
    // that only looks for expiry date to reduce token usage
    return scanProduct(imageUri)
}
