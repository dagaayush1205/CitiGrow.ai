package com.example.citigrow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Noto assets ship offline. Baloo display faces can be added without changing screen layouts. */
@Composable fun growTypography(language: String = "en"): Typography {
    val assets = LocalContext.current.assets
    val latin = remember(assets) { FontFamily(
        Font("fonts/noto_sans_regular.ttf", assets, FontWeight.Normal),
        Font("fonts/noto_sans_medium.ttf", assets, FontWeight.Medium),
        Font("fonts/noto_sans_semibold.ttf", assets, FontWeight.SemiBold),
        Font("fonts/noto_sans_bold.ttf", assets, FontWeight.Bold)
    ) }
    val body = remember(assets, language) { when (language) {
        "hi" -> FontFamily(Font("fonts/noto_sans_devanagari.ttf", assets))
        "ta" -> FontFamily(Font("fonts/noto_sans_tamil.ttf", assets))
        else -> latin
    } }
    // Optional original display assets; never silently download fonts at app startup.
    val display = remember(assets, language, body) {
        val prefix = if (language == "ta") "baloo_thambi2" else "baloo2"
        if (runCatching { assets.open("fonts/${prefix}_bold.ttf").close() }.isSuccess)
            FontFamily(Font("fonts/${prefix}_bold.ttf", assets, FontWeight.Bold)) else body
    }
    fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, heading: Boolean = false) = TextStyle(
        fontFamily = if (heading) display else body, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = 0.sp)
    return Typography(
        displayLarge = style(38, 42, FontWeight.ExtraBold, true), displayMedium = style(34, 36, FontWeight.ExtraBold, true),
        headlineLarge = style(28, 33, FontWeight.ExtraBold, true), headlineMedium = style(26, 31, FontWeight.ExtraBold, true),
        headlineSmall = style(24, 29, FontWeight.ExtraBold, true),
        titleLarge = style(20, 25, FontWeight.Bold, true), titleMedium = style(18, 23, FontWeight.Bold, true), titleSmall = style(16, 21, FontWeight.Bold),
        bodyLarge = style(15, 23), bodyMedium = style(13, 19), bodySmall = style(12, 18),
        labelLarge = style(16, 20, FontWeight.Bold), labelMedium = style(13, 19, FontWeight.Bold), labelSmall = style(11, 16, FontWeight.SemiBold)
    )
}
