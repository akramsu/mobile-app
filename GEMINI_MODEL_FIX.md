# Gemini Model Fix

## Issue
Error: `models/gemini-1.5-flash is not found for API version VI`

## Solution Applied
Changed model name from `"gemini-1.5-flash"` to `"gemini-1.5-flash-latest"` in GeminiApiService.kt

## Alternative Models (if issue persists)

If you still get errors, try these models in order:

### Option 1: Gemini Pro (Most Stable)
```kotlin
private const val MODEL_NAME = "gemini-pro"
```
- **Stable and widely available**
- Free tier: 60 requests/minute
- Good for production

### Option 2: Gemini 1.5 Flash (Latest)
```kotlin
private const val MODEL_NAME = "gemini-1.5-flash-latest"
```
- **Currently applied**
- Faster responses
- Free tier: 15 requests/minute

### Option 3: Gemini 1.5 Flash (Explicit Version)
```kotlin
private const val MODEL_NAME = "gemini-1.5-flash-8b-latest"
```
- Lightweight version
- Even faster
- Good for mobile

## How to Test Different Models

1. Open `GeminiApiService.kt`
2. Find line 22: `private const val MODEL_NAME = "..."`
3. Change to desired model name
4. Rebuild: `./gradlew.bat assembleDebug -x lint`
5. Test AI features

## Verify API Key is Working

Run this in terminal to check if key is valid:
```bash
curl -H "Content-Type: application/json" \
-d '{"contents":[{"parts":[{"text":"Say hello"}]}]}' \
"https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY"
```

Replace `YOUR_API_KEY` with your actual key from local.properties.

## Current Configuration
- **Model**: gemini-1.5-flash-latest
- **API Key Location**: local.properties (line 14)
- **Rate Limits**: 15 req/min, 1500 req/day
- **Timeout**: 30 seconds

## Next Steps
1. Install the updated APK on your device/emulator
2. Test AI Chef with 2-3 ingredients
3. If error persists, try changing to `"gemini-pro"` (most stable)
4. Check logcat for detailed error messages

## Additional Troubleshooting

### If API Key Issues:
- Verify key starts with `AIza`
- No quotes or spaces in local.properties
- Rebuild after changing key

### If Network Issues:
- Check internet connection
- Try on different network (mobile data vs WiFi)
- Verify firewall isn't blocking Google AI API

### If Still Not Working:
Check available models for your API key:
```bash
curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_API_KEY"
```

This will list all models accessible with your key.
