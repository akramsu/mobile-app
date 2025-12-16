package com.freshly.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    private var cloudName = ""
    private var apiKey = ""
    private var apiSecret = ""
    private var isInitialized = false
    
    // Create a trust manager that doesn't validate certificate chains
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    
    // Create an SSL context that uses our trust manager
    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, trustAllCerts, SecureRandom())
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier(HostnameVerifier { _, _ -> true })
        .build()
    
    // Initialize Cloudinary (call this once in Application or MainActivity)
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                // Get credentials from BuildConfig
                cloudName = com.freshly.app.BuildConfig.CLOUDINARY_CLOUD_NAME
                apiKey = com.freshly.app.BuildConfig.CLOUDINARY_API_KEY
                apiSecret = com.freshly.app.BuildConfig.CLOUDINARY_API_SECRET
                
                if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
                    Log.w(TAG, "Cloudinary credentials not configured. Please add them to local.properties")
                    return
                }
                
                isInitialized = true
                Log.d(TAG, "Cloudinary initialized successfully (Direct HTTP mode)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Cloudinary", e)
            }
        }
    }
    
    /**
     * Upload image to Cloudinary with compression and resizing using direct HTTP
     * Returns the secure URL of the uploaded image
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "avatars",
        maxSize: Int = 800
    ): String = withContext(Dispatchers.IO) {
        try {
            // Check if Cloudinary is initialized
            if (!isInitialized) {
                Log.e(TAG, "Cloudinary not initialized!")
                throw Exception("Cloudinary not configured. Please add credentials to local.properties")
            }
            
            Log.d(TAG, "Starting image upload from URI: $imageUri")
            
            // Read and compress the image
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Failed to open image")
            
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                throw Exception("Failed to decode image")
            }
            
            Log.d(TAG, "Image decoded: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Resize bitmap to reduce file size
            val resizedBitmap = resizeBitmap(originalBitmap, maxSize)
            Log.d(TAG, "Image resized to: ${resizedBitmap.width}x${resizedBitmap.height}")
            
            // Convert to byte array with compression
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()
            Log.d(TAG, "Image compressed to ${imageData.size} bytes")
            
            // Clean up
            originalBitmap.recycle()
            resizedBitmap.recycle()
            
            // Generate timestamp
            val timestamp = (System.currentTimeMillis() / 1000).toString()
            
            // Create signature for upload
            val signature = generateSignature(folder, timestamp)
            
            val uploadUrl = "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
            Log.d(TAG, "Upload URL: $uploadUrl")
            Log.d(TAG, "Cloud name: $cloudName, API key: ${apiKey.take(5)}...")
            Log.d(TAG, "Signature: ${signature.take(10)}...")
            
            // Build multipart request
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", 
                    imageData.toRequestBody("image/jpeg".toMediaType()))
                .addFormDataPart("folder", folder)
                .addFormDataPart("timestamp", timestamp)
                .addFormDataPart("api_key", apiKey)
                .addFormDataPart("signature", signature)
                .build()
            
            val request = Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build()
            
            Log.d(TAG, "Sending HTTP upload request...")
            
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                
                Log.d(TAG, "Response code: ${response.code}, message: ${response.message}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload failed with code ${response.code}: $responseBody")
                    throw Exception("Upload failed (${response.code}): $responseBody")
                }
                
                Log.d(TAG, "Upload response: $responseBody")
                
                val jsonResponse = JSONObject(responseBody)
                val secureUrl = jsonResponse.getString("secure_url")
                
                Log.d(TAG, "Upload successful: $secureUrl")
                return@withContext secureUrl
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            throw e
        }
    }
    
    /**
     * Generate signature for Cloudinary upload
     */
    private fun generateSignature(folder: String, timestamp: String): String {
        val toSign = "folder=$folder&timestamp=$timestamp$apiSecret"
        val md = MessageDigest.getInstance("SHA-1")
        val digest = md.digest(toSign.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Resize bitmap to fit within maxSize while maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Generate optimized Cloudinary URL with transformations
     */
    fun getOptimizedUrl(
        originalUrl: String,
        width: Int = 400,
        height: Int = 400,
        crop: String = "fill"
    ): String {
        // Extract the part after 'upload/' and insert transformations
        val parts = originalUrl.split("/upload/")
        if (parts.size != 2) return originalUrl
        
        val transformation = "w_$width,h_$height,c_$crop,q_auto,f_auto"
        return "${parts[0]}/upload/$transformation/${parts[1]}"
    }
}
