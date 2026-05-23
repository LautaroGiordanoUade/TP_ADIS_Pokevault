package com.pokevault.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = MarketOrange,
    onPrimary = Color.Black,
    secondary = Ink,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = SoftGray,
    outline = LineGray,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun PokeMarketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = PokeTypography,
        content = content,
    )
}
