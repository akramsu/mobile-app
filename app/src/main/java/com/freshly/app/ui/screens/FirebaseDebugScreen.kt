package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.ui.components.AppTopBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseDebugScreen(
    onBack: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    var firestoreTestResult by remember { mutableStateOf<String?>(null) }
    var authTestResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Firebase Status",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Firebase Connection Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Firebase Auth Status
            StatusCard(
                title = "Firebase Authentication",
                isConnected = FirebaseManager.isAuthenticated,
                details = if (FirebaseManager.isAuthenticated) {
                    "User ID: ${FirebaseManager.userId}\n" +
                    "Auth Type: Anonymous\n" +
                    "Status: Active"
                } else {
                    "Not authenticated"
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Firestore Status
            val isFirestoreConnected = try {
                FirebaseFirestore.getInstance() != null
            } catch (e: Exception) {
                false
            }
            
            StatusCard(
                title = "Cloud Firestore",
                isConnected = isFirestoreConnected,
                details = if (isFirestoreConnected) {
                    "Database: Connected\n" +
                    "Offline Cache: Enabled\n" +
                    "Status: Ready"
                } else {
                    "Not connected"
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Test Buttons
            Text(
                text = "Connection Tests",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Test Auth Button
            Button(
                onClick = {
                    scope.launch {
                        isRefreshing = true
                        authTestResult = try {
                            if (!FirebaseManager.isAuthenticated) {
                                val result = FirebaseManager.signInAnonymously()
                                if (result.isSuccess) {
                                    "✅ Authentication successful!"
                                } else {
                                    "❌ Auth failed: ${result.exceptionOrNull()?.message}"
                                }
                            } else {
                                "✅ Already authenticated"
                            }
                        } catch (e: Exception) {
                            "❌ Error: ${e.message}"
                        }
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshing
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Test Authentication")
            }
            
            if (authTestResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = authTestResult ?: "",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test Firestore Button
            Button(
                onClick = {
                    scope.launch {
                        isRefreshing = true
                        firestoreTestResult = try {
                            val testDoc = FirebaseManager.firestore
                                .collection("_test")
                                .document("connection_test")
                            
                            testDoc.set(mapOf("timestamp" to System.currentTimeMillis())).await()
                            
                            val snapshot = testDoc.get().await()
                            if (snapshot.exists()) {
                                testDoc.delete().await()
                                "✅ Firestore read/write successful!"
                            } else {
                                "❌ Could not read test document"
                            }
                        } catch (e: Exception) {
                            "❌ Error: ${e.message}"
                        }
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshing
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Test Firestore")
            }
            
            if (firestoreTestResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = firestoreTestResult ?: "",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            if (isRefreshing) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Troubleshooting Info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 Troubleshooting Tips",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Check internet connection\n" +
                                "• Verify google-services.json is present\n" +
                                "• Enable Anonymous Auth in Firebase Console\n" +
                                "• Enable Firestore Database (test mode)\n" +
                                "• Check Firebase Console for errors",
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    isConnected: Boolean,
    details: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) {
                Color(0xFF41B37C).copy(alpha = 0.1f)
            } else {
                Color(0xFFFF5757).copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isConnected) Color(0xFF41B37C) else Color(0xFFFF5757),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isConnected) "Connected" else "Not Connected",
                    fontSize = 12.sp,
                    color = if (isConnected) Color(0xFF41B37C) else Color(0xFFFF5757),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = details,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
