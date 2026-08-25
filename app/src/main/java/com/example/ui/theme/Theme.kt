package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoPrimaryContainer,
    onPrimaryContainer = OnIndigoPrimaryContainer,
    secondary = SkyAccent,
    onSecondary = Color.Black,
    secondaryContainer = SkyAccentContainer,
    onSecondaryContainer = Color.White,
    tertiary = AmberAccent,
    background = SophisticatedDarkBackground,
    onBackground = OnBackgroundDark,
    surface = SophisticatedDarkSurface,
    onSurface = OnSurfaceDark,
    surfaceVariant = SophisticatedDarkSurfaceVariant,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = SophisticatedDarkBorder,
    outlineVariant = SophisticatedDarkBorderSubtle,
    error = CrimsonPrimary,
    onError = Color.White,
    errorContainer = CrimsonContainer,
    onErrorContainer = OnCrimsonContainer
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryVariant,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = SkyAccent,
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = CriticalRed,
    onError = Color.White
)

@Composable
fun EmergencyRecorderTheme(
    darkTheme: Boolean = true, // Default to dark theme for emergency/tactical feel & battery efficiency
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    EmergencyRecorderTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
