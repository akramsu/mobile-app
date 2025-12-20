package com.freshly.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freshly.app.data.api.GeminiApiService
import com.freshly.app.data.model.PantryItem
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.data.repository.UserRepository
import com.freshly.app.ui.screens.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for AI Assistant screen
 * Handles chat messages, streaming responses, and insights generation
 */
class AIAssistantViewModel : ViewModel() {
    
    private val geminiService = GeminiApiService()
    private val pantryRepository = PantryRepository()
    private val userRepository = UserRepository()
    
    // Pantry items for quick insights
    val pantryItems: StateFlow<List<PantryItem>> = pantryRepository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Chat messages
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    // AI Insights
    private val _insights = MutableStateFlow<Map<String, String>>(emptyMap())
    val insights: StateFlow<Map<String, String>> = _insights.asStateFlow()
    
    private val _showInsights = MutableStateFlow(false)
    val showInsights: StateFlow<Boolean> = _showInsights.asStateFlow()
    
    // Loading states
    private val _isLoadingInsights = MutableStateFlow(false)
    val isLoadingInsights: StateFlow<Boolean> = _isLoadingInsights.asStateFlow()
    
    private val _isGeneratingResponse = MutableStateFlow(false)
    val isGeneratingResponse: StateFlow<Boolean> = _isGeneratingResponse.asStateFlow()
    
    // Streaming text (accumulates as chunks arrive)
    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Current streaming job (for cancellation)
    private var streamingJob: Job? = null
    
    /**
     * Send a message to AI assistant
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Add user message immediately
        _messages.value += ChatMessage(text, isUser = true)
        _isGeneratingResponse.value = true
        _currentStreamingText.value = ""
        _errorMessage.value = null
        
        // Start streaming response
        streamingJob = viewModelScope.launch {
            try {
                val pantryItems = pantryRepository.items.first()
                
                // Collect streaming chunks
                val fullResponse = StringBuilder()
                
                geminiService.chatStream(text, pantryItems)
                    .collect { chunk ->
                        fullResponse.append(chunk)
                        _currentStreamingText.value = fullResponse.toString()
                    }
                
                // Add complete AI message
                _messages.value += ChatMessage(
                    fullResponse.toString(),
                    isUser = false
                )
                
            } catch (e: Exception) {
                val errorMsg = "Sorry, I encountered an error: ${e.message}"
                _messages.value += ChatMessage(errorMsg, isUser = false)
                _errorMessage.value = errorMsg
            } finally {
                _isGeneratingResponse.value = false
                _currentStreamingText.value = ""
            }
        }
    }
    
    /**
     * Stop generating current response
     */
    fun stopGenerating() {
        streamingJob?.cancel()
        _isGeneratingResponse.value = false
        
        // Add partial response if any
        if (_currentStreamingText.value.isNotBlank()) {
            _messages.value += ChatMessage(
                _currentStreamingText.value + " [Stopped]",
                isUser = false
            )
        }
        
        _currentStreamingText.value = ""
    }
    
    /**
     * Generate personalized insights
     */
    fun generateInsights() {
        if (_isLoadingInsights.value) return
        
        _isLoadingInsights.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                // Small delay for better UX
                delay(500)
                
                val pantryItems = pantryRepository.items.first()
                val user = userRepository.user.first()
                
                android.util.Log.d("AIAssistantViewModel", "Generating insights for ${pantryItems.size} items")
                
                val result = geminiService.generateInsights(
                    pantryItems,
                    user.name,
                    user.dietaryRestrictions
                )
                
                if (result.isSuccess) {
                    val insightsMap = result.getOrNull() ?: emptyMap()
                    android.util.Log.d("AIAssistantViewModel", "Got ${insightsMap.size} insights: ${insightsMap.keys}")
                    _insights.value = insightsMap
                    _showInsights.value = true
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to generate insights"
                    android.util.Log.e("AIAssistantViewModel", "Insights generation failed: $error")
                    _errorMessage.value = error
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AIAssistantViewModel", "Exception generating insights", e)
                _errorMessage.value = "Error generating insights: ${e.message}"
            } finally {
                _isLoadingInsights.value = false
            }
        }
    }
    
    /**
     * Refresh insights (with cooldown)
     */
    fun refreshInsights() {
        _showInsights.value = false
        _insights.value = emptyMap()
        generateInsights()
    }
    
    /**
     * Clear all messages
     */
    fun clearMessages() {
        _messages.value = emptyList()
        _currentStreamingText.value = ""
        _errorMessage.value = null
    }
    
    /**
     * Dismiss error
     */
    fun dismissError() {
        _errorMessage.value = null
    }
    
    /**
     * Get API quota information
     */
    fun getQuotaInfo(): Pair<Int, Int> {
        return geminiService.getRemainingQuota()
    }
    
    /**
     * Get daily usage percentage
     */
    fun getDailyUsagePercentage(): Int {
        return geminiService.getDailyUsagePercentage()
    }
    
    /**
     * Send suggestion chip message
     */
    fun sendSuggestion(suggestionText: String) {
        sendMessage(suggestionText)
    }
}
