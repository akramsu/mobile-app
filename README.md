# Freshly 🥗

A modern Android application that helps users manage their food inventory, discover recipes, and learn about food safety through AI-powered insights and an educational quiz system.

## 📱 Features

### Core Features
- **Food Expiration Tracking**: Track all your food items and get reminded before they expire to minimize waste and save money
- **Smart Expiration Alerts**: Receive timely notifications about food items that are approaching their expiration dates
- **AI-Powered Food Insights**: Get instant analysis and recommendations about your food items using Google's Gemini AI
- **Quick Insights Dashboard**: View key metrics including expiration warnings, storage tips, health benefits, allergen alerts, and usage suggestions
- **Recipe Management**: Save, search, and organize your favorite recipes with detailed ingredients and step-by-step instructions
- **Recent Recipes**: Quick access to your 5 most recently saved recipes on the home screen
- **AI Recipe Generation**: Generate custom recipes based on available ingredients using AI
- **YouTube Integration**: Search for recipe tutorials directly from the app
- **Food Safety Quiz**: Test your knowledge with 65+ questions covering expiration dates, storage methods, food safety, and nutrition

### User Experience
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Gradient Borders**: Elegant styling throughout the app with custom gradient effects
- **Real-time Search**: Filter saved recipes instantly by title, description, or ingredients
- **Responsive Design**: Smooth animations and intuitive navigation
- **Profile Management**: Track your recipes, achievements, and food safety knowledge

## 🛠️ Tech Stack

### Frontend
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material Design 3
- **Navigation**: Jetpack Navigation Compose
- **State Management**: StateFlow, ViewModel

### Backend & Services
- **Database**: Firebase Firestore
- **AI Integration**: Google Gemini API (gemini-2.5-flash)
- **Authentication**: Firebase Authentication

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines & Flow
- **Data Persistence**: Firestore real-time listeners with local caching

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API 24+)
- Gradle 8.0+
- Firebase account with Firestore enabled
- Google AI Studio API key (for Gemini AI)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/akramsu/mobile-app.git
cd mobile-app
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app with package name: `com.example.freshly`
4. Download `google-services.json`
5. Place it in the `app/` directory

### 3. Configure Gemini AI API

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create an API key
3. Open `local.properties` (create if it doesn't exist)
4. Add your API key:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
gemini.api.key=YOUR_GEMINI_API_KEY_HERE
```

### 4. Build the Project

Using Android Studio:
- Open the project in Android Studio
- Wait for Gradle sync to complete
- Click **Run** or press `Shift + F10`

Using Command Line:
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## 📂 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/freshly/
│   │   ├── data/
│   │   │   ├── models/          # Data models (Recipe, QuizQuestion)
│   │   │   └── repository/      # Data repositories
│   │   ├── ui/
│   │   │   ├── screens/         # Composable screens
│   │   │   ├── components/      # Reusable UI components
│   │   │   ├── theme/           # Material Design theme
│   │   │   └── navigation/      # Navigation graph
│   │   ├── viewmodel/           # ViewModels for MVVM
│   │   ├── services/            # AI services (Gemini API)
│   │   └── MainActivity.kt      # App entry point
│   └── res/
│       ├── drawable/            # Icons and images
│       ├── values/              # Strings, colors, themes
│       └── mipmap/              # Launcher icons
└── build.gradle.kts             # App-level Gradle config
```

## 🎯 Key Features in Detail

### AI Food Insights
- **Token Optimization**: Efficient 2048-token limit for fast responses
- **Streaming Support**: Real-time AI response streaming
- **Smart Prompts**: Concise, focused prompts for accurate analysis
- **Error Handling**: Robust error management with user-friendly messages

### Recipe Management
- **Firestore Integration**: Real-time sync across devices
- **Timestamp Tracking**: Automatic `savedAt` field for sorting
- **Search Functionality**: Filter by multiple criteria
- **YouTube Integration**: One-click recipe tutorial search

### Profile System
- **Recipe Stats**: Track saved recipes count
- **Knowledge Score**: Quiz performance tracking
- **Quick Actions**: Edit profile, view recipes, take quiz
- **Elegant Design**: Horizontal button layout with gradient borders

## 🔧 Configuration Files

### `build.gradle.kts` (App Level)
```kotlin
android {
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
}
```

### `libs.versions.toml`
Key dependencies:
- Compose BOM: 2024.04.01
- Firebase BOM: 33.0.0
- Kotlin: 1.9.0
- Coroutines: 1.7.3

## 🎨 Design System

- **Primary Color**: Green (#4CAF50)
- **Typography**: Poppins font family
- **Color Scheme**: Material Design 3 dynamic theming
- **Gradients**: Custom gradient borders throughout the app
- **Icons**: Material Icons with custom launcher icon

## 📱 Minimum Requirements

- Android 7.0 (API 24) or higher
- Internet connection for AI features and Firebase sync
- ~50 MB storage space

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Developer

**Akram**
- GitHub: [@akramsu](https://github.com/akramsu)

## 🙏 Acknowledgments

- [Google Gemini AI](https://ai.google.dev/) for AI-powered insights
- [Firebase](https://firebase.google.com/) for backend services
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern Android UI
- [Material Design 3](https://m3.material.io/) for design guidelines

---