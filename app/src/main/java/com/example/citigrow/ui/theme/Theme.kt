package com.example.citigrow.ui.theme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
val Navy = Color(0xFF102A43)
val Blue = Color(0xFF1677C8)
val TealInk = Color(0xFF0B5E50)
val Cloud = Color(0xFFF7F9FC)
val Mint = Color(0xFFE3F5F1)
val Gold = Color(0xFFFFF1C7)
@Composable
fun CitiGrowTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = TealInk,
        background = Cloud, surface = Color.White, onSurface = Navy,
        onBackground = Navy, onPrimary = Color.White, secondaryContainer = Mint), typography = Typography, content = content)
}
