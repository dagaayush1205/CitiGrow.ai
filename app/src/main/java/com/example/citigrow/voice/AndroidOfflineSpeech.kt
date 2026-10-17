package com.example.citigrow.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Selects an installed, non-network voice explicitly; never silently invokes cloud TTS. */
class AndroidOfflineSpeech(context: Context) : AutoCloseable {
    private val app = context.applicationContext
    private val ready = CompletableDeferred<Boolean>()
    private val pending = ConcurrentHashMap<String, CancellableContinuation<Unit>>()
    private val tts = TextToSpeech(app) { status -> ready.complete(status == TextToSpeech.SUCCESS) }
    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit // Generation, not audible playback.
            override fun onDone(utteranceId: String?) { if (utteranceId != null) pending.remove(utteranceId)?.let { if (it.isActive) it.resume(Unit) } }
            @Deprecated("Platform callback") override fun onError(utteranceId: String?) {
                if (utteranceId != null) pending.remove(utteranceId)?.let { if (it.isActive) it.resumeWithException(IllegalStateException("Offline voice could not generate this instruction.")) }
            }
            override fun onError(utteranceId: String?, errorCode: Int) { onError(utteranceId) }
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                if (utteranceId != null) pending.remove(utteranceId)?.cancel()
            }
        })
    }
    suspend fun synthesize(text: String, language: String): File {
        require(language in setOf("en", "hi"))
        check(withTimeout(15_000) { ready.await() }) { "Android text-to-speech is unavailable on this phone." }
        val voice = tts.voices.orEmpty().filter {
            it.locale.language == language && !it.isNetworkConnectionRequired &&
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty()
        }.sortedWith(compareByDescending<android.speech.tts.Voice> { it.locale.country == "IN" }.thenByDescending { it.quality }).firstOrNull()
            ?: error("Install an offline ${if (language == "en") "English" else "Hindi"} voice in Android text-to-speech settings.")
        check(tts.setVoice(voice) == TextToSpeech.SUCCESS) { "This offline voice could not be selected." }
        tts.setSpeechRate(1f)
        val identity = "${tts.defaultEngine}|${voice.name}|$language|$text"
        val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
        val directory = File(app.cacheDir, "speech/android").apply { mkdirs() }
        val output = File(directory, "$hash.wav")
        if (output.isFile && output.length() > 44) return output.also { it.setLastModified(System.currentTimeMillis()) }
        val temporary = File(directory, "$hash-${UUID.randomUUID()}.part.wav")
        try {
            withTimeout(90_000) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val id = UUID.randomUUID().toString()
                    pending[id] = continuation
                    continuation.invokeOnCancellation { pending.remove(id); tts.stop() }
                    if (tts.synthesizeToFile(text, Bundle(), temporary, id) != TextToSpeech.SUCCESS) {
                        pending.remove(id)
                        if (continuation.isActive) continuation.resumeWithException(IllegalStateException("Unable to start offline speech generation."))
                    }
                }
            }
            currentCoroutineContext().ensureActive()
            check(temporary.length() > 44 && temporary.renameTo(output)) { "No generated audio was saved." }
            var total = directory.listFiles()?.sumOf { it.length() } ?: 0
            directory.listFiles()?.filter { it != output && it.extension == "wav" }?.sortedBy { it.lastModified() }?.forEach {
                if (total > 20L*1024*1024) { val size = it.length(); if (it.delete()) total -= size }
            }
            return output
        } finally { temporary.delete() }
    }
    fun stop() { tts.stop() }
    override fun close() {
        pending.values.forEach { it.cancel() }; pending.clear(); tts.stop(); tts.shutdown()
    }
}
