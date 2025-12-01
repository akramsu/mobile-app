package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.ui.components.PrimaryButton
import com.freshly.app.ui.components.SecondaryButton
import com.freshly.app.ui.theme.Primary500
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            emoji = "📦",
            title = "Track Your Food",
            description = "Keep tabs on everything in your pantry, fridge, and freezer"
        ),
        OnboardingPage(
            emoji = "♻️",
            title = "Reduce Waste",
            description = "Get notified before items expire and save money"
        ),
        OnboardingPage(
            emoji = "👨‍🍳",
            title = "AI-Powered Recipes",
            description = "Create delicious meals with ingredients you already have"
        )
    )
    
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacer (removed skip button)
            Spacer(modifier = Modifier.height(56.dp))
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Pager
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }
            
            // Page indicator
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.padding(16.dp),
                activeColor = Primary500,
                inactiveColor = Primary500.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action button - Next for pages 1-2, Get Started for page 3
            PrimaryButton(
                text = if (isLastPage) "Get Started" else "Next",
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.emoji,
            fontSize = 120.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
