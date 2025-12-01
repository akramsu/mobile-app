package com.freshly.app

import android.os.Bundle
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
import com.freshly.app.navigation.AppNavGraph
import com.freshly.app.navigation.Screen
import com.freshly.app.ui.theme.FreshlyTheme
import com.freshly.app.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
    
    LaunchedEffect(Unit) {
        scope.launch {
            val hasOnboarded = preferencesManager.hasOnboarded.first()
            startDestination = if (hasOnboarded) {
                Screen.Main.route
            } else {
                Screen.Splash.route
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
