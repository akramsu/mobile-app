package com.freshly.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    private var isInitialized = false
    
    // Initialize Cloudinary (call this once in Application or MainActivity)
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                // Get credentials from BuildConfig
                val cloudName = com.freshly.app.BuildConfig.CLOUDINARY_CLOUD_NAME
                val apiKey = com.freshly.app.BuildConfig.CLOUDINARY_API_KEY
                val apiSecret = com.freshly.app.BuildConfig.CLOUDINARY_API_SECRET
                
                if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
                    Log.w(TAG, "Cloudinary credentials not configured. Please add them to local.properties")
                    return
                }
                
                val config = mapOf(
                    "cloud_name" to cloudName,
                    "api_key" to apiKey,
                    "api_secret" to apiSecret
                )
                MediaManager.init(context, config)
                isInitialized = true
                Log.d(TAG, "Cloudinary initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Cloudinary", e)
            }
        }
    }
    
    /**
     * Upload image to Cloudinary with compression and resizing
     * Returns the secure URL of the uploaded image
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String = "avatars",
        maxSize: Int = 800
    ): String = suspendCancellableCoroutine { continuation ->
        try {
            // Read and compress the image
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                continuation.resumeWithException(Exception("Failed to decode image"))
                return@suspendCancellableCoroutine
            }
            
            // Resize bitmap to reduce file size
            val resizedBitmap = resizeBitmap(originalBitmap, maxSize)
            
            // Convert to byte array with compression
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()
            
            // Clean up
            originalBitmap.recycle()
            resizedBitmap.recycle()
            
            // Upload to Cloudinary
            val options = mapOf(
                "folder" to folder,
                "resource_type" to "image",
                "overwrite" to true,
                "invalidate" to true
            )
            
            MediaManager.get().upload(imageData).options(options).callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Upload started: $requestId")
                }
                
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                    Log.d(TAG, "Upload progress: $progress%")
                }
                
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        Log.d(TAG, "Upload successful: $secureUrl")
                        continuation.resume(secureUrl)
                    } else {
                        continuation.resumeWithException(Exception("No secure_url in response"))
                    }
                }
                
                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Upload failed: ${error.description}")
                    continuation.resumeWithException(Exception("Upload failed: ${error.description}"))
                }
                
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "Upload rescheduled: ${error.description}")
                }
            }).dispatch()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing upload", e)
            continuation.resumeWithException(e)
        }
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
