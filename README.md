# Freshly 🥗

A modern Android application that helps users manage their food inventory, discover recipes, and learn about food safety through AI-powered insights and an educational quiz system.

## 📱 Features

### Core Features
- **Food Expiration Tracking**: Track all your food items and get reminded before they expire to minimize waste and save money
- **Smart Expiration Alerts**: Receive timely notifications about food items that are approaching their expiration dates
- **AI-Powered Product Scanning**: Scan product labels with your camera using Gemini Vision AI to automatically extract product name, expiry date, quantity, and category
- **Dual-Photo Scanning**: Capture front and back of products for comprehensive information extraction
- **AI-Powered Food Insights**: Get instant analysis and recommendations about your food items using Google's Gemini AI
- **Quick Insights Dashboard**: View key metrics including expiration warnings, storage tips, health benefits, allergen alerts, and usage suggestions
- **Image Upload**: Upload and store product photos and profile avatars using Cloudinary
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
- **AI Integration**: Google Gemini API (gemini-1.5-flash)
- **Authentication**: Firebase Authentication
- **Image Storage**: Cloudinary
- **Computer Vision**: Google ML Kit (Text Recognition, Barcode Scanning)
- **Background Tasks**: WorkManager for scheduled notifications
- **HTTP Client**: OkHttp3 for API requests

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Async Processing**: Kotlin Coroutines & Flow
- **Data Persistence**: Firestore real-time listeners with local caching

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API 26+)
- Gradle 8.0+
- Firebase account with Firestore and Authentication enabled
- Google AI Studio API key (for Gemini AI)
- Cloudinary account (for image storage)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/akramsu/mobile-app.git
cd mobile-app
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app with package name: `com.freshly.app`
4. Download `google-services.json`
5. Place it in the `app/` directory

### 3. Configure Gemini AI API

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create an API key
3. Open `local.properties` (create if it doesn't exist)
4. Add your API key:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEYS=YOUR_GEMINI_API_KEY_HERE
```

### 4. Configure Cloudinary

1. Visit [Cloudinary](https://cloudinary.com/) and create a free account
2. Go to your [Dashboard](https://console.cloudinary.com/console)
3. Copy your **Cloud Name**, **API Key**, and **API Secret**
4. Add them to `local.properties`:
```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

> **Note**: Cloudinary is used for image uploads (food item photos and user profile avatars). Without proper configuration, image upload features will not work.

### 5. Build the Project

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
│   ├── java/com/freshly/app/
│   │   ├── data/
│   │   │   ├── models/          # Data models (PantryItem, Recipe, QuizQuestion, User)
│   │   │   ├── repository/      # Data repositories
│   │   │   └── firebase/        # Firebase managers
│   │   ├── ui/
│   │   │   ├── screens/         # Composable screens
│   │   │   ├── components/      # Reusable UI components
│   │   │   └── theme/           # Material Design theme
│   │   ├── navigation/          # Navigation graph and routes
│   │   ├── viewmodel/           # ViewModels for MVVM
│   │   ├── utils/               # Utility classes (CloudinaryManager, Gemini scanners)
│   │   ├── notifications/       # Notification helpers and workers
│   │   └── MainActivity.kt      # App entry point
│   ├── res/
│   │   ├── drawable/            # Icons and images
│   │   ├── values/              # Strings, colors, themes
│   │   ├── mipmap/              # Launcher icons
│   │   └── xml/                 # File provider paths
│   └── AndroidManifest.xml      # App manifest with permissions
└── build.gradle.kts             # App-level Gradle config
```

## 🎯 Key Features in Detail

### AI Product Scanning
- **Gemini Vision Integration**: Automatically extract product information from camera images
- **Dual-Photo Support**: Scan front and back of products for complete details
- **Smart Field Extraction**: Auto-fill product name, expiry date, quantity, unit, and category
- **Confidence Scoring**: Visual indicators showing extraction accuracy
- **Cloudinary Integration**: Automatic image upload and optimization (800x800px, JPEG compression)
- **ML Kit Support**: Text recognition and barcode scanning capabilities (optional enhancement)

### AI Food Insights
- **Token Optimization**: Efficient 2048-token limit for fast responses
- **Streaming Support**: Real-time AI response streaming
- **Smart Prompts**: Concise, focused prompts for accurate analysis
- **Error Handling**: Robust error management with user-friendly messages
- **Multi-Model Support**: Gemini 1.5 Flash for fast, accurate responses

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
    namespace = "com.freshly.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.freshly.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### `libs.versions.toml`
Key dependencies:
- Compose BOM: 2024.11.00
- Firebase BOM: 33.7.0
- Kotlin: 2.0.21
- Coroutines: 1.7.3
- AGP: 8.13.1
- Navigation Compose: 2.9.6
- ML Kit Text Recognition: 16.0.0
- ML Kit Barcode Scanning: 17.2.0
- Gemini AI: 0.2.2
- OkHttp: 4.12.0
- Coil: 2.5.0

## 🎨 Design System

- **Primary Color**: Green (#4CAF50)
- **Typography**: Poppins font family
- **Color Scheme**: Material Design 3 dynamic theming
- **Gradients**: Custom gradient borders throughout the app
- **Icons**: Material Icons with custom launcher icon

## 📱 Minimum Requirements

- Android 8.0 (API 26) or higher
- Internet connection for AI features, Firebase sync, and image uploads
- Camera permission for product scanning
- Storage permission for image uploads
- Notification permission for expiration alerts
- ~50 MB storage space

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Google Gemini AI](https://ai.google.dev/) for AI-powered insights and vision scanning
- [Firebase](https://firebase.google.com/) for backend services
- [Cloudinary](https://cloudinary.com/) for image storage and optimization
- [Google ML Kit](https://developers.google.com/ml-kit) for text recognition and barcode scanning
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern Android UI
- [Material Design 3](https://m3.material.io/) for design guidelines
- [OkHttp](https://square.github.io/okhttp/) for efficient HTTP communication
- [Coil](https://coil-kt.github.io/coil/) for image loading

---