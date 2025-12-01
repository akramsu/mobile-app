package com.freshly.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary500,
    onPrimary = OnPrimary,
    primaryContainer = Primary700,
    secondary = AI,
    tertiary = BrandBlue,
    background = BgDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    onBackground = OnSurfaceDark,
    error = Danger,
    onError = OnPrimary,
    surfaceVariant = Color(0xFF2A2A2A),
    outline = DividerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = OnPrimary,
    primaryContainer = Primary700,
    secondary = AI,
    tertiary = BrandBlue,
    background = Bg100,
    surface = Surface100,
    onSurface = OnSurface,
    onBackground = Text100,
    error = Danger,
    onError = OnPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    outline = Divider
)

@Composable
fun FreshlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
