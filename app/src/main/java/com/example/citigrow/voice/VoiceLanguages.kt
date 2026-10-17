package com.example.citigrow.voice

/** Speaker IDs from the VITS Rasa model card; only locally verified routes are enabled. */
object VoiceLanguages {
    val onnxSpeakers: Map<String, Long> = mapOf("ta" to 18L, "ml" to 11L)
}
