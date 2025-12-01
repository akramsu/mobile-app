package com.freshly.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.components.FreshlyCard
import com.freshly.app.ui.components.KPICard
import com.freshly.app.ui.theme.Danger
import com.freshly.app.ui.theme.Primary500

@Composable
fun AnalyticsScreen(
    onViewRecommendations: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(title = "Analytics")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // KPI Cards
            item {
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Saved",
                        value = "24",
                        icon = "✅",
                        color = Primary500,
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Wasted",
                        value = "3",
                        icon = "❌",
                        color = Danger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KPICard(
                        title = "Savings",
                        value = "$89",
                        icon = "💰",
                        color = Primary500,
                        modifier = Modifier.weight(1f)
                    )
                    KPICard(
                        title = "Items",
                        value = "42",
                        icon = "📦",
                        color = Primary500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Chart placeholder
            item {
                Text(
                    text = "Usage Trends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                FreshlyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "📊 Chart View\n(Implement with Vico library)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // AI Insights
            item {
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                FreshlyCard(
                    backgroundColor = com.freshly.app.ui.theme.AI.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "🤖 Great job!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You've reduced waste by 85% this month. Keep buying what you need!",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
