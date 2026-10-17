package com.example.citigrow.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
val Navy = Color(0xFF102A43)
val Blue = Color(0xFF1677C8)
val Teal = Color(0xFF19A38C)
val TealLight = Color(0xFF5CC9B5)
val TealInk = Color(0xFF0B5E50)
val Cloud = Color(0xFFF7F9FC)
val Mint = Color(0xFFE3F5F1)
val Gold = Color(0xFFFFF1C7)
val GoldFill = Color(0xFFF0B429)
val GoldInk = Color(0xFF7A4F00)
val GoldSurface = Color(0xFFFFF8E6)
val BlueTint = Color(0xFFE6F0FA)
val BlueInk = Color(0xFF0F4C81)
val Border = Color(0xFFE4E9F0)
val InputBorder = Color(0xFFD9E2EC)
val Muted = Color(0xFF52606D)
val GreyTint = Color(0xFFEEF2F6)
val PurpleTint = Color(0xFFEAE8FB)
val PurpleInk = Color(0xFF3E3796)
val RedTint = Color(0xFFFDECEA)
val RedInk = Color(0xFF9B1C1C)
@Composable
fun CitiGrowTheme(content: @Composable () -> Unit) {
    val typography = growTypography()
    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = TealInk,
        background = Cloud, surface = Color.White, onSurface = Navy, surfaceTint = Color.Transparent,
        onSurfaceVariant = Muted, surfaceVariant = GreyTint, outline = InputBorder, outlineVariant = Border,
        primaryContainer = BlueTint, onPrimaryContainer = BlueInk, tertiary = GoldFill,
        error = RedInk, errorContainer = RedTint,
        onBackground = Navy, onPrimary = Color.White, secondaryContainer = Mint), typography = typography, content = content)
}
