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

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary = PrimaryLight,
    onSecondary = PrimaryDark,
    secondaryContainer = Color(0xFF1E2B47),
    onSecondaryContainer = Color.White,
    tertiary = PrimaryLight,
    onTertiary = PrimaryDark,
    tertiaryContainer = Color(0xFF1E2B47),
    background = SweetOledBg,
    surface = SweetOledSurface,
    onBackground = SweetOledOnSurface,
    onSurface = SweetOledOnSurface,
    onSurfaceVariant = SweetOledOnSurfaceVariant,
    surfaceContainer = SweetOledSurfaceContainer,
    surfaceContainerLow = SweetOledSurfaceLow,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Error,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    tertiary = Violet,
    onTertiary = Color.White,
    tertiaryContainer = VioletLight,
    onTertiaryContainer = VioletDark,
    background = AppBackground,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceSecondary,
    surfaceContainerLow = AppBackground,
    surfaceContainerHigh = PrimaryLight,
    surfaceContainerHighest = PrimarySoft,
    outline = Border,
    outlineVariant = Divider,
    error = Error,
    onError = Color.White,
    errorContainer = ErrorLight,
    onErrorContainer = ErrorDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun SweetPdfTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = MyApplicationTheme(darkTheme, dynamicColor, content)

