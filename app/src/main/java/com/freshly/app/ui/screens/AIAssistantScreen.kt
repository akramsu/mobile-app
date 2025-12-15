package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.theme.AI500
import com.freshly.app.ui.theme.Primary500
import com.freshly.app.viewmodel.AIAssistantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AIAssistantScreen(
    viewModel: AIAssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val showInsights by viewModel.showInsights.collectAsState()
    val isGeneratingInsights by viewModel.isLoadingInsights.collectAsState()
    val isGeneratingResponse by viewModel.isGeneratingResponse.collectAsState()
    val currentStreamingText by viewModel.currentStreamingText.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppTopBar(title = "AI Assistant")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main content area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = listState
            ) {
                // AI Insights Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = AI500
                            )
                            Text(
                                text = "AI Insights",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Get Insights Button
                        if (!showInsights) {
                            Surface(
                                onClick = {
                                    viewModel.generateInsights()
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(AI500, Color(0xFF7C3AED))
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isGeneratingInsights) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Get My AI Insights",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Insights Card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AI500.copy(alpha = 0.1f),
                                border = androidx.compose.foundation.BorderStroke(2.dp, AI500)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint = AI500,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Your AI Insights",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = AI500
                                        )
                                    }
                                    
                                    // Display each insight
                                    insights["achievement"]?.let { 
                                        Text(it, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) 
                                    }
                                    insights["tip"]?.let { 
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Primary500.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = it,
                                                modifier = Modifier.padding(12.dp),
                                                fontSize = 14.sp,
                                                color = Primary500,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    insights["savings"]?.let { 
                                        Text(it, style = MaterialTheme.typography.bodyMedium) 
                                    }
                                    insights["environmental"]?.let { 
                                        Text(it, style = MaterialTheme.typography.bodyMedium) 
                                    }
                                    insights["urgent"]?.let { 
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = it,
                                                modifier = Modifier.padding(12.dp),
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    // Refresh button
                                    TextButton(
                                        onClick = { viewModel.refreshInsights() },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("Refresh", color = AI500)
                                    }
                                }
                            }
                        }
                        
                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        // Chat Section Header
                        Text(
                            text = "Chat with AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Suggestion chips
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            SuggestionChip(
                                "Give me a recipe using items I already have",
                                onClick = {
                                    viewModel.sendSuggestion("Give me a recipe using items I already have")
                                    scope.launch {
                                        delay(300)
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            )
                            SuggestionChip(
                                "Which food will expire in the next 3 days?",
                                onClick = {
                                    viewModel.sendSuggestion("Which food will expire in the next 3 days?")
                                    scope.launch {
                                        delay(300)
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            )
                            SuggestionChip(
                                "How can I reduce food waste this month?",
                                onClick = {
                                    viewModel.sendSuggestion("How can I reduce food waste this month?")
                                    scope.launch {
                                        delay(300)
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            )
                        }
                    }
                }

                // Messages
                items(messages) { message ->
                    ChatBubble(message)
                }
                
                // Streaming response (current incomplete message)
                if (isGeneratingResponse && currentStreamingText.isNotBlank()) {
                    item {
                        ChatBubble(ChatMessage(currentStreamingText, isUser = false))
                    }
                }
            }

            // Input field (at bottom) - positioned above bottom navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask me anything about your pantry...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                scope.launch {
                                    delay(300)
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank()) Primary500 else Color.Gray
                            ),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) Primary500 else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
