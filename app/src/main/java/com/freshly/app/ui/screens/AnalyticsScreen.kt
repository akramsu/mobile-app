package com.freshly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshly.app.ui.components.AppTopBar
import com.freshly.app.ui.theme.*

@Composable
fun AnalyticsScreen(
    onViewRecommendations: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Analytics"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // This Month Section
            item {
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // KPI Cards Grid - 2x2
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: Saved and Wasted
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Saved",
                            value = "24",
                            icon = "✅",
                            backgroundColor = Color(0xFFD4F4DD),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Wasted",
                            value = "3",
                            icon = "❌",
                            backgroundColor = Color(0xFFFFDFDF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Row 2: Savings and Items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Savings",
                            value = "$89",
                            icon = "💰",
                            backgroundColor = Color(0xFFFFF4D6),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Items",
                            value = "42",
                            icon = "📦",
                            backgroundColor = Color(0xFFE8E8E8),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Usage Trends Section
            item {
                Text(
                    text = "Usage Trends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Usage This Month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Used progress bar
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Used",
                                    modifier = Modifier.width(80.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.67f)
                                            .background(Primary500)
                                    )
                                }
                                Text(
                                    text = "21",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            // Wasted progress bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Wasted",
                                    modifier = Modifier.width(80.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.25f)
                                            .background(Danger)
                                    )
                                }
                                Text(
                                    text = "3",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            
            // AI Insights Section
            item {
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Left colored border
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(120.dp)
                                .background(AI500)
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "AI INSIGHTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AI500,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "You've made 12 recipes this month! Keep it up to unlock the \"Master Chef\" badge.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = onViewRecommendations,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "View Recommendations →",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AI500
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.5f),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = when(label) {
                        "Saved" -> Primary500
                        "Wasted" -> Danger
                        "Savings" -> Warning500
                        "Items" -> Primary500
                        else -> Color.Black
                    }
                )
            }
            Text(
                text = icon,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
