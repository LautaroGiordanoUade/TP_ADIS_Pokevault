package com.pokevault.mobile.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = MarketOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Ink,
    secondary = MarketOrangeDark,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SoftGray,
    onSurfaceVariant = Muted,
    outline = LineGray,
    error = Color(0xFFB00020),
)

private val DarkScheme = darkColorScheme(
    primary = MarketOrangeLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF5A3200),
    onPrimaryContainer = Color(0xFFFFDDB0),
    secondary = MarketOrange,
    onSecondary = Color.Black,
    background = DarkInk,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MutedDark,
    outline = Color(0xFF4B4D58),
    error = Color(0xFFFFB4AB),
)

@Composable
fun PokeMarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkScheme
        else -> LightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PokeTypography,
        content = content,
    )
}
