package com.example.citigrow.voice

import android.content.Context
import android.media.MediaPlayer

/** Engines return false when unavailable, allowing a caption fallback without fake audio. */
interface VoiceEngine {
    fun play(key: String, text: String, language: String): Boolean
    fun stop()
}

class RecordedVoice(private val context: Context) : VoiceEngine {
    private var player: MediaPlayer? = null
    override fun play(key: String, text: String, language: String): Boolean {
        stop()
        return runCatching {
            context.assets.openFd("audio/$language/$key.ogg").use { clip ->
                player = MediaPlayer().apply { setDataSource(clip.fileDescriptor, clip.startOffset, clip.length); prepare(); start() }
            }
            true
        }.getOrElse { stop(); false }
    }
    override fun stop() { player?.release(); player = null }
}

/** Install a validated on-device Hindi/Tamil model behind this interface after the pilot decision. */
class PendingOnDeviceVoice : VoiceEngine {
    override fun play(key: String, text: String, language: String) = false
    override fun stop() = Unit
}

class VoiceController(context: Context, private val tts: VoiceEngine = PendingOnDeviceVoice()) {
    private val clips = RecordedVoice(context)
    fun read(key: String, text: String, language: String): Boolean {
        stop()
        return clips.play(key, text, language) || tts.play(key, text, language)
    }
    fun stop() { clips.stop(); tts.stop() }
}
