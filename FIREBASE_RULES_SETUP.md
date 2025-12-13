# Firebase Security Rules Setup

## Problem
Your app can't access Firestore because the default security rules block all access.

## Quick Fix (5 minutes)

### 1. Open Firebase Console
Go to: https://console.firebase.google.com/
Select your "Freshly" project

### 2. Set Up Firestore Security Rules

1. In left sidebar, click **"Firestore Database"**
2. Click the **"Rules"** tab at the top
3. Replace the existing rules with:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // User documents - users can only read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Pantry items subcollection
      match /pantryItems/{itemId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Recipes subcollection
      match /recipes/{recipeId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Achievements subcollection
      match /achievements/{achievementId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

4. Click **"Publish"** button

### 3. Enable Anonymous Authentication

1. In left sidebar, click **"Authentication"**
2. Click **"Get started"** (if first time)
3. Click the **"Sign-in method"** tab
4. Find **"Anonymous"** in the list
5. Click on it and toggle **"Enable"**
6. Click **"Save"**

### 4. Test Your App

Run the app again and check Firebase Status screen. You should now see:
- ✅ Firebase Authentication: Connected
- ✅ Cloud Firestore: Connected
- ✅ Test Authentication: Success
- ✅ Test Firestore: Success

## What These Rules Do

### Security Model
- Each user can only access their own data
- Anonymous users get a unique `userId` automatically
- Data is organized under `/users/{userId}/...`

### Structure
```
/users
  /{userId}              ← User profile
    /pantryItems         ← User's pantry items
      /{itemId}
    /recipes             ← User's saved recipes
      /{recipeId}
    /achievements        ← User's achievements
      /{achievementId}
```

## Production Rules (Optional - For Later)

For production, add validation:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow create: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth != null && request.auth.uid == userId
                    && request.resource.data.userId == userId;
      
      match /pantryItems/{itemId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /recipes/{recipeId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /achievements/{achievementId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

## Troubleshooting

### Still getting "PERMISSION_DENIED"?
1. Make sure you published the rules (not just saved)
2. Wait 30 seconds for rules to propagate
3. Restart your app completely
4. Check that Anonymous auth is enabled

### "Auth failed: This operation is restricted to administrators"?
1. Make sure Anonymous authentication is **Enabled** in Firebase Console
2. Go to Authentication → Sign-in method → Anonymous → Toggle ON

### Need to reset?
1. In Firestore, delete all documents (or the whole database)
2. In Authentication, delete all users
3. Restart your app - it will create a fresh anonymous user

## Next Steps

Once working, you can:
1. Add email/password authentication later
2. Add data validation rules
3. Set up indexes for complex queries
4. Enable backups in Firestore settings
