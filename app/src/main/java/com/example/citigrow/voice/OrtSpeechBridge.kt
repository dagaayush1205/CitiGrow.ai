package com.example.citigrow.voice

import java.nio.FloatBuffer
import java.nio.LongBuffer

/** Narrow lazy bridge to the pinned ONNX Runtime Android AAR. Keeping runtime loading
 * here lets screens and tokenizer tests work without loading JNI or a 118 MB model.
 * All calls are confined to OnnxSpeech's single inference worker. */
internal class OrtSpeechBridge(modelPath: String) : AutoCloseable {
    private val environmentClass = Class.forName("ai.onnxruntime.OrtEnvironment")
    private val tensorClass = Class.forName("ai.onnxruntime.OnnxTensor")
    private val sessionClass = Class.forName("ai.onnxruntime.OrtSession")
    private val environment = environmentClass.getMethod("getEnvironment").invoke(null)
    private val session: Any
    val sampleRate: Int
    init {
        val optionsClass = Class.forName("ai.onnxruntime.OrtSession\$SessionOptions")
        val options = optionsClass.getConstructor().newInstance()
        try {
            optionsClass.getMethod("setIntraOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 2)
            optionsClass.getMethod("setInterOpNumThreads", Int::class.javaPrimitiveType).invoke(options, 1)
            session = requireNotNull(environmentClass.getMethod("createSession", String::class.java, optionsClass).invoke(environment, modelPath, options))
        } finally { (options as AutoCloseable).close() }
        try {
            val names = sessionClass.getMethod("getInputNames").invoke(session) as Set<*>
            require(names == setOf("x", "x_length", "noise_scale", "length_scale", "noise_scale_w", "sid", "emotion_id")) { "Unexpected VITS model input contract: $names" }
            val metadata = sessionClass.getMethod("getMetadata").invoke(session)
            val values = metadata.javaClass.getMethod("getCustomMetadata").invoke(metadata) as Map<*, *>
            sampleRate = values["sample_rate"].toString().toInt()
            require(sampleRate == 24000) { "The packaged model is not the validated 24 kHz checkpoint." }
        } catch (error: Throwable) { (session as AutoCloseable).close(); throw error }
    }
    fun generate(ids: LongArray, speaker: Long, emotion: Long): FloatArray {
        val inputs = linkedMapOf<String, AutoCloseable>()
        fun longs(name: String, values: LongArray, shape: LongArray) {
            inputs[name] = tensorClass.getMethod("createTensor", environmentClass, LongBuffer::class.java, LongArray::class.java)
                .invoke(null, environment, LongBuffer.wrap(values), shape) as AutoCloseable
        }
        fun scalar(name: String, value: Float) {
            inputs[name] = tensorClass.getMethod("createTensor", environmentClass, FloatBuffer::class.java, LongArray::class.java)
                .invoke(null, environment, FloatBuffer.wrap(floatArrayOf(value)), longArrayOf()) as AutoCloseable
        }
        try {
            longs("x", ids, longArrayOf(1, ids.size.toLong()))
            longs("x_length", longArrayOf(ids.size.toLong()), longArrayOf(1))
            longs("sid", longArrayOf(speaker), longArrayOf(1)); longs("emotion_id", longArrayOf(emotion), longArrayOf(1))
            scalar("noise_scale", .667f); scalar("length_scale", 1f); scalar("noise_scale_w", .667f)
            val result = sessionClass.getMethod("run", Map::class.java).invoke(session, inputs)
            try {
                val tensor = result.javaClass.getMethod("get", Int::class.javaPrimitiveType).invoke(result, 0)
                val buffer = tensorClass.getMethod("getFloatBuffer").invoke(tensor) as? FloatBuffer ?: error("Expected a floating-point waveform.")
                return FloatArray(buffer.remaining()).also { buffer.get(it) }
            } finally { (result as AutoCloseable).close() }
        } finally { inputs.values.forEach { it.close() } }
    }
    override fun close() { (session as AutoCloseable).close() }
}
