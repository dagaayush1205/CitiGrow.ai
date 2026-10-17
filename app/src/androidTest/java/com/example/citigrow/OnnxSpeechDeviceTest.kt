package com.example.citigrow

import androidx.test.platform.app.InstrumentationRegistry
import com.example.citigrow.voice.OnnxSpeech
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Real JNI/model test: requires the packaged ONNX Runtime AAR and a physical device/AVD. */
class OnnxSpeechDeviceTest {
    @Test fun suppliedModelGeneratesAndCachesMalayalamOnDevice() = runBlocking {
        val engine = OnnxSpeech(InstrumentationRegistry.getInstrumentation().targetContext)
        try {
            val first = engine.synthesize("ജില്ല", "ml")
            assertTrue(first.length() > 44)
            assertEquals(first.absolutePath, engine.synthesize("ജില്ല", "ml").absolutePath)
        } finally { engine.close() }
    }
    @Test fun suppliedModelGeneratesAndCachesTamilOnDevice() = runBlocking {
        val engine = OnnxSpeech(InstrumentationRegistry.getInstrumentation().targetContext)
        try {
            val first = engine.synthesize("மாவட்டம்", "ta")
            assertTrue(first.length() > 44)
            assertEquals(first.absolutePath, engine.synthesize("மாவட்டம்", "ta").absolutePath)
        } finally { engine.close() }
    }
}
