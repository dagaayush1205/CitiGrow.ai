package com.example.citigrow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.citigrow.data.GrowStore
import com.example.citigrow.ui.*
import com.example.citigrow.ui.theme.CitiGrowTheme
import com.example.citigrow.voice.*
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class RegistrationNarrationTest {
    @get:Rule val compose = createComposeRule()
    private class FakeNarrator : FormNarrator {
        override var state by mutableStateOf(VoiceState())
        var prompts = emptyList<VoicePrompt>()
        var language = ""
        override fun guide(prompts: List<VoicePrompt>, language: String, beforePlayback: suspend (String) -> Unit) {
            this.prompts = prompts; this.language = language; state = VoiceState(VoicePhase.GENERATING, "Preparing")
        }
        override fun stop() { state = VoiceState() }
    }
    @Test fun englishFieldsHighlightOnlyDuringPlaybackAndClearOnStop() {
        verifyFields("en")
    }
    @Test fun malayalamFieldsHighlightOnlyDuringPlaybackAndClearOnStop() {
        verifyFields("ml")
    }
    private fun verifyFields(language: String) {
        val narrator = FakeNarrator()
        val store = GrowStore(InstrumentationRegistry.getInstrumentation().targetContext)
        store.change { store.state.put("lang", language) }
        val session = AppSession(store, {}, {}, { _, _ -> }, {}, "", narrator)
        compose.setContent { CitiGrowTheme { CompositionLocalProvider(LocalGrowStore provides store) {
            Column(Modifier.verticalScroll(rememberScrollState())) { RegisterScreen(session, false) {} }
        } } }
        compose.waitUntil { narrator.prompts.isNotEmpty() }
        compose.runOnIdle {
            assertEquals(language, narrator.language)
            assertEquals(listOf("intro0", "name", "age", "education", "state", "district", "continue"), narrator.prompts.map { it.id })
            if (language == "ml") assertTrue(narrator.prompts.first { it.id == "name" }.text.startsWith("വിദ്യാർത്ഥിയുടെ പേര്"))
        }
        val reading = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Reading aloud")
        compose.onNodeWithTag("registration-field-name").assert(!reading)
        compose.runOnIdle { narrator.state = VoiceState(VoicePhase.PLAYING, "Reading", "name") }
        compose.onNodeWithTag("registration-field-name").assert(reading)
        compose.onNodeWithTag("registration-field-age").assert(!reading)
        compose.onNodeWithText(store.t("stopReading"), substring = false).performScrollTo().performClick()
        compose.onNodeWithTag("registration-field-name").assert(!reading)
    }
}
