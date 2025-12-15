# Cloudinary Setup for Profile Images

This app uses Cloudinary for storing and managing user profile images. Follow these steps to set it up:

## 1. Create a Free Cloudinary Account

1. Go to [https://cloudinary.com/users/register/free](https://cloudinary.com/users/register/free)
2. Sign up for a free account (no credit card required)
3. Verify your email address

## 2. Get Your Cloudinary Credentials

After signing in:

1. Go to your Dashboard: [https://console.cloudinary.com/](https://console.cloudinary.com/)
2. You'll see your **Account Details** at the top:
   - **Cloud Name**: e.g., `dxyz123abc`
   - **API Key**: e.g., `123456789012345`
   - **API Secret**: e.g., `abcdefghijklmnopqrstuvwxyz123` (click "Reveal" to see it)

## 3. Add Credentials to local.properties

1. Open `local.properties` in the project root
2. Add these lines (replace with your actual credentials):

```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name_here
CLOUDINARY_API_KEY=your_api_key_here
CLOUDINARY_API_SECRET=your_api_secret_here
```

Example:
```properties
CLOUDINARY_CLOUD_NAME=dxyz123abc
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdefghijklmnopqrstuvwxyz123
```

## 4. Rebuild the App

After adding the credentials:
```bash
./gradlew clean assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Free Tier Limits

Cloudinary's free tier includes:
- **25 GB** storage
- **25 GB/month** bandwidth
- **25,000 transformations/month**
- Automatic image optimization
- Global CDN

This is more than enough for thousands of users!

## Features

- ✅ User profile image upload
- ✅ Automatic image compression and resizing (max 800px)
- ✅ JPEG optimization (85% quality)
- ✅ Stored in `/avatars/` folder on Cloudinary
- ✅ Fallback to first letter of username if no image
- ✅ Progress indicator during upload

## Security Note

⚠️ Never commit `local.properties` to version control! It's already in `.gitignore`.

## Troubleshooting

**"Cloudinary credentials not configured"**
- Make sure you added all three credentials to `local.properties`
- Rebuild the app after adding credentials

**"Upload failed"**
- Check your internet connection
- Verify credentials are correct
- Check Cloudinary dashboard for quota limits

**Image not showing**
- Clear app data and try again
- Check if the URL in Firestore is valid
- Verify image uploaded successfully in Cloudinary dashboard

## Need Help?

Visit [Cloudinary Documentation](https://cloudinary.com/documentation) for more information.
