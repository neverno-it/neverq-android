package com.neverno.neverq.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours — matches neverno.in web exactly ───────────────────────────
val CfBlue        = Color(0xFF1565C0)
val CfBlueDark    = Color(0xFF0D47A1)
val CfBlueLight   = Color(0xFFE8F1FF)
val CfNavy        = Color(0xFF15233B)
val CfNavyDeep    = Color(0xFF202B7A)
val CfRed         = Color(0xFFC62828)
val CfRedLight    = Color(0xFFFDEEEE)
val CfGreen       = Color(0xFF2E7D32)
val CfGreenLight  = Color(0xFFE8F5E9)
val CfOrange      = Color(0xFFE65100)
val CfOrangeLight = Color(0xFFFFF3E0)
val CfPurple      = Color(0xFF6A1B9A)
val CfPurpleLight = Color(0xFFF3E5F5)
val CfSurface     = Color(0xFFF6F8FC)
val CfBorder      = Color(0xFFE4E7F2)
val CfText        = Color(0xFF172033)
val CfMuted       = Color(0xFF6B7280)

private val LightColorScheme = lightColorScheme(
    primary             = CfBlue,
    onPrimary           = Color.White,
    primaryContainer    = CfBlueLight,
    onPrimaryContainer  = CfBlueDark,
    secondary           = CfNavy,
    onSecondary         = Color.White,
    secondaryContainer  = CfBlueLight,
    onSecondaryContainer = CfNavy,
    tertiary            = CfGreen,
    onTertiary          = Color.White,
    tertiaryContainer   = CfGreenLight,
    error               = CfRed,
    onError             = Color.White,
    errorContainer      = CfRedLight,
    background          = CfSurface,
    onBackground        = CfText,
    surface             = Color.White,
    onSurface           = CfText,
    surfaceVariant      = CfBlueLight,
    onSurfaceVariant    = CfMuted,
    outline             = CfBorder,
)

@Composable
fun NeverQTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = Typography(),
        content     = content,
    )
}
