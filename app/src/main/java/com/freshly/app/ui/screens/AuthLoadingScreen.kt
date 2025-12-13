package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.repository.PantryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Loading screen that shows Firebase authentication progress
 */
@Composable
fun AuthLoadingScreen(
    onAuthComplete: () -> Unit
) {
    var authStatus by remember { mutableStateOf("Initializing...") }
    var isAuthenticating by remember { mutableStateOf(true) }
    var authSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Step 1: Check Firebase connection
                authStatus = "Connecting to Firebase..."
                delay(500)
                
                // Step 2: Check if already authenticated
                if (FirebaseManager.isAuthenticated) {
                    authStatus = "Already signed in!"
                    authSuccess = true
                    delay(800)
                    onAuthComplete()
                    return@launch
                }
                
                // Step 3: Sign in anonymously
                authStatus = "Creating anonymous account..."
                delay(500)
                
                val result = FirebaseManager.signInAnonymously()
                
                if (result.isSuccess) {
                    authStatus = "Authentication successful!"
                    authSuccess = true
                    
                    // Step 4: Initialize sample data
                    delay(500)
                    authStatus = "Setting up your pantry..."
                    PantryRepository().initializeSampleItems()
                    
                    delay(800)
                    authStatus = "Ready!"
                    delay(500)
                    
                    onAuthComplete()
                } else {
                    authStatus = "Authentication failed"
                    authSuccess = false
                    errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                }
            } catch (e: Exception) {
                authStatus = "Error occurred"
                authSuccess = false
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isAuthenticating = false
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // App Logo/Title
            Text(
                text = "🥗",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Text(
                text = "Freshly",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // Loading indicator
            if (isAuthenticating) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (authSuccess) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color(0xFF41B37C),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Error",
                    tint = Color(0xFFFF5757),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 24.dp)
                )
            }
            
            // Status text
            Text(
                text = authStatus,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF5757).copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Error Details:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFF5757)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onAuthComplete() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continue Anyway")
                }
            }
            
            // Firebase connection status
            Spacer(modifier = Modifier.height(32.dp))
            
            FirebaseStatusCard()
        }
    }
}

@Composable
fun FirebaseStatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Firebase Status",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Auth status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val isAuthConnected = try {
                    FirebaseAuth.getInstance() != null
                } catch (e: Exception) {
                    false
                }
                
                Icon(
                    imageVector = if (isAuthConnected) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isAuthConnected) Color(0xFF41B37C) else Color(0xFFFF5757),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Authentication: ${if (isAuthConnected) "Connected" else "Not Connected"}",
                    fontSize = 12.sp
                )
            }
            
            // Firestore status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val isFirestoreConnected = try {
                    FirebaseFirestore.getInstance() != null
                } catch (e: Exception) {
                    false
                }
                
                Icon(
                    imageVector = if (isFirestoreConnected) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isFirestoreConnected) Color(0xFF41B37C) else Color(0xFFFF5757),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Firestore: ${if (isFirestoreConnected) "Connected" else "Not Connected"}",
                    fontSize = 12.sp
                )
            }
            
            // User ID
            if (FirebaseManager.isAuthenticated) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "User ID: ${FirebaseManager.userId.take(8)}...",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
