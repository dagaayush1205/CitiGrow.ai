package com.example.citigrow.voice

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.Executors

/** Lazy, serialized CPU inference with app-private generated audio caching. */
class OnnxSpeech(context: Context) : AutoCloseable {
    private val app = context.applicationContext
    private val worker = Executors.newSingleThreadExecutor()
    private val dispatcher = worker.asCoroutineDispatcher()
    private var bridge: OrtSpeechBridge? = null
    private var tokenizer: VitsTokenizer? = null
    private val configuration by lazy { JSONObject(app.assets.open("tts/rasa/config.json").bufferedReader().use { it.readText() }) }

    suspend fun synthesize(text: String, language: String): File = withContext(dispatcher) {
        val speaker = requireNotNull(VoiceLanguages.onnxSpeakers[language]) { "Unsupported ONNX speech language: $language" }
        val directory = File(app.cacheDir, "speech/rasa").apply { mkdirs() }
        val hash = sha256("${configuration.getString("sha256")}|$language|$speaker|0|$text".toByteArray())
        val output = File(directory, "$hash.wav")
        if (output.isFile && output.length() > 44) return@withContext output.also { it.setLastModified(System.currentTimeMillis()) }
        ensureActive()
        if (bridge == null) {
            val model = File(app.noBackupFilesDir, "tts/rasa-${configuration.getString("sha256").take(12)}.onnx")
            if (!model.isFile) {
                model.parentFile?.mkdirs()
                val temporary = File(model.parentFile, "model.part")
                try {
                    val digest = MessageDigest.getInstance("SHA-256")
                    app.assets.open("tts/rasa/model.onnx").use { input -> temporary.outputStream().buffered().use { destination ->
                        val buffer = ByteArray(1024*1024)
                        while (true) {
                            ensureActive(); val read = input.read(buffer); if (read < 0) break
                            digest.update(buffer, 0, read); destination.write(buffer, 0, read)
                        }
                    } }
                    check(digest.digest().joinToString("") { "%02x".format(it) } == configuration.getString("sha256")) { "Voice model checksum mismatch." }
                    check(temporary.renameTo(model)) { "Unable to install the bundled voice model." }
                } finally { temporary.delete() }
            }
            ensureActive()
            bridge = OrtSpeechBridge(model.absolutePath)
            tokenizer = VitsTokenizer(JSONObject(app.assets.open("tts/rasa/vocab.json").bufferedReader().use { it.readText() }))
        }
        ensureActive()
        val samples = bridge!!.generate(tokenizer!!.encode(text), speaker = speaker, emotion = 0)
        ensureActive() // JNI inference may finish after Stop; never play a cancelled result.
        WaveFile.write(output, samples, bridge!!.sampleRate)
        trimCache(directory, output)
        output
    }
    private fun trimCache(directory: File, keep: File) {
        val files = directory.listFiles()?.filter { it.extension == "wav" }?.sortedBy { it.lastModified() } ?: return
        var bytes = files.sumOf { it.length() }
        files.filter { it != keep }.forEach { file -> if (bytes > 30L*1024*1024) { val size = file.length(); if (file.delete()) bytes -= size } }
    }
    override fun close() { worker.execute { bridge?.close(); bridge = null }; worker.shutdown() }
    private fun sha256(value: ByteArray) = MessageDigest.getInstance("SHA-256").digest(value).joinToString("") { "%02x".format(it) }
}
