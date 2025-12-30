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
[{"title":"","description":"","cookTime":25,"servings":2,"difficulty":"Easy","ingredients":[{"name":"","amount":"","isMatched":true}],"steps":[""],"tags":[""],"youtubeVideoLink":""}]
For youtubeVideoLink: ALWAYS return a YouTube search URL in this format: https://www.youtube.com/results?search_query=RECIPE_NAME+recipe (replace spaces with +). Example: "Chicken Stir Fry" becomes https://www.youtube.com/results?search_query=Chicken+Stir+Fry+recipe
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
        val totalItems = pantryData["totalItems"] as? Int ?: 0
        val expiringSoon = pantryData["expiringSoon"] as? Int ?: 0
        val expiredThisMonth = pantryData["expiredThisMonth"] as? Int ?: 0
        @Suppress("UNCHECKED_CAST")
        val topCategories = (pantryData["topCategories"] as? List<String>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val expiringItemsList = (pantryData["expiringItemsList"] as? List<String>) ?: emptyList()
        
        @Suppress("UNCHECKED_CAST")
        val dietaryRestrictions = (userProfile["dietaryRestrictions"] as? List<String>)?.joinToString(", ") ?: "None"
        val userName = userProfile["userName"] ?: "there"
        
        return """
Pantry: $totalItems items, $expiringSoon expiring${if (expiringItemsList.isNotEmpty()) ": ${expiringItemsList.take(3).joinToString(", ")}" else ""}

Generate concise insights JSON (1-2 sentences max each):

{"achievement":"🎉 Congrats message with waste prevented","tip":"💡 Quick recipe using ${if (expiringItemsList.isNotEmpty()) "expiring items" else "pantry items"}","savings":"💰 Money/meals saved estimate","urgent":"${if (expiringSoon > 0) "⚡ List items expiring with action" else "✅ Nothing expiring"}","environmental":"🌍 Water/CO2 saved"}

Keep brief and actionable. Return only JSON.
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
- YouTube search link: Return YouTube search URL in format: https://www.youtube.com/results?search_query=$title+recipe (replace spaces with +)

JSON:
{"description":"","steps":[""],"tips":[""],"cookTime":25,"youtubeVideoLink":""}
        """.trimIndent()
    }
}
