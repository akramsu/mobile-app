package com.freshly.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.freshly.app.data.firebase.FirebaseManager
import com.freshly.app.data.repository.PantryRepository
import com.freshly.app.navigation.AppNavGraph
import com.freshly.app.navigation.Screen
import com.freshly.app.notifications.ExpiryCheckWorker
import com.freshly.app.notifications.NotificationHelper
import com.freshly.app.ui.theme.FreshlyTheme
import com.freshly.app.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Schedule periodic expiry checks
        scheduleExpiryCheck()
        
        setContent {
            val context = LocalContext.current
            val preferencesManager = remember { PreferencesManager(context) }
            val isDarkMode by preferencesManager.darkMode.collectAsState(initial = false)
            
            FreshlyTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FreshlyApp()
                }
            }
        }
    }
}

@Composable
fun FreshlyApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val preferencesManager = remember { PreferencesManager(context) }
    
    var startDestination by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Determine start destination
    LaunchedEffect(Unit) {
        scope.launch {
            // Check if user has completed onboarding
            val hasOnboarded = preferencesManager.hasOnboarded.first()
            
            // Check if already authenticated
            val isAuthenticated = FirebaseManager.isAuthenticated
            
            startDestination = when {
                // Not authenticated - show splash then sign in
                !isAuthenticated -> Screen.Splash.route
                // Authenticated but not onboarded - show onboarding
                !hasOnboarded -> Screen.Onboarding.route
                // Authenticated and onboarded - go directly to main
                else -> Screen.Main.route
            }
        }
    }
    
    startDestination?.let { destination ->
        AppNavGraph(
            navController = navController,
            startDestination = destination,
            onOnboardingComplete = {
                scope.launch {
                    preferencesManager.setOnboarded(true)
                }
            }
        )
    }
}

private fun ComponentActivity.scheduleExpiryCheck() {
    // Create periodic work request - runs every 12 hours
    val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(
        repeatInterval = 12,
        repeatIntervalTimeUnit = TimeUnit.HOURS
    ).build()
    
    // Schedule the work (replace existing if already scheduled)
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        ExpiryCheckWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule if already running
        workRequest
    )
    
    Log.d("MainActivity", "Expiry check scheduled to run every 12 hours")
}
