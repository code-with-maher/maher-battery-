package com.maher.powerpulse.ui.theme

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

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF10B981),        // Battery Green
    secondary = Color(0xFF3B82F6),      // Battery Blue
    tertiary = Color(0xFFF59E0B),       // Orange Warning
    background = Color(0xFF0B0F19),     // Cosmic Background
    surface = Color(0xFF151D30),        // Cosmic Surface
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onPrimary = Color.White
  )

private val LightColorScheme = DarkColorScheme // Enforce high-end dark look everywhere

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for the dark aesthetic
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve branding
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
