package com.example.citigrow

import androidx.test.platform.app.InstrumentationRegistry
import com.example.citigrow.voice.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SpeechCoreTest {
    private fun fixture(path: String, fromApp: Boolean = false): String {
        val file = File(if (fromApp) "app/src/main/assets" else "app/src/androidTest/assets", path)
        if (file.exists()) return file.readText()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        return (if (fromApp) instrumentation.targetContext else instrumentation.context).assets.open(path).bufferedReader().use { it.readText() }
    }
    @Test fun tokenizerMatchesPythonReferenceAcrossFourScripts() {
        val tokenizer = VitsTokenizer(JSONObject(fixture("tts/rasa/vocab.json", true)))
        val cases = JSONArray(fixture("tts/tokenizer_cases.json"))
        for (i in 0 until cases.length()) {
            val case = cases.getJSONObject(i); val ids = case.getJSONArray("ids")
            assertArrayEquals(case.getString("text"), LongArray(ids.length()) { ids.getLong(it) }, tokenizer.encode(case.getString("text")))
        }
    }
    @Test fun malayalamUsesItsOwnModelSpeaker() {
        assertEquals(11L, VoiceLanguages.onnxSpeakers["ml"])
        assertEquals(18L, VoiceLanguages.onnxSpeakers["ta"])
        assertFalse(VoiceLanguages.onnxSpeakers.containsKey("en"))
    }
    @Test fun registrationTranslationsHaveAllPromptsInEveryLanguage() {
        val prompts = JSONObject(fixture("tts/registration_prompts.json", true))
        val keys = prompts.getJSONObject("en").keys().asSequence().toSet()
        for (language in listOf("en", "hi", "ta", "ml")) {
            val translated = prompts.getJSONObject(language)
            assertEquals(keys, translated.keys().asSequence().toSet())
            keys.forEach { id ->
                assertTrue(translated.getJSONObject(id).getString("label").isNotBlank())
                assertTrue(translated.getJSONObject(id).getString("hint").isNotBlank())
            }
        }
        val tokenizer = VitsTokenizer(JSONObject(fixture("tts/rasa/vocab.json", true)))
        keys.forEach { id ->
            val prompt = prompts.getJSONObject("ml").getJSONObject(id)
            assertTrue(tokenizer.encode("${prompt.getString("label")}. ${prompt.getString("hint")}").size > 1)
        }
    }
    @Test fun blankTokensAreInsertedExactlyOnce() {
        val tokenizer = VitsTokenizer(JSONObject("""{"a":1,"b":2," ":3}"""))
        assertArrayEquals(longArrayOf(0,1,0,2,0), tokenizer.encode(" AB! "))
    }
    @Test(expected = IllegalArgumentException::class) fun unsupportedTextDoesNotGenerateSilentAudio() {
        VitsTokenizer(JSONObject("""{"a":1}""")).encode("😀")
    }
    @Test fun waveFileUsesCorrectRateAndClipsPcmWithoutWrapping() {
        val directory = kotlin.io.path.createTempDirectory("citigrow-wave-test").toFile()
        val file = File(directory, "test.wav")
        try {
            WaveFile.write(file, floatArrayOf(-2f, 0f, 2f), 24000)
            val bytes = file.readBytes(); val data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals("RIFF", String(bytes, 0, 4)); assertEquals("WAVE", String(bytes, 8, 4))
            assertEquals(24000, data.getInt(24)); assertEquals(48000, data.getInt(28))
            assertEquals(6, data.getInt(40)); assertEquals((-32767).toShort(), data.getShort(44))
            assertEquals(0.toShort(), data.getShort(46)); assertEquals(32767.toShort(), data.getShort(48))
            assertFalse(File(directory, "test.wav.part").exists())
        } finally { file.delete(); directory.delete() }
    }
    @Test fun invalidWaveformDoesNotLeaveACacheFile() {
        val directory = kotlin.io.path.createTempDirectory("citigrow-invalid-wave").toFile()
        val file = File(directory, "test.wav")
        try {
            assertTrue(runCatching { WaveFile.write(file, floatArrayOf(Float.NaN), 24000) }.isFailure)
            assertFalse(file.exists())
        } finally { file.delete(); directory.delete() }
    }
}
