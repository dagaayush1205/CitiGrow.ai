package com.example.citigrow.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.citigrow.ui.theme.CitiGrowTheme

// Render the actual application entry point, including its shell and bundled assets.
@Preview(name = "Welcome · reference 390 × 844", widthDp = 390, heightDp = 844, showBackground = true)
@Preview(name = "Welcome · compact 320 × 720", widthDp = 320, heightDp = 720, showBackground = true)
@Preview(name = "Welcome · large text", widthDp = 390, heightDp = 844, fontScale = 1.3f, showBackground = true)
@Composable private fun ApplicationPreview() { CitiGrowTheme { CitiGrowApp() } }
