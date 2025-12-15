package com.freshly.app.data.api

import com.freshly.app.data.model.PantryItem

/**
 * Utility for building optimized prompts for Gemini API
 */
object GeminiPromptBuilder {
    
    /**
     * Build prompt for recipe generation from pantry items
     */
    fun buildRecipePrompt(
        ingredients: List<String>,
        dietaryPreferences: List<String> = emptyList(),
        skillLevel: String = "Medium"
    ): String {
        val ingredientList = ingredients.joinToString(", ")
        val dietary = if (dietaryPreferences.isNotEmpty()) " ${dietaryPreferences.joinToString(", ")}" else ""
        
        return """
2 recipes: $ingredientList$dietary
JSON:
[{"title":"","description":"","cookTime":25,"servings":2,"difficulty":"Easy","ingredients":[{"name":"","amount":"","isMatched":true}],"steps":[""],"tags":[""]}]
3 steps max, brief
        """.trimIndent()
    }
    
    /**
     * Build prompt for AI chat assistant
     */
    fun buildChatPrompt(
        userMessage: String,
        pantryContext: String
    ): String {
        return """
You are a friendly AI assistant specializing in food management and reducing food waste. You help users manage their pantry, suggest recipes, and provide cooking tips.

Current pantry state:
$pantryContext

User question: $userMessage

Guidelines:
- Be concise and friendly (2-4 sentences max unless explaining a recipe)
- Use emojis sparingly but appropriately (🎉 💡 ⚡ 🌱 💰)
- If suggesting recipes, mention specific items from the pantry
- If discussing expiry dates, be specific and urgent for items expiring soon
- Provide actionable advice, not just information
- If user asks about recipes, suggest 1-2 specific ideas with item names
- If user asks about expiring items, list them with days remaining

Keep responses natural and conversational.
        """.trimIndent()
    }
    
    /**
     * Build prompt for generating personalized insights
     */
    fun buildInsightsPrompt(
        pantryData: Map<String, Any>,
        userProfile: Map<String, Any>
    ): String {
        val totalItems = pantryData["totalItems"] ?: 0
        val expiringSoon = pantryData["expiringSoon"] ?: 0
        val expiredThisMonth = pantryData["expiredThisMonth"] ?: 0
        val topCategories = pantryData["topCategories"] ?: emptyList<String>()
        val expiringItemsList = pantryData["expiringItemsList"] ?: emptyList<String>()
        
        val dietaryRestrictions = (userProfile["dietaryRestrictions"] as? List<*>)?.joinToString(", ") ?: "None"
        val userName = userProfile["userName"] ?: "there"
        
        return """
Analyze this user's food management data and generate personalized, actionable insights.

User: $userName
Pantry Overview:
- Total items: $totalItems
- Expiring soon (≤3 days): $expiringSoon items
- Expired this month: $expiredThisMonth items
- Top categories: $topCategories
- Items expiring soon: $expiringItemsList

User Profile:
- Dietary preferences: $dietaryRestrictions

Generate 4-5 specific insights in this exact format (one insight per line):

🎉 [Positive achievement or progress - be specific and encouraging]

💡 [Actionable tip using specific item names from the pantry]

💰 [Estimated money saved or waste reduced - use realistic numbers based on expired/expiring items]

🌱 [Environmental impact statement - quantify if possible]

⚡ [URGENT action for items expiring in 1-3 days - list specific items]

Requirements:
- Be specific - use actual item names when available
- Estimate concrete numbers (e.g., "$12 saved", "2 lbs waste prevented")
- Make it personal and encouraging
- Focus on actionable advice, not generic tips
- If no items expiring, focus on achievement and maintenance tips
        """.trimIndent()
    }
    
    /**
     * Build pantry context string from items
     */
    fun buildPantryContext(items: List<PantryItem>, maxItems: Int = 15): String {
        if (items.isEmpty()) {
            return "User's pantry is currently empty."
        }
        
        return buildString {
            appendLine("User's pantry items:")
            items.take(maxItems).forEach { item ->
                val expiry = item.getDaysUntilExpiry()
                val status = when {
                    expiry < 0 -> "⚠️ EXPIRED ${-expiry} days ago"
                    expiry == 0 -> "⚠️ EXPIRES TODAY"
                    expiry <= 3 -> "⚡ Expiring in $expiry days"
                    else -> "Fresh ($expiry days left)"
                }
                appendLine("- ${item.name} (${item.quantity} ${item.unit}) - $status - ${item.category}")
            }
            if (items.size > maxItems) {
                appendLine("... and ${items.size - maxItems} more items")
            }
        }
    }
    
    /**
     * Build prompt for generating detailed recipe view
     */
    fun buildDetailedRecipePrompt(
        title: String,
        ingredients: List<String>
    ): String {
        return """
Recipe: $title
Ingredients: ${ingredients.joinToString(", ")}

Generate brief recipe:
- 2-3 sentence description
- 3 simple steps
- 2-3 cooking tips
- cookTime (minutes)

JSON:
{"description":"","steps":[""],"tips":[""],"cookTime":25}
        """.trimIndent()
    }
}
