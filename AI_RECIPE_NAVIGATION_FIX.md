# AI Recipe Navigation Fix

## Problem
When users generated recipes using the AI Chef, clicking on them would show "Recipe not found" error. This happened because:
1. AI-generated recipes had random UUID identifiers
2. These recipes existed only in the AIChefViewModel's memory state
3. RecipeRepository.getRecipeById() only checked static recipes and Firebase
4. Navigation passed the recipe ID, but the repository couldn't find it

## Solution
Implemented an in-memory cache for AI-generated recipes in RecipeRepository:

### Changes Made

#### 1. Added Recipe Cache
```kotlin
// RecipeRepository.kt
private val generatedRecipeCache = mutableMapOf<String, Recipe>()
```

#### 2. Cache Recipes When Generated
When recipes are successfully generated, they're automatically cached:
```kotlin
recipes.forEach { recipe ->
    generatedRecipeCache[recipe.id] = recipe
    Log.d("RecipeRepository", "Cached generated recipe: ${recipe.id} - ${recipe.title}")
}
```

#### 3. Updated Recipe Lookup Priority
The `getRecipeById()` method now searches in this order:
1. **AI-generated cache** (new) - finds freshly generated recipes
2. **Static recommended recipes** - the 4 pre-defined recipes on home screen
3. **Firebase** - user's saved recipes

```kotlin
suspend fun getRecipeById(id: String): Recipe? {
    // First check AI-generated recipe cache
    generatedRecipeCache[id]?.let { 
        return it 
    }
    
    // Then check static recommended recipes
    val staticRecipe = getRecommendedRecipes().find { it.id == id }
    if (staticRecipe != null) return staticRecipe
    
    // Finally try Firebase
    // ... Firebase logic
}
```

#### 4. Added Cache Management
```kotlin
fun clearGeneratedRecipeCache() {
    generatedRecipeCache.clear()
}
```

## How It Works Now

1. **User generates recipes** in AI Chef screen
2. **Recipes are created** with random UUIDs
3. **Repository caches** each recipe by its ID
4. **User clicks** on a generated recipe
5. **Navigation** passes the recipe ID
6. **RecipeDetailScreen** calls `getRecipeById(id)`
7. **Repository finds** the recipe in the cache
8. **Recipe displays** successfully!

## Recipe Types Supported

| Recipe Type | Storage | Lifetime | Example IDs |
|------------|---------|----------|-------------|
| Static Recommended | Hardcoded in code | Permanent | `pasta-primavera`, `chicken-teriyaki-bowl` |
| AI-Generated | In-memory cache | Until app closes | Random UUIDs like `a1b2c3d4-...` |
| User Saved | Firebase Firestore | Permanent | Custom IDs or UUIDs |

## Performance Impact
- **Memory**: Minimal - only stores recipes user actively generates
- **Speed**: Instant lookup - no network call needed
- **Cache size**: Grows only when user generates recipes, cleared on app restart

## Future Enhancements
Consider these improvements:
1. **Persist to Firebase**: Save generated recipes automatically
2. **Cache expiry**: Clear old recipes after 24 hours
3. **Favorite marking**: Let users save generated recipes permanently
4. **Recipe history**: Show previously generated recipes

## Testing
To verify the fix works:
1. Open AI Chef screen
2. Select ingredients and generate recipes
3. Wait for recipes to appear
4. Click on any generated recipe
5. Recipe details should display correctly (no "not found" error)

## Related Files
- [data/repository/RecipeRepository.kt](app/src/main/java/com/freshly/app/data/repository/RecipeRepository.kt) - Cache implementation
- [ui/screens/AIChefScreen.kt](app/src/main/java/com/freshly/app/ui/screens/AIChefScreen.kt) - Recipe generation UI
- [ui/screens/OtherScreens.kt](app/src/main/java/com/freshly/app/ui/screens/OtherScreens.kt) - RecipeDetailScreen
