package com.example.citigrow

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SdkSuppress
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.toArgb
import com.example.citigrow.ui.theme.Navy
import com.example.citigrow.ui.theme.Cloud
import com.example.citigrow.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class JourneyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test
    @SdkSuppress(minSdkVersion = 26)
    fun welcomeUsesReferencePaletteAndThreeLanguageChoices() {
        compose.onNodeWithText("हिन्दी", substring = false).assertExists()
        compose.onNodeWithText("தமிழ்", substring = false).assertExists()
        compose.onAllNodesWithText("English").onFirst().assertExists()
        val pixels = compose.onRoot().captureToImage().toPixelMap()
        // Sample the left gutter, away from glyphs and illustration details.
        val x = (pixels.width * 0.02f).toInt()
        assertEquals(Navy.toArgb(), pixels[x, (pixels.height * 0.10f).toInt()].toArgb())
        compose.onNodeWithText("हिन्दी", substring = false).performScrollTo()
        val languageY = compose.onNodeWithText("हिन्दी", substring = false).fetchSemanticsNode().boundsInRoot.center.y.toInt()
        val bodyPixels = compose.onRoot().captureToImage().toPixelMap()
        assertEquals(Cloud.toArgb(), bodyPixels[x, languageY].toArgb())
    }
    @Test fun sharedPhoneRequiresPinBeforeShowingStudentHome() {
        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.onNodeWithText("Begin My Journey").performClick()
        compose.onNodeWithText("Ram").performClick()
        compose.onNodeWithContentDescription("PIN").performTextInput("1111")
        compose.onNodeWithText("Unlock", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Ram", substring = false).assertIsDisplayed()
        compose.onNodeWithText("Your next step").assertIsDisplayed()
    }
    @Test fun repositoryRetainsProgressWithoutPlaintextPins() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = GrowStore(context)
        val p = store.profiles.first(); val id = p.text("id")
        val before = p.optInt("study", 30)
        store.change("Test daily time") { p.put("study", 45) }
        val reloaded = GrowStore(context)
        assertEquals(45, reloaded.profile(id).optInt("study"))
        assertFalse(reloaded.profile(id).has("pin"))
        assertTrue(reloaded.unlock(reloaded.profile(id), "1111"))
        store.change { p.put("study", before) }
    }
}
