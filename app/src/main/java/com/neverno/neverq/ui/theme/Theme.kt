package com.neverno.neverq.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// NeverQ brand: Blue primary, Red accent
private val NeverQBlue = Color(0xFF1E3A5F)
private val NeverQRed = Color(0xFFD32F2F)
private val NeverQLightBlue = Color(0xFF2E5E9E)

private val LightColorScheme = lightColorScheme(
    primary = NeverQBlue,
    secondary = NeverQRed,
    tertiary = Color(0xFF388E3C),
    background = Color(0xFFF5F5F5),
    surface = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = NeverQLightBlue,
    secondary = Color(0xFFEF5350),
    tertiary = Color(0xFF66BB6A),
)

@Composable
fun NeverQTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}
