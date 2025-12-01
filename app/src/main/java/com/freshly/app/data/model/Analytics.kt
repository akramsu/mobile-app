package com.freshly.app.data.model

data class AnalyticsData(
    val totalItemsTracked: Int,
    val itemsSaved: Int,
    val itemsWasted: Int,
    val savingsAmount: Float,
    val categoryBreakdown: Map<Category, Int>,
    val monthlyTrends: List<MonthlyData>
)

data class MonthlyData(
    val month: String,
    val saved: Int,
    val wasted: Int
)

data class AIInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val priority: Priority
)

enum class InsightType {
    EXPIRING_ITEMS, WASTE_PATTERN, SHOPPING_TIP, RECIPE_SUGGESTION
}

enum class Priority {
    HIGH, MEDIUM, LOW
}
