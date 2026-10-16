package com.example.citigrow

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.citigrow.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class JourneyTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun sharedPhoneRequiresPinBeforeShowingStudentHome() {
        compose.onNodeWithText("Continue").performScrollTo().performClick()
        compose.onNodeWithText("Begin My Journey").performClick()
        compose.onNodeWithText("Ram").performClick()
        compose.onNodeWithText("PIN", useUnmergedTree = true).performTextInput("1111")
        compose.onNodeWithText("Unlock", substring = false).performScrollTo().performClick()
        compose.onNodeWithText("Hello, Ram ☀").assertIsDisplayed()
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
