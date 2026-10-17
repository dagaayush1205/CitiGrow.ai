package com.example.citigrow.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.AudioManager
import android.media.AudioFocusRequest
import android.os.Build
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class VoicePhase { IDLE, GENERATING, PLAYING, UNAVAILABLE }
data class VoiceState(val phase: VoicePhase = VoicePhase.IDLE, val message: String = "", val field: String? = null)
data class VoicePrompt(val id: String, val text: String)

interface FormNarrator {
    val state: VoiceState
    fun guide(prompts: List<VoicePrompt>, language: String, beforePlayback: suspend (String) -> Unit = {})
    fun stop()
}

/** One owner, one narration job, one player. Highlight state follows audible playback,
 * including cache hits, rather than timers or inference duration. */
class VoiceController(context: Context) : FormNarrator, AutoCloseable {
    private val app = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val onnx = OnnxSpeech(app)
    private val androidDelegate = lazy { AndroidOfflineSpeech(app) }
    private val android by androidDelegate
    private var job: Job? = null
    private var player: MediaPlayer? = null
    private var closed = false
    private var generation = 0L
    private val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var hasFocus = false
    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
            scope.launch { stop() }
    }
    override var state by mutableStateOf(VoiceState())
        private set

    fun read(key: String, text: String, language: String) = guide(listOf(VoicePrompt(key, text)), language)

    override fun guide(prompts: List<VoicePrompt>, language: String, beforePlayback: suspend (String) -> Unit) {
        stop()
        if (closed || prompts.isEmpty()) return
        val owner = generation
        job = scope.launch {
            try {
                for (prompt in prompts) {
                    ensureActive()
                    state = VoiceState(VoicePhase.GENERATING, "Preparing audio on this phone…")
                    val clip = findClip(prompt.id, language)
                    val generated = if (clip == null) {
                        if (language in VoiceLanguages.onnxSpeakers) onnx.synthesize(prompt.text, language)
                        else android.synthesize(prompt.text, language)
                    } else null
                    ensureActive()
                    beforePlayback(prompt.id)
                    ensureActive()
                    acquireFocus()
                    play(clip, generated) {
                        state = VoiceState(VoicePhase.PLAYING, "Reading aloud · ${if (language in VoiceLanguages.onnxSpeakers) "on-device VITS" else "offline Android voice"}", prompt.id)
                    }
                }
                state = VoiceState()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                state = VoiceState(VoicePhase.UNAVAILABLE, error.cause?.message ?: error.message ?: "Audio unavailable on this device.")
            }
            catch (error: LinkageError) {
                state = VoiceState(VoicePhase.UNAVAILABLE, "The on-device voice runtime is unavailable for this phone. Check the ONNX Runtime installation.")
            }
            finally { if (generation == owner) abandonFocus() }
        }
    }
    @Suppress("DEPRECATION")
    private fun acquireFocus() {
        if (hasFocus) return
        val result = if (Build.VERSION.SDK_INT >= 26) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                .setOnAudioFocusChangeListener(focusListener).build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        check(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { "Audio is busy. Try reading the instructions again." }
        hasFocus = true
    }
    @Suppress("DEPRECATION")
    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= 26) focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        else if (hasFocus) audioManager.abandonAudioFocus(focusListener)
        hasFocus = false; focusRequest = null
    }
    private fun findClip(key: String, language: String): String? {
        if (!key.matches(Regex("[a-zA-Z0-9_-]+"))) return null
        val path = "audio/$language/$key.ogg"
        return runCatching { app.assets.openFd(path).close(); path }.getOrNull()
    }
    private suspend fun play(clip: String?, file: File?, started: () -> Unit) {
        var owned: MediaPlayer? = null
        try {
        withTimeout(120_000) {
            suspendCancellableCoroutine<Unit> { continuation ->
                val current = MediaPlayer()
                owned = current
                player = current
                current.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                current.setOnPreparedListener {
                    if (continuation.isActive) { it.start(); started() }
                }
                current.setOnCompletionListener { if (continuation.isActive) continuation.resume(Unit) }
                current.setOnErrorListener { _, _, _ ->
                    if (continuation.isActive) continuation.resumeWithException(IllegalStateException("Unable to play generated speech."))
                    true
                }
                continuation.invokeOnCancellation {
                    // Cancellation is requested on the main thread by Stop/navigation.
                    if (player === current) { current.release(); player = null }
                }
                try {
                    if (clip != null) app.assets.openFd(clip).use { current.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
                    else current.setDataSource(requireNotNull(file).absolutePath)
                    current.prepareAsync()
                } catch (error: Exception) { if (continuation.isActive) continuation.resumeWithException(error) }
            }
        }
        } finally {
            if (player === owned) { player?.release(); player = null }
        }
    }
    override fun stop() {
        generation++
        job?.cancel(); job = null
        player?.release(); player = null
        abandonFocus()
        state = VoiceState()
    }
    override fun close() {
        if (closed) return
        stop(); closed = true; scope.cancel(); onnx.close()
        // Do not instantiate Android TTS just to dispose an unused engine.
        if (androidDelegate.isInitialized()) android.close()
    }
}
