package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledColorScheme =
  darkColorScheme(
    primary = GasEmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = GasEmeraldLight,
    secondary = GasOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color.White,
    tertiary = GasBlueLight,
    background = Color.Black,
    surface = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF262626),
    outlineVariant = Color(0xFF181818)
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = GasEmeraldLight,
    onPrimary = GasSlateDarker,
    primaryContainer = GasEmeraldDark,
    onPrimaryContainer = GasEmeraldLight,
    secondary = GasOrangeLight,
    onSecondary = GasSlateDarker,
    secondaryContainer = GasOrangeAccent,
    onSecondaryContainer = Color.White,
    tertiary = GasBlueLight,
    background = GasSlateDarker,
    surface = GasSlateDark,
    onBackground = GasSlateLighter,
    onSurface = GasSlateLighter,
    surfaceVariant = GasSlate,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = GasSlateLight,
    outlineVariant = GasSlate
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GasEmerald,
    onPrimary = Color.White,
    primaryContainer = BentoGreenContainer,
    onPrimaryContainer = BentoGreenText,
    secondary = GasOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = GasBlueAccent,
    background = BentoBg,
    surface = Color.White,
    onBackground = BentoTextPrimary,
    onSurface = BentoTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = BentoTextSecondary,
    outline = BentoBorder,
    outlineVariant = BentoBorder
  )

@Composable
fun GasolinaHoyTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  isAmoled: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      isAmoled -> AmoledColorScheme
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).apply {
          isAppearanceLightStatusBars = !darkTheme
          isAppearanceLightNavigationBars = !darkTheme
        }
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  isAmoled: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  GasolinaHoyTheme(darkTheme = darkTheme, isAmoled = isAmoled, dynamicColor = dynamicColor, content = content)
}



