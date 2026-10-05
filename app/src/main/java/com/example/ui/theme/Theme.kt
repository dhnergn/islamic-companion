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
    primary = IslamicGreenMedium,
    secondary = IslamicGreenLight,
    tertiary = IslamicGold,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextDarkPrimary,
    onSurface = TextDarkPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IslamicGreenMedium,
    secondary = WarmGoldAccent,
    tertiary = IslamicGold,
    background = CreamBackground,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = TextLightPrimary,
    onBackground = TextLightPrimary,
    onSurface = TextLightPrimary,
    surfaceVariant = Color(0xFFE5DFD1),
    onSurfaceVariant = IslamicGreenDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false to preserve the customized beautiful brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
