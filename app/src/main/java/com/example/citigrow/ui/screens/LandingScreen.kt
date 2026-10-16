package com.example.citigrow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.citigrow.utils.Translator
import com.example.citigrow.utils.tr

private val CitiGrowPurple = Color(0xFF5B2D8E)
private val CitiGrowGreen = Color(0xFF00C48C)
private val LandingBackground = Color(0xFFF3F4F8)

@Composable
fun LandingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LandingBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "CitiGrow",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = CitiGrowPurple,
        )
        Text(
            text = tr("Grow with your city"),
            fontSize = 14.sp,
            color = Color.Gray,
        )

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = tr("Choose your language"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LanguageButton(
                    label = "English",
                    languageCode = Translator.ENGLISH,
                    modifier = Modifier.weight(1f),
                )
                LanguageButton(
                    label = "हिंदी",
                    languageCode = Translator.HINDI,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LanguageButton(
                    label = "ಕನ್ನಡ",
                    languageCode = Translator.KANNADA,
                    modifier = Modifier.weight(1f),
                )
                LanguageButton(
                    label = "தமிழ்",
                    languageCode = Translator.TAMIL,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CitiGrowGreen),
        ) {
            Text(
                text = tr("Continue"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun LanguageButton(
    label: String,
    languageCode: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isSelected = Translator.currentLanguage == languageCode

    Button(
        onClick = { Translator.setLanguage(context, languageCode) },
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CitiGrowPurple else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp,
        ),
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
