# 🤖 Gemini API Integration Plan for Freshly App

## Executive Summary
This document outlines the complete implementation plan for integrating Google's Gemini API into two AI-powered features:
1. **AI Chef Screen** - Recipe generation based on pantry items
2. **AI Assistant Screen** - Conversational chatbot for food management insights

---

## 📋 Table of Contents
1. [Current State Analysis](#current-state-analysis)
2. [Gemini API Setup](#gemini-api-setup)
3. [Architecture Design](#architecture-design)
4. [Implementation Roadmap](#implementation-roadmap)
5. [Code Structure](#code-structure)
6. [Security & Best Practices](#security--best-practices)
7. [Testing Strategy](#testing-strategy)
8. [Cost Estimation](#cost-estimation)

---

## 1. Current State Analysis

### 1.1 AI Chef Screen (`AIChefScreen.kt`)

**Current Features:**
- ✅ Pantry item selection with checkbox interface
- ✅ Visual highlighting of expiring items
- ✅ Loading states with progress indicator
- ✅ Recipe result display with matched ingredients
- ✅ Item image preview (48dp thumbnails)
- ⚠️ **Mock recipe generation** (800ms delay, returns static sample data)

**Current Implementation:**
```kotlin
// AIChefViewModel.kt - Line 43
suspend fun generateRecipes() {
    _isLoading.value = true
    recipeRepository.generateRecipes(_selectedItems.value.toList())
        .collect { recipes ->
            _recipes.value = recipes
            _isLoading.value = false
        }
}

// RecipeRepository.kt - Line 22
suspend fun generateRecipes(selectedIngredients: List<String>): Flow<List<Recipe>> = flow {
    delay(800) // ⚠️ Simulated delay
    emit(getSampleRecipes(selectedIngredients)) // ⚠️ Static data
}
```

**Recipe Data Structure:**
```kotlin
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val cookTime: Int,
    val servings: Int,
    val difficulty: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    val matchedIngredients: List<String>,
    val tags: List<String>
)
```

**Missing Features:**
- ❌ Real AI recipe generation
- ❌ Dietary preference consideration
- ❌ Nutritional information
- ❌ Recipe difficulty personalization
- ❌ Error handling for API failures
- ❌ Retry mechanism
- ❌ Caching for generated recipes

---

### 1.2 AI Assistant Screen (`AIAssistantScreen.kt`)

**Current Features:**
- ✅ Two-panel interface: AI Insights + Chatbot
- ✅ "Get My AI Insights" button with gradient design
- ✅ Static insights display (food waste stats, tips)
- ✅ Suggestion chips for common questions
- ✅ Chat bubble UI with user/AI differentiation
- ✅ Smooth animations and scrolling
- ⚠️ **Mock responses** (1-2 second delays, hardcoded text)

**Current Implementation:**
```kotlin
// AIAssistantScreen.kt - Lines 94-104 (Insights)
isGeneratingInsights = true
scope.launch {
    kotlinx.coroutines.delay(2000) // ⚠️ Simulated
    showInsights = true
    isGeneratingInsights = false
}

// AIAssistantScreen.kt - Lines 348-357 (Chat)
scope.launch {
    kotlinx.coroutines.delay(1500)
    messages = messages + ChatMessage(
        "I understand you're asking about: \"$userMessage\"\n\n...",
        false
    )
}
```

**Chat Features:**
- Pre-defined suggestion chips:
  - "Give me a recipe using items I already have"
  - "Which food will expire in the next 3 days?"
  - "How can I reduce food waste this month?"
- Chat bubble styling with color differentiation
- Auto-scroll to latest message

**Missing Features:**
- ❌ Real Gemini-powered conversations
- ❌ Context awareness (pantry state, user history)
- ❌ Multi-turn conversation memory
- ❌ Personalized insights based on actual data
- ❌ Streaming responses (progressive text display)
- ❌ Error handling and fallback messages
- ❌ Rate limiting and quota management

---

## 2. Gemini API Setup

### 2.1 API Key Management

**Options:**
1. **BuildConfig (Recommended for Development)** - Store in `local.properties`
2. **Firebase Remote Config (Recommended for Production)** - Secure server-side storage
3. **Backend Proxy (Most Secure)** - Never expose key to client

**Implementation Choice:** BuildConfig + Backend Proxy hybrid
- Development: Local API key for testing
- Production: Backend proxy with rate limiting

### 2.2 Required Dependencies

Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    // Existing dependencies...
    
    // Gemini AI SDK (FREE tier: 1,500 requests/day)
    implementation("com.google.ai.client.generativeai:generativeai:0.2.2")
    
    // For streaming responses (already included)
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

### 2.3 API Key Configuration

**Step 1:** Add to `local.properties` (ignored by Git):
```properties
GEMINI_API_KEY=YOUR_ACTUAL_API_KEY_HERE
```

**Step 2:** Add to `app/build.gradle.kts`:
```kotlin
android {
    // ... existing config
    
    defaultConfig {
        // ... existing config
        
        // Load API key from local.properties
        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "GEMINI_API_KEY", 
            "\"${properties.getProperty("GEMINI_API_KEY", "")}\"")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true // ⚠️ Enable BuildConfig
    }
}
```

**Step 3:** Access in code:
```kotlin
val apiKey = BuildConfig.GEMINI_API_KEY
```

---

## 3. Architecture Design

### 3.1 New Components

```
app/
├── data/
│   ├── api/
│   │   ├── GeminiApiService.kt         # 🆕 Gemini API wrapper
│   │   ├── GeminiPromptBuilder.kt      # 🆕 Prompt construction utilities
│   │   └── GeminiResponseParser.kt     # 🆕 Parse AI responses to models
│   ├── model/
│   │   ├── GeminiRequest.kt            # 🆕 API request models
│   │   ├── GeminiResponse.kt           # 🆕 API response models
│   │   └── ChatContext.kt              # 🆕 Conversation state
│   └── repository/
│       ├── RecipeRepository.kt         # ✏️ Update with Gemini
│       └── ChatRepository.kt           # 🆕 Chat history management
├── viewmodel/
│   ├── AIChefViewModel.kt              # ✏️ Update with real API
│   └── AIAssistantViewModel.kt         # 🆕 Extract logic from screen
└── ui/screens/
    ├── AIChefScreen.kt                 # ✏️ Error states
    └── AIAssistantScreen.kt            # ✏️ Extract to ViewModel

Legend: 🆕 New file | ✏️ Modify existing
```

### 3.2 Data Flow

#### AI Chef Flow:
```
User Selects Items
    ↓
AIChefViewModel.generateRecipes()
    ↓
RecipeRepository.generateRecipes(items)
    ↓
GeminiApiService.generateRecipes(prompt)
    ↓
Gemini API → JSON Response
    ↓
GeminiResponseParser.parseRecipes()
    ↓
Recipe objects
    ↓
StateFlow update → UI refresh
```

#### AI Assistant Flow:
```
User Sends Message
    ↓
AIAssistantViewModel.sendMessage(text)
    ↓
GeminiPromptBuilder.buildContextualPrompt(message, pantryState)
    ↓
GeminiApiService.chat(prompt, conversationHistory)
    ↓
Gemini API → Streaming Response
    ↓
Real-time StateFlow updates
    ↓
UI displays streaming text
```

---

## 4. Implementation Roadmap

### Phase 1: Foundation (Days 1-2) 🏗️

**1.1 Setup Dependencies & API Key**
- [ ] Add Gemini SDK to `build.gradle.kts`
- [ ] Configure `local.properties` with API key
- [ ] Enable BuildConfig feature
- [ ] Test API key access in code

**1.2 Create Core API Service**
- [ ] Create `GeminiApiService.kt` with initialization
- [ ] Implement basic API call with error handling
- [ ] Create `GeminiRequest` and `GeminiResponse` models
- [ ] Test with simple prompt

**Deliverable:** Working Gemini API connection with test prompt

---

### Phase 2: AI Chef Integration (Days 3-5) 👨‍🍳

**2.1 Recipe Generation Prompt Engineering**
- [ ] Create `GeminiPromptBuilder.kt`
- [ ] Design recipe generation prompt template:
  ```
  System: You are a professional chef assistant...
  User pantry: [item1, item2, item3]
  Constraints: [dietary preferences], [skill level]
  Output: JSON array of 3 recipes with structure...
  ```
- [ ] Test prompt quality with various ingredient combinations

**2.2 Response Parsing**
- [ ] Create `GeminiResponseParser.kt`
- [ ] Parse JSON responses to `Recipe` objects
- [ ] Handle malformed responses gracefully
- [ ] Add validation for required fields

**2.3 Update RecipeRepository**
- [ ] Replace mock data with `GeminiApiService.generateRecipes()`
- [ ] Add retry logic (3 attempts)
- [ ] Implement response caching (avoid duplicate calls)
- [ ] Add error state handling

**2.4 Update AIChefViewModel**
- [ ] Add error state to StateFlow
- [ ] Implement loading timeout (30 seconds)
- [ ] Add user dietary preferences from UserRepository
- [ ] Pass skill level context to API

**2.5 Update AIChefScreen UI**
- [ ] Add error dialog with retry button
- [ ] Add timeout warning
- [ ] Display API rate limit messages
- [ ] Add "Save Recipe" functionality

**Deliverable:** Fully functional AI recipe generation

---

### Phase 3: AI Assistant Chat (Days 6-9) 💬

**3.1 Create AIAssistantViewModel**
- [ ] Extract all business logic from `AIAssistantScreen`
- [ ] Create `ChatRepository.kt` for conversation persistence
- [ ] Implement conversation history (last 10 messages)
- [ ] Add pantry context loading

**3.2 Conversational Prompts**
- [ ] Design system prompt for food management assistant
- [ ] Build context-aware prompts with:
  - Current pantry items (names, quantities, expiry dates)
  - User profile (dietary restrictions, preferences)
  - Recent activity (items added/consumed)
- [ ] Test multi-turn conversations

**3.3 Streaming Responses**
- [ ] Implement `GeminiApiService.chatStream()`
- [ ] Add StateFlow for progressive text updates
- [ ] Display typing indicator while streaming
- [ ] Handle stream interruptions

**3.4 Insights Generation**
- [ ] Create dedicated prompt for insights:
  ```
  Analyze user's pantry data:
  - Total items: X
  - Expiring soon: Y
  - Food waste this month: Z
  - Shopping patterns: [data]
  
  Generate 3-5 actionable insights with:
  - Emoji-prefixed tips
  - Specific item recommendations
  - Cost savings estimates
  ```
- [ ] Parse structured insights response
- [ ] Add refresh cooldown (5 minutes)

**3.5 UI Enhancements**
- [ ] Add error messages for failed chat
- [ ] Implement retry button
- [ ] Add "Stop generating" button for streams
- [ ] Save conversation history locally

**Deliverable:** Fully conversational AI assistant with context awareness

---

### Phase 4: Advanced Features (Days 10-12) 🚀

**4.1 Recipe Image Generation**
- [ ] Integrate with image generation API (DALL-E or Imagen)
- [ ] Generate recipe images based on title/description
- [ ] Cache generated images
- [ ] Add placeholder while generating

**4.2 Nutritional Information**
- [ ] Add nutritional data to Recipe model
- [ ] Prompt Gemini to estimate:
  - Calories per serving
  - Macros (protein, carbs, fats)
  - Key vitamins/minerals
- [ ] Display nutrition cards in recipe view

**4.3 Smart Suggestions**
- [ ] Pre-generate suggestions based on:
  - Items expiring in 24 hours
  - Incomplete ingredient sets (suggest what to buy)
  - Seasonal recipes
- [ ] Display as notification chips

**4.4 Conversation Memory**
- [ ] Persist chat history to Firestore:
  ```
  /users/{userId}/chat_history/{messageId}
  ```
- [ ] Load last 20 messages on app start
- [ ] Add "Clear history" button

**Deliverable:** Production-ready AI features with advanced capabilities

---

### Phase 5: Production Hardening (Days 13-14) 🛡️

**5.1 Error Handling**
- [ ] Network connectivity checks
- [ ] API quota exceeded handling
- [ ] Malformed response fallbacks
- [ ] User-friendly error messages

**5.2 Rate Limiting**
- [ ] Track API calls per user per day
- [ ] Implement client-side throttling
- [ ] Show quota usage in settings
- [ ] Add premium tier for unlimited calls

**5.3 Security**
- [ ] Move API key to backend proxy (Firebase Functions)
- [ ] Validate requests server-side
- [ ] Implement user authentication checks
- [ ] Add content filtering for inappropriate prompts

**5.4 Performance Optimization**
- [ ] Cache generated recipes (30 days)
- [ ] Debounce chat input (300ms)
- [ ] Lazy load conversation history
- [ ] Optimize prompt token usage

**5.5 Testing**
- [ ] Unit tests for prompt builders
- [ ] Integration tests for API service
- [ ] UI tests for error states
- [ ] Load testing for concurrent users

**Deliverable:** Production-ready, secure, performant AI features

---

## 5. Code Structure

### 5.1 GeminiApiService.kt (New)

```kotlin
package com.freshly.app.data.api

import android.util.Log
import com.freshly.app.BuildConfig
import com.freshly.app.data.model.Recipe
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class GeminiApiService {
    
    // 🆓 FREE TIER: Gemini 1.5 Flash - 1,500 requests/day, 15 requests/minute
    private val recipeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",  // Using Flash for both to maximize free tier
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.8f      // Creative but controlled
            topK = 40
            topP = 0.95f
            maxOutputTokens = 2048  // Enough for 3 detailed recipes
        }
    )
    
    private val chatModel = GenerativeModel(
        modelName = "gemini-1.5-flash",  // Same model for consistency
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.9f      // More conversational
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024  // Shorter chat responses
        }
    )
    
    private val rateLimiter = GeminiRateLimiter()  // Prevent exceeding free tier limits
    
    /**
     * Generate recipes based on pantry items
     */
    suspend fun generateRecipes(
        ingredients: List<String>,
        dietaryPreferences: List<String> = emptyList(),
        skillLevel: String = "Medium"
    ): Result<List<Recipe>> {
        return try {
            // Check free tier rate limits
            if (!rateLimiter.canMakeRequest()) {
                val (dailyRemaining, minuteRemaining) = rateLimiter.getRemainingRequests()
                return Result.failure(Exception(
                    "Rate limit reached. Daily: $dailyRemaining, Minute: $minuteRemaining remaining."
                ))
            }
            
            val prompt = GeminiPromptBuilder.buildRecipePrompt(
                ingredients, 
                dietaryPreferences, 
                skillLevel
            )
            
            rateLimiter.recordRequest()
            val response = recipeModel.generateContent(prompt)
            val recipes = GeminiResponseParser.parseRecipes(response.text ?: "")
            
            Result.success(recipes)
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Recipe generation failed", e)
            
            // Handle specific errors
            when {
                e.message?.contains("429") == true -> {
                    Result.failure(Exception("Rate limit exceeded. Please wait a minute."))
                }
                e.message?.contains("quota") == true -> {
                    Result.failure(Exception("Daily quota reached (1,500 requests). Resets in 24 hours."))
                }
                else -> Result.failure(e)
            }
        }
    }
    
    /**
     * Chat with streaming responses
     */
    fun chatStream(
        userMessage: String,
        conversationHistory: List<Content>,
        pantryContext: String
    ): Flow<String> {
        val prompt = GeminiPromptBuilder.buildChatPrompt(
            userMessage,
            pantryContext
        )
        
        return chatModel.generateContentStream(prompt)
            .map { response -> response.text ?: "" }
            .catch { e ->
                Log.e("GeminiApiService", "Chat stream failed", e)
                emit("Sorry, I encountered an error. Please try again.")
            }
    }
    
    /**
     * Generate AI insights
     */
    suspend fun generateInsights(
        pantryData: Map<String, Any>,
        userProfile: Map<String, Any>
    ): Result<String> {
        return try {
            val prompt = GeminiPromptBuilder.buildInsightsPrompt(
                pantryData,
                userProfile
            )
            
            val response = chatModel.generateContent(prompt)
            Result.success(response.text ?: "No insights available")
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Insights generation failed", e)
            Result.failure(e)
        }
    }
}
```

---

### 5.2 GeminiPromptBuilder.kt (New)

```kotlin
package com.freshly.app.data.api

object GeminiPromptBuilder {
    
    fun buildRecipePrompt(
        ingredients: List<String>,
        dietaryPreferences: List<String>,
        skillLevel: String
    ): String {
        val ingredientList = ingredients.joinToString(", ")
        val dietary = if (dietaryPreferences.isNotEmpty()) {
            "Dietary restrictions: ${dietaryPreferences.joinToString(", ")}"
        } else ""
        
        return """
You are a professional chef assistant helping users create recipes from their pantry items.

Available ingredients: $ingredientList
Skill level: $skillLevel
$dietary

Generate exactly 3 creative recipes that:
1. Use as many of the listed ingredients as possible
2. Are achievable for someone with $skillLevel cooking skills
3. Take 15-45 minutes to prepare
4. Are practical for home cooking

Return ONLY a valid JSON array with this exact structure (no markdown, no extra text):
[
  {
    "title": "Recipe Name",
    "description": "Brief appetizing description (1-2 sentences)",
    "cookTime": 30,
    "servings": 4,
    "difficulty": "Easy|Medium|Hard",
    "ingredients": [
      {"name": "Ingredient 1", "amount": "200g", "isMatched": true},
      {"name": "Ingredient 2", "amount": "1 cup", "isMatched": false}
    ],
    "steps": [
      "Step 1 description",
      "Step 2 description"
    ],
    "tags": ["Italian", "Quick", "Healthy"]
  }
]

Important: Mark "isMatched": true only for ingredients from the available list.
        """.trimIndent()
    }
    
    fun buildChatPrompt(
        userMessage: String,
        pantryContext: String
    ): String {
        return """
You are a friendly AI assistant specializing in food management and reducing food waste.

Current pantry context:
$pantryContext

User question: $userMessage

Provide helpful, actionable advice. Be concise and friendly. Use emojis appropriately.
If suggesting recipes, mention specific items from the pantry.
If discussing expiry dates, be specific and urgent for items expiring soon.
        """.trimIndent()
    }
    
    fun buildInsightsPrompt(
        pantryData: Map<String, Any>,
        userProfile: Map<String, Any>
    ): String {
        return """
Analyze this user's food management data and generate 3-5 actionable insights.

Pantry Data:
- Total items: ${pantryData["totalItems"]}
- Expiring soon (3 days): ${pantryData["expiringSoon"]}
- Expired this month: ${pantryData["expiredThisMonth"]}
- Most common categories: ${pantryData["topCategories"]}

User Profile:
- Dietary preferences: ${userProfile["dietaryRestrictions"]}
- Household size: ${userProfile["householdSize"]}

Generate insights in this format:
🎉 [Positive achievement or progress]
💡 [Specific actionable tip with item names]
💰 [Cost savings estimate]
🌱 [Environmental impact]
⚡ [Urgent action for expiring items]

Be specific, use actual item names, and estimate concrete numbers (money saved, waste reduced).
        """.trimIndent()
    }
}
```

---

### 5.3 GeminiResponseParser.kt (New)

```kotlin
package com.freshly.app.data.api

import android.util.Log
import com.freshly.app.data.model.Ingredient
import com.freshly.app.data.model.Recipe
import org.json.JSONArray
import org.json.JSONException
import java.util.*

object GeminiResponseParser {
    
    fun parseRecipes(jsonResponse: String): List<Recipe> {
        return try {
            // Extract JSON array from response (handle markdown wrapping)
            val cleanJson = jsonResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val jsonArray = JSONArray(cleanJson)
            val recipes = mutableListOf<Recipe>()
            
            for (i in 0 until jsonArray.length()) {
                val recipeJson = jsonArray.getJSONObject(i)
                
                // Parse ingredients
                val ingredientsArray = recipeJson.getJSONArray("ingredients")
                val ingredients = mutableListOf<Ingredient>()
                val matchedIngredients = mutableListOf<String>()
                
                for (j in 0 until ingredientsArray.length()) {
                    val ingJson = ingredientsArray.getJSONObject(j)
                    val name = ingJson.getString("name")
                    val amount = ingJson.getString("amount")
                    val isMatched = ingJson.optBoolean("isMatched", false)
                    
                    ingredients.add(Ingredient(name, amount, isMatched))
                    if (isMatched) matchedIngredients.add(name)
                }
                
                // Parse steps
                val stepsArray = recipeJson.getJSONArray("steps")
                val steps = mutableListOf<String>()
                for (j in 0 until stepsArray.length()) {
                    steps.add(stepsArray.getString(j))
                }
                
                // Parse tags
                val tagsArray = recipeJson.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArray != null) {
                    for (j in 0 until tagsArray.length()) {
                        tags.add(tagsArray.getString(j))
                    }
                }
                
                // Create Recipe object
                val recipe = Recipe(
                    id = UUID.randomUUID().toString(),
                    title = recipeJson.getString("title"),
                    description = recipeJson.getString("description"),
                    imageUrl = "", // To be generated later
                    cookTime = recipeJson.getInt("cookTime"),
                    servings = recipeJson.getInt("servings"),
                    difficulty = recipeJson.getString("difficulty"),
                    ingredients = ingredients,
                    steps = steps,
                    matchedIngredients = matchedIngredients,
                    tags = tags
                )
                
                recipes.add(recipe)
            }
            
            recipes
        } catch (e: JSONException) {
            Log.e("GeminiResponseParser", "Failed to parse recipes", e)
            emptyList()
        }
    }
    
    fun parseInsights(response: String): Map<String, String> {
        // Parse structured insights from response
        val insights = mutableMapOf<String, String>()
        
        val lines = response.split("\n").filter { it.isNotBlank() }
        
        for (line in lines) {
            when {
                line.startsWith("🎉") -> insights["achievement"] = line
                line.startsWith("💡") -> insights["tip"] = line
                line.startsWith("💰") -> insights["savings"] = line
                line.startsWith("🌱") -> insights["environmental"] = line
                line.startsWith("⚡") -> insights["urgent"] = line
            }
        }
        
        return insights
    }
}
```

---

### 5.4 Updated RecipeRepository.kt

```kotlin
// Replace generateRecipes method in RecipeRepository.kt

private val geminiService = GeminiApiService()

suspend fun generateRecipes(
    selectedIngredients: List<String>,
    dietaryPreferences: List<String> = emptyList(),
    skillLevel: String = "Medium"
): Flow<List<Recipe>> = flow {
    // Show loading state
    emit(emptyList())
    
    // Call Gemini API with retry logic
    var attempts = 0
    var result: Result<List<Recipe>>? = null
    
    while (attempts < 3) {
        result = geminiService.generateRecipes(
            selectedIngredients,
            dietaryPreferences,
            skillLevel
        )
        
        if (result.isSuccess) {
            emit(result.getOrNull() ?: emptyList())
            return@flow
        }
        
        attempts++
        delay(1000 * attempts) // Exponential backoff
    }
    
    // If all retries fail, throw exception
    throw result?.exceptionOrNull() ?: Exception("Failed to generate recipes")
}
```

---

### 5.5 New AIAssistantViewModel.kt

```kotlin
package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.api.GeminiApiService
import com.freshly.app.data.api.GeminiPromptBuilder
import com.freshly.app.data.model.ChatMessage
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AIAssistantViewModel : ViewModel() {
    
    private val geminiService = GeminiApiService()
    private val pantryRepository = PantryRepository()
    private val userRepository = UserRepository()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _insights = MutableStateFlow<Map<String, String>>(emptyMap())
    val insights: StateFlow<Map<String, String>> = _insights.asStateFlow()
    
    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights: StateFlow<Boolean> = _isLoadingInsights.asStateFlow()
    
    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()
    
    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()
    
    fun sendMessage(text: String) {
        // Add user message
        _messages.value += ChatMessage(text, isUser = true)
        _isGeneratingResponse.value = true
        _currentStreamingText.value = ""
        
        viewModelScope.launch {
            try {
                // Build pantry context
                val pantryItems = pantryRepository.items.first()
                val pantryContext = buildPantryContext(pantryItems)
                
                // Stream response
                geminiService.chatStream(
                    userMessage = text,
                    conversationHistory = emptyList(), // TODO: Build from _messages
                    pantryContext = pantryContext
                ).collect { chunk ->
                    _currentStreamingText.value += chunk
                }
                
                // Add complete AI message
                _messages.value += ChatMessage(
                    _currentStreamingText.value,
                    isUser = false
                )
                
            } catch (e: Exception) {
                _messages.value += ChatMessage(
                    "Sorry, I encountered an error: ${e.message}",
                    isUser = false
                )
            } finally {
                _isGeneratingResponse.value = false
                _currentStreamingText.value = ""
            }
        }
    }
    
    fun generateInsights() {
        _isLoadingInsights.value = true
        
        viewModelScope.launch {
            try {
                val pantryItems = pantryRepository.items.first()
                val user = userRepository.user.first()
                
                val pantryData = mapOf(
                    "totalItems" to pantryItems.size,
                    "expiringSoon" to pantryItems.count { it.getDaysUntilExpiry() <= 3 },
                    "expiredThisMonth" to 0, // TODO: Calculate from analytics
                    "topCategories" to pantryItems
                        .groupBy { it.category }
                        .map { it.key }
                        .take(3)
                )
                
                val userProfile = mapOf(
                    "dietaryRestrictions" to user.dietaryRestrictions,
                    "householdSize" to 1 // TODO: Add to user profile
                )
                
                val result = geminiService.generateInsights(pantryData, userProfile)
                
                if (result.isSuccess) {
                    val insightsText = result.getOrNull() ?: ""
                    _insights.value = GeminiResponseParser.parseInsights(insightsText)
                }
                
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingInsights.value = false
            }
        }
    }
    
    private fun buildPantryContext(items: List<PantryItem>): String {
        return buildString {
            appendLine("User's current pantry:")
            items.take(10).forEach { item ->
                val expiry = item.getDaysUntilExpiry()
                val status = when {
                    expiry < 0 -> "EXPIRED"
                    expiry <= 3 -> "Expiring in $expiry days"
                    else -> "Fresh"
                }
                appendLine("- ${item.name} (${item.quantity} ${item.unit}) - $status")
            }
            if (items.size > 10) {
                appendLine("... and ${items.size - 10} more items")
            }
        }
    }
    
    fun clearMessages() {
        _messages.value = emptyList()
    }
}
```

---

## 6. Security & Best Practices

### 6.1 API Key Protection

**❌ Never Do:**
```kotlin
// WRONG: Hardcoded API key
val apiKey = "AIzaSyD..."
```

**✅ Do This:**
```kotlin
// Correct: BuildConfig
val apiKey = BuildConfig.GEMINI_API_KEY

// Even better: Backend proxy
suspend fun generateRecipes(items: List<String>): Result<List<Recipe>> {
    return apiClient.post("/api/generate-recipes") {
        setBody(GenerateRecipesRequest(items))
    }
}
```

### 6.2 Rate Limiting

```kotlin
class GeminiRateLimiter {
    private val maxCallsPerDay = 100
    private val callTimestamps = mutableListOf<Long>()
    
    fun canMakeRequest(): Boolean {
        val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        callTimestamps.removeAll { it < oneDayAgo }
        return callTimestamps.size < maxCallsPerDay
    }
    
    fun recordRequest() {
        callTimestamps.add(System.currentTimeMillis())
    }
}
```

### 6.3 Content Filtering

```kotlin
fun sanitizeUserInput(input: String): String {
    return input
        .take(500) // Limit length
        .replace(Regex("[<>]"), "") // Remove HTML
        .trim()
}
```

### 6.4 Error Handling Best Practices

```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val type: ErrorType) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

enum class ErrorType {
    NETWORK,
    API_QUOTA_EXCEEDED,
    INVALID_RESPONSE,
    TIMEOUT,
    UNKNOWN
}
```

---

## 7. Testing Strategy

### 7.1 Unit Tests

```kotlin
class GeminiPromptBuilderTest {
    @Test
    fun `buildRecipePrompt includes all ingredients`() {
        val ingredients = listOf("Chicken", "Rice", "Tomatoes")
        val prompt = GeminiPromptBuilder.buildRecipePrompt(
            ingredients,
            emptyList(),
            "Easy"
        )
        
        assertTrue(prompt.contains("Chicken"))
        assertTrue(prompt.contains("Rice"))
        assertTrue(prompt.contains("Tomatoes"))
    }
    
    @Test
    fun `buildRecipePrompt handles dietary restrictions`() {
        val prompt = GeminiPromptBuilder.buildRecipePrompt(
            listOf("Chicken"),
            listOf("Vegan", "Gluten-free"),
            "Medium"
        )
        
        assertTrue(prompt.contains("Vegan"))
        assertTrue(prompt.contains("Gluten-free"))
    }
}

class GeminiResponseParserTest {
    @Test
    fun `parseRecipes handles valid JSON`() {
        val json = """[{"title": "Test Recipe", ...}]"""
        val recipes = GeminiResponseParser.parseRecipes(json)
        
        assertEquals(1, recipes.size)
        assertEquals("Test Recipe", recipes[0].title)
    }
    
    @Test
    fun `parseRecipes returns empty list for malformed JSON`() {
        val recipes = GeminiResponseParser.parseRecipes("invalid json")
        assertTrue(recipes.isEmpty())
    }
}
```

### 7.2 Integration Tests

```kotlin
class GeminiApiServiceTest {
    private lateinit var service: GeminiApiService
    
    @Before
    fun setup() {
        service = GeminiApiService()
    }
    
    @Test
    fun `generateRecipes returns valid recipes`() = runBlocking {
        val result = service.generateRecipes(
            listOf("Chicken", "Rice", "Tomatoes")
        )
        
        assertTrue(result.isSuccess)
        val recipes = result.getOrNull()!!
        assertTrue(recipes.isNotEmpty())
        assertTrue(recipes.all { it.title.isNotBlank() })
    }
}
```

### 7.3 UI Tests

```kotlin
@Test (FREE TIER) 🎉

### 8.1 Gemini API Pricing - FREE Tier

**Gemini 1.5 Flash (Recommended):**
- **Cost:** $0 (FREE)
- **Daily Limit:** 1,500 requests
- **Rate Limit:** 15 requests/minute
- **Perfect for:** Development, personal use, small apps

**Gemini 1.5 Pro (Alternative):**
- **Cost:** $0 (FREE)
- **Daily Limit:** 50 requests
- **Rate Limit:** 2 requests/minute
- **Use when:** Need more complex reasoning (not needed for this project)

### 8.2 Usage Estimates (FREE Tier)

**For 1 user (development):**
- AI Chef: 5-10 requests/day ✅ Well within limit
- AI Assistant: 20-50 messages/day ✅ Well within limit
- **Total:** ~30-60 requests/day (out of 1,500 free)

**For 100 users (small app):**
- AI Chef: 5 requests/user = 500 requests/day ✅ Still FREE!
- AI Assistant: 10 messages/user = 1,000 requests/day ✅ Still FREE!
- **Total:** ~1,500 requests/day = **At the free limit but still $0!**

**For 300+ users (need to optimize):**
- Would exceed 1,500/day limit
- Options:
  1. Implement aggressive caching (reduce by 50%)
  2. Add backend rate limiting per user
  3. Consider paid tier ($0.10 per 1M tokens)

### 8.3 When You Need Paid Tier

**Stay FREE if:**
- ✅ Personal/hobby project
- ✅ < 300 daily active users
- ✅ Users make < 5 AI requests per day
- ✅ Smart caching implemented

**Upgrade to Paid if:**
- ❌ > 1,500 requests per day
- ❌ Need faster rate limits (60 req/min)
- ❌ Commercial app with 1,000+ users
- ❌ Real-time streaming for many users

**Paid tier cost:** $0.10 per 1 million input tokens (~$0.30 per 1 million output tokens)
- Still very cheap! Example: 10,000 requests/day = ~$15/month

### 8.4 Free Tier Optimization Strategies

1. **Recipe Caching (Save 40-60% requests)**
   ```kotlin
   // Cache recipes for same ingredient combinations
   val cacheKey = ingredients.sorted().joinToString(",")
   val cached = cache.get(cacheKey)
   if (cached != null) return cached
   ```

2. **Rate Limiting Per User (Prevent abuse)**
   ```kotlin
   // Limit each user to 10 AI requests per day
   val userDailyLimit = 10
   if (userRequestCount >= userDailyLimit) {
       showPremiumUpgradeDialog()
   }
   ```

3. **Prompt Optimization (Save tokens)**
   - Use shorter prompts (save 20-30% tokens)
   - Request fewer recipes (2 instead of 3)
   - Compress pantry context

4. **Smart Suggestions (Pre-compute)**
   - Generate daily recipe suggestions in background
   - Cache insights for all users
   - Reduce real-time API calls

5. **Debouncing (Prevent spam)**
   ```kotlin
   // Wait 500ms before sending chat message
   var chatJob: Job? = null
   fun onUserTyping() {
       chatJob?.cancel()
       chatJob = viewModelScope.launch {
           delay(500)
           sendMessage()
       }
   }
   ```

**With these optimizations: Support 500+ users on FREE tier!**
### 8.3 Monthly Cost Projections

**Scenario: 1,000 active users**
- AI Chef: 5 requests/month/user = 5,000 requests × $0.002625 = **$13.13/month**
- AI Assistant: 20 messages/month/user = 20,000 messages × $0.0001725 = **$3.45/month**
- **Total:** ~$16.58/month

**Scenario: 10,000 active users**
- AI Chef: **$131.25/month**
- AI Assistant: **$34.50/month**
- **Total:** ~$165.75/month

### 8.4 Cost Optimization Strategies

1. **Caching:** Cache recipe responses for same ingredient combinations (reduce 40% of calls)
2. **Prompt Compression:** Reduce token count in prompts (save 20%)
3. **Flash Model:** Use Flash instead of Pro where possible (save 70%)
4. **Batch Processing:** Combine multiple insights into one API call
5. **Rate Limiting:** Cap free users at 10 requests/day

---

## 9. Timeline Summary

| Phase | Duration | Deliverable |
|-------|----------|-------------|
| **Phase 1: Foundation** | 2 days | Working Gemini API connection |
| **Phase 2: AI Chef** | 3 days | Real recipe generation |
| **Phase 3: AI Assistant** | 4 days | Conversational chatbot with context |
| **Phase 4: Advanced Features** | 3 days | Images, nutrition, memory |
| **Phase 5: Production Hardening** | 2 days | Security, testing, optimization |
| **Total** | **14 days** | Production-ready AI features |

---

## 10. Next Steps

### Immediate Actions:
1. **Get Gemini API Key**
   - Visit: https://ai.google.dev/
   - Create project and enable Gemini API
   - Copy API key to `local.properties`

2. **Add Dependencies**
   - Update `app/build.gradle.kts` with Gemini SDK
   - Enable BuildConfig
   - Sync Gradle

3. **Test Connection**
   - Create simple test activity/composable
   - Call Gemini with "Hello" prompt
   - Verify response

4. **Start Implementation**
   - Follow Phase 1 checklist
   - Create `GeminiApiService.kt`
   - Test with sample prompts

---

## 11. Troubleshooting Guide

### Common Issues:

**1. "API Key not found"**
- Check `local.properties` exists in root directory
- Verify key format: `GEMINI_API_KEY=AIza...`
- Rebuild project to regenerate BuildConfig

**2. "Quota exceeded"**
- Check API quota in Google Cloud Console
- Implement rate limiting
- Consider switching to Flash model

**3. "Invalid JSON response"**
- Improve prompt clarity (specify JSON structure)
- Add JSON validation before parsing
- Log raw response for debugging

**4. "Timeout errors"**
- Increase timeout to 30 seconds
- Implement retry with exponential backoff
- Check network connectivity

**5. "Malformed recipes"**
- Strengthen prompt constraints
- Add response validation
- Provide few-shot examples in prompt

---

## 12. Additional Resources

### Documentation:
- [Gemini API Docs](https://ai.google.dev/docs)
- [Kotlin SDK Guide](https://github.com/google/generative-ai-android)
- [Prompt Engineering Best Practices](https://ai.google.dev/docs/prompt_best_practices)

### Sample Prompts:
- [Recipe generation examples](https://github.com/google-gemini/cookbook)
- [Chat assistant patterns](https://ai.google.dev/examples)

### Community:
- [Stack Overflow](https://stackoverflow.com/questions/tagged/google-gemini)
- [Google AI Discord](https://discord.gg/googleai)

---

## Summary

This plan provides a complete roadmap for integrating Gemini API into Freshly's AI features:

✅ **AI Chef:** Real recipe generation from pantry items  
✅ **AI Assistant:** Context-aware chatbot with streaming responses  
✅ **Security:** Proper API key management and rate limiting  
✅ **Testing:** Comprehensive unit, integration, and UI tests  
✅ **Cost Management:** Estimated $16-165/month for 1K-10K users  
✅ **Timeline:** 14 days to production-ready implementation

**Key Success Factors:**
- Start with Phase 1 to establish foundation
- Test prompts thoroughly with real data
- Implement robust error handling from day 1
- Monitor API costs and optimize continuously
- Prioritize user experience with streaming and caching

Ready to transform Freshly into an AI-powered food management assistant! 🚀
