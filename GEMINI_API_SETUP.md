# 🚀 Gemini API Setup Guide (FREE Tier)

## Quick Start - Get Your Free API Key

### Step 1: Get API Key (2 minutes)

1. **Visit Google AI Studio:**
   - Go to: https://aistudio.google.com/app/apikey
   - Sign in with your Google account

2. **Create API Key:**
   - Click "Create API Key" button
   - Select "Create API key in new project" (or use existing project)
   - Copy the API key (starts with `AIza...`)

3. **Important:** Keep your API key secret! Never commit it to Git.

---

## Step 2: Configure API Key in Project

### Option A: Using local.properties (Recommended)

1. **Open/Create `local.properties` in project root:**
   ```properties
   # SDK location (already exists)
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\sdk
   
   # Add this line (replace with your actual key):
   GEMINI_API_KEY=AIzaSyD_your_actual_api_key_here
   ```

2. **Verify `.gitignore` excludes this file:**
   ```gitignore
   # Already in .gitignore (Android Studio default)
   local.properties
   ```

3. **Build Configuration - Update `app/build.gradle.kts`:**
   
   Add this inside the `android` block:
   ```kotlin
   android {
       // ... existing config
       
       defaultConfig {
           // ... existing config
           
           // Load API key from local.properties
           val properties = Properties()
           properties.load(project.rootProject.file("local.properties").inputStream())
           buildConfigField(
               "String", 
               "GEMINI_API_KEY", 
               "\"${properties.getProperty("GEMINI_API_KEY", "")}\""
           )
       }
       
       buildFeatures {
           compose = true
           buildConfig = true  // ⚠️ IMPORTANT: Enable BuildConfig
       }
   }
   ```

4. **Sync Gradle** and rebuild project

5. **Access in Code:**
   ```kotlin
   import com.freshly.app.BuildConfig
   
   val apiKey = BuildConfig.GEMINI_API_KEY
   ```

---

## Step 3: Add Gemini SDK Dependency

**Update `app/build.gradle.kts` dependencies:**

```kotlin
dependencies {
    // ... existing dependencies
    
    // Gemini AI SDK (FREE)
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    
    // Required for coroutines (already included)
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

**Sync Gradle** again.

---

## Step 4: Test Your Setup

**Create a simple test to verify API connection:**

```kotlin
package com.freshly.app.utils

import android.util.Log
import com.freshly.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.runBlocking

object GeminiTest {
    fun testConnection() = runBlocking {
        try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
            
            val response = model.generateContent("Say hello!")
            Log.d("GeminiTest", "✅ Success: ${response.text}")
            
        } catch (e: Exception) {
            Log.e("GeminiTest", "❌ Error: ${e.message}", e)
        }
    }
}
```

**Run from MainActivity onCreate (temporarily):**
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test API (remove after verification)
        GeminiTest.testConnection()
        
        // ... rest of code
    }
}
```

**Check Logcat:** You should see "✅ Success: Hello! How can I help you today?"

---

## Free Tier Best Practices

### 1. Rate Limiting (15 req/min, 1,500 req/day)

**Implement client-side throttling:**

```kotlin
class GeminiRateLimiter {
    private val dailyLimit = 1500
    private val minuteLimit = 15
    
    private val dailyTimestamps = mutableListOf<Long>()
    private val minuteTimestamps = mutableListOf<Long>()
    
    fun canMakeRequest(): Boolean {
        val now = System.currentTimeMillis()
        
        // Clean old timestamps
        dailyTimestamps.removeAll { now - it > 24 * 60 * 60 * 1000 }
        minuteTimestamps.removeAll { now - it > 60 * 1000 }
        
        return dailyTimestamps.size < dailyLimit && 
               minuteTimestamps.size < minuteLimit
    }
    
    fun recordRequest() {
        val now = System.currentTimeMillis()
        dailyTimestamps.add(now)
        minuteTimestamps.add(now)
    }
    
    fun getRemainingRequests(): Pair<Int, Int> {
        return Pair(
            dailyLimit - dailyTimestamps.size,
            minuteLimit - minuteTimestamps.size
        )
    }
}
```

### 2. Caching Strategy

**Cache recipe responses to avoid duplicate API calls:**

```kotlin
class RecipeCache {
    private val cache = mutableMapOf<String, List<Recipe>>()
    private val maxAge = 24 * 60 * 60 * 1000L // 24 hours
    
    fun get(ingredients: List<String>): List<Recipe>? {
        val key = ingredients.sorted().joinToString(",")
        return cache[key]
    }
    
    fun put(ingredients: List<String>, recipes: List<Recipe>) {
        val key = ingredients.sorted().joinToString(",")
        cache[key] = recipes
    }
}
```

### 3. Error Handling

**Handle rate limit errors gracefully:**

```kotlin
suspend fun generateRecipes(ingredients: List<String>): Result<List<Recipe>> {
    return try {
        if (!rateLimiter.canMakeRequest()) {
            return Result.failure(Exception("Daily/minute limit reached. Try again later."))
        }
        
        rateLimiter.recordRequest()
        
        val response = geminiModel.generateContent(prompt)
        Result.success(parseRecipes(response.text ?: ""))
        
    } catch (e: Exception) {
        when {
            e.message?.contains("429") == true -> {
                Result.failure(Exception("Rate limit exceeded. Please wait a minute."))
            }
            e.message?.contains("quota") == true -> {
                Result.failure(Exception("Daily quota reached. Resets in 24 hours."))
            }
            else -> Result.failure(e)
        }
    }
}
```

---

## Recommended Model Configuration

### For AI Chef (Recipe Generation)

```kotlin
val recipeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",  // FREE: 1,500 req/day
    apiKey = BuildConfig.GEMINI_API_KEY,
    generationConfig = generationConfig {
        temperature = 0.8f      // Creative recipes
        topK = 40
        topP = 0.95f
        maxOutputTokens = 2048  // Enough for 3 recipes
    }
)
```

### For AI Assistant (Chat)

```kotlin
val chatModel = GenerativeModel(
    modelName = "gemini-1.5-flash",  // FREE: 1,500 req/day
    apiKey = BuildConfig.GEMINI_API_KEY,
    generationConfig = generationConfig {
        temperature = 0.9f      // More conversational
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1024  // Shorter responses
    }
)
```

---

## Free Tier Usage Estimates

**Assuming 1 user (you during development):**

| Feature | Requests/Day | Within Free Limit? |
|---------|--------------|-------------------|
| AI Chef | 5-10 | ✅ Yes (1,500/day) |
| AI Assistant | 20-50 | ✅ Yes (1,500/day) |
| **Total** | **25-60/day** | ✅ **Well within limit!** |

**For production with multiple users:**
- 100 users × 5 requests = 500/day ✅ Still free!
- 300 users × 5 requests = 1,500/day ✅ At limit
- 500+ users = Need paid tier or backend rate limiting

---

## Troubleshooting

### ❌ "API key not found"

**Check:**
1. `local.properties` file exists in project root (same level as `build.gradle.kts`)
2. Key format is correct: `GEMINI_API_KEY=AIza...` (no quotes, no spaces)
3. Gradle sync completed after adding `buildConfigField`
4. BuildConfig is enabled: `buildFeatures { buildConfig = true }`

**Solution:**
```bash
# In Android Studio terminal:
cd d:/projects/freshly/android-studio
echo "GEMINI_API_KEY=AIzaSyD_your_key_here" >> local.properties

# Then: Build > Clean Project > Rebuild Project
```

---

### ❌ "429 Too Many Requests"

**You hit the rate limit (15/min or 1,500/day).**

**Solution:**
- Wait 1 minute (for minute limit)
- Wait 24 hours (for daily limit)
- Implement caching to reduce requests
- Add rate limiter to your code

---

### ❌ "Invalid JSON response"

**Gemini returned malformed JSON.**

**Solution:**
- Improve prompt clarity (see prompt engineering tips)
- Add JSON validation:
  ```kotlin
  val cleanJson = response.text
      ?.replace("```json", "")
      ?.replace("```", "")
      ?.trim()
  ```
- Add error handling:
  ```kotlin
  try {
      val recipes = JSONArray(cleanJson)
  } catch (e: JSONException) {
      Log.e("Parser", "Invalid JSON: $cleanJson")
      return emptyList()
  }
  ```

---

### ❌ "Network error"

**No internet connection.**

**Solution:**
- Check device/emulator has internet access
- Test with: `curl https://generativelanguage.googleapis.com`
- Add connectivity check:
  ```kotlin
  val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) 
      as ConnectivityManager
  val network = connectivityManager.activeNetwork
  if (network == null) {
      // Show "No internet" message
  }
  ```

---

## Security Checklist

✅ **Do:**
- Store API key in `local.properties` (gitignored)
- Use BuildConfig to access key
- Add rate limiting to prevent abuse
- Validate/sanitize user inputs before sending to API

❌ **Don't:**
- Hardcode API key in source files
- Commit `local.properties` to Git
- Share API key in screenshots or logs
- Expose key in BuildConfig.java (ProGuard will obfuscate in release)

---

## Next Steps

1. ✅ Get your free API key from https://aistudio.google.com/app/apikey
2. ✅ Add to `local.properties`
3. ✅ Update `app/build.gradle.kts` (enable BuildConfig)
4. ✅ Add Gemini SDK dependency
5. ✅ Sync Gradle
6. ✅ Test connection with GeminiTest
7. ✅ Start implementing AI Chef (Phase 2 of main plan)

---

## Useful Links

- **Get API Key:** https://aistudio.google.com/app/apikey
- **Gemini Docs:** https://ai.google.dev/gemini-api/docs
- **Kotlin SDK:** https://github.com/google/generative-ai-android
- **Rate Limits:** https://ai.google.dev/pricing
- **Prompt Gallery:** https://ai.google.dev/examples

---

## Cost Summary

### FREE Forever! 🎉

| Model | Cost |
|-------|------|
| Gemini 1.5 Flash | **$0** (up to 1,500 req/day) |
| Gemini 1.5 Pro | **$0** (up to 50 req/day) |

**Your app will stay FREE for:**
- Personal use (unlimited)
- Development & testing
- Small user base (< 300 daily active users)

**Paid tier only needed when:**
- 1,500+ requests per day
- Need higher rate limits (60 req/min)
- Commercial app with thousands of users

For your project, **FREE tier is perfect!** 🚀
