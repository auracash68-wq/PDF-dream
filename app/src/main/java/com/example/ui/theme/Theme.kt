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
    primary = SweetOrange,
    onPrimary = Color.White,
    primaryContainer = SweetOrangeDark,
    onPrimaryContainer = SweetOrangeFixed,
    secondary = SweetBlue,
    onSecondary = Color.White,
    secondaryContainer = SweetBlueContainer,
    onSecondaryContainer = Color.White,
    tertiary = SweetEmerald,
    onTertiary = Color.White,
    tertiaryContainer = SweetEmeraldContainer,
    background = SweetOledBg,
    surface = SweetOledSurface,
    onBackground = SweetOledOnSurface,
    onSurface = SweetOledOnSurface,
    onSurfaceVariant = SweetOledOnSurfaceVariant,
    surfaceContainer = SweetOledSurfaceContainer,
    surfaceContainerLow = SweetOledSurfaceLow,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SweetOrange,
    onPrimary = Color.White,
    primaryContainer = SweetOrangeDark,
    onPrimaryContainer = SweetOrangeFixed,
    secondary = SweetBlue,
    onSecondary = Color.White,
    secondaryContainer = SweetBlueFixed,
    onSecondaryContainer = SweetBlueOnFixedVariant,
    tertiary = SweetEmerald,
    onTertiary = Color.White,
    tertiaryContainer = SweetEmeraldFixed,
    onTertiaryContainer = SweetEmeraldOnFixed,
    background = SweetBg,
    surface = SweetSurface,
    onBackground = SweetOnSurface,
    onSurface = SweetOnSurface,
    onSurfaceVariant = SweetOnSurfaceVariant,
    surfaceContainer = SweetSurfaceContainer,
    surfaceContainerLow = SweetSurfaceLow,
    surfaceContainerHigh = SweetSurfaceContainerHigh,
    surfaceContainerHighest = SweetSurfaceContainerHighest,
    outline = SweetOutline,
    outlineVariant = SweetOutlineVariant,
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

